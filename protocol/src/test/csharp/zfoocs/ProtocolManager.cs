using System;
using System.Collections.Generic;

namespace zfoocs
{
    public class ProtocolManager
    {
        public static readonly short MAX_PROTOCOL_NUM = short.MaxValue;


        private static readonly IProtocolRegistration[] protocols = new IProtocolRegistration[MAX_PROTOCOL_NUM];
        private static readonly Dictionary<Type, short> protocolIdMap = new Dictionary<Type, short>();
        private static bool initialized = false;


        public static void InitProtocol()
        {
            if (initialized)
            {
                return;
            }
            initialized = true;
            protocols[0] = new EmptyObjectRegistration();
            protocolIdMap[typeof(EmptyObject)] = 0;
            protocols[1] = new VeryBigObjectRegistration();
            protocolIdMap[typeof(VeryBigObject)] = 1;
            protocols[100] = new ComplexObjectRegistration();
            protocolIdMap[typeof(ComplexObject)] = 100;
            protocols[101] = new NormalObjectRegistration();
            protocolIdMap[typeof(NormalObject)] = 101;
            protocols[102] = new ObjectARegistration();
            protocolIdMap[typeof(ObjectA)] = 102;
            protocols[103] = new ObjectBRegistration();
            protocolIdMap[typeof(ObjectB)] = 103;
            protocols[104] = new SimpleObjectRegistration();
            protocolIdMap[typeof(SimpleObject)] = 104;
        }

        public static short GetProtocolId(Type type)
        {
            return protocolIdMap[type];
        }

        public static IProtocolRegistration GetProtocol(short protocolId)
        {
            var protocol = protocols[protocolId];
            if (protocol == null)
            {
                throw new Exception("[protocolId:" + protocolId + "] not exist");
            }
            return protocol;
        }

        public static void Write(ByteBuffer buffer, object packet)
        {
            var protocolId = protocolIdMap[packet.GetType()];
            // write protocol id to buffer
            buffer.WriteShort(protocolId);
            // write packet
            GetProtocol(protocolId).Write(buffer, packet);
        }

        public static object Read(ByteBuffer buffer)
        {
            var protocolId = buffer.ReadShort();
            return GetProtocol(protocolId).Read(buffer);
        }
    }
}
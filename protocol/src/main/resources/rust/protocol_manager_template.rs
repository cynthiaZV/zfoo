#![allow(unused_imports)]
#![allow(dead_code)]
#![allow(non_snake_case)]
#![allow(non_camel_case_types)]
use std::any::Any;
use crate::${protocol_root_path}::i_byte_buffer::IByteBuffer;
${protocol_imports}


pub fn write(buffer: &mut dyn IByteBuffer, packet: &dyn Any, protocolId: i16) {
    buffer.writeShort(protocolId);
    writeNoProtocolId(buffer, packet, protocolId);
}

pub fn writeNoProtocolId(buffer: &mut dyn IByteBuffer, packet: &dyn Any, protocolId: i16) {
    match protocolId {
        ${protocol_write_serialization}
        _ => println!("protocolId:[{}] not found", protocolId)
    }
}

pub fn read(buffer: &mut dyn IByteBuffer) -> Box<dyn Any> {
    let protocolId = buffer.readShort();
    return readNoProtocolId(buffer, protocolId);
}

pub fn readNoProtocolId(buffer: &mut dyn IByteBuffer, protocolId: i16) -> Box<dyn Any> {
    let packet = match protocolId {
        ${protocol_read_deserialization}
        _ => Box::new(String::from("protocolId not found"))
    };
    return packet;
}
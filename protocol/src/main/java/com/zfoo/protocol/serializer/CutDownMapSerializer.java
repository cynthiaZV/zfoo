/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.serializer;

import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.registration.EnhanceUtils;
import com.zfoo.protocol.registration.field.BaseField;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.serializer.reflect.IntSerializer;
import com.zfoo.protocol.serializer.reflect.LongSerializer;
import com.zfoo.protocol.serializer.reflect.ObjectProtocolSerializer;
import com.zfoo.protocol.serializer.reflect.StringSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CutDownMapSerializer implements ICutDownSerializer {

    private static final CutDownMapSerializer INSTANCE = new CutDownMapSerializer();

    public static CutDownMapSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var mapField = (MapField) fieldRegistration;
        var keyRegistration = mapField.getMapKeyRegistration();
        var valueRegistration = mapField.getMapValueRegistration();
        var keySerializer = keyRegistration.serializer();
        var valueSerializer = valueRegistration.serializer();

        // 直接在字节码里调用方法是为了减小生成字节码的体积，下面的代码去掉也不会有任何影响
        if (keyRegistration instanceof BaseField) {
            if (keySerializer == IntSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeIntIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeIntIntMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeIntIntMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeIntIntMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeIntIntMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteIntIntMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteIntIntMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeIntIntMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeIntLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeIntLongMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeIntLongMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeIntLongMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeIntLongMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteIntLongMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteIntLongMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeIntLongMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeIntStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeIntStringMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeIntStringMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeIntStringMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeIntStringMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteIntStringMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteIntStringMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeIntStringMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    var protocolId = ((ObjectProtocolField) valueRegistration).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeIntPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeIntPacketMap({}, {})", objectStr, protocolId)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeIntPacketMap({}, {})", objectStr, protocolId)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeIntPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteIntPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Cpp, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeIntPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Java:
                            builder.append(StringUtils.format("buffer.writeIntPacketMap({}, (short) {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Rust, Golang, Protobuf:
                        default:
                    }
                }
            } else if (keySerializer == LongSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeLongIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeLongIntMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeLongIntMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeLongIntMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeLongIntMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteLongIntMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteLongIntMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeLongIntMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeLongLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeLongLongMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeLongLongMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeLongLongMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeLongLongMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteLongLongMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteLongLongMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeLongLongMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeLongStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeLongStringMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeLongStringMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeLongStringMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeLongStringMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteLongStringMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteLongStringMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeLongStringMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    var protocolId = ((ObjectProtocolField) valueRegistration).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeLongPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeLongPacketMap({}, {})", objectStr, protocolId)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeLongPacketMap({}, {})", objectStr, protocolId)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeLongPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteLongPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Cpp, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeLongPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Java:
                            builder.append(StringUtils.format("buffer.writeLongPacketMap({}, (short) {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Rust, Golang, Protobuf:
                        default:
                    }
                }
            } else if (keySerializer == StringSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeStringIntMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeStringIntMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeStringIntMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeStringIntMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeStringIntMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteStringIntMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteStringIntMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeStringIntMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeStringLongMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeStringLongMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeStringLongMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeStringLongMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeStringLongMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteStringLongMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteStringLongMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeStringLongMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeStringStringMap($1, (Map){});", EnhanceUtils.byteBufUtils, objectStr));
                            return true;
                        case Rust:
                            builder.append(StringUtils.format("buffer.writeStringStringMap(&{});", objectStr)).append(LS);
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeStringStringMap({})", objectStr)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeStringStringMap({})", objectStr)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeStringStringMap({});", objectStr)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteStringStringMap({});", objectStr)).append(LS);
                            return true;
                        case Golang:
                            builder.append(StringUtils.format("buffer.WriteStringStringMap({})", objectStr)).append(LS);
                            return true;
                        case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeStringStringMap({});", objectStr)).append(LS);
                            return true;
                    }
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    var protocolId = ((ObjectProtocolField) valueRegistration).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writeStringPacketMap($1, (Map){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            return true;
                        case Kotlin, Scala, GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writeStringPacketMap({}, {})", objectStr, protocolId)).append(LS);
                            return true;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writeStringPacketMap({}, {})", objectStr, protocolId)).append(LS);
                            return true;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writeStringPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WriteStringPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Cpp, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writeStringPacketMap({}, {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Java:
                            builder.append(StringUtils.format("buffer.writeStringPacketMap({}, (short) {});", objectStr, protocolId)).append(LS);
                            return true;
                        case Rust, Golang, Protobuf:
                        default:
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var mapField = (MapField) fieldRegistration;
        var keyRegistration = mapField.getMapKeyRegistration();
        var valueRegistration = mapField.getMapValueRegistration();
        var keySerializer = keyRegistration.serializer();
        var valueSerializer = valueRegistration.serializer();

        var map = "map" + GenerateProtocolFile.localVariableId++;

        if (keyRegistration instanceof BaseField) {
            if (keySerializer == IntSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readIntIntMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readIntIntMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readIntIntMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readIntIntMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readIntIntMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readIntIntMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readIntIntMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntIntMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntIntMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readIntIntMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readIntIntMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readIntIntMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readIntIntMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readIntIntMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readIntLongMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readIntLongMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readIntLongMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readIntLongMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readIntLongMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readIntLongMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readIntLongMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntLongMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntLongMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readIntLongMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readIntLongMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readIntLongMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readIntLongMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readIntLongMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readIntStringMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readIntStringMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readIntStringMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readIntStringMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readIntStringMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readIntStringMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readIntStringMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntStringMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntStringMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readIntStringMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readIntStringMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readIntStringMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readIntStringMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readIntStringMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    var protocolId = ((ObjectProtocolField) valueRegistration).getProtocolId();
                    var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId);
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readIntPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readIntPacketMap({}) as! Dictionary<Int, {}>", map, protocolId, protocolName)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readIntPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readIntPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readIntPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readIntPacketMap({});", map, protocolId)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadIntPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readIntPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Dart:
                            builder.append(StringUtils.format("var {} = buffer.readIntPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Java:
                            builder.append(StringUtils.format("var {} = buffer.readIntPacketMap({}.class, (short) {});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readIntPacketMap({}::class.java, {})", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readIntPacketMap(classOf[{}], {})", map, protocolName, protocolId)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readIntPacketMap({});", map, protocolId)).append(LS);
                            return map;
                        case Rust, Golang, Protobuf:
                    }
                }
            } else if (keySerializer == LongSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readLongIntMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readLongIntMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readLongIntMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readLongIntMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readLongIntMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readLongIntMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readLongIntMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongIntMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongIntMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readLongIntMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readLongIntMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readLongIntMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readLongIntMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readLongIntMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readLongLongMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readLongLongMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readLongLongMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readLongLongMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readLongLongMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readLongLongMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readLongLongMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongLongMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongLongMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readLongLongMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readLongLongMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readLongLongMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readLongLongMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readLongLongMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readLongStringMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readLongStringMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readLongStringMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readLongStringMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readLongStringMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readLongStringMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readLongStringMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongStringMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongStringMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readLongStringMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readLongStringMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readLongStringMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readLongStringMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readLongStringMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    var protocolId = ((ObjectProtocolField) valueRegistration).getProtocolId();
                    var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId);
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readLongPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readLongPacketMap({}) as! Dictionary<Int64, {}>", map, protocolId, protocolName)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readLongPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readLongPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readLongPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readLongPacketMap({});", map, protocolId)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadLongPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readLongPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Dart:
                            builder.append(StringUtils.format("var {} = buffer.readLongPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Java:
                            builder.append(StringUtils.format("var {} = buffer.readLongPacketMap({}.class, (short) {});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readLongPacketMap({}::class.java, {})", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readLongPacketMap(classOf[{}], {})", map, protocolName, protocolId)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readLongPacketMap({});", map, protocolId)).append(LS);
                            return map;
                        case Rust, Golang, Protobuf:
                        default:
                    }
                }
            } else if (keySerializer == StringSerializer.INSTANCE) {
                if (valueSerializer == IntSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readStringIntMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readStringIntMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readStringIntMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readStringIntMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readStringIntMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readStringIntMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readStringIntMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringIntMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringIntMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readStringIntMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readStringIntMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readStringIntMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readStringIntMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readStringIntMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == LongSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readStringLongMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readStringLongMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readStringLongMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readStringLongMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readStringLongMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readStringLongMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readStringLongMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringLongMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringLongMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readStringLongMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readStringLongMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readStringLongMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readStringLongMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readStringLongMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == StringSerializer.INSTANCE) {
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readStringStringMap($1);", map, EnhanceUtils.byteBufUtils));
                            return map;
                        case Rust:
                            builder.append(StringUtils.format("let {} = buffer.readStringStringMap();", map)).append(LS);
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readStringStringMap()", map)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readStringStringMap()", map)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readStringStringMap()", map)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readStringStringMap()", map)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readStringStringMap();", map)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringStringMap();", map)).append(LS);
                            return map;
                        case Golang:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringStringMap()", map)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readStringStringMap();", map)).append(LS);
                            return map;
                        case Java, Dart:
                            builder.append(StringUtils.format("var {} = buffer.readStringStringMap();", map)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readStringStringMap()", map)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readStringStringMap", map)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readStringStringMap();", map)).append(LS);
                            return map;
                    }
                } else if (valueSerializer == ObjectProtocolSerializer.INSTANCE) {
                    var protocolId = ((ObjectProtocolField) valueRegistration).getProtocolId();
                    var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId);
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("Map {} = {}.readStringPacketMap($1, {});", map, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            return map;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readStringPacketMap({}) as! Dictionary<String, {}>", map, protocolId, protocolName)).append(LS);
                            return map;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readStringPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readStringPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readStringPacketMap({})", map, protocolId)).append(LS);
                            return map;
                        case Php:
                            map = "$" + map;
                            builder.append(StringUtils.format("{} = $buffer->readStringPacketMap({});", map, protocolId)).append(LS);
                            return map;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadStringPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readStringPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Dart:
                            builder.append(StringUtils.format("var {} = buffer.readStringPacketMap<{}>({});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Java:
                            builder.append(StringUtils.format("var {} = buffer.readStringPacketMap({}.class, (short) {});", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readStringPacketMap({}::class.java, {})", map, protocolName, protocolId)).append(LS);
                            return map;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readStringPacketMap(classOf[{}], {})", map, protocolName, protocolId)).append(LS);
                            return map;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readStringPacketMap({});", map, protocolId)).append(LS);
                            return map;
                        case Rust, Golang, Protobuf:
                        default:
                    }
                }
            }
        }
        GenerateProtocolFile.localVariableId--;
        return null;
    }
}

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
import com.zfoo.protocol.registration.field.ListField;
import com.zfoo.protocol.registration.field.ObjectProtocolField;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import static com.zfoo.protocol.util.FileUtils.LS;

/**
 * @author godotg
 */
public class CutDownListSerializer implements ICutDownSerializer {

    private static final CutDownListSerializer INSTANCE = new CutDownListSerializer();

    public static CutDownListSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean writeObject(StringBuilder builder, String objectStr, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var listField = (ListField) fieldRegistration;
        var flag = true;

        var listName = getListClassName(listField);
        switch (listName) {
            case "Boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeBoolList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeBoolArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeBoolArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeBoolArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeBoolArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteBoolList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteBoolArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeBoolList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeBoolList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeByteList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeByteArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeByteArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeByteArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeByteArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteByteList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteByteArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeByteList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeByteList({})", objectStr)).append(LS);
                        break;

                    default:
                        flag = false;
                }
                break;
            case "Short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeShortList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeShortArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeShortArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeShortArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeShortArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteShortList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteShortArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeShortList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeShortList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Integer":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeIntList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeIntArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeIntArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeIntArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeIntArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteIntList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteIntArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeIntList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeIntList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Long": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeLongList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeLongArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeLongArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeLongArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeLongArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteLongList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteLongArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeLongList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeLongList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            }
            case "Float": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeFloatList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeFloatArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeFloatArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeFloatArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteFloatList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteFloatArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeFloatList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeFloatList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            }
            case "Double": {
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeDoubleList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeDoubleArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeDoubleArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteDoubleList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteDoubleArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeDoubleList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeDoubleList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            }
            case "String":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("{}.writeStringList($1, (List){});", EnhanceUtils.byteBufUtils, objectStr));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("buffer.writeStringArray(&{});", objectStr)).append(LS);
                        break;
                    case GdScript, Python, Ruby, Swift:
                        builder.append(StringUtils.format("buffer.writeStringArray({})", objectStr)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("buffer:writeStringArray({})", objectStr)).append(LS);
                        break;
                    case Php:
                        builder.append(StringUtils.format("$buffer->writeStringArray({});", objectStr)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("buffer.WriteStringList({});", objectStr)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("buffer.WriteStringArray({})", objectStr)).append(LS);
                        break;
                    case Cpp, Java, Dart, JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("buffer.writeStringList({});", objectStr)).append(LS);
                        break;
                    case Kotlin, Scala:
                        builder.append(StringUtils.format("buffer.writeStringList({})", objectStr)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                // List<IProtocol>
                if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
                    var protocolId = ((ObjectProtocolField) listField.getListElementRegistration()).getProtocolId();
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("{}.writePacketList($1, (List){}, {});", EnhanceUtils.byteBufUtils, objectStr, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            break;
                        case GdScript, Python, Ruby, Swift:
                            builder.append(StringUtils.format("buffer.writePacketArray({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case Lua:
                            builder.append(StringUtils.format("buffer:writePacketArray({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case Php:
                            builder.append(StringUtils.format("$buffer->writePacketArray({}, {});", objectStr, protocolId)).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("buffer.WritePacketList({}, {});", objectStr, protocolId)).append(LS);
                            break;
                        case Cpp, Dart, JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("buffer.writePacketList({}, {});", objectStr, protocolId)).append(LS);
                            break;
                        case Java:
                            builder.append(StringUtils.format("buffer.writePacketList({}, (short) {});", objectStr, protocolId)).append(LS);
                            break;
                        case Kotlin, Scala:
                            builder.append(StringUtils.format("buffer.writePacketList({}, {})", objectStr, protocolId)).append(LS);
                            break;
                        case Rust, Golang, Protobuf:
                        default:
                            flag = false;
                    }
                } else {
                    flag = false;
                }
        }

        return flag;
    }

    @Override
    public String readObject(StringBuilder builder, Field field, IFieldRegistration fieldRegistration, CodeLanguage language) {
        var listField = (ListField) fieldRegistration;
        var list = "list" + GenerateProtocolFile.localVariableId++;
        var flag = true;

        var listName = getListClassName(listField);
        switch (listName) {
            case "Boolean":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readBoolList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readBoolArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readBoolArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readBoolArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readBoolArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readBoolArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = buffer->readBoolArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadBoolList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadBoolArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readBoolList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readBoolList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readBoolList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readBoolList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readBoolList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Byte":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readByteList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readByteArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readByteArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readByteArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readByteArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readByteArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readByteArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadByteArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readByteList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readByteList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readByteList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readByteList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readByteList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Short":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readShortList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readShortArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readShortArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readShortArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readShortArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readShortArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readShortArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadShortArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readShortList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readShortList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readShortList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readShortList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readShortList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Integer":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readIntList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readIntArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readIntArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readIntArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readIntArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readIntArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readIntArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadIntArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readIntList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readIntList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readIntList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readIntList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readIntList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Long":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readLongList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readLongArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readLongArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readLongArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readLongArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readLongArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readLongArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadLongArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readLongList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readLongList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readLongList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readLongList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readLongList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Float":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readFloatList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readFloatArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readFloatArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readFloatArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readFloatArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readFloatArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readFloatArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadFloatArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readFloatList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readFloatList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readFloatList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readFloatList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readFloatList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "Double":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readDoubleList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readDoubleArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readDoubleArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readDoubleArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readDoubleArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readDoubleArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readDoubleArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadDoubleArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readDoubleList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readDoubleList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readDoubleList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readDoubleList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readDoubleList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            case "String":
                switch (language) {
                    case Enhance:
                        builder.append(StringUtils.format("List {} = {}.readStringList($1);", list, EnhanceUtils.byteBufUtils));
                        break;
                    case Rust:
                        builder.append(StringUtils.format("let {} = buffer.readStringArray();", list)).append(LS);
                        break;
                    case Swift:
                        builder.append(StringUtils.format("let {} = buffer.readStringArray()", list)).append(LS);
                        break;
                    case GdScript:
                        builder.append(StringUtils.format("var {} = buffer.readStringArray()", list)).append(LS);
                        break;
                    case Python, Ruby:
                        builder.append(StringUtils.format("{} = buffer.readStringArray()", list)).append(LS);
                        break;
                    case Lua:
                        builder.append(StringUtils.format("local {} = buffer:readStringArray()", list)).append(LS);
                        break;
                    case Php:
                        list = "$" + list;
                        builder.append(StringUtils.format("{} = $buffer->readStringArray();", list)).append(LS);
                        break;
                    case CSharp:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringList();", list)).append(LS);
                        break;
                    case Golang:
                        builder.append(StringUtils.format("var {} = buffer.ReadStringArray()", list)).append(LS);
                        break;
                    case Cpp:
                        builder.append(StringUtils.format("auto {} = buffer.readStringList();", list)).append(LS);
                        break;
                    case Java, Dart:
                        builder.append(StringUtils.format("var {} = buffer.readStringList();", list)).append(LS);
                        break;
                    case Kotlin:
                        builder.append(StringUtils.format("val {} = buffer.readStringList()", list)).append(LS);
                        break;
                    case Scala:
                        builder.append(StringUtils.format("val {} = buffer.readStringList", list)).append(LS);
                        break;
                    case JavaScript, EcmaScript, TypeScript:
                        builder.append(StringUtils.format("const {} = buffer.readStringList();", list)).append(LS);
                        break;
                    default:
                        flag = false;
                }
                break;
            default:
                if (listField.getListElementRegistration() instanceof ObjectProtocolField) {
                    var protocolId = ((ObjectProtocolField) listField.getListElementRegistration()).getProtocolId();
                    var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId);
                    switch (language) {
                        case Enhance:
                            builder.append(StringUtils.format("List {} = {}.readPacketList($1, {});", list, EnhanceUtils.byteBufUtils, EnhanceUtils.getProtocolRegistrationFieldNameByProtocolId(protocolId)));
                            break;
                        case Swift:
                            builder.append(StringUtils.format("let {} = buffer.readPacketArray({}) as! Array<{}>", list, protocolId, protocolName)).append(LS);
                            break;
                        case GdScript:
                            builder.append(StringUtils.format("var {} = buffer.readPacketArray({})", list, protocolId)).append(LS);
                            break;
                        case Python, Ruby:
                            builder.append(StringUtils.format("{} = buffer.readPacketArray({})", list, protocolId)).append(LS);
                            break;
                        case Lua:
                            builder.append(StringUtils.format("local {} = buffer:readPacketArray({})", list, protocolId)).append(LS);
                            break;
                        case Php:
                            list = "$" + list;
                            builder.append(StringUtils.format("{} = $buffer->readPacketArray({});", list, protocolId)).append(LS);
                            break;
                        case CSharp:
                            builder.append(StringUtils.format("var {} = buffer.ReadPacketList<{}>({});", list, protocolName, protocolId)).append(LS);
                            break;
                        case Cpp:
                            builder.append(StringUtils.format("auto {} = buffer.readPacketList<{}>({});", list, protocolName, protocolId)).append(LS);
                            break;
                        case Dart:
                            builder.append(StringUtils.format("var {} = buffer.readPacketList<{}>({});", list, protocolName, protocolId)).append(LS);
                            break;
                        case Java:
                            builder.append(StringUtils.format("var {} = buffer.readPacketList({}.class, (short) {});", list, protocolName, protocolId)).append(LS);
                            break;
                        case Kotlin:
                            builder.append(StringUtils.format("val {} = buffer.readPacketList({}::class.java, {})", list, protocolName, protocolId)).append(LS);
                            break;
                        case Scala:
                            builder.append(StringUtils.format("val {} = buffer.readPacketList(classOf[{}], {})", list, protocolName, protocolId)).append(LS);
                            break;
                        case JavaScript, EcmaScript, TypeScript:
                            builder.append(StringUtils.format("const {} = buffer.readPacketList({});", list, protocolId)).append(LS);
                            break;
                        case Rust, Golang, Protobuf:
                        default:
                            flag = false;
                    }
                } else {
                    flag = false;
                }
        }


        if (flag) {
            return list;
        } else {
            GenerateProtocolFile.localVariableId--;
            return null;
        }
    }

    public String getListClassName(ListField listField) {
        if (listField.getListElementRegistration() instanceof BaseField) {
            return ((Class<?>) ((ParameterizedType) listField.getType()).getActualTypeArguments()[0]).getSimpleName();
        } else {
            return listField.getType().getTypeName();
        }
    }
}

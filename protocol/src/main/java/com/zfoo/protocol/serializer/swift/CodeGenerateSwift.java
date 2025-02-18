/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.serializer.swift;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CodeTemplatePlaceholder;
import com.zfoo.protocol.serializer.ICodeGenerate;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;


/**
 * @author godotg
 */
public class CodeGenerateSwift implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateSwift.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfooswift";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, ISwiftSerializer> swiftSerializerMap = new HashMap<>();

    public static ISwiftSerializer swiftSerializer(ISerializer serializer) {
        return swiftSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        swiftSerializerMap.put(BoolSerializer.INSTANCE, new SwiftBoolSerializer());
        swiftSerializerMap.put(ByteSerializer.INSTANCE, new SwiftByteSerializer());
        swiftSerializerMap.put(ShortSerializer.INSTANCE, new SwiftShortSerializer());
        swiftSerializerMap.put(IntSerializer.INSTANCE, new SwiftIntSerializer());
        swiftSerializerMap.put(LongSerializer.INSTANCE, new SwiftLongSerializer());
        swiftSerializerMap.put(FloatSerializer.INSTANCE, new SwiftFloatSerializer());
        swiftSerializerMap.put(DoubleSerializer.INSTANCE, new SwiftDoubleSerializer());
        swiftSerializerMap.put(StringSerializer.INSTANCE, new SwiftStringSerializer());
        swiftSerializerMap.put(ArraySerializer.INSTANCE, new SwiftArraySerializer());
        swiftSerializerMap.put(ListSerializer.INSTANCE, new SwiftListSerializer());
        swiftSerializerMap.put(SetSerializer.INSTANCE, new SwiftSetSerializer());
        swiftSerializerMap.put(MapSerializer.INSTANCE, new SwiftMapSerializer());
        swiftSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new SwiftObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile(registrations);

        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : GenerateProtocolFile.subProtocolFirst(registrations)) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("swift/ProtocolsTemplate.swift");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.swift", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Swift protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile(registrations);

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("swift/ProtocolTemplate.swift");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.swift", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Swift protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile(registrations);

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("swift/ProtocolTemplate.swift");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.swift", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Swift protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile(List<ProtocolRegistration> registrations) throws IOException {
        var list = List.of("swift/ByteBuffer.swift", "swift/IProtocolRegistration.swift");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "swift/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("swift/ProtocolManagerTemplate.swift");
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration()", protocol_id, protocol_name)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.swift"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Swift protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("swift/ProtocolClassTemplate.swift");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Swift)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("swift/ProtocolRegistrationTemplate.swift");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var swiftBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Swift);
            for (var fieldNote : fieldNotes) {
                swiftBuilder.append(fieldNote).append(LS);
            }
            var fieldTypeDefaultValue = swiftSerializer(fieldRegistration.serializer()).fieldTypeDefaultValue(field, fieldRegistration);
            var fieldType = fieldTypeDefaultValue.getKey();
            var fieldDefaultValue = fieldTypeDefaultValue.getValue();
            swiftBuilder.append(StringUtils.format("var {}: {} = {}", fieldName, fieldType, fieldDefaultValue)).append(LS);
        }
        return swiftBuilder.toString();
    }


    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var swiftBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            swiftBuilder.append("let beforeWriteIndex = buffer.getWriteOffset()").append(LS);
            swiftBuilder.append(StringUtils.format("buffer.writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            swiftBuilder.append("buffer.writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            swiftSerializer(fieldRegistration.serializer()).writeObject(swiftBuilder, "message." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            swiftBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return swiftBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var swiftBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                swiftBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = swiftSerializer(fieldRegistration.serializer()).readObject(swiftBuilder, 1, field, fieldRegistration);
                swiftBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                swiftBuilder.append("}").append(LS);
                continue;
            }
            var readObject = swiftSerializer(fieldRegistration.serializer()).readObject(swiftBuilder, 0, field, fieldRegistration);
            swiftBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return swiftBuilder.toString();
    }

    public static String toSwiftClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "Bool";
                return typeName;
            case "byte":
            case "Byte":
                typeName = "Int8";
                return typeName;
            case "short":
            case "Short":
                typeName = "Int16";
                return typeName;
            case "int":
            case "Integer":
                typeName = "Int";
                return typeName;
            case "long":
            case "Long":
                typeName = "Int64";
                return typeName;
            case "float":
            case "Float":
                typeName = "Float32";
                return typeName;
            case "double":
            case "Double":
                typeName = "Float64";
                return typeName;
            case "char":
            case "Character":
            case "String":
                typeName = "String";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replaceAll("[B|b]oolean\\[", "Boolean");
        typeName = typeName.replace("<Boolean", "<Boolean");
        typeName = typeName.replace("Boolean>", "Boolean>");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "Int8");
        typeName = typeName.replace("Byte>", "Int8>");
        typeName = typeName.replace("<Byte", "<Int8");

        // 将Short转为short
        typeName = typeName.replace("Short[", "Int16");
        typeName = typeName.replace("Short>", "Int16>");
        typeName = typeName.replace("<Short", "<Int16");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "Int");
        typeName = typeName.replace("Integer>", "Int>");
        typeName = typeName.replace("<Integer", "<Int");

        // 将Long转为long
        typeName = typeName.replace("Long[", "Int64");
        typeName = typeName.replace("Long>", "Int64>");
        typeName = typeName.replace("<Long", "<Int64");

        // 将Float转为float
        typeName = typeName.replace("Float[", "Float32");
        typeName = typeName.replace("Float>", "Float32>");
        typeName = typeName.replace("<Float", "<Float32");

        // 将Double转为double
        typeName = typeName.replace("Double[", "Float64");
        typeName = typeName.replace("Double>", "Float64>");
        typeName = typeName.replace("<Double", "<Float64");

        // 将Character转为Char
        typeName = typeName.replace("Character[", "String");
        typeName = typeName.replace("Character>", "String>");
        typeName = typeName.replace("<Character", "<String");

        // 将String转为string
        typeName = typeName.replace("String[", "String");
        typeName = typeName.replace("String>", "String>");
        typeName = typeName.replace("<String", "<String");

        typeName = typeName.replace("Map<", "Dictionary<");
        typeName = typeName.replace("Set<", "Set<");
        typeName = typeName.replace("List<", "Array<");

        return typeName;
    }

}

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

package com.zfoo.protocol.serializer.scala;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CodeTemplatePlaceholder;
import com.zfoo.protocol.serializer.ICodeGenerate;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
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
public class CodeGenerateScala implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateScala.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfooscala";
    private static String protocolOutputPath = StringUtils.EMPTY;
    public static String protocolPackage = "com.zfoo.scala";

    private static final Map<ISerializer, IScalaSerializer> scalaSerializerMap = new HashMap<>();

    public static IScalaSerializer scalaSerializer(ISerializer serializer) {
        return scalaSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        scalaSerializerMap.put(BoolSerializer.INSTANCE, new ScalaBoolSerializer());
        scalaSerializerMap.put(ByteSerializer.INSTANCE, new ScalaByteSerializer());
        scalaSerializerMap.put(ShortSerializer.INSTANCE, new ScalaShortSerializer());
        scalaSerializerMap.put(IntSerializer.INSTANCE, new ScalaIntSerializer());
        scalaSerializerMap.put(LongSerializer.INSTANCE, new ScalaLongSerializer());
        scalaSerializerMap.put(FloatSerializer.INSTANCE, new ScalaFloatSerializer());
        scalaSerializerMap.put(DoubleSerializer.INSTANCE, new ScalaDoubleSerializer());
        scalaSerializerMap.put(StringSerializer.INSTANCE, new ScalaStringSerializer());
        scalaSerializerMap.put(ArraySerializer.INSTANCE, new ScalaArraySerializer());
        scalaSerializerMap.put(ListSerializer.INSTANCE, new ScalaListSerializer());
        scalaSerializerMap.put(SetSerializer.INSTANCE, new ScalaSetSerializer());
        scalaSerializerMap.put(MapSerializer.INSTANCE, new ScalaMapSerializer());
        scalaSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new ScalaObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        var protocol_root_path = StringUtils.format("package {}", protocolPackage);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolManagerTemplate.scala");
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols({}) = {}Registration", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put(classOf[{}], {})", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.scala"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Scala protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : GenerateProtocolFile.subProtocolFirst(registrations)) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolsTemplate.scala");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.scala", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Scala protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolManagerTemplate.scala");
        var protocol_manager_registrations = new StringBuilder();
        var protocol_imports = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {}.{}.{}", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name)).append(LS);
            protocol_imports.append(StringUtils.format("import {}.{}.{}Registration", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols({}) = {}Registration", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put(classOf[{}], {})", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, StringUtils.format("package {}", protocolPackage)
                , CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.scala"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Scala protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolTemplate.scala");
            var protocol_root_path = StringUtils.format("package {}.{}", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id));
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Scala)
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.scala", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Scala protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        var protocol_root_path = StringUtils.format("package {}", protocolPackage);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolManagerTemplate.scala");
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols({}) = {}Registration", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put(classOf[{}], {})", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.scala"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Scala protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolTemplate.scala");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                    , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.scala", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Scala protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() {
        var rootPackage = StringUtils.format("package {}", protocolPackage);
        var list = List.of("scala/IProtocolRegistration.scala"
                , "scala/ByteBuffer.scala");
        for (var fileName : list) {
            // IProtocolRegistration
            var template = ClassUtils.getFileFromClassPathToString(fileName);
            var formatTemplate = CodeTemplatePlaceholder.formatTemplate(template, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, rootPackage
                    , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
            ));
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "scala/")));
            FileUtils.writeStringToFile(createFile, formatTemplate, false);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolClassTemplate.scala");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Scala)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("scala/ProtocolRegistrationTemplate.scala");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        ));
        return formatProtocolTemplate;
    }


    private String protocol_imports_fold(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var scalaBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("import {}.{}.{}", protocolPackage, GenerateProtocolPath.protocolPathPeriod(subProtocolId), protocolName);
            scalaBuilder.append(subProtocolPath).append(LS);
        }
        scalaBuilder.append(StringUtils.format("import {}.IProtocolRegistration", protocolPackage)).append(LS);
        scalaBuilder.append(StringUtils.format("import {}.ByteBuffer", protocolPackage)).append(LS);
        return scalaBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var scalaBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Scala);
            for (var fieldNote : fieldNotes) {
                scalaBuilder.append(fieldNote).append(LS);
            }
            var fieldTypeDefaultValue = scalaSerializer(fieldRegistration.serializer()).fieldTypeDefaultValue(field, fieldRegistration);
            var fieldType = fieldTypeDefaultValue.getKey();
            var fieldDefaultValue = fieldTypeDefaultValue.getValue();
            scalaBuilder.append(StringUtils.format("var {}: {} = {}", fieldName, fieldType, fieldDefaultValue)).append(LS);
        }
        return scalaBuilder.toString();
    }


    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var scalaBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            scalaBuilder.append("val beforeWriteIndex = buffer.getWriteOffset").append(LS);
            scalaBuilder.append(StringUtils.format("buffer.writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            scalaBuilder.append("buffer.writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            scalaSerializer(fieldRegistration.serializer()).writeObject(scalaBuilder, "message." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            scalaBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return scalaBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var scalaBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                scalaBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = scalaSerializer(fieldRegistration.serializer()).readObject(scalaBuilder, 1, field, fieldRegistration);
                scalaBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                scalaBuilder.append("}").append(LS);
                continue;
            }
            var readObject = scalaSerializer(fieldRegistration.serializer()).readObject(scalaBuilder, 0, field, fieldRegistration);
            scalaBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return scalaBuilder.toString();
    }

    public static String toScalaClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "Boolean";
                return typeName;
            case "byte":
            case "Byte":
                typeName = "Byte";
                return typeName;
            case "short":
            case "Short":
                typeName = "Short";
                return typeName;
            case "int":
            case "Integer":
                typeName = "Int";
                return typeName;
            case "long":
            case "Long":
                typeName = "Long";
                return typeName;
            case "float":
            case "Float":
                typeName = "Float";
                return typeName;
            case "double":
            case "Double":
                typeName = "Double";
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
        typeName = typeName.replace("<Boolean", "[Boolean");
        typeName = typeName.replace("Boolean>", "Boolean]");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "Byte");
        typeName = typeName.replace("Byte>", "Byte]");
        typeName = typeName.replace("<Byte", "[Byte");

        // 将Short转为short
        typeName = typeName.replace("Short[", "Short");
        typeName = typeName.replace("Short>", "Short]");
        typeName = typeName.replace("<Short", "[Short");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "Int");
        typeName = typeName.replace("Integer>", "Int]");
        typeName = typeName.replace("<Integer", "[Int");

        // 将Long转为long
        typeName = typeName.replace("Long[", "Long");
        typeName = typeName.replace("Long>", "Long]");
        typeName = typeName.replace("<Long", "[Long");

        // 将Float转为float
        typeName = typeName.replace("Float[", "Float");
        typeName = typeName.replace("Float>", "Float]");
        typeName = typeName.replace("<Float", "[Float");

        // 将Double转为double
        typeName = typeName.replace("Double[", "Double");
        typeName = typeName.replace("Double>", "Double]");
        typeName = typeName.replace("<Double", "[Double");

        // 将Character转为Char
        typeName = typeName.replace("Character[", "String");
        typeName = typeName.replace("Character>", "String]");
        typeName = typeName.replace("<Character", "[String");

        // 将String转为string
        typeName = typeName.replace("String[", "String");
        typeName = typeName.replace("String>", "String]");
        typeName = typeName.replace("<String", "[String");

        typeName = typeName.replace("Map<", "Map[");
        typeName = typeName.replace("Set<", "Set[");
        typeName = typeName.replace("List<", "List[");
        typeName = typeName.replace(">", "]");

        return typeName;
    }

}

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

package com.zfoo.orm.cache.wrapper;

import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.schema.NamespaceHandler;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author godotg
 */
public abstract class EnhanceUtils {

    static {
        // 适配Tomcat，因为Tomcat不是用的默认的类加载器，而Javassist用的是默认的加载器
        var classArray = new Class<?>[]{
                IEntityWrapper.class
        };

        var classPool = ClassPool.getDefault();

        for (var clazz : classArray) {
            if (classPool.find(clazz.getName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }
    }

    public static String toOriginType(Field idField) {
        var idType = idField.getType();
        if (idType == int.class) {
            return "((Integer)$1).intValue()";
        } else if (idType == Integer.class) {
            return "(Integer)$1";
        } else if (idType == long.class) {
            return "((Long)$1).longValue()";
        } else if (idType == Long.class) {
            return "(Long)$1";
        } else if (idType == float.class) {
            return "((Float)$1).floatValue()";
        } else if (idType == Float.class) {
            return "(Float)$1";
        } else if (idType == double.class) {
            return "((Double)$1).doubleValue()";
        } else if (idType == Double.class) {
            return "(Double)$1";
        } else if (idType == String.class) {
            return "(String)$1";
        } else {
            return StringUtils.format("({})$1", ObjectId.class.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public static <PK extends Comparable<PK>, E extends IEntity<PK>> IEntityWrapper<PK, E> createEntityWrapper(EntityWrapper<PK, E> entityWrapper) {
        try {
            var classPool = ClassPool.getDefault();

            Class<?> clazz = entityWrapper.getEntityClass();
            Field idField = entityWrapper.getIdField();
            Method setIdMethod = entityWrapper.getSetIdMethod();
            Field versionField = entityWrapper.getVersionField();
            Method getVersionMethod = entityWrapper.getGetVersionMethod();
            Method setVersionMethod = entityWrapper.getSetVersionMethod();

            // 定义类名称
            CtClass enhanceClazz = classPool.makeClass(EnhanceUtils.class.getName() + StringUtils.capitalize(NamespaceHandler.ORM) + UuidUtils.getLocalIntId());
            enhanceClazz.addInterface(classPool.get(IEntityWrapper.class.getName()));

            // 定义类实现的接口方法newEntity
            CtMethod newEntityMethod = new CtMethod(classPool.get(IEntity.class.getName()), "newEntity", classPool.get(new String[]{Comparable.class.getName()}), enhanceClazz);
            newEntityMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
            String newEntityMethodBody = StringUtils.format("{ {} entity = new {}(); entity.{}({}); return entity; }", clazz.getName(), clazz.getName(), setIdMethod.getName(), toOriginType(idField));
            newEntityMethod.setBody(newEntityMethodBody);
            enhanceClazz.addMethod(newEntityMethod);

            // 定义类实现的接口方法versionFieldName
            CtMethod versionFieldNameMethod = new CtMethod(classPool.get(String.class.getName()), "versionFieldName", null, enhanceClazz);
            versionFieldNameMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
            String versionFieldNameMethodBody = versionField == null ? "{ return null; }" : StringUtils.format("{ return \"{}\"; }", entityWrapper.versionFieldName());
            versionFieldNameMethod.setBody(versionFieldNameMethodBody);
            enhanceClazz.addMethod(versionFieldNameMethod);

            // 定义类实现的接口方法gvs
            CtMethod gvsMethod = new CtMethod(classPool.get(long.class.getName()), "gvs", classPool.get(new String[]{IEntity.class.getName()}), enhanceClazz);
            gvsMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
            String gvsMethodBody = getVersionMethod == null ? "{ return 0L; }" : StringUtils.format("{ return (({})$1).{}(); }", clazz.getName(), getVersionMethod.getName());
            gvsMethod.setBody(gvsMethodBody);
            enhanceClazz.addMethod(gvsMethod);

            // 定义类实现的接口方法svs
            CtMethod svsMethod = new CtMethod(classPool.get(void.class.getName()), "svs", classPool.get(new String[]{IEntity.class.getName(), long.class.getName()}), enhanceClazz);
            svsMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
            String svsMethodBody = setVersionMethod == null ? "{}" : StringUtils.format("{ (({})$1).{}($2); }", clazz.getName(), setVersionMethod.getName());
            svsMethod.setBody(svsMethodBody);
            enhanceClazz.addMethod(svsMethod);

            // 释放缓存
            enhanceClazz.detach();

            Class<?> resultClazz = enhanceClazz.toClass(IEntityWrapper.class);
            return (IEntityWrapper<PK, E>) resultClazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package org.apache.rat.config.parameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.AbstractHeaderMatcher;
import org.apache.rat.license.ILicense;

public class DescriptionBuilder {

    public static Description build(Object obj) {
        ConfigComponent configComponent = obj.getClass().getAnnotation(ConfigComponent.class);
        if (configComponent == null) {
            return null;
        }

        List<Description> children = getConfigComponents(obj.getClass());

        return new Description(configComponent, null, children);
    }

    public static Description build(Object obj, Method m) {
        ConfigComponent configComponent = m.getAnnotation(ConfigComponent.class);
        if (configComponent == null) {
            return null;
        }

        String value = null;
        try {
            Object o = m.invoke(obj);
            value = o == null ?  null : o.toString();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new Description(configComponent, value, Collections.emptyList());
    }
    

    private static List<Description> getConfigComponents(Class<?> clazz) {
        if (clazz==null || clazz == String.class || clazz == Object.class) {
            return Collections.emptyList();
        }
        List<Description> result = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            ConfigComponent configComponent = f.getAnnotation(ConfigComponent.class);
            System.out.println(f.getName());
            if (configComponent != null) {
                String name = StringUtils.isBlank(configComponent.name()) ? f.getName() :  configComponent.name();
                Class<?> childClazz = configComponent.parameterType() == null ? f.getType() : configComponent.parameterType();
                Description desc = new Description(configComponent.type(), name, 
                        configComponent.desc(), null, getConfigComponents(childClazz));
                result.add(desc);
            }
        }
        result.addAll(getConfigComponents(clazz.getSuperclass()));
        Arrays.stream(clazz.getInterfaces()).forEach(c -> result.addAll(getConfigComponents(c)));
        return result;
    }


//    public static Description build(ILicense license) {
//        IHeaderMatcher matcher = license.getMatcher();
//        List<Description> children = new ArrayList<>();
//        for (Method m : license.getClass().getMethods()) {
//            ConfigComponent config = m.getAnnotation(ConfigComponent.class);
//            if (config != null) {
//                Object value = m.invoke(license);
//                children.add( new Description(config, value == null ? null : value.toString(), null));
//            }
//        }
//        if (matcher != null) {
//            children.add(DescriptionBuilder.build(matcher));
//        }
//        return new Description(Component.Type.License, license.getName(), "", null, children);
//    }
    
    public static Description buildMap(Class<?> clazz) {
        ConfigComponent configComponent = clazz.getAnnotation(ConfigComponent.class);
        if (configComponent == null) {
            return null;
        }
        List<Description> children = getConfigComponents(clazz);
          
        return new Description(configComponent, null, children);
    }
}

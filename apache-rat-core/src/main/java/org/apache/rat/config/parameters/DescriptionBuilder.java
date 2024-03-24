package org.apache.rat.config.parameters;

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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.AbstractHeaderMatcher;
import org.apache.rat.license.ILicense;

public class DescriptionBuilder {

    public static Description build(Object obj) {
        ConfigComponent configComponent = obj.getClass().getAnnotation(ConfigComponent.class);
        if (configComponent == null) {
//            if (obj instanceof ILicense) {
//                return build((ILicense) obj);
//            }
            return null;
        }

        Optional<Method> configChildren =  getConfigChildren(obj.getClass());

        List<Description> children = getConfigComponents(obj.getClass()).stream().map(m -> DescriptionBuilder.build(obj, m))
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (configChildren.isPresent()) {
            Method m = configChildren.get();
            try {
                Object childrenObj = m.invoke(obj);
                if (childrenObj instanceof Iterable) {
                    for (Object o : (Iterable) childrenObj) {
                        Description childDesc = DescriptionBuilder.build(o);
                        if (childDesc != null) {
                            children.add(childDesc);
                        }
                    }
                } else {
                    Description childDesc = DescriptionBuilder.build(childrenObj);
                    if (childDesc != null) {
                        children.add(childDesc);
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return new Description(configComponent, null, children);
    }
    
    private static Optional<Method> getConfigChildren(Class<?> clazz) {
        return  Arrays.stream(clazz.getMethods())
            .filter(m -> Objects.nonNull(m.getAnnotation(ConfigChildren.class))).findFirst();
    }
    
    private static List<Method> getConfigComponents(Class<?> clazz) {
        return Arrays.stream(clazz.getMethods())
            .filter(m -> Objects.nonNull(m.getAnnotation(ConfigComponent.class))).collect(Collectors.toList());
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
        List<Description> children = new ArrayList<>();
          for (Method m : clazz.getMethods()) {
              ConfigComponent config = m.getAnnotation(ConfigComponent.class);
              if (config != null) {
                  children.add(new Description(config, null, null));
              }
          }
          return new Description(configComponent, null, children);
    }
}

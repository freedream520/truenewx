package org.truenewx.core.util;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 类的工具类<br/>
 * 本类中的方法只与Class有关，不与某个具体Object相关
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class ClassUtil {

    private ClassUtil() {
    }

    /**
     * 查找指定类的指定属性字段上的指定类型注解
     *
     * @param clazz
     *            类
     * @param propertyName
     *            属性名，可以是父类中的属性
     * @param annotationClass
     *            注解类型
     * @return 注解对象
     */
    public static <A extends Annotation> A findAnnotation(final Class<?> clazz,
            final String propertyName, final Class<A> annotationClass) {
        final Field field = findField(clazz, propertyName);
        if (field != null) {
            final A annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * 在指定类及其各级父类中查找表示指定属性的Field对象，如果无法找到则返回null
     *
     * @param clazz
     *            类
     * @param propertyName
     *            属性名
     * @return 表示指定属性的Field对象
     */
    public static Field findField(final Class<?> clazz, final String propertyName) {
        if (clazz != null && clazz != Object.class) { // Object类无法取到任何属性
            try {
                return clazz.getDeclaredField(propertyName);
            } catch (final SecurityException e) {
                // 当前类找到但无权限访问，则返回null
                return null;
            } catch (final NoSuchFieldException e) {
                // 当前类找不到，则到父类中找
                return findField(clazz.getSuperclass(), propertyName);
            }
        }
        return null;
    }

    /**
     * 获取指定超类型的第index个实际泛型类型
     *
     * @param superType
     *            超类型
     * @param index
     *            要取的泛型位置索引
     * @return 实际泛型类型
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getActualGenericType(final ParameterizedType superType,
            final int index) {
        if (superType != null) {
            final Type[] types = superType.getActualTypeArguments();
            Type type = types[index];
            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getRawType();
            }
            if (type instanceof Class) {
                return (Class<T>) type;
            }
        }
        return null;
    }

    /**
     * 获取指定类的源于其父类的实际泛型类型集<br/>
     * 如果指定类没有父类，或者父类没有泛型，则返回长度为0的空数组
     *
     * @param clazz
     *            指定了父类实际泛型的类
     * @return 实际泛型类型集
     */
    public static Class<?>[] getActualGenericTypes(final Class<?> clazz) {
        final Type type = clazz.getGenericSuperclass();
        return getActualGenericTypes(type);
    }

    /**
     * 获取指定类的源于指定接口的实际泛型类型集
     *
     * @param clazz
     *            指定了接口实际泛型的类
     * @param interfaceClass
     *            接口类型
     * @return 实际泛型类型集
     */
    public static <I, C extends I> Class<?>[] getActualGenericTypes(final Class<C> clazz,
            final Class<I> interfaceClass) {
        final Collection<ParameterizedType> types = findParameterizedGenericInterfaces(clazz);
        final Type type = getMatchedGenericType(types, interfaceClass);
        return getActualGenericTypes(type);
    }

    /**
     * 获取指定类型的实际泛型类型集
     *
     * @param type
     *            类型
     * @return 实际泛型类型集
     */
    private static Class<?>[] getActualGenericTypes(final Type type) {
        if (type instanceof ParameterizedType) {
            final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            final Class<?>[] classes = new Class<?>[types.length];
            for (int i = 0; i < types.length; i++) {
                Type argType = types[i];
                if (argType instanceof ParameterizedType) {
                    argType = ((ParameterizedType) argType).getRawType();
                } else if (argType instanceof Class) {
                    classes[i] = (Class<?>) argType;
                } else {
                    classes[i] = null;
                }
            }
            return classes;
        }
        return new Class<?>[0];
    }

    /**
     * 获取指定类的源于指定接口的第index个实际泛型类型
     *
     * @param clazz
     *            指定了指定接口实际泛型的类
     * @param interfaceClass
     *            接口类型
     * @param index
     *            要取的泛型位置索引
     * @return 实际泛型类型
     */
    public static <I, C extends I, T> Class<T> getActualGenericType(final Class<C> clazz,
            final Class<I> interfaceClass, final int index) {
        final Collection<ParameterizedType> types = findParameterizedGenericInterfaces(clazz);
        final ParameterizedType superInterface = getMatchedGenericType(types, interfaceClass);
        return getActualGenericType(superInterface, index);
    }

    /**
     * 查出指定类的所有带泛型的接口类型清单
     *
     * @param clazz
     *            类型
     * @return 带泛型的接口类型清单
     */
    private static Collection<ParameterizedType> findParameterizedGenericInterfaces(
            final Class<?> clazz) {
        final List<ParameterizedType> types = new ArrayList<>();
        for (final Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                types.add((ParameterizedType) type);
            }
        }
        final Type superclass = clazz.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            types.add((ParameterizedType) superclass);
        }
        return types;
    }

    /**
     * 获取指定类的源于其父类的第index个实际泛型类型
     *
     * @param clazz
     *            指定了父类实际泛型的类
     * @param index
     *            要取的泛型位置索引
     * @return 实际泛型类型
     */
    public static <T> Class<T> getActualGenericType(final Class<?> clazz, final int index) {
        final Type superClass = clazz.getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            return getActualGenericType((ParameterizedType) superClass, index);
        }
        return null;
    }

    /**
     * 获取指定类的指定注解的value()值
     *
     * @param clazz
     *            类
     * @param annotationClass
     *            注解类
     * @return 注解的value()值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getClassAnnotationValue(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass) {
        final Annotation annotation = AnnotationUtils.findAnnotation(clazz, annotationClass);
        return annotation == null ? null : (T) AnnotationUtils.getValue(annotation);
    }

    /**
     * 获取指定类的指定属性字段上指定类型的注解value()值
     *
     * @param clazz
     *            类
     * @param propertyName
     *            属性名
     * @param annotationClass
     *            注解类型
     * @return 注解的value()值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldAnnotationValue(final Class<?> clazz, final String propertyName,
            final Class<? extends Annotation> annotationClass) {
        final Annotation annotation = findAnnotation(clazz, propertyName, annotationClass);
        return annotation == null ? null : (T) AnnotationUtils.getValue(annotation);
    }

    /**
     * 获取指定类型集中与指定接口类型泛型匹配的类型
     *
     * @param types
     *            类型集
     * @param interfaceClass
     *            接口类型
     * @return 指定类型集中与指定接口类型泛型匹配的类型
     */
    private static ParameterizedType getMatchedGenericType(
            final Collection<ParameterizedType> types, final Class<?> interfaceClass) {
        for (final ParameterizedType type : types) {
            final Type rawType = type.getRawType();
            if (rawType.equals(interfaceClass)) {
                return type;
            }
        }
        for (final ParameterizedType type : types) {
            final Type rawType = type.getRawType();
            if (rawType instanceof Class) {
                final Class<?> rawClass = (Class<?>) rawType;
                if (interfaceClass.isAssignableFrom(rawClass)) {
                    final Type[] actualTypeArguments = type.getActualTypeArguments();
                    // 构造满足条件的ParameterizedType
                    return new ParameterizedType() {
                        @Override
                        public Type[] getActualTypeArguments() {
                            // 从子接口中找出匹配父接口泛型的类型
                            final TypeVariable<?>[] typeParameters = interfaceClass
                                    .getTypeParameters();
                            final Type[] result = new Type[typeParameters.length];
                            for (int i = 0; i < typeParameters.length; i++) {
                                final TypeVariable<?>[] tvs = rawClass.getTypeParameters();
                                for (int j = 0; j < tvs.length; j++) {
                                    final TypeVariable<?> tv = tvs[j];
                                    if (tv.getName().equals(typeParameters[i].getName())) {
                                        result[i] = actualTypeArguments[j];
                                        break;
                                    }
                                }
                            }
                            return result;
                        }

                        @Override
                        public Type getRawType() {
                            return type.getRawType();
                        }

                        @Override
                        public Type getOwnerType() {
                            return type.getOwnerType();
                        }
                    };
                }
            }
        }
        return null;
    }

    /**
     * 获取指定类的指定公开静态属性值
     *
     * @param clazz
     *            类
     * @param propertyName
     *            属性名
     * @return 属性值
     */
    public static Object getPublicStaticPropertyValue(final Class<?> clazz,
            final String propertyName) {
        try {
            final Field field = clazz.getDeclaredField(propertyName);
            if (field != null) {
                final int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                    return field.get(null);
                }
            }
        } catch (final Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }

    /**
     * 获取指定类的简单属性名。简单属性包括：原始类型，字符串，数字，日期，URI，URL，Locale
     *
     * @param clazz
     *            类
     * @return 指定类的简单属性名
     */
    public static Set<String> getSimplePropertyNames(final Class<?> clazz) {
        final Set<String> names = new HashSet<String>();
        final PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);
        for (final PropertyDescriptor pd : propertyDescriptors) {
            if (BeanUtils.isSimpleValueType(pd.getPropertyType())) {
                final String name = pd.getName();
                if (!"class".equals(name)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /**
     * 获取指定类的所有简单属性字段。简单属性包括：原始类型，字符串，数字，日期，URI，URL，Locale
     *
     * @param clazz
     *            类
     * @return 指定类的所有简单属性字段
     */
    public static List<Field> getSimplePropertyField(final Class<?> clazz) {
        final List<Field> fields = new ArrayList<Field>();
        // 在所有有效属性中识别简单类型字段
        final PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(clazz);
        for (final PropertyDescriptor pd : propertyDescriptors) {
            final Class<?> propertyType = pd.getPropertyType();
            if (propertyType != null && BeanUtils.isSimpleValueType(propertyType)) {
                final String name = pd.getName();
                if (!"class".equals(name)) {
                    final Field field = findField(clazz, name);
                    if (field != null) {
                        fields.add(field);
                    }
                }
            }
        }
        return fields;
    }

    /**
     * 查找指定类型中的属性-类型映射集
     *
     * @param clazz
     *            类型
     * @param gettable
     *            是否包含可读属性
     * @param settable
     *            是否包含可写属性
     * @param parent
     *            是否包含父类中的属性
     * @param includes
     *            包含的属性集
     * @param excludes
     *            排除的属性集
     * @return 属性-类型映射集
     */
    public static Collection<PropertyMeta> findPropertyMetas(final Class<?> clazz,
            final boolean gettable, final boolean settable, final boolean parent,
            final String[] includes, final String[] excludes) {
        final Map<String, PropertyMeta> result = new LinkedHashMap<>();
        if (gettable || settable) {
            if (parent) { // 先加入父类的属性
                final Class<?> superclass = clazz.getSuperclass();
                if (superclass != null && superclass != Object.class) {
                    final Collection<PropertyMeta> propertyMetas = findPropertyMetas(superclass,
                            gettable, settable, parent, includes, excludes);
                    for (final PropertyMeta propertyMeta : propertyMetas) {
                        result.put(propertyMeta.getName(), propertyMeta);
                    }
                }
            }
            if (clazz.isInterface()) { // 如果类型为接口，则根据getter/setter方法名称进行筛选
                for (final Method method : clazz.getMethods()) {
                    final String propertyName = getPropertyName(method, gettable, settable);
                    if (propertyName != null) {
                        Class<?> propertyType = null;
                        final String methodName = method.getName();
                        if (methodName.startsWith("get")) { // getter方法，类型取方法结果类型
                            propertyType = method.getReturnType();
                        } else if (methodName.startsWith("set")) { // setter方法，类型取第一个参数的类型
                            propertyType = method.getParameterTypes()[0];
                        }
                        // 属于包含属性清单且不属于排除属性清单才加入
                        if (propertyType != null && isIncluded(includes, excludes, propertyName)) {
                            addPropertyMeta(result, propertyName, propertyType,
                                    method.getAnnotations());
                        }
                    }
                }
            } else { // 如果类型为类则获取其属性描述进行筛选
                final PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
                for (final PropertyDescriptor pd : pds) {
                    final String propertyName = pd.getName();
                    final Method readMethod = pd.getReadMethod();
                    final Method writeMethod = pd.getWriteMethod();
                    if (!"class".equals(propertyName)
                            && ((gettable && readMethod != null)
                                    || (settable && writeMethod != null))
                            && isIncluded(includes, excludes, propertyName)) {
                        // 先添加声明字段上的注解
                        addPropertyMeta(result, propertyName, pd.getPropertyType(),
                                getDeclaredFieldAnnotations(clazz, propertyName));
                        if (readMethod != null) { // 尝试添加读方法上的注解
                            addPropertyMeta(result, propertyName, pd.getPropertyType(),
                                    readMethod.getAnnotations());
                        }
                        if (writeMethod != null) { // 尝试添加写方法上的注解
                            addPropertyMeta(result, propertyName, pd.getPropertyType(),
                                    writeMethod.getAnnotations());
                        }
                    }
                }
            }
        }
        return result.values();
    }

    private static void addPropertyMeta(final Map<String, PropertyMeta> result,
            final String propertyName, final Class<?> propertyType,
            final Annotation[] annotations) {
        PropertyMeta propertyMeta = result.get(propertyName);
        if (propertyMeta == null) {
            propertyMeta = new PropertyMeta(propertyName, propertyType, annotations);
        } else {
            propertyMeta.addAnnotations(annotations);
        }
        result.put(propertyName, propertyMeta);
    }

    private static Annotation[] getDeclaredFieldAnnotations(final Class<?> clazz,
            final String propertyName) {
        try {
            final Field field = clazz.getDeclaredField(propertyName);
            return field.getAnnotations();
        } catch (NoSuchFieldException | SecurityException e) {
        }
        return new Annotation[0];
    }

    private static boolean isIncluded(final String[] includes, final String[] excludes,
            final String s) {
        return (ArrayUtils.isEmpty(includes) || ArrayUtils.contains(includes, s))
                && (ArrayUtils.isEmpty(excludes) || !ArrayUtils.contains(excludes, s));
    }

    /**
     * 获取指定方法可能访问的属性名，如果该方法不是getter/setter方法，则返回null
     *
     * @param method
     *            方法
     * @param getter
     *            是否考虑为getter方法的可能
     * @param setter
     *            是否考虑为setter方法的可能
     * @return 属性名
     */
    private static String getPropertyName(final Method method, final boolean getter,
            final boolean setter) {
        // 至少要考虑为getter/setter方法中的一种
        if (getter || setter) {
            final String methodName = method.getName();
            if (methodName.length() > 3) { // 3为"get"或"set"的长度
                String propertyName = methodName.substring(3); // 可能的属性名
                // 截取出来的属性名首字母需为大写
                if (propertyName.length() > 0 && Character.isUpperCase(propertyName.charAt(0))) {
                    propertyName = StringUtil.firstToLowerCase(propertyName);
                    if (getter && isPropertyMethod(method, propertyName, true)) {
                        return propertyName;
                    }
                    if (setter && isPropertyMethod(method, propertyName, false)) {
                        return propertyName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 判断指定方法是否指定属性的访问方法
     *
     * @param method
     *            方法
     * @param propertyName
     *            属性名
     * @param getter
     *            是否getter方法，false-setter方法
     * @return 指定方法是否指定属性的访问方法
     */
    private static boolean isPropertyMethod(final Method method, final String propertyName,
            final boolean getter) {
        // 必须是公开的非静态方法
        if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
            // 方法名称要匹配
            final String methodName = (getter ? "get" : "set")
                    + StringUtil.firstToUpperCase(propertyName);
            if (methodName.equals(method.getName())) {
                if (getter) { // getter方法必须无参数且返回结果不为void
                    return method.getParameterTypes().length == 0
                            && method.getReturnType() != void.class;
                } else { // setter方法必须有一个参数且返回结果为void
                    return method.getParameterTypes().length == 1
                            && method.getReturnType() == void.class;
                }
            }
        }
        return false;
    }

    /**
     * 查找指定类型及其父类中指定属性的访问方法
     *
     * @param clazz
     *            类型
     * @param propertyName
     *            属性名
     * @param getter
     *            是否getter方法，false-setter方法
     * @return 指定类型中指定属性的访问方法
     */
    public static Method findPropertyMethod(final Class<?> clazz, final String propertyName,
            final boolean getter) {
        for (final Method method : clazz.getMethods()) {
            if (isPropertyMethod(method, propertyName, getter)) {
                return method;
            }
        }
        // 此时没找到，则尝试从父类中查找
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return findPropertyMethod(superclass, propertyName, getter);
        }
        return null;
    }

    /**
     * 在指定类型中查找所有符合Bean规范的属性描述集合
     *
     * @param clazz
     *            类型
     * @param propertyType
     *            期望的属性类型，为空时忽略属性类型限制
     * @return 符合Bean规范的属性描述集合
     */
    public static List<PropertyDescriptor> findBeanPropertyDescriptors(final Class<?> clazz,
            final Class<?> propertyType) {
        final List<PropertyDescriptor> list = new ArrayList<>();
        final PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
        for (final PropertyDescriptor pd : pds) {
            final Class<?> pt = pd.getPropertyType();
            // 带参数的get方法会导致属性类型为null的属性描述，应忽略
            if (pt != null && (propertyType == null || propertyType.isAssignableFrom(pt))) {
                list.add(pd);
            }
        }
        return list;
    }

    /**
     * 判断指定类型集合中是否有至少一个类型，与指定类型相同或为其父类型
     *
     * @param classes
     *            类型集合
     * @param clazz
     *            类型
     * @return 指定类型集合中是否有至少一个类型，与指定类型相同或为其父类型
     */
    public static boolean oneIsAssignableFrom(final Class<?>[] classes, final Class<?> clazz) {
        for (final Class<?> type : classes) {
            if (type == null || clazz == null) {
                System.out.println();
            }
            if (type.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定类型是否复合类型
     *
     * @param type
     *            类型
     * @return 指定类型是否复合类型
     */
    public static boolean isComplex(final Class<?> type) {
        return !ClassUtils.isPrimitiveOrWrapper(type) && !CharSequence.class.isAssignableFrom(type);
    }

    /**
     * 获取指定类型（包括父类）中指定方法名称和参数个数的公开方法清单
     *
     * @param type
     *            类型
     * @param methodName
     *            方法名称
     * @param argCount
     *            参数个数，小于0时忽略个数，返回所有同名方法
     * @return 指定类型中指定方法名称和参数个数的公开方法清单
     */
    public static Collection<Method> findPublicMethods(final Class<?> type, final String methodName,
            final int argCount) {
        final Collection<Method> methods = new ArrayList<>();
        for (final Method method : type.getMethods()) {
            if (method.getName().equals(methodName)
                    && (argCount < 0 || method.getParameterTypes().length == argCount)) {
                methods.add(method);
            }
        }
        return methods;
    }
}

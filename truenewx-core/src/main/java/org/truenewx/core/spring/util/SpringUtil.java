package org.truenewx.core.spring.util;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.truenewx.core.util.StringUtil;

/**
 * Spring工具类
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class SpringUtil {

    private SpringUtil() {
    }

    /**
     * 从Spring容器中获取指定名称的bean
     *
     * @param context
     *            Spring容器上下文
     * @param beanName
     *            bean名称
     * @return bean对象，如果找不到则返回null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getBeanByName(final ApplicationContext context, final String beanName) {
        try {
            return (T) context.getBean(beanName);
        } catch (final BeansException e) {
            return null;
        }
    }

    /**
     * 按照默认命名规则，从Spring容器上下文中获取指定类型的bean。默认命名规则为类名首字母小写
     *
     * @param context
     *            Spring容器上下文
     * @param beanClass
     *            bean类型
     * @return Spring容器上下文中指定类型的bean
     */
    public static <T> T getBeanByDefaultName(final ApplicationContext context,
                    final Class<T> beanClass) {
        try {
            return context.getBean(StringUtil.firstToLowerCase(beanClass.getSimpleName()),
                            beanClass);
        } catch (final BeansException e) {
            return null;
        }
    }

    /**
     * 从Spring容器上下文中获取指定类型的第一个bean，优先获取bean名称为默认命名规则下的名称的bean， 如果没有则获取Spring容器中默认顺序下的第一个bean。
     * 该方法一般用于在确知Spring容器中只有一个指定类型的bean，或不关心指定类型实现时
     *
     * @param context
     *            Spring容器上下文
     * @param beanClass
     *            bean类型
     * @param exclusiveClasses
     *            排除的类型。该类型（一般为指定bean类型的子类）的bean不作为有效结果对象返回
     * @return Spring容器上下文中获取指定类型的第一个bean
     */
    public static <T> T getFirstBeanByClass(final ApplicationContext context,
                    final Class<T> beanClass, final Class<?>... exclusiveClasses) {
        if (context == null) {
            return null;
        }
        final T bean = getBeanByDefaultName(context, beanClass);
        if (bean != null && !ArrayUtils.contains(exclusiveClasses, bean.getClass())) {
            return bean;
        }
        final String[] beanNames = context.getBeanNamesForType(beanClass);
        for (final String beanName : beanNames) {
            final T obj = context.getBean(beanName, beanClass);
            if (!ArrayUtils.contains(exclusiveClasses, obj.getClass())) {
                return obj;
            }
        }
        return getFirstBeanByClass(context.getParent(), beanClass, exclusiveClasses);
    }

}

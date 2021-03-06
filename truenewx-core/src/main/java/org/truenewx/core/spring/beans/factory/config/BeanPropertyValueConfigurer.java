package org.truenewx.core.spring.beans.factory.config;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.truenewx.core.spring.beans.ContextInitializedBean;

/**
 * Bean属性值配置器
 * 
 * @author jianglei
 * @since JDK 1.8
 */
public class BeanPropertyValueConfigurer implements InitializingBean, ContextInitializedBean {
    /**
     * Bean对象
     */
    private Object bean;
    /**
     * Bean名称
     */
    private String beanName;
    /**
     * Bean类型
     */
    private Class<?> beanClass;
    /**
     * 属性名
     */
    private String propertyName;
    /**
     * 属性值
     */
    private Object propertyValue;

    /**
     * @param bean
     *            Bean对象
     */
    public void setBean(final Object bean) {
        this.bean = bean;
    }

    /**
     * @param beanName
     *            Bean名称
     */
    public void setBeanName(final String beanName) {
        this.beanName = beanName;
    }

    /**
     * @param beanClass
     *            Bean类型
     */
    public void setBeanClass(final Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * @param name
     *            属性名
     */
    public void setPropertyName(final String name) {
        this.propertyName = name;
    }

    /**
     * @param value
     *            属性值
     */
    public void setPropertyValue(final Object value) {
        this.propertyValue = value;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.bean != null) {
            BeanUtils.setProperty(this.bean, this.propertyName, this.propertyValue);
        }
    }

    @Override
    public void afterInitialized(final ApplicationContext context) throws Exception {
        if (this.bean == null) {
            Assert.isTrue(this.beanName != null || this.beanClass != null); // Bean名称或类型至少一个不为null
            if (this.beanName != null && this.beanClass != null) {
                this.bean = context.getBean(this.beanName, this.beanClass);
            } else if (this.beanName != null) {
                this.bean = context.getBean(this.beanName);
            } else if (this.beanClass != null) {
                this.bean = context.getBean(this.beanClass);
            }
            afterPropertiesSet();
        }
    }

}

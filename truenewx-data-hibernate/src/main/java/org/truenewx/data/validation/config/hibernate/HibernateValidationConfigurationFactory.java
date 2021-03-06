package org.truenewx.data.validation.config.hibernate;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Constraint;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.truenewx.core.spring.beans.ContextInitializedBean;
import org.truenewx.core.util.ClassUtil;
import org.truenewx.data.model.Entity;
import org.truenewx.data.model.Model;
import org.truenewx.data.model.TransportModel;
import org.truenewx.data.orm.hibernate.LocalSessionFactoryRegistry;
import org.truenewx.data.validation.config.ValidationConfiguration;
import org.truenewx.data.validation.config.ValidationConfigurationFactory;
import org.truenewx.data.validation.config.annotation.InheritConstraint;
import org.truenewx.data.validation.rule.DecimalRule;
import org.truenewx.data.validation.rule.LengthRule;
import org.truenewx.data.validation.rule.MarkRule;
import org.truenewx.data.validation.rule.ValidationRule;
import org.truenewx.data.validation.rule.builder.ValidationRuleBuilder;

/**
 * 校验配置工厂实现
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class HibernateValidationConfigurationFactory
                implements ValidationConfigurationFactory, ContextInitializedBean {
    private LocalSessionFactoryRegistry sessionFactoryRegistry;
    private Map<Class<? extends Model>, ValidationConfiguration> configurations = new HashMap<>();
    private Map<Class<Annotation>, ValidationRuleBuilder<?>> ruleBuilders = new HashMap<>();

    @Autowired
    public void setSessionFactoryRegistry(
                    final LocalSessionFactoryRegistry sessionFactoryRegistry) {
        this.sessionFactoryRegistry = sessionFactoryRegistry;
    }

    @SuppressWarnings("unchecked")
    public void setValidationRuleBuilders(final Collection<ValidationRuleBuilder<?>> builders) {
        for (final ValidationRuleBuilder<?> builder : builders) {
            for (final Class<?> constraintType : builder.getConstraintTypes()) {
                Assert.isTrue(isConstraintAnnotation(constraintType));
                // setter方法设置的处理器优先，会覆盖掉已有的处理器
                this.ruleBuilders.put((Class<Annotation>) constraintType, builder);
            }
        }
    }

    private boolean isConstraintAnnotation(final Class<?> annoClass) {
        return annoClass.getAnnotation(Constraint.class) != null;
    }

    @Override
    public synchronized ValidationConfiguration getConfiguration(
                    final Class<? extends Model> modelClass) {
        ValidationConfiguration configuration = this.configurations.get(modelClass);
        if (configuration == null) {
            configuration = buildConfiguration(modelClass);
            if (configuration != null) {
                this.configurations.put(modelClass, configuration);
            }
        }
        return configuration;
    }

    @SuppressWarnings("unchecked")
    private ValidationConfiguration buildConfiguration(final Class<? extends Model> modelClass) {
        final ValidationConfiguration configuration = new ValidationConfiguration(modelClass);
        if (TransportModel.class.isAssignableFrom(modelClass)) {
            addEntityClassRulesFromTransportClass(configuration,
                            (Class<? extends TransportModel<?>>) modelClass);
        } else if (Entity.class.isAssignableFrom(modelClass)) {
            addEntityClassRulesFromPersistentConfig(configuration,
                            (Class<? extends Entity>) modelClass);
        }
        addRulesByAnnotation(configuration, modelClass);

        return configuration;
    }

    /**
     * 从指定传输模型类对应的实体类中添加校验规则到指定校验配置中
     *
     * @param configuration
     *            校验配置
     * @param transportClass
     *            传输模型类
     */
    private void addEntityClassRulesFromTransportClass(final ValidationConfiguration configuration,
                    final Class<? extends TransportModel<?>> transportClass) {
        final Class<? extends Entity> entityClass = ClassUtil.getActualGenericType(transportClass,
                        TransportModel.class, 0);
        final List<Field> fields = ClassUtil.getSimplePropertyField(transportClass);
        for (final Field field : fields) {
            // 加入对应实体的校验规则
            // 只加入传输模型中存在的简单属性的校验规则
            Class<? extends Entity> entityType = entityClass;
            String propertyName = field.getName();
            final InheritConstraint ic = field.getAnnotation(InheritConstraint.class);
            if (ic != null) {
                if (StringUtils.isNotBlank(ic.value())) {
                    propertyName = ic.value();
                }
                if (ic.type() != Entity.class) {
                    entityType = ic.type();
                }
            }
            if (entityType != null) {
                final ValidationConfiguration entityConfig = getConfiguration(entityType);
                if (entityConfig != null) {
                    final Set<ValidationRule> rules = entityConfig.getRules(propertyName);
                    if (rules != null && rules.size() > 0) {
                        configuration.getRules(field.getName()).addAll(rules);
                    }
                }
            }
            addRulesByPropertyAnnotations(configuration, field);
        }
    }

    /**
     * 从指定实体类对应的持久化配置中添加校验规则到指定校验配置中
     *
     * @param configuration
     *            校验配置
     * @param entityClass
     *            实体类
     */
    private void addEntityClassRulesFromPersistentConfig(
                    final ValidationConfiguration configuration,
                    final Class<? extends Entity> entityClass) {
        final Iterator<Property> properties = this.sessionFactoryRegistry
                        .getClassProperties(entityClass);
        if (properties != null) {
            while (properties.hasNext()) {
                addRuleByProperty(configuration, entityClass, properties.next());
            }
        }
    }

    /**
     * 向指定校验设置中添加指定实体类型中指定属性的规则
     *
     * @param configuration
     *            校验配置
     * @param entityClass
     *            实体类型
     * @param property
     *            属性
     */
    @SuppressWarnings("unchecked")
    private void addRuleByProperty(final ValidationConfiguration configuration,
                    final Class<? extends Entity> entityClass, final Property property) {
        final String propertyName = property.getName();
        final PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(entityClass, propertyName);
        if (pd != null) {
            final Class<?> propertyClass = pd.getPropertyType();
            // 只处理字符串型、数值、日期型
            if (CharSequence.class.isAssignableFrom(propertyClass)
                            || Number.class.isAssignableFrom(propertyClass)
                            || (propertyClass.isPrimitive() && propertyClass != boolean.class)
                            || Date.class.isAssignableFrom(propertyClass)) {
                final Iterator<Column> columns = property.getColumnIterator();
                // 只支持对应且仅对应一个物理字段的
                if (!columns.hasNext()) {
                    return;
                }
                final Column column = columns.next();
                if (columns.hasNext()) {
                    return;
                }
                if (CharSequence.class.isAssignableFrom(propertyClass)) { // 字符串型
                    final int maxLength = column.getLength();
                    if (maxLength > 0) { // 长度大于0才有效
                        final LengthRule rule = new LengthRule();
                        rule.setMax(maxLength);
                        configuration.addRule(propertyName, rule);
                    }
                } else if (Date.class.isAssignableFrom(propertyClass)) { // 日期型
                    if (!column.isNullable()) { // 不允许为null的日期型，添加不允许为空白的约束
                        configuration.addRule(propertyName, new MarkRule(NotBlank.class));
                    }
                } else { // 数值型
                    if (!column.isNullable()) { // 不允许为null的数值型，添加不允许为空白的约束
                        configuration.addRule(propertyName, new MarkRule(NotBlank.class));
                    }
                    int precision = column.getPrecision();
                    int scale = column.getScale();
                    if (propertyClass == long.class) {
                        if (precision > 20) {
                            precision = 20;
                        }
                        scale = 0;
                    } else if (propertyClass == int.class) {
                        if (precision > 11) {
                            precision = 11;
                        }
                        scale = 0;
                    } else if (propertyClass == short.class) {
                        if (precision > 5) {
                            precision = 5;
                        }
                        scale = 0;
                    } else if (propertyClass == byte.class) {
                        if (precision > 3) {
                            precision = 3;
                        }
                        scale = 0;
                    }
                    if (scale >= 0 && precision > scale) { // 精度大于等于0且长度大于精度才有效，不支持负精度
                        final DecimalRule rule = new DecimalRule();
                        rule.setPrecision(precision);
                        rule.setScale(scale);
                        configuration.addRule(propertyName, rule);
                    }
                }
            }
        }
    }

    /**
     * 从指定类的校验约束注解中添加校验规则到指定校验配置中
     *
     * @param configuration
     *            校验配置
     * @param clazz
     *            类
     */
    private void addRulesByAnnotation(final ValidationConfiguration configuration,
                    final Class<?> clazz) {
        final List<Field> fields = ClassUtil.getSimplePropertyField(clazz);
        for (final Field field : fields) {
            addRulesByPropertyAnnotations(configuration, field);
        }
    }

    private void addRulesByPropertyAnnotations(final ValidationConfiguration configuration,
                    final Field field) {
        final String propertyName = field.getName();
        // 先在属性字段上找约束注解生成规则
        for (final Annotation annotation : field.getAnnotations()) {
            addRuleByPropertyAnnotation(configuration, propertyName, annotation);
        }
        // 再尝试在属性的getter方法上找约束注解生成规则，这意味着getter方法上的约束注解优先级更高
        final Method method = ClassUtil.findPropertyMethod(field.getDeclaringClass(), propertyName,
                        true);
        if (method != null) {
            for (final Annotation annotation : method.getAnnotations()) {
                addRuleByPropertyAnnotation(configuration, propertyName, annotation);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addRuleByPropertyAnnotation(final ValidationConfiguration configuration,
                    final String propertyName, final Annotation annotation) {
        final Class<? extends Annotation> annoClass = annotation.annotationType();
        if (isConstraintAnnotation(annoClass)) {
            final ValidationRuleBuilder<ValidationRule> builder = (ValidationRuleBuilder<ValidationRule>) this.ruleBuilders
                            .get(annoClass);
            if (builder != null) {
                final Class<? extends ValidationRule> ruleClass = ClassUtil.getActualGenericType(
                                builder.getClass(), ValidationRuleBuilder.class, 0);
                ValidationRule rule = configuration.getRule(propertyName, ruleClass);
                if (rule == null) {
                    rule = builder.create(annotation);
                    configuration.addRule(propertyName, rule);
                } else {
                    builder.update(annotation, rule);
                }
            }
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void afterInitialized(final ApplicationContext context) throws Exception {
        final Map<String, ValidationRuleBuilder> beans = context
                        .getBeansOfType(ValidationRuleBuilder.class);
        for (final ValidationRuleBuilder<?> builder : beans.values()) {
            for (final Class<?> constraintType : builder.getConstraintTypes()) {
                Assert.isTrue(isConstraintAnnotation(constraintType));
                // 不覆盖通过setter方法设置的处理器
                if (!this.ruleBuilders.containsKey(constraintType)) {
                    this.ruleBuilders.put((Class<Annotation>) constraintType, builder);
                }
            }
        }
    }

}

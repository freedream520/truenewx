package org.truenewx.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.truenewx.core.spring.beans.factory.TransactionalBeanFactory;

/**
 * 服务工厂实现
 *
 * @author jianglei
 * @since JDK 1.8
 */
@org.springframework.stereotype.Service
public class ServiceFactoryImpl implements ServiceFactory, ServiceRegistrar {

    private TransactionalBeanFactory beanFactory;

    private Map<Class<?>, Service> transactionalServices = new ConcurrentHashMap<>();
    private Map<Class<?>, Service> untransactionalServices = new ConcurrentHashMap<>();

    @Autowired
    public void setBeanFactory(final TransactionalBeanFactory transactionalBeanFactory) {
        this.beanFactory = transactionalBeanFactory;
    }

    @Override
    public void register(final Class<? extends Service> serviceInterface,
                    final Service transactionalService, final Service untransactionalService) {
        if (transactionalService != null && serviceInterface.isInstance(transactionalService)) {
            this.transactionalServices.put(serviceInterface, transactionalService);
        }
        if (untransactionalService != null && serviceInterface.isInstance(untransactionalService)) {
            this.untransactionalServices.put(serviceInterface, untransactionalService);
        }
    }

    @Override
    public <S extends Service> S getService(final Class<S> serviceClass) {
        return getService(serviceClass, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Service> S getService(final Class<S> serviceClass,
                    final boolean transactional) {
        if (transactional) {
            S service = (S) this.transactionalServices.get(serviceClass);
            if (service == null) { // 如果没有缓存，则尝试从bean工厂中获取并缓存
                service = this.beanFactory.getBean(serviceClass, true);
                if (service != null) {
                    this.transactionalServices.put(serviceClass, service);
                }
            }
            return service;
        } else {
            S service = (S) this.untransactionalServices.get(serviceClass);
            if (service == null) { // 如果没有缓存，则尝试从bean工厂中获取并缓存
                service = this.beanFactory.getBean(serviceClass, false);
                if (service != null) {
                    this.untransactionalServices.put(serviceClass, service);
                }
            }
            return service;
        }
    }

}

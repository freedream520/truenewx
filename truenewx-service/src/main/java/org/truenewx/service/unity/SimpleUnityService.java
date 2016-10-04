package org.truenewx.service.unity;

import java.io.Serializable;

import org.truenewx.core.exception.HandleableException;
import org.truenewx.data.model.unity.Unity;

/**
 * 简单的单体服务
 *
 * @author jianglei
 * @since JDK 1.8
 */
public interface SimpleUnityService<T extends Unity<K>, K extends Serializable>
                extends UnityService<T, K> {
    /**
     * 添加单体
     *
     * @param unity
     *            存放添加数据的单体对象
     * @return 添加成功的单体
     * @throws HandleableException
     *             如果添加校验失败
     */
    T add(T unity) throws HandleableException;

    /**
     * 修改单体
     *
     * @param id
     *            要修改单体的标识
     * @param unity
     *            存放修改数据的单体对象
     * @return 修改后的单体
     * @throws HandleableException
     *             如果修改校验失败
     */
    T update(K id, T unity) throws HandleableException;

}

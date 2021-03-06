package org.truenewx.data.model.unity;

import java.io.Serializable;

/**
 * 抽象单体
 *
 * @param <K>
 *            标识的类型
 * @author jianglei
 * @since JDK 1.8
 */
public abstract class AbstractUnity<K extends Serializable> implements Unity<K> {
    /**
     * 标识
     */
    private K id;

    /**
     * 用指定标识构造单体对象
     *
     * @param id
     *            标识
     */
    protected AbstractUnity(final K id) {
        this.id = id;
    }

    /**
     * 默认构造函数
     */
    public AbstractUnity() {
    }

    @Override
    public K getId() {
        return this.id;
    }

    protected void setId(final K id) {
        this.id = id;
    }

}

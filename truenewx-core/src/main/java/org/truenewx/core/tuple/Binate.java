package org.truenewx.core.tuple;

/**
 * 二元的（由两个元素组成的）
 * 
 * @author jianglei
 * @since JDK 1.8
 * @param <L>
 *            左元类型
 * @param <R>
 *            右元类型
 */
public interface Binate<L, R> {
    /**
     * 获取左元
     * 
     * @return 左元
     */
    L getLeft();

    /**
     * 获取右元
     * 
     * @return 右元
     */
    R getRight();

}

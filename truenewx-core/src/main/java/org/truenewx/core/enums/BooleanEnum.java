package org.truenewx.core.enums;

import org.truenewx.core.annotation.Caption;

/**
 * 布尔枚举
 *
 * @author jianglei
 * @since JDK 1.8
 */
public enum BooleanEnum {

    @Caption("是")
    TRUE,

    @Caption("否")
    FALSE;

    /**
     * 用指定布尔值构建布尔枚举
     *
     * @param value
     *            布尔值
     * @return 布尔枚举
     */
    public static BooleanEnum valueOf(final boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /**
     * @return 布尔值
     */
    public boolean value() {
        return this == TRUE;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

}

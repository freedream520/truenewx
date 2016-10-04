package org.truenewx.web.rpc;

import java.io.Serializable;

/**
 * RPC端口
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class RpcPort implements Serializable {

    private static final long serialVersionUID = 581148104864740468L;

    /**
     * Bean ID
     */
    private String beanId;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数个数
     */
    private Integer argCount;

    /**
     *
     * @param beanId
     *            Bean ID
     * @param methodName
     *            方法名
     */
    public RpcPort(final String beanId, final String methodName) {
        this.beanId = beanId;
        this.methodName = methodName;
    }

    /**
     *
     * @param beanId
     *            Bean ID
     * @param methodName
     *            方法名
     * @param argCount
     *            参数个数
     */
    public RpcPort(final String beanId, final String methodName, final int argCount) {
        this.beanId = beanId;
        this.methodName = methodName;
        this.argCount = argCount;
    }

    /**
     * @return Bean ID
     */
    public String getBeanId() {
        return this.beanId;
    }

    /**
     * @return 方法名
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     *
     * @return 参数个数
     */
    public Integer getArgCount() {
        return this.argCount;
    }

    /**
     * 判断指定RPC是否匹配当前PRC端口
     *
     * @param beanId
     *            Bean Id
     * @param methodName
     *            方法名
     * @param argCount
     *            参数个数，为null时忽略参数个数的比较
     * @return 指定RPC是否匹配当前PRC端口
     */
    public boolean isMatched(final String beanId, final String methodName, final Integer argCount) {
        if (this.beanId.equals(beanId) && this.methodName.equals(methodName)) {
            if (this.argCount == null || argCount == null) {
                return true; // 任意一个参数个数为null，均不再进行参数个数的比较匹配
            }
            return this.argCount.equals(argCount);
        }
        return false;
    }

}

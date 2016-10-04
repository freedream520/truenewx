package org.truenewx.web.spring.context;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Spring Web上下文工具类<br/>
 * 要求web.xml中具有如下配置：<br/>
 * &lt;listener&gt;<br/>
 * &lt;listener-class&gt;<br/>
 * org.springframework.web.context.request.RequestContextListener<br/>
 * &lt;/listener-class&gt;<br/>
 * &lt;/listener&gt;
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class SpringWebContext {

    private SpringWebContext() {
    }

    public static HttpServletRequest getRequest() {
        final ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();
        return sra.getRequest();
    }

    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletContext getServletContext() {
        return getSession().getServletContext();
    }

    /**
     * @return 区域
     */
    public static Locale getLocale() {
        return getRequest().getLocale();
    }

    /**
     * 设置request的属性
     *
     * @param name
     *            属性名
     * @param value
     *            属性值
     */
    public static void set(final String name, final Object value) {
        getRequest().setAttribute(name, value);
    }

    /**
     * 获取request的属性值
     *
     * @param name
     *            属性名
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(final String name) {
        return (T) getRequest().getAttribute(name);
    }

    /**
     * 移除request的属性
     *
     * @param name
     *            属性名
     * @return 被移除的属性值，没有该属性则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(final String name) {
        final HttpServletRequest request = getRequest();
        final Object value = request.getAttribute(name);
        if (value != null) {
            request.removeAttribute(name);
        }
        return (T) value;
    }

    /**
     * 设置属性至SESSION中
     *
     * @param name
     *            属性名
     * @param value
     *            属性值
     */
    public static void setToSession(final String name, final Object value) {
        getSession().setAttribute(name, value);
    }

    /**
     * 从SESSION获取指定属性
     *
     * @param name
     *            属性
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromSession(final String name) {
        return (T) getSession().getAttribute(name);
    }

    /**
     * 移除SESSION中的指定属性
     *
     * @param name
     *            属性名
     * @return 被移除的属性值，没有该属性则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T removeFromSession(final String name) {
        final HttpSession session = getSession();
        final Object value = session.getAttribute(name);
        if (value != null) {
            session.removeAttribute(name);
        }
        return (T) value;
    }

    /**
     * 转换指定结果名为直接重定向的结果名
     *
     * @param result
     *            结果名
     * @return 直接重定向的结果名
     */
    public static String toRedirectResult(final String result) {
        return StringUtils.join("redirect:", result);
    }

    public static RequestMethod getRequestMethod() {
        final String method = getRequest().getMethod().toUpperCase();
        return EnumUtils.getEnum(RequestMethod.class, method);
    }

    /**
     * 使当前session失效，下次再使用session时将重新创建新的session
     */
    public static void invalidateSession() {
        getSession().invalidate();
    }
}

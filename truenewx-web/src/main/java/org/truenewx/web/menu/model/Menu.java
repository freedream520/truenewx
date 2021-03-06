package org.truenewx.web.menu.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.truenewx.core.tuple.Binary;
import org.truenewx.core.tuple.Binate;
import org.truenewx.web.security.authority.Authority;

/**
 * 菜单类
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class Menu implements Serializable {

    private static final long serialVersionUID = 7864620719633440806L;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单项集合
     */
    private List<MenuItem> items = new ArrayList<>();

    /**
     * 不可见的菜单操作集合
     */
    private List<MenuOperation> operations = new ArrayList<>();

    /**
     * 菜单构造函数
     *
     * @param name
     *            菜单名称
     */
    public Menu(final String name) {
        this.name = name;
    }

    /**
     * 菜单构造函数
     *
     * @param name
     *            菜单名称
     * @param items
     *            菜单项集合
     */
    public Menu(final String name, final List<MenuItem> items) {
        this.name = name;
        this.items = items;
    }

    /**
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return 菜单项集合
     */
    public Iterable<MenuItem> getItems() {
        return this.items;
    }

    /**
     * 获取指定菜单项
     *
     * @param index
     *            菜单项索引下标
     * @return 菜单项
     */
    public MenuItem getItem(final int index) {
        return this.items.get(index);
    }

    /**
     *
     * @return 不可见的菜单操作集合
     */
    public Iterable<MenuOperation> getOperations() {
        return this.operations;
    }

    public void addItem(final MenuItem item) {
        this.items.add(item);
    }

    public void addOperation(final MenuOperation operation) {
        this.operations.add(operation);
    }

    public Authority getAuthority(final String href, final HttpMethod method) {
        for (final MenuItem item : this.items) {
            final Authority authority = item.getAuthority(href, method);
            if (authority != null) {
                return authority;
            }
        }
        for (final MenuOperation operation : this.operations) {
            final Authority authority = operation.getAuthority(href, method);
            if (authority != null) {
                return authority;
            }
        }
        return null;
    }

    public Authority getAuthority(final String beanId, final String methodName,
            final Integer argCount) {
        for (final MenuItem item : this.items) {
            final Authority authority = item.getAuthority(beanId, methodName, argCount);
            if (authority != null) {
                return authority;
            }
        }
        for (final MenuOperation operation : this.operations) {
            final Authority authority = operation.getAuthority(beanId, methodName, argCount);
            if (authority != null) {
                return authority;
            }
        }
        return null;
    }

    public Authority[] getAllAuthorities() {
        final Set<Authority> authorities = new HashSet<>();
        for (final MenuItem item : this.items) {
            authorities.addAll(item.getAllAuthorities());
        }
        for (final MenuOperation operation : this.operations) {
            final Authority authority = operation.getAuthority();
            if (authority != null) {
                authorities.add(authority);
            }
        }
        return authorities.toArray(new Authority[authorities.size()]);
    }

    /**
     * 获取匹配指定链接地址和链接方法的菜单动作下标和对象集合
     *
     * @param href
     *            链接地址
     * @param method
     *            链接方法
     * @return 匹配指定链接地址和链接方法的菜单动作下标和对象集合
     */
    public List<Binate<Integer, MenuAction>> indexesOf(final String href, final HttpMethod method) {
        if (StringUtils.isBlank(href)) {
            return null;
        }
        for (int i = 0; i < this.items.size(); i++) {
            final MenuItem item = this.items.get(i);
            // 先在更下级中找
            final List<Binate<Integer, MenuAction>> indexes = item.indexesOf(href, method);
            if (indexes.size() > 0) { // 在下级中找到
                indexes.add(0, new Binary<Integer, MenuAction>(i, item)); // 加上对应的下级索引
                return indexes;
            }
            // 更下级中没找到再到直接下级找，以免更下级中包含有与直接下级一样的链接
            if (item.contains(href, method)) { // 在当前级别找到
                indexes.add(new Binary<Integer, MenuAction>(i, item));
                return indexes;
            }
        }
        return new ArrayList<>();
    }

    public List<Binate<Integer, MenuAction>> indexesOf(final String beanId, final String methodName,
            final Integer argCount) {
        for (int i = 0; i < this.items.size(); i++) {
            final MenuItem item = this.items.get(i);
            // 先在更下级中找
            final List<Binate<Integer, MenuAction>> indexes = item.indexesOf(beanId, methodName,
                    argCount);
            if (indexes.size() > 0) { // 在下级中找到
                indexes.add(0, new Binary<Integer, MenuAction>(i, item)); // 加上对应的下级索引
                return indexes;
            }
        }
        // 没有下级则在不可见操作中查找
        final List<Binate<Integer, MenuAction>> indexes = new ArrayList<>();
        for (int i = 0; i < this.operations.size(); i++) {
            final MenuOperation operation = this.operations.get(i);
            if (operation.contains(beanId, methodName, argCount)) {
                indexes.add(new Binary<Integer, MenuAction>(i, operation));
            }
        }
        return indexes;
    }

    /**
     * 获取匹配指定RPC的菜单操作集合，包括各级子菜单中的菜单操作
     *
     * @param beanId
     *            Bean Id
     * @param methodName
     *            方法名
     * @param argCount
     *            参数个数，为null时忽略参数个数比较
     * @return 匹配指定RPC的菜单操作集合
     */
    public List<MenuOperation> getOperations(final String beanId, final String methodName,
            final Integer argCount) {
        if (StringUtils.isNotBlank(beanId) && StringUtils.isNotBlank(methodName)) {
            final List<MenuOperation> operations = new ArrayList<>();
            for (final MenuItem item : this.items) {
                operations.addAll(item.getOperations(beanId, methodName, argCount));
            }
            for (final MenuOperation operation : this.operations) {
                if (operation.contains(beanId, methodName, argCount)) {
                    operations.add(operation);
                }
            }
            return operations;
        }
        return null;
    }

    /**
     * 获取指定权限的菜单动作集合
     *
     * @param authority
     *            权限名称
     * @return 指定权限的菜单动作集合
     */
    public List<MenuAction> getActions(final String authority) {
        if (StringUtils.isNotEmpty(authority)) {
            for (final MenuItem item : this.items) {
                final List<MenuAction> list = item.getActions(authority);
                if (list.size() > 0) {
                    list.add(0, item);
                    return list;
                }
            }
        }
        return null;
    }

}

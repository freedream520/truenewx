package org.truenewx.core.enums.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 枚举项
 * 
 * @author jianglei
 * @since JDK 1.8
 */
public class EnumItem implements Comparable<EnumItem> {
    private int ordinal;
    private String key;
    private String caption;
    private Map<String, EnumItem> children = new LinkedHashMap<String, EnumItem>();

    public EnumItem(final int ordinal, final String key, final String caption) {
        if (key == null) {
            throw new IllegalArgumentException("The key must be not null");
        }
        this.ordinal = ordinal;
        this.key = key;
        this.caption = caption;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public String getKey() {
        return this.key;
    }

    public String getCaption() {
        return this.caption;
    }

    /**
     * 添加一个子项
     * 
     * @param child
     *            子项
     */
    public void addChild(final EnumItem child) {
        this.children.put(child.getKey(), child);
    }

    /**
     * 添加多个子项
     * 
     * @param children
     *            子项集
     */
    public void addChildren(final Collection<EnumItem> children) {
        synchronized (this.children) {
            for (final EnumItem child : children) {
                addChild(child);
            }
        }
    }

    /**
     * 设置子项集
     * 
     * @param children
     *            子项集
     */
    public void setChildren(final Collection<EnumItem> children) {
        this.children.clear();
        addChildren(children);
    }

    /**
     * 获取子项集的值
     * 
     * @return
     */
    public Iterable<EnumItem> getChildren() {
        return this.children.values();
    }

    /**
     * 逐级获取指定键的子级枚举项
     * 
     * @param key
     *            键
     * @param keys
     *            其它键
     * 
     * @return 子级枚举项
     */
    public EnumItem getChild(final String key, final String... keys) {
        EnumItem child = this.children.get(key);
        for (final String k : keys) {
            if (child == null) {
                break;
            }
            child = child.getChild(k);
        }
        return child;
    }

    public Set<String> getChildNames() {
        return this.children.keySet();
    }

    public List<EnumItem> getChildrenPath(final String... keys) {
        final List<EnumItem> children = new ArrayList<EnumItem>();
        if (keys.length == 0) {
            children.addAll(this.children.values());
            Collections.sort(children);
        } else {
            final EnumItem child = getChild(null, keys);
            if (child != null) {
                return child.getChildrenPath();
            }
        }
        return children;
    }

    public EnumItem getChildByCaption(final String caption) {
        if (caption != null) {
            for (final EnumItem item : this.children.values()) {
                if (caption.equals(item.getCaption())) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * 获取指定显示说明对应的枚举项相对顶级枚举类型项的枚举项路径
     * 
     * @param caption
     *            显示说明
     * @return 枚举项路径
     */
    public List<EnumItem> getChildrenPathByCaption(final String caption) {
        final List<EnumItem> chain = new ArrayList<EnumItem>();
        for (final EnumItem child : this.children.values()) {
            if (child.getCaption().equals(caption)) {
                chain.add(child);
                break;
            }
            final List<EnumItem> childChain = child.getChildrenPathByCaption(caption);
            if (childChain != null && childChain.size() > 0) {
                chain.add(child);
                chain.addAll(childChain);
                break;
            }
        }
        return chain;
    }

    @Override
    public String toString() {
        return "[" + this.ordinal + "]" + this.key + "=" + this.caption;
    }

    @Override
    public int compareTo(final EnumItem other) {
        if (this.ordinal == other.ordinal) { // 序号相等时比较其它两个属性
            if (this.caption == null || this.caption.equals(other.caption)) { // 当前说明为空或两个说明相等时，最后用键排序
                return this.key.compareTo(other.key);
            } else { // 其次用说明排序
                return this.caption.compareTo(other.caption);
            }
        } else { // 优先用序号排序
            return Integer.valueOf(this.ordinal).compareTo(other.ordinal);
        }
    }

}

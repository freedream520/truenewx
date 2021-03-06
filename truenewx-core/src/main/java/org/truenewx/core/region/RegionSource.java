package org.truenewx.core.region;

import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * 行政区划来源
 *
 * @author jianglei
 * @since JDK 1.8
 */
public interface RegionSource {
    /**
     * 国家代号的长度
     */
    int NATION_LENGTH = 2;

    /**
     * 获取指定行政区划，其显示名为指定显示区域下的文本<br/>
     *
     * @param regionCode
     *            行政区划代号
     * @param locale
     *            显示区域
     * @return 对应区划选项
     */
    @Nullable
    Region getRegion(String regionCode, @Nullable Locale locale);

    /**
     * 获取指定国家的指定行政区划的选项
     *
     * @param nation
     *            国家代号
     * @param provinceCaption
     *            省份名称
     * @param cityCaption
     *            市名称
     * @param countyCaption
     *            县名称
     * @param locale
     *            显示区域
     *
     * @return 对应区划选项
     */
    @Nullable
    Region getRegion(String nation, @Nullable String provinceCaption, @Nullable String cityCaption,
            @Nullable String countyCaption, @Nullable Locale locale);

    /**
     * 获取国家级区划选项集，其选项显示名为指定显示区域下的文本
     *
     * @param locale
     *            显示区域
     *
     * @return 国家级区划选项集
     */
    Collection<Region> getNationalRegions(@Nullable Locale locale);

    @Nullable
    Region getNationalRegion(String nation, Locale locale);

}

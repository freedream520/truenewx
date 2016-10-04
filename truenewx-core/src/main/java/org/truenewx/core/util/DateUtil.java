package org.truenewx.core.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.truenewx.core.Strings;

/**
 * 日期工具类
 *
 * @author jianglei
 * @since JDK 1.8
 */
public class DateUtil {
    /**
     * 最未来的时间
     */
    public static final Date MOST_FUTURE_TIME = new Date(Long.MAX_VALUE);
    /**
     * 短日期格式
     */
    public static final String SHORT_DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";
    /**
     * 长日期格式
     */
    public static final String LONG_DATE_PATTERN = DateUtil.SHORT_DATE_PATTERN + Strings.SPACE
                    + DateUtil.TIME_PATTERN;
    /**
     * 没分隔符长日期格式
     */
    public static final String LONG_DATE_NO_DELIMITER_PATTERN = "yyyyMMddHHmmss";

    private static final long MS_ONE_SECOND = 1000;

    private static final long MS_ONE_MINUTE = 60 * 1000;

    private static final long MS_ONE_HOUR = 60 * DateUtil.MS_ONE_MINUTE;

    private static final long MS_ONE_DAY = 24 * 60 * DateUtil.MS_ONE_MINUTE;

    private DateUtil() {
    }

    /**
     * 获取当前日期，短日期格式yyyy-MM-dd
     *
     * @return 当前日期
     */
    public static String getCurrentDate() {
        return formatShort(new Date());
    }

    /**
     * 获取当前时间，长日期格式yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间
     */
    public static String getCurrentTime() {
        return formatLong(new Date());
    }

    /**
     * 获取当前时间，长日期格式yyyyMMddHHmmss
     *
     * @return 当前时间
     */
    public static String getCurrentTimeNoDelimiter() {
        return formatLongNoDelimiter(new Date());
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * 按照指定格式解析字符串型日期值为日期对象
     *
     * @param date
     *            字符串型日期
     * @param pattern
     *            日期格式
     * @return 日期对象
     */
    public static Date parse(final String date, final String pattern) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        final DateFormat formater = new SimpleDateFormat(pattern);
        try {
            return formater.parse(date);
        } catch (final ParseException e) {
            return null;
        }
    }

    /**
     * 按照指定格式格式化日期对象为字符串型日期
     *
     * @param date
     *            日期对象
     * @param pattern
     *            日期格式
     * @return 字符串型日期
     */
    public static String format(final Date date, final String pattern) {
        if (date == null) {
            return null;
        }
        final DateFormat formater = new SimpleDateFormat(pattern);
        return formater.format(date);
    }

    /**
     * 按照短日期格式(yyyy-MM-dd)解析字符串型日期值为日期对象
     *
     * @param date
     *            字符串型日期
     * @return 日期对象
     */
    public static Date parseShort(final String date) {
        return parse(date, DateUtil.SHORT_DATE_PATTERN);
    }

    /**
     * 按照短日期格式(yyyy-MM-dd)格式化日期对象为字符串型日期
     *
     * @param date
     *            日期对象
     * @return 字符串型日期
     */
    public static String formatShort(final Date date) {
        return format(date, DateUtil.SHORT_DATE_PATTERN);
    }

    /**
     * 按照长日期格式(yyyy-MM-dd HH:mm:ss)解析字符串型日期值为日期对象
     *
     * @param date
     *            字符串型日期
     * @return 日期对象
     */
    public static Date parseLong(final String date) {
        return parse(date, DateUtil.LONG_DATE_PATTERN);
    }

    /**
     * 按照长日期格式(yyyy-MM-dd HH:mm:ss)转换日期对象为字符串型日期
     *
     * @param date
     *            日期对象
     * @return 字符串型日期
     */
    public static String formatLong(final Date date) {
        return format(date, DateUtil.LONG_DATE_PATTERN);
    }

    /**
     * 按照长日期格式(yyyyMMddHHmmss)转换日期对象为字符串型日期
     *
     * @param date
     *            日期对象
     * @return 字符串型日期
     */
    public static String formatLongNoDelimiter(final Date date) {
        return format(date, DateUtil.LONG_DATE_NO_DELIMITER_PATTERN);
    }

    /**
     * 获取指定时间的日历对象
     *
     * @param date
     *            时间
     * @return 日历对象
     */
    public static Calendar getCalendar(final Date date) {
        if (date == null) {
            return null;
        }
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    /**
     * 计算指定两个时间之间的相差月份数。如果earlierDate晚于laterDate，则返回负值
     *
     * @param earlierDate
     *            较早时间
     * @param laterDate
     *            较晚时间
     * @return 天数差
     */
    public static int monthsBetween(final Date earlierDate, final Date laterDate) {
        final Calendar earlierCalendar = Calendar.getInstance();
        final Calendar laterCalendar = Calendar.getInstance();
        earlierCalendar.setTime(earlierDate);
        laterCalendar.setTime(laterDate);
        final int months = (laterCalendar.get(Calendar.YEAR) - earlierCalendar.get(Calendar.YEAR))
                        * 12;
        return months - earlierCalendar.get(Calendar.MONTH) + laterCalendar.get(Calendar.MONTH);
    }

    /**
     * 计算指定两个时间之间的相差天数。如果earlierDate晚于laterDate，则返回负值
     *
     * @param earlierDate
     *            较早时间
     * @param laterDate
     *            较晚时间
     * @return 天数差
     */
    public static int daysBetween(final Date earlierDate, final Date laterDate) {
        final Calendar earlierCalendar = setTimeToCalendar(earlierDate, 0, 0, 0, 0);
        final Calendar laterCalendar = setTimeToCalendar(laterDate, 0, 0, 0, 0);
        final long dms = laterCalendar.getTimeInMillis() - earlierCalendar.getTimeInMillis();
        return (int) (dms / DateUtil.MS_ONE_DAY);
    }

    /**
     * 计算指定两个时间之间的相差小时之差。如果earlierDate晚于laterDate，则返回负值
     *
     * @param earlierDate
     *            较早时间
     * @param laterDate
     *            较晚时间
     * @return 小时之差
     */
    public static int hoursBetween(final Date earlierDate, final Date laterDate) {
        final Calendar earlierCalendar = setTimeToCalendar(earlierDate, -1, 0, 0, 0);
        final Calendar laterCalendar = setTimeToCalendar(laterDate, -1, 0, 0, 0);
        final long dms = laterCalendar.getTimeInMillis() - earlierCalendar.getTimeInMillis();
        return (int) (dms / DateUtil.MS_ONE_HOUR);
    }

    /**
     * 计算指定两个时间之间的相差分钟数。如果earlierDate晚于laterDate，则返回负值
     *
     * @param earlierDate
     *            较早时间
     * @param laterDate
     *            较晚时间
     * @return 分钟差
     */
    public static int minutesBetween(final Date earlierDate, final Date laterDate) {
        final Calendar earlierCalendar = setTimeToCalendar(earlierDate, -1, -1, 0, 0);
        final Calendar laterCalendar = setTimeToCalendar(laterDate, -1, -1, 0, 0);
        final long dms = laterCalendar.getTimeInMillis() - earlierCalendar.getTimeInMillis();
        return (int) (dms / DateUtil.MS_ONE_MINUTE);
    }

    /**
     * 计算指定两个时间之间的相差秒数。如果earlierDate晚于laterDate，则返回负值
     *
     * @param earlierDate
     *            较早时间
     * @param laterDate
     *            较晚时间
     * @return 秒差
     */
    public static long secondsBetween(final Date earlierDate, final Date laterDate) {
        final Calendar earlierCalendar = setTimeToCalendar(earlierDate, -1, -1, -1, 0);
        final Calendar laterCalendar = setTimeToCalendar(laterDate, -1, -1, -1, 0);
        final long dms = laterCalendar.getTimeInMillis() - earlierCalendar.getTimeInMillis();
        return dms / DateUtil.MS_ONE_SECOND;
    }

    /**
     * 创建指定值的日期
     *
     * @param year
     *            年
     * @param month
     *            月
     * @param day
     *            日
     * @param hour
     *            时
     * @param minute
     *            分
     * @param second
     *            秒
     * @param millisecond
     *            毫秒
     * @return 日期
     */
    public static Date createDate(final int year, final int month, final int day, final int hour,
                    final int minute, final int second, final int millisecond) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1); // 月份从0开始
        c.set(Calendar.DATE, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, millisecond);
        return c.getTime();
    }

    /**
     * 获取指定日期加上指定年数后的日期值。若年数为负，则实际进行减操作
     *
     * @param date
     *            原日期
     * @param years
     *            年数
     * @return 计算后的新日期
     */
    public static Date addYears(final Date date, final int years) {
        if (years == 0) {
            return date;
        }
        final Calendar c = getCalendar(date);
        c.add(Calendar.YEAR, years);
        return c.getTime();
    }

    /**
     * 获取指定日期加上指定月数后的日期值。若月数为负，则实际进行减操作。
     *
     * @param date
     *            原日期
     * @param days
     *            月数
     * @return 计算后的新日期
     */
    public static Date addMonths(final Date date, final int months) {
        if (months == 0) {
            return date;
        }
        final Calendar c = getCalendar(date);
        c.add(Calendar.MONTH, months);
        return c.getTime();
    }

    /**
     * 获取指定日期加上指定天数后的日期值。若天数为负，则实际进行减操作。
     *
     * @param date
     *            原日期
     * @param days
     *            天数
     * @return 计算后的新日期
     */
    public static Date addDays(final Date date, final int days) {
        if (days == 0) {
            return date;
        }
        final Calendar c = getCalendar(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    /**
     * 获取指定日期加上指定小时数后的日期值。若小时数为负，则实际进行减操作。
     *
     * @param date
     *            原日期
     * @param hours
     *            小时数
     * @return 计算后的新日期
     */
    public static Date addHours(final Date date, final int hours) {
        if (hours == 0) {
            return date;
        }
        final Calendar c = getCalendar(date);
        c.add(Calendar.HOUR_OF_DAY, hours);
        return c.getTime();
    }

    /**
     * 获取指定日期加上指定分钟数后的日期值。若分钟数为负，则实际进行减操作。
     *
     * @param date
     *            原日期
     * @param hours
     *            分钟数
     * @return 计算后的新日期
     */
    public static Date addMinutes(final Date date, final int minutes) {
        if (minutes == 0) {
            return date;
        }
        final Calendar c = getCalendar(date);
        c.add(Calendar.MINUTE, minutes);
        return c.getTime();
    }

    /**
     * 获取指定日期加上指定秒数后的日期值。若秒数为负，则实际进行减操作。
     *
     * @param date
     *            原日期
     * @param seconds
     *            秒数
     * @return 计算后的新日期
     */
    public static Date addSeconds(final Date date, final int seconds) {
        if (seconds == 0) {
            return date;
        }
        final Calendar c = getCalendar(date);
        c.add(Calendar.SECOND, seconds);
        return c.getTime();
    }

    /**
     * 为指定日期设置年月日，返回新日期
     *
     * @param date
     *            原日期
     * @param year
     *            年
     * @param month
     *            月
     * @param day
     *            日
     * @return 新日期
     */
    public static Date setDate(final Date date, final int year, final int month, final int day) {
        final Calendar c = getCalendar(date);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1); // 月份从0开始
        c.set(Calendar.DATE, day);
        return c.getTime();
    }

    /**
     * 为指定日期设置时分秒毫秒，返回新日期
     *
     * @param date
     *            原日期
     * @param hour
     *            时
     * @param minute
     *            分
     * @param second
     *            秒
     * @param millisecond
     *            毫秒
     * @return 新日期
     */
    public static Date setTime(final Date date, final int hour, final int minute, final int second,
                    final int millisecond) {
        final Calendar c = setTimeToCalendar(date, hour, minute, second, millisecond);
        return c == null ? null : c.getTime();
    }

    private static Calendar setTimeToCalendar(final Date date, final int hour, final int minute,
                    final int second, final int millisecond) {
        final Calendar c = getCalendar(date);
        if (c == null) {
            return null;
        }
        if (hour >= 0) {
            c.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0) {
            c.set(Calendar.MINUTE, minute);
        }
        if (second >= 0) {
            c.set(Calendar.SECOND, second);
        }
        if (millisecond >= 0) {
            c.set(Calendar.MILLISECOND, millisecond);
        }
        return c;
    }

    /**
     * 获取指定日期集合中最早的日期
     *
     * @param dates
     *            日期集合
     * @return 最早的日期
     */
    public static Date earliest(final Date... dates) {
        Date result = null;
        for (final Date date : dates) {
            if (result == null) {
                result = date;
            } else if (date.before(result)) {
                result = date;
            }
        }
        return result;
    }

    /**
     * 获取指定日期集合中最晚的日期
     *
     * @param dates
     *            日期集合
     * @return 最晚的日期
     */
    public static Date latest(final Date... dates) {
        Date result = null;
        for (final Date date : dates) {
            if (result == null) {
                result = date;
            } else if (date.after(result)) {
                result = date;
            }
        }
        return result;
    }

    public static int getYear(final Date date) {
        return getCalendar(date).get(Calendar.YEAR);
    }

    public static int getMonth(final Date date) {
        return getCalendar(date).get(Calendar.MONTH) + 1; // 月份从0开始
    }

    public static int getDay(final Date date) {
        return getCalendar(date).get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断指定日期是否周末
     *
     * @param date
     *            日期
     * @return 是否周末
     */
    public static boolean isWeekend(final Date date) {
        final int weekday = getCalendar(date).get(Calendar.DAY_OF_WEEK);
        return weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY;
    }

}

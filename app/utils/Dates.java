package utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dates {
    private static final Map<String, SimpleDateFormat> formatCache = new ConcurrentHashMap<>();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    private static final SimpleDateFormat CHINESE_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日");

    private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

    public static String formatDate(Date date) {
        return CHINESE_DATE_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        return CHINESE_DATE_TIME_FORMAT.format(date);
    }

    public static String formatDate(Date date, String format) {
        if (null == date || StringUtils.isBlank(format)) {
            return null;
        }
        SimpleDateFormat formatter;
        if (formatCache.containsKey(format)) {
            formatter = formatCache.get(format);
        } else {
            formatter = new SimpleDateFormat(format);
        }

        return formatter.format(date);
    }

    public static Date parseDate(String str) {
        try {
            return DATE_FORMAT.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDate(String source, String format) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(format)) {
            return null;
        }
        SimpleDateFormat formatter;
        if (formatCache.containsKey(format)) {
            formatter = formatCache.get(format);
        } else {
            formatter = new SimpleDateFormat(format);
        }

        try {
            return formatter.parse(source);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String convert(String source, String sourceFormat, String targetFormat) {
        if (StringUtils.isBlank(source) || StringUtils.isBlank(sourceFormat) || StringUtils.isBlank(targetFormat)) {
            return null;
        }

        return formatDate(parseDate(source, sourceFormat), targetFormat);
    }


    public static Date getBeginOfDay(Date date) {
        if (null == date) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date getEndOfDay(Date date) {
        if (null == date) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    public static boolean isBetween(Date date, Date begin, Date end) {
        if (null == date) {
            return false;
        }

        if (begin != null && end != null) {
            return !date.before(begin) && !date.after(end);
        }

        if (begin != null) {
            return !date.before(begin);
        }

        if (end != null) {
            return !date.after(end);
        }

        return true;
    }

    public static String getCurrentTime(){
    	return formatDateTime(new Date());
    }
    
    public static String getCurrentDay(){
    	return formatDate(new Date());
    }
    /**
     * 将系统毫秒数转成日期
     * @param time
     * @return
     */
    public static Date parseDateByLong(Long time){
    	Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
    	return calendar.getTime();
    }
}

package com.developcollect.easycode.utils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/26 15:38
 */
public class CronUtil extends cn.hutool.cron.CronUtil {


    /**
     * 获取 quarz cron表达式 根据执行时间
     *
     * @param jobTime 任务执行时间
     * @return String
     */
    public static String createCron(Date jobTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jobTime);
        return createCron(
                calendar.get(Calendar.SECOND),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                "?",
                calendar.get(Calendar.YEAR));
    }

    /**
     * 获取 quarz cron表达式 根据执行时间
     *
     * @param jobBeginTime 任务开始时间
     * @param interval     间隔长度
     * @param intervalUnit 间隔单位
     * @return String
     */
    public static String createCron(Date jobBeginTime, int interval, TimeUnit intervalUnit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jobBeginTime);

        return createCron(
                calendar.get(Calendar.SECOND),
                intervalUnit == TimeUnit.MINUTE ? calendar.get(Calendar.MINUTE) + "/" + interval : calendar.get(Calendar.MINUTE),
                intervalUnit == TimeUnit.HOUR ? calendar.get(Calendar.HOUR_OF_DAY) + "/" + interval : calendar.get(Calendar.HOUR_OF_DAY),
                intervalUnit == TimeUnit.DAY ? calendar.get(Calendar.DAY_OF_MONTH) + "/" + interval : calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                "?",
                ""
        );
    }

    public static String createCron(LocalDateTime jobTime) {
        return createCron(
                jobTime.getSecond(),
                jobTime.getMinute(),
                jobTime.getHour(),
                jobTime.getDayOfMonth(),
                jobTime.getMonth().getValue(),
                "?",
                jobTime.getYear()
        );
    }

    public static String createCron(LocalDateTime jobBeginTime, int interval, TimeUnit intervalUnit) {
        return createCron(
                jobBeginTime.getSecond(),
                intervalUnit == TimeUnit.MINUTE ? jobBeginTime.getMinute() + "/" + interval : jobBeginTime.getMinute(),
                intervalUnit == TimeUnit.HOUR ? jobBeginTime.getHour() + "/" + interval : jobBeginTime.getHour(),
                intervalUnit == TimeUnit.DAY ? jobBeginTime.getDayOfMonth() + "/" + interval : jobBeginTime.getDayOfMonth(),
                jobBeginTime.getMonth().getValue(),
                "?",
                ""
        );
    }

    private static String createCron(Object second, Object minute, Object hour, Object day, Object month, Object week, Object year) {
        return String.format("%s %s %s %s %s %s %s", second, minute, hour, day, month, week, year);
    }

    /**
     * 时间单位 1天 2时 3分
     *
     * @author: tom
     * @date: 2019年5月14日 下午5:40:53
     */
    public enum TimeUnit {
        /**
         * 1 天
         */
        DAY,
        /**
         * 2小时
         */
        HOUR,
        /**
         * 3 分
         */
        MINUTE;
    }

}

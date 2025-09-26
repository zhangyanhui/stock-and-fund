package com.buxuesong.account.infrastructure.general.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static boolean isTradingTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime openTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(9).plusMinutes(15);
        LocalDateTime closeTime = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(15).plusMinutes(30);
        if (currentDateTime.compareTo(openTime) >= 0 && currentDateTime.compareTo(closeTime) <= 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为交易日（周一至周五）
     */
    public static boolean isTradingDay() {
        DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    public static String getLocalDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)).toString();
    }
}

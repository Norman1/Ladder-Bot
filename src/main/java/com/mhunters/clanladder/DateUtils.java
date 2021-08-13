package com.mhunters.clanladder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("M/d/y HH:mm:ss");

    private DateUtils() {

    }

    /**
     * Parses a date.
     *
     * @param dateString a date string in as used by Warzone.
     * @return
     */
    public static LocalDateTime parseDate(String dateString) {
        return LocalDateTime.parse(dateString, FORMATTER);
    }

    public static String format(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }


}

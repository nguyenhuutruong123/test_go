package com.yes4all.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateUtils {
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String ISO_DATE_TIME_FORMAT_CUSTOM = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String SIMPLE_DATE_FORMAT = "dd-MMM-yyyy";
    public static final String SIMPLE_DATE_FORMAT_TO_SECOND_SIMPLE = "dd-MM-yyyy HH:mm:ss";

    public static final String SIMPLE_DATE_FORMAT_ENG =  "MM-dd-yyyy HH:mm:ss";

    public static final String SIMPLE_DATE_FORMAT_YYYY = "yyyy-MM-dd";
    public static final String SIMPLE_DATE_FORMAT_SEARCH = "mm/dd/yyyy";



    public static final String SIMPLE_DATE_FORMAT_PO_UPLOAD = "dd-MMM-yy";
    public static final String SIMPLE_DATE_FORMAT_PO_WH_UPLOAD = "dd/MM/yyyy";
    public static final String SIMPLE_DATE_FORMAT_TO_MONTH = "MM/dd/yyyy";
    public static final String DATE_WITH_ALPHABET_MONTH_REGEX = "^([1-9]|[12]\\d|3[01])[-/.]([A-Z][a-z]{2}[-/.])(19|20)\\d{2}$";

    public static String formatDate(Date date, String format) {
        DateFormat df = getDateFormat(format);
        return isNotEmpty(date) ? df.format(date) : "";
    }

    public static Date parseDateString(String dateStr, String pattern) {

        if (StringUtils.isNotBlank(dateStr) && StringUtils.isNotBlank(pattern)) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(pattern);
                return df.parse(dateStr);
            } catch (ParseException ex) {
                return null;
            }
        }
        return null;
    }

    public static boolean isNotEmpty(Object data) {
        return !isEmpty(data);
    }

    public static boolean isEmpty(Object data) {
        return data == null;
    }

    public static DateFormat getDateFormat(String format) {
        return strictDateFormatForPattern(format);
    }

    private static DateFormat strictDateFormatForPattern(String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setLenient(false);
        return dateFormat;
    }

    public static boolean validateDateFormat(String dateString) {
        if (CommonDataUtil.isEmpty(dateString)) {
            return false;
        }
        Pattern pattern = Pattern.compile(DATE_WITH_ALPHABET_MONTH_REGEX);
        return pattern.matcher(dateString).matches();
    }

    public static Date formatDateTime(String dateStr) {
        String format = DateUtils.validateDateFormat(dateStr) ? DateUtils.SIMPLE_DATE_FORMAT :
            DateUtils.ISO_DATE_TIME_FORMAT;
        return DateUtils.parseDateString(dateStr, format);
    }




    public static Date convertStringToInstantDate(String dateString) {

        return DateUtils.parseDateString(dateString, ISO_DATE_TIME_FORMAT_CUSTOM);

    }


    public static StopWatch initStopWatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    public static Instant convertStringToInstant(String dateString) {
        // Add 12:00:00 because if not date will save back 1 day
        dateString = dateString + " 12:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT_TO_SECOND_SIMPLE);

        LocalDateTime localDate = LocalDateTime.parse(dateString, formatter);
        return localDate.atZone(ZoneId.systemDefault()).toInstant();
    }
    public static Instant convertStringToInstantBooking(String dateString) {
        // Add 12:00:00 because if not date will save back 1 day
        dateString = dateString + " 12:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT_ENG);

        LocalDateTime localDate = LocalDateTime.parse(dateString, formatter);
        return localDate.atZone(ZoneId.systemDefault()).toInstant();
    }
    public static LocalDate convertStringLocalDate(String strDate) {
        Date date = DateUtils.parseDateString(strDate, SIMPLE_DATE_FORMAT_PO_UPLOAD);
        if (date == null) {
            return null;
        }
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    }
    public static LocalDate convertStringLocalDateWH(String strDate) {
        Date date = DateUtils.parseDateString(strDate, SIMPLE_DATE_FORMAT_PO_WH_UPLOAD);
        if (date == null) {
            return null;
        }
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    }
    public static LocalDate convertStringLocalDateDDMMYYYY(String strDate) {
        Date date = DateUtils.parseDateString(strDate, SIMPLE_DATE_FORMAT_YYYY);
        if (date == null) {
            return null;

        }
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    }

    public static LocalDate convertStringLocalDateBooking(String strDate) {
        Date date = DateUtils.parseDateString(strDate, SIMPLE_DATE_FORMAT_TO_MONTH);
        if(date==null){
            return null;
        }
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

    }

    public static String convertStringLocalDateSearch(String strDate) {
        Date date = DateUtils.parseDateString(strDate, SIMPLE_DATE_FORMAT_TO_MONTH);
        if (date == null) {
            return null;

        }
        LocalDateTime time = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        return time.toString();

    }


    public static String calculateTime(StopWatch stopWatch) {
        stopWatch.stop();
        String pattern = "mm:ss:SSS";
        Date date = new Date(stopWatch.getTotalTimeMillis());
        Format format = new SimpleDateFormat(pattern);
        return format.format(date) + " [mm:ss:SSS]";
    }

    public static String convertInstantToString(Instant dateStr, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Date date = Date.from(dateStr);
        return formatter.format(date);
    }


}

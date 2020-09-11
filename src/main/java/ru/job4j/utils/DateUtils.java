package ru.job4j.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtils.class);

    /**
     * Custom date -> Java Date converter.
     * Parses custom date value with custom symbols to  Java Date format.
     *
     * @param date custom string date value.
     * @param dfs custom date symbols.
     * @param sdf custom date format for parsing.
     * @return Java Date format.
     */
    public static Date getDate(String date, DateFormatSymbols dfs, SimpleDateFormat sdf) {
        Date rsl = null;

        if (date.contains("сегодня")) {
            SimpleDateFormat sdfToday = new SimpleDateFormat("d MMMMM yy", dfs);
            String formattedDate = sdfToday.format(new Date());
            date = date.replaceFirst("сегодня", formattedDate);
        } else if (date.contains("вчера")) {
            SimpleDateFormat sdfToday = new SimpleDateFormat("d MMMMM yy", dfs);
            String formattedDate = sdfToday
                    .format(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)));
            date = date.replaceFirst("вчера", formattedDate);
        }

        try {
            rsl = sdf.parse(date);
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
        }
        return rsl;
    }
}

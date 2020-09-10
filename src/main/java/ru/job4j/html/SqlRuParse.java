package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SqlRuParse {
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class);

    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select(".postslisttopic");
        for (Element td : row) {
            Element href = td.child(0);
            System.out.println(href.attr("href"));
            System.out.println(href.text());
            Element date = td.parent().child(5);
            System.out.println(getDate(date.text()));
        }
    }

    /**
     * Converts custom date format to Java Date format.
     *
     * @param date custom date format.
     * @return converted Date.
     */
    public static Date getDate(String date) {
        Date rsl = null;
        DateFormatSymbols dfs = new DateFormatSymbols(new Locale("ru"));
        dfs.setMonths(new String[] {
                "янв",
                "фев",
                "мар",
                "апр",
                "май",
                "июн",
                "июл",
                "авг",
                "сен",
                "окт",
                "ноя",
                "дек"
        });
        SimpleDateFormat sdfToday = new SimpleDateFormat("d MMMMM yy", dfs);

        if (date.contains("сегодня")) {
            String formattedDate = sdfToday.format(new Date());
            date = date.replaceFirst("сегодня", formattedDate);
        } else if (date.contains("вчера")) {
            String formattedDate = sdfToday
                    .format(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)));
            date = date.replaceFirst("вчера", formattedDate);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("d MMMMM yy, hh:mm", dfs);
        try {
            rsl = sdf.parse(date);
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
        }
        return rsl;
    }
}

package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.utils.DateUtils;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class SqlRuParse {
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class);
    private DateFormatSymbols dfs;
    private SimpleDateFormat sdf;

    public SqlRuParse() {
        dfs = new DateFormatSymbols(new Locale("ru"));
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
        sdf = new SimpleDateFormat("d MMMMM yy, hh:mm", dfs);
    }

    public static void main(String[] args) {
        SqlRuParse parser = new SqlRuParse();

        String url = "https://www.sql.ru/forum/job-offers/";
        ArrayList<Elements> elements = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            StringBuilder sb = new StringBuilder().append(url).append(i);
            try {
                Document doc = Jsoup.connect(sb.toString()).get();
                Elements row = doc.select(".postslisttopic");
                elements.add(row);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        elements.stream()
                .flatMap(Collection::stream)
                .forEach(e -> {
                    Element href = e.child(0);
                    System.out.println(href.attr("href"));
                    System.out.println(href.text());
                    Element date = e.parent().child(5);
                    System.out.println(DateUtils.getDate(date.text(), parser.dfs, parser.sdf));
                });
    }

    /**
     * Method parses html page for getting post message.
     *
     * @param link url of page with post.
     * @return message from post.
     */
    public String getMsg(String link) {
        String msg = "";
        try {
            Document doc = Jsoup.connect(link).get();
            Element row =
                    doc.getElementsByClass("msgTable").get(0).getElementsByClass("msgBody").get(1);
            msg = row.text();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return msg;
    }

    /**
     * Method parses html page for getting post creation date.
     *
     * @param link url of page with post.
     * @return creation date of post.
     */
    public String getCreateDate(String link) {
        String date = "";
        try {
            Document doc = Jsoup.connect(link).get();
            Element row =
                    doc.getElementsByClass("msgFooter").get(0);
            String footer = row.text();
            date = footer.substring(0, footer.indexOf("[") - 1);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return date;
    }
}

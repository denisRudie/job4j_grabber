package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.utils.DateUtils;

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

    public static void main(String[] args) throws Exception {
        SqlRuParse parser = new SqlRuParse();

        String url = "https://www.sql.ru/forum/job-offers/";
        ArrayList<Elements> elements = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            StringBuilder sb = new StringBuilder().append(url).append(i);
            Document doc = Jsoup.connect(sb.toString()).get();
            Elements row = doc.select(".postslisttopic");
            elements.add(row);
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
}

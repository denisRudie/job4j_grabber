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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SqlRuParse implements Parse {
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class);
    private DateFormatSymbols dfs;
    private SimpleDateFormat sdf;

    public SqlRuParse() {
        dfs = new DateFormatSymbols(new Locale("ru"));
        dfs.setMonths(new String[]{
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

    /**
     * Method parses html page with vacancies.
     * Getting general info about post from page with vacancies.
     * For getting detail info calls detail() method.
     *
     * @param link page with vacancies.
     * @return list of posts on page.
     */
    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements row = doc.select(".postslisttopic");
            row.forEach(e -> {
                String postLink = e.child(0).attr("href");
                Post p = detail(postLink);
                p.setName(e.child(0).text());
                p.setAuthor(e.parent().getElementsByClass("altCol").get(0).text());
                p.setAnswersCount(Integer.parseInt(e.parent().child(3).text()));
                p.setViewsCount(Integer.parseInt(e.parent().child(4).text()));
                p.setLastMessageDate(DateUtils.getDate(e.parent().child(5).text(), dfs, sdf));
                list.add(p);
            });
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    /**
     * Method parses post.
     *
     * @param link post link.
     * @return Post with link, message and createDate.
     */
    @Override
    public Post detail(String link) {
        String postMessage = "";
        Date createDate = null;

        try  {
            Document doc = Jsoup.connect(link).get();
            Element row = doc.getElementsByClass("msgTable").first();
            postMessage = row.getElementsByClass("msgBody").get(1).text();
            String footer = row.getElementsByClass("msgFooter").first().text();
            createDate = DateUtils.getDate(footer.substring(0, footer.indexOf("[") - 1), dfs, sdf);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return new Post(
                "",
                link,
                "",
                postMessage,
                createDate,
                -1,
                -1,
                null);
    }
}
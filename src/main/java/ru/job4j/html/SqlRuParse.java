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
import java.util.*;

/**
 * Parser for sql.ru jobs page.
 */
public class SqlRuParse implements Parse {
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class);
    private DateFormatSymbols dfs;
    private SimpleDateFormat sdf;

    /**
     * Constructor creates custom months names for parsing Data from sql.ru.
     * Also creates Date format like sql.ru Date format.
     */
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
     * Getting general info about post.
     * Detail info could be requested from detail() method.
     *
     * @param link page with vacancies.
     * @return list of posts on page. Each post doesn't contain message and create date.
     */
    @Override
    public List<Post> list(String link) {
        List<Post> javaVacancies = new ArrayList<>();
        List<Elements> elementsList = new ArrayList<>();
        try {
            int pageCounter = getPageCounter(link);
            for (int i = 1; i <= pageCounter; i++) {
                StringBuilder sb = new StringBuilder().append(link).append("/").append(i);
                Document page = Jsoup.connect(sb.toString()).get();
                Elements vacanciesOnPage = page.select(".postslisttopic");
                elementsList.add(vacanciesOnPage);
            }

            elementsList.stream()
                    .flatMap(Collection::stream)
                    .forEach(e -> {
                        String postLink = e.child(0).attr("href");
                        String name = e.child(0).text();
                        if (name.toLowerCase().contains("java")) {
                            Post p = new Post(
                                    name,
                                    postLink,
                                    e.parent().getElementsByClass("altCol").get(0).text(),
                                    "",
                                    null,
                                    Integer.parseInt(e.parent().child(3).text()),
                                    Integer.parseInt(e.parent().child(4).text()),
                                    DateUtils.getDate(
                                            e.parent().child(5).text(), dfs, sdf));
                            javaVacancies.add(p);
                        }
                    });
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return javaVacancies;
    }

    /**
     * Method goes inside the post and add message and createDate.
     *
     * @param post without details.
     * @return post with details.
     */
    @Override
    public Post detail(Post post) {
        String postMessage = "";
        Date createDate = null;

        try {
            Document doc = Jsoup.connect(post.getLink()).get();
            Element row = doc.getElementsByClass("msgTable").first();
            postMessage = row.getElementsByClass("msgBody").get(1).text();
            String footer = row.getElementsByClass("msgFooter").first().text();
            createDate = DateUtils.getDate(footer.substring(0, footer.indexOf("[") - 1), dfs, sdf);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        post.setMessage(postMessage);
        post.setCreateDate(createDate);
        return post;
    }

    /**
     * Method parses page with vacancies for count of pages.
     *
     * @param link page with vacancies.
     * @return count of pages.
     */
    private int getPageCounter(String link) {
        int count = -1;
        try {
            Document doc = Jsoup.connect(link).get();
            count = Integer.parseInt(
                    doc.getElementsByClass("sort_options")
                            .get(1)
                            .getElementsByTag("td")
                            .first()
                            .children()
                            .last()
                            .text());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return count;
    }
}
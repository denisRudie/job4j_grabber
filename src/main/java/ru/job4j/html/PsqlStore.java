package ru.job4j.html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Store for keeping data from sql.ru jobs page.
 */
public class PsqlStore implements Store, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class);
    private Connection cnn;
    private List<Post> currentDBPosts;
    private List<Post> sqlRuPosts;
    private SqlRuParse parser;
    private int addedPosts;
    private int updatedPosts;
    private int deletedPosts;

    /**
     * Constructor creates connection to DB.
     * Also put current data from DB to currentDBPosts list.
     * Also creates parser for additional parsing for posts, which doesn't exist in DB.
     * Also creates ArrayList for keeping posts loaded from sql.ru. It uses for finding deleted
     * posts.
     *
     * @param cfg Property object with DB connection configuration.
     */
    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver"));
            cnn = DriverManager.getConnection(cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password"));
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        currentDBPosts = getAll();
        parser = new SqlRuParse();
        sqlRuPosts = new ArrayList<>();
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Properties pr = new Properties();
        try (InputStream is = PsqlStore.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            pr.load(is);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        try (PsqlStore store = new PsqlStore(pr)) {
            SqlRuParse parser = store.parser;
            List<Post> savedPosts = parser.list(pr.getProperty("forParse"));
            savedPosts.forEach(store::save);

            long finish = System.currentTimeMillis();
            System.out.println(finish - start);
        }
    }

    /**
     * Method saves the post to database.
     * Checks if post already exist in DB, method just update 3 fields.
     * If post doesn't exist in DB, method requests details from parser and insert post into DB.
     * Each post will be added to sqlRuPosts list, which needed for checking deleted posts.
     *
     * @param post which have to save in DB.
     * @return post id from DB.
     */
    @Override
    public int save(Post post) {
        int id = -1;
        if (currentDBPosts.contains(post)) {
            try (PreparedStatement ps = cnn.prepareStatement("UPDATE post SET " +
                            "answers_count = ?, " +
                            "views_count = ?, " +
                            "last_message = ? " +
                            "WHERE link = ?",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, post.getAnswersCount());
                ps.setInt(2, post.getViewsCount());
                ps.setDate(3, new Date(post.getLastMessageDate().getTime()));
                ps.setString(4, post.getLink());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        id = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
            updatedPosts++;
        } else {
            Post detailedPost = parser.detail(post);
            try (PreparedStatement ps = cnn.prepareStatement("insert into post (name, text, " +
                            "link, created, author, answers_count, views_count, last_message) " +
                            "values (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, detailedPost.getName());
                ps.setString(2, detailedPost.getMessage());
                ps.setString(3, detailedPost.getLink());
                ps.setDate(4, new Date(detailedPost.getCreateDate().getTime()));
                ps.setString(5, detailedPost.getAuthor());
                ps.setInt(6, detailedPost.getAnswersCount());
                ps.setInt(7, detailedPost.getViewsCount());
                ps.setDate(8, new Date(detailedPost.getLastMessageDate().getTime()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        id = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
            addedPosts++;
        }
        sqlRuPosts.add(post);
        return id;
    }

    /**
     * Method returns all posts from DB.
     *
     * @return list of posts from DB.
     */
    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (Statement ps = cnn.createStatement();
             ResultSet rs = ps.executeQuery("select * from post")) {
            while (rs.next()) {
                list.add(new Post(
                        rs.getString("name"),
                        rs.getString("link"),
                        rs.getString("author"),
                        rs.getString("text"),
                        new java.util.Date(rs.getDate("created").getTime()),
                        rs.getInt("answers_count"),
                        rs.getInt("views_count"),
                        new java.util.Date(rs.getDate("last_message").getTime())));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    /**
     * Method returns post from DB by id.
     *
     * @param id of post.
     * @return post or null.
     */
    @Override
    public Post findById(String id) {
        Post post = null;
        try (PreparedStatement ps = cnn.prepareStatement("select * from post where id = ?")) {
            ps.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    post = new Post(
                            rs.getString("name"),
                            rs.getString("text"),
                            rs.getString("link"),
                            rs.getString("created"),
                            rs.getDate("author"),
                            rs.getInt("answers_count"),
                            rs.getInt("views_count"),
                            rs.getDate("last_message"));
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return post;
    }

    /**
     * Close method implementation.
     * Closes DB connection and prints log.
     */
    @Override
    public void close() {
        try {
            if (cnn != null) {
                cnn.close();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Show working log.
     */
    @Override
    public void log() {
        LOG.info(String.format("\nGrabbing complete.\n" +
                        "%d posts was added.\n" +
                        "%d posts was updated.\n" +
                        "%d posts was deleted.\n",
                addedPosts, updatedPosts, deletedPosts));
        addedPosts = 0;
        updatedPosts = 0;
        deletedPosts = 0;
    }

    /**
     * Method deletes all posts from DB, which has been deleted from sql.ru.
     */
    @Override
    public void delete() {
        currentDBPosts.removeAll(sqlRuPosts);
        deletedPosts = currentDBPosts.size();
        for (Post post : currentDBPosts) {
            try (PreparedStatement ps = cnn.prepareStatement("DELETE FROM post WHERE link = ?")) {
                ps.setString(1, post.getLink());
                ps.executeUpdate();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        currentDBPosts = getAll();
        sqlRuPosts.clear();
    }
}
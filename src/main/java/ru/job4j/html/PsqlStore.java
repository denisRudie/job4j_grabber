package ru.job4j.html;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class);
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver"));
            cnn = DriverManager.getConnection(cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password"));
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        Properties pr = new Properties();
        try (InputStream is = PsqlStore.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            pr.load(is);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        try (PsqlStore store = new PsqlStore(pr)) {
            SqlRuParse parser = new SqlRuParse();
            List<Post> savedPosts = parser.list("https://www.sql.ru/forum/job-offers/20");
            savedPosts.forEach(store::save);

            List<Post> loadedPosts = store.getAll();
            if (loadedPosts.containsAll(savedPosts)) {
                System.out.println("Success");
            }
        }
    }

    @Override
    public int save(Post post) {
        int id = -1;
        try (PreparedStatement ps = cnn.prepareStatement("insert into post (name, text, link, " +
                "created, author, answers_count, views_count, last_message) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getName());
            ps.setString(2, post.getMessage());
            ps.setString(3, post.getLink());
            ps.setDate(4, new Date(post.getCreateDate().getTime()));
            ps.setString(5, post.getAuthor());
            ps.setInt(6, post.getAnswersCount());
            ps.setInt(7, post.getViewsCount());
            ps.setDate(8, new Date(post.getLastMessageDate().getTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return id;
    }

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
}
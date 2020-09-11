package ru.job4j.html;

import java.util.Date;

public class Post {
    private String name;
    private String link;
    private String author;
    private String message;
    private Date createDate;
    private int answersCount;
    private int viewsCount;
    private Date lastMessageDate;

    public Post(String name,
                String link,
                String author,
                String message,
                Date createDate,
                int answersCount,
                int viewsCount,
                Date lastMessageDate) {
        this.name = name;
        this.link = link;
        this.author = author;
        this.message = message;
        this.createDate = createDate;
        this.answersCount = answersCount;
        this.viewsCount = viewsCount;
        this.lastMessageDate = lastMessageDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAnswersCount() {
        return answersCount;
    }

    public void setAnswersCount(int answersCount) {
        this.answersCount = answersCount;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}

package ru.ianedw.avitoparser.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Post {
    private String name;
    private int price;
    private String link;
    private LocalDateTime publicationTime;

    public Post() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public LocalDateTime getPublicationTime() {
        return publicationTime;
    }

    public void setPublicationTime(LocalDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return Objects.equals(link, post.link);
    }

    @Override
    public int hashCode() {
        return link != null ? link.hashCode() : 0;
    }
}

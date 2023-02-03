package ru.ianedw.avitoparser.models;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Target")
public class Target {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "link")
    private String link;

    public Target() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        if (id != target.id) return false;
        return Objects.equals(link, target.link);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (link != null ? link.hashCode() : 0);
        return result;
    }
}

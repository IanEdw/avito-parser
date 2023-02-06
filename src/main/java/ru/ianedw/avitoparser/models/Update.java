package ru.ianedw.avitoparser.models;

import java.util.Map;
import java.util.TreeSet;

public class Update {
    private Map<Integer, TreeSet<Post>> targetPosts;

    public Update() {
    }

    public Map<Integer, TreeSet<Post>> getTargetPosts() {
        return targetPosts;
    }

    public void setTargetPosts(Map<Integer, TreeSet<Post>> targetPosts) {
        this.targetPosts = targetPosts;
    }
}

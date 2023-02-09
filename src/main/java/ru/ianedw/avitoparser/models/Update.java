package ru.ianedw.avitoparser.models;

import java.util.Map;

public class Update {
    private Map<Integer, Map<String, Post>> targetPosts;

    public Update() {
    }

    public Map<Integer, Map<String, Post>> getTargetPosts() {
        return targetPosts;
    }

    public void setTargetPosts(Map<Integer, Map<String, Post>> targetPosts) {
        this.targetPosts = targetPosts;
    }
}

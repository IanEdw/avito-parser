package ru.ianedw.avitoparser.models;

import ru.ianedw.avitoparser.util.CustomLinkedHashMap;

import java.util.Map;

public class Update {
    private Map<Integer, CustomLinkedHashMap<String, Post>> targetPosts;

    public Update() {
    }

    public Map<Integer, CustomLinkedHashMap<String, Post>> getTargetPosts() {
        return targetPosts;
    }

    public void setTargetPosts(Map<Integer, CustomLinkedHashMap<String, Post>> targetPosts) {
        this.targetPosts = targetPosts;
    }
}

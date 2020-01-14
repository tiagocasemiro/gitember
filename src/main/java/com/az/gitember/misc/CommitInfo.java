package com.az.gitember.misc;

public class CommitInfo {

    private final String name;
    private final String sha;

    public CommitInfo(String name, String sha) {
        this.name = name;
        this.sha = sha;
    }

    public String getName() {
        return name;
    }

    public String getSha() {
        return sha;
    }
}

package com.az.gitember.misc;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmBranch /*extends Pair<String, String>*/ {

    public enum BranchType {
        LOCAL("local branch"),
        REMOTE("remote branch"),
        TAG("tag");

        String typeName;

        BranchType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    private boolean head;
    private String remoteName;

    private final BranchType branchType;
    private final String sha;
    private final String shortName;
    private final String fullName;

    public ScmBranch(String shortName, String fullName, BranchType branchType, String sha) {
        this.shortName = shortName;
        this.fullName = fullName;
        this.branchType = branchType;
        this.sha = sha;
    }


    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public BranchType getBranchType() {
        return branchType;
    }

    public String getSha() {
        return sha;
    }

    @Override
    public String toString() {
        return "ScmBranch{" +
                "short=" + shortName +
                "sha=" + sha +
                "head=" + head +
                ", branchType=" + branchType +
                ", remoteName='" + remoteName + '\'' +
                '}';
    }
}

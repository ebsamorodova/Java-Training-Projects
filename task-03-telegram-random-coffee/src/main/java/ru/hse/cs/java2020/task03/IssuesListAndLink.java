package ru.hse.cs.java2020.task03;

import java.io.Serializable;
import java.util.List;

public class IssuesListAndLink implements Serializable {
    private String nextPageLink;
    private List<String> issuesKeys;
    private int pageNumber;

    public IssuesListAndLink(String link, List<String> issues, int page) {
        nextPageLink = link;
        issuesKeys = issues;
        pageNumber = page;
    }

    public List<String> getIssuesKeys() {
        return issuesKeys;
    }

    public String getNextPageLink() {
        return nextPageLink;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}

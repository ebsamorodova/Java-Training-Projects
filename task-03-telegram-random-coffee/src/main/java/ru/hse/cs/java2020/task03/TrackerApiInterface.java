package ru.hse.cs.java2020.task03;

import java.util.List;

public interface TrackerApiInterface {
    IssueInfo getIssueInfo(String oauthToken, String orgId, String issueId);
    String createNewIssue(String oauthToken, String orgId,
                           String summary, String queue, String description, Boolean assignMe);
    List<String> findAssignedIssues(String oauthToken, String orgId);
    List<String> findNextAssignedIssues(String oauthToken, String orgId);
}

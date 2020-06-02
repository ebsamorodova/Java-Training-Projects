package ru.hse.cs.java2020.task03;

import java.util.List;

public interface TrackerApiInterface {
    IssueInfo getIssueInfo(String oauthToken, String orgId, String issueId) throws TrackerApiError;
    List<String> getIssueComments(String oauthToken, String orgId, String issueId) throws TrackerApiError;
    String createNewIssue(String oauthToken, String orgId, String summary, String description,
                          String queueKey, boolean assignMe) throws TrackerApiError;
    String getUserUid(String oauthToken, String orgId) throws TrackerApiError;
    void tryQueueKey(String oauthToken, String orgId, String queue) throws TrackerApiError;
    IssuesListAndLink findAssignedIssues(String oauthToken, String orgId) throws TrackerApiError;
    IssuesListAndLink findNextAssignedIssues(String oauthToken, String orgId, String curPageLink, int pageNumber) throws TrackerApiError;
}

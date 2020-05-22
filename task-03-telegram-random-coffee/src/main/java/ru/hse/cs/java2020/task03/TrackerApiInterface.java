package ru.hse.cs.java2020.task03;

import java.util.List;

public interface TrackerApiInterface {
    IssueInfo getIssueInfo(String oauthToken, String orgId, String issueId) throws TrackerApiError;
    String createNewIssue(String oauthToken, String orgId) throws TrackerApiError;
    void setCreateSummary(String summary);
    void setCreateQueue(String queue);
    void setCreateDescription(String description);
    void setCreateAssignMe(String assignMe);
    List<String> findAssignedIssues(String oauthToken, String orgId) throws TrackerApiError;
    List<String> findNextAssignedIssues(String oauthToken, String orgId) throws TrackerApiError;
}

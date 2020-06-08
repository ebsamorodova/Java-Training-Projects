package ru.hse.cs.java2020.task03;

public interface UserInfoStorageInterface {
    boolean containsUser(long id);
    String getUserToken(long id); // null if no such user
    String getUserOrgId(long id);
    void setUserInfo(long id, String token, String orgId);

    boolean containsSummary(long id);
    void setIssueSummary(long id, String summary);
    String getIssueSummary(long id);
    boolean containsDescription(long id);
    void setIssueDescription(long id, String description);
    String getIssueDescription(long id);
    boolean containsQueue(long id);
    void setIssueQueue(long id, String queueKey);
    String getIssueQueue(long id);
    boolean containsAssignMe(long id);
    void setAssignMe(long id, boolean assignMe);
    boolean getIssueAssignMe(long id);
    void clearIssueInfo(long id);

    boolean containsIssuesInfo(long id);
    void setIssuesInfo(long id, IssuesListAndLink issueInfo);
    IssuesListAndLink getIssuesInfo(long id);

    void close();
}

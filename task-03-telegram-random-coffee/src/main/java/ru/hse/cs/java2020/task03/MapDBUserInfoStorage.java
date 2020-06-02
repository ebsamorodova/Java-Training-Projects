package ru.hse.cs.java2020.task03;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.concurrent.ConcurrentMap;

public class MapDBUserInfoStorage implements UserInfoStorageInterface, AutoCloseable {
    private ConcurrentMap<Long, String> tokenMap;
    private ConcurrentMap<Long, String> orgIdMap;
    private ConcurrentMap<Long, String> issueSummaryMap;
    private ConcurrentMap<Long, String> issueDescriptionMap;
    private ConcurrentMap<Long, String> issueQueueMap;
    private ConcurrentMap<Long, Boolean> issueAssignMeMap;
    private ConcurrentMap<Long, IssuesListAndLink> issuesResponseInfo;
    private DB db;

    public MapDBUserInfoStorage(String pathToFile) {
        db = DBMaker.fileDB(pathToFile + "/file.db").make();
        tokenMap = db.hashMap("oauthTokens", Serializer.LONG, Serializer.STRING).createOrOpen();
        orgIdMap = db.hashMap("xOrgId", Serializer.LONG, Serializer.STRING).createOrOpen();
        issueSummaryMap = db.hashMap("summary", Serializer.LONG, Serializer.STRING).createOrOpen();
        issueDescriptionMap = db.hashMap("description", Serializer.LONG, Serializer.STRING).createOrOpen();
        issueQueueMap = db.hashMap("queue", Serializer.LONG, Serializer.STRING).createOrOpen();
        issueAssignMeMap = db.hashMap("assignMe", Serializer.LONG, Serializer.BOOLEAN).createOrOpen();
        //noinspection unchecked
        issuesResponseInfo = db.hashMap("myIssues")
                .keySerializer(Serializer.LONG)
                .valueSerializer((Serializer<IssuesListAndLink>) Serializer.JAVA)
                .createOrOpen();
    }

    @Override
    public void setUserInfo(long id, String token, String orgId) {
        tokenMap.put(id, token);
        orgIdMap.put(id, orgId);
        db.commit();
    }

    @Override
    public void setIssueSummary(long id, String summary) {
        issueSummaryMap.put(id, summary);
        db.commit();
    }

    @Override
    public void setIssueDescription(long id, String description) {
        issueDescriptionMap.put(id, description);
        db.commit();
    }

    @Override
    public void setIssueQueue(long id, String queueKey) {
        issueQueueMap.put(id, queueKey);
        db.commit();
    }

    @Override
    public void setAssignMe(long id, boolean assignMe) {
        issueAssignMeMap.put(id, assignMe);
        db.commit();
    }

    @Override
    public void clearIssueInfo(long id) {
        issueSummaryMap.remove(id);
        issueDescriptionMap.remove(id);
        issueQueueMap.remove(id);
        issueAssignMeMap.remove(id);
        db.commit();
    }

    @Override
    public boolean containsSummary(long id) {
        return issueSummaryMap.containsKey(id);
    }

    @Override
    public String getIssueSummary(long id) {
        return issueSummaryMap.get(id);
    }

    @Override
    public boolean containsDescription(long id) {
        return issueDescriptionMap.containsKey(id);
    }

    @Override
    public String getIssueDescription(long id) {
        return issueDescriptionMap.get(id);
    }

    @Override
    public boolean containsQueue(long id) {
        return issueQueueMap.containsKey(id);
    }

    @Override
    public String getIssueQueue(long id) {
        return issueQueueMap.get(id);
    }

    @Override
    public boolean containsAssignMe(long id) {
        return issueAssignMeMap.containsKey(id);
    }

    @Override
    public boolean getIssueAssignMe(long id) {
        return issueAssignMeMap.get(id);
    }

    @Override
    public boolean containsUser(long id) {
        return tokenMap.containsKey(id);
    }

    @Override
    public String getUserToken(long id) {
        return tokenMap.get(id);
    }

    @Override
    public String getUserOrgId(long id) {
        return orgIdMap.get(id);
    }

    @Override
    public boolean containsIssuesInfo(long id) {
        return issuesResponseInfo.containsKey(id);
    }

    @Override
    public IssuesListAndLink getIssuesInfo(long id) {
        return issuesResponseInfo.get(id);
    }

    @Override
    public void setIssuesInfo(long id, IssuesListAndLink issueInfo) {
        issuesResponseInfo.put(id, issueInfo);
    }

    @Override
    public void close() {
        db.commit();
        db.close();
    }
}

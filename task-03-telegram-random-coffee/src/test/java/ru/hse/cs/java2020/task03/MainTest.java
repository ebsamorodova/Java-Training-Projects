package ru.hse.cs.java2020.task03;

import org.junit.Test;

import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class MainTest {
    // здесь и далее надо будет заменить значения
    private final String oauthToken = "AgAAAABAV8HKAAZY24LbejGY0khIs9dhnIchMQk";
    private final String orgId = "3972196";
    private final String realId = "1079493066";
    private final String realToken = "1113952534:AAFF2g-hWtkA9zaAJypM-wTbzOkZZiFcqAc";

    // тесты про Яндекс.Трекер API
    @Test
    public void testFindIssue() {
        TrackerApiInterface trackerClient = new TrackerApiClient();

        var issueId = "TESTHSEJAVA-1";
        try {
            var issueInfo = trackerClient.getIssueInfo(oauthToken, orgId, issueId);
            IssueInfo testIssue = new IssueInfo("TESTHSEJAVA-1", "test bot",
                    "aaaaaaaaaaaaa", "Екатерина Самородова",
                    "Екатерина Самородова", new ArrayList<>(), new ArrayList<>());
            assertEquals(issueInfo.toString(), testIssue.toString());
        } catch (TrackerApiError e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void testGetUserUid() {
        TrackerApiInterface trackerClient = new TrackerApiClient();
        try {
            var userUid = trackerClient.getUserUid(oauthToken, orgId);
            assertEquals(realId, userUid);
        } catch (TrackerApiError e) {
            fail();
        }
    }

    @Test
    public void testWrongQueueCreating() {
        TrackerApiInterface trackerClient = new TrackerApiClient();
        trackerClient.setCreateSummary("summary");
        trackerClient.setCreateDescription("description");
        trackerClient.setCreateQueue("TESTHSEPYTHON"); // нет такой очереди
        trackerClient.setCreateAssignMe("true");
        try {
            trackerClient.createNewIssue(oauthToken, orgId);
            fail();
        } catch (TrackerApiError e) {
            assertEquals(e.getMessage(), "[\"Очередь не существует.\"]");
        }
    }

    // тесты про telegram-bots API
    @Test
    public void testBotInfo() {
        String path;
        try {
            path = Files.createTempDirectory(null).toString();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        TrackerApiInterface trackerClient = new TrackerApiClient();
        UserInfoStorageInterface storage = new MapDBUserInfoStorage(path);
        MyTrackerBot myNewBot = new MyTrackerBot(trackerClient, storage);
        assertEquals(myNewBot.getBotUsername(), "my tracker bot");
        assertEquals(myNewBot.getBotToken(), realToken);
        myNewBot.close();
    }

    // а дальше непонятно, какие тесты писать к боту
}

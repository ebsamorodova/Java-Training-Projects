package ru.hse.cs.java2020.task03;

import org.junit.Test;

import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.*;

public class MainTest {
    // здесь и далее надо будет заменить значения
    private final String oauthToken = "token";
    private final String orgId = "id";
    private final String userId = "userId";
    private final String botToken = "botToken";

    // тесты про Яндекс.Трекер API
    @Test
    public void testFindIssue() {
        TrackerApiInterface trackerClient = new TrackerApiClient();

        var issueId = "TESTHSEJAVA-1";
        try {
            var issueInfo = trackerClient.getIssueInfo(oauthToken, orgId, issueId);
            IssueInfo testIssue = new IssueInfo("TESTHSEJAVA-1", "test bot",
                    "aaaaaaaaaaaaa", "Екатерина Самородова",
                    "Екатерина Самородова", Collections.singletonList("Екатерина Самородова"));
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
            assertEquals(userId, userUid);
        } catch (TrackerApiError e) {
            fail();
        }
    }

    @Test
    public void testWrongQueueCreating() {
        TrackerApiInterface trackerClient = new TrackerApiClient();
        try {
            trackerClient.tryQueueKey(oauthToken, orgId, "TESTHSEPYTHON"); // нет такой очереди
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
        assertEquals(myNewBot.getBotToken(), botToken);
        myNewBot.close();
    }

    // тесты про бд
    @Test
    public void testDB() {
        String path;
        try {
            path = Files.createTempDirectory(null).toString();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        UserInfoStorageInterface storage = new MapDBUserInfoStorage(path);
        storage.setUserInfo(1, "aaa", "bbb");
        assertTrue(storage.containsUser(1));
        assertFalse(storage.containsUser(2));
        assertEquals(storage.getUserOrgId(1), "bbb");
        assertEquals(storage.getUserToken(1), "aaa");
    }
}

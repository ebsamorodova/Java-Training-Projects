package ru.hse.cs.java2020.task03;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrackerApiClient implements TrackerApiInterface {
    private final int httpCodeOk = 200;
    private final int httpCodeCreated = 201;
    private final int lastLinkLength = 13;
    private final String prefixUri = "https://api.tracker.yandex.net/v2/";
//    private HttpResponse<String> lastResponse = null;
//    private int pageCnt = 0;
//    private final int pageSize = 5;
//    private HashMap<Integer, IssueInfo> myIssuesMap;

    private HttpResponse<String> getResponse(String uri, String oauthToken, String orgId) throws TrackerApiError {
        System.err.println("GET request uri: " + uri);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgId)
                .build();
        return getResponseAns(client, request, httpCodeOk);
    }

    private HttpResponse<String> postResponse(String uri, String oauthToken, String orgId, String requestBody, int ok)  throws TrackerApiError {
        System.err.println("POST request uri: " + uri + "\nPOST request body: " + requestBody);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestCreatingIssue = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgId)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return getResponseAns(client, requestCreatingIssue, ok);
    }

    private HttpResponse<String> getResponseAns(HttpClient client, HttpRequest request, int ok) throws TrackerApiError {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != ok) {
                System.err.println("response error: " + response.body());
                JSONObject obj = new JSONObject(response.body());
                String errorMessages = obj.getJSONArray("errorMessages").toString();
                throw new TrackerApiError(errorMessages);
            }
            return response;
        } catch (IOException | InterruptedException e) {
            throw new TrackerApiError(e.getMessage());
        }
    }

    @Override
    public String getUserUid(String oauthToken, String orgId) throws TrackerApiError {
        var responseMe = getResponse(prefixUri + "myself",
                oauthToken, orgId);
        JSONObject obj = new JSONObject(responseMe.body());
        return ((Long) obj.getLong("uid")).toString();
    }

    @Override
    public IssueInfo getIssueInfo(String oauthToken, String orgId, String issueId) throws TrackerApiError {
        var response = getResponse(prefixUri + "issues/" + issueId,
                oauthToken, orgId);

        // two OK responses!
        JSONObject obj = new JSONObject(response.body());
        var issueDescription = obj.getString("description");
        var issueName = obj.getString("summary");
        var issueAuthor = obj.getJSONObject("createdBy").getString("display");
        var issueExecutor = obj.getJSONObject("assignee").getString("display");

        JSONArray issueFollowersList = new JSONArray();
        try {
            issueFollowersList = obj.getJSONArray("followers");
        } catch (JSONException e) {
            System.err.println(e.getMessage());
        }
        var issueFollowers = new ArrayList<String>();
        for (int i = 0; i < issueFollowersList.length(); i++) {
            issueFollowers.add(issueFollowersList.getJSONObject(i).getString("display"));
        }
        return new IssueInfo(issueId, issueName, issueDescription, issueAuthor, issueExecutor, issueFollowers);
    }

    @Override
    public List<String> getIssueComments(String oauthToken, String orgId, String issueId) throws TrackerApiError {
        var responseComments = getResponse(prefixUri + "issues/" + issueId + "/comments?",
                oauthToken, orgId);
        JSONArray objComments = new JSONArray(responseComments.body());
        var issueComments = new ArrayList<String>();
        for (int i = 0; i < objComments.length(); i++) {
            issueComments.add(objComments.getJSONObject(i).getString("text"));
        }
        return issueComments;
    }

    @Override
    public void tryQueueKey(String oauthToken, String orgId, String queue) throws TrackerApiError {
        getResponse(prefixUri + "queues/" + queue, oauthToken, orgId);
    }

    @Override
    public String createNewIssue(String oauthToken, String orgId, String summary, String description,
                                 String queueKey, boolean assignMe) throws TrackerApiError {
        var objectMapper = new ObjectMapper();
        var values = new HashMap<String, String>() {{
            put("summary", summary);
            put("description", description);
            put("queue", queueKey);
        }};

        if (assignMe) {
            var creatorId = getUserUid(oauthToken, orgId);
            values.put("assignee",  creatorId);
        }

        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assert (requestBody != null);
        var response = postResponse(prefixUri + "issues/",
                oauthToken, orgId, requestBody, httpCodeCreated);
        JSONObject obj = new JSONObject(response.body());
        return "https://tracker.yandex.ru/" + obj.getString("key");
    }

    @Override
    public IssuesListAndLink findAssignedIssues(String oauthToken, String orgId) throws TrackerApiError {
        var userId = getUserUid(oauthToken, orgId);
        var requestBody = "{\"filter\": { \"assignee\" : \"" + userId + "\"}}";
        var uri = prefixUri + "issues/_search?order=%2BupdatedAt&scrollType=sorted&perScroll=5";
        var response = postResponse(uri, oauthToken, orgId, requestBody, httpCodeOk);
        String nextPageLink = null;
        if (response.headers().allValues("link").size() > 1) { // нет следующих
            var headerLink = response.headers().allValues("link").get(1); // ref="next"
            nextPageLink = headerLink.substring(1, headerLink.length() - lastLinkLength);
        }
        return new IssuesListAndLink(nextPageLink, getIssuesList(response), 0);
    }

    @Override
    public IssuesListAndLink findNextAssignedIssues(String oauthToken, String orgId, String curPageLink, int page) throws TrackerApiError {
        var userId = getUserUid(oauthToken, orgId);
        var requestBody = "{\"filter\": { \"assignee\" : \"" + userId + "\"}}";
        var response = postResponse(curPageLink, oauthToken, orgId, requestBody, httpCodeOk);
        String nextPageLink = null;
        if (response.headers().allValues("link").size() > 1) { // нет следующих
            var headerLink = response.headers().allValues("link").get(1); // ref="next"
            nextPageLink = headerLink.substring(1, headerLink.length() - lastLinkLength);
        }
        return new IssuesListAndLink(nextPageLink, getIssuesList(response), page + 1);
    }

    @NotNull
    private List<String> getIssuesList(HttpResponse<String> response) throws TrackerApiError {
        JSONArray issues = new JSONArray(response.body());

        var issuesList = new ArrayList<String>();
        for (int i = 0; i < issues.length(); i++) {
            var issueKey = issues.getJSONObject(i).getString("key");
            issuesList.add(issueKey);
        }
        return issuesList;
    }
}

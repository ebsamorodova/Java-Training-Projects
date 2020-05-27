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
    private HttpResponse<String> lastResponse = null;
    private String createQueueKey = null;
    private String createSummary = null;
    private String createDescription = null;
    private String createAssignMe = null;

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
        var responseMe = getResponse("https://api.tracker.yandex.net/v2/myself",
                oauthToken, orgId);
        JSONObject obj = new JSONObject(responseMe.body());
        return ((Long) obj.getLong("uid")).toString();
    }

    @Override
    public IssueInfo getIssueInfo(String oauthToken, String orgId, String issueId) throws TrackerApiError {
        var response = getResponse("https://api.tracker.yandex.net/v2/issues/" + issueId,
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
        var responseComments = getResponse("https://api.tracker.yandex.net/v2/issues/" + issueId + "/comments?",
                oauthToken, orgId);
        JSONArray objComments = new JSONArray(responseComments.body());
        var issueComments = new ArrayList<String>();
        for (int i = 0; i < objComments.length(); i++) {
            issueComments.add(objComments.getJSONObject(i).getString("text"));
        }
        return issueComments;
    }

    @Override
    public void setCreateQueue(String oauthToken, String orgId, String queue) throws TrackerApiError {
        getResponse("https://api.tracker.yandex.net/v2/queues/" + queue, oauthToken, orgId);
        createQueueKey = queue;
    }

    @Override
    public void setCreateSummary(String summary) {
        createSummary = summary;
    }

    @Override
    public void setCreateDescription(String description) {
        createDescription = description;
    }

    @Override
    public void setCreateAssignMe(String assignMe) {
        createAssignMe = assignMe;
    }

    @Override
    public String createNewIssue(String oauthToken, String orgId) throws TrackerApiError {
        var objectMapper = new ObjectMapper();
        if (createDescription == null || createSummary == null || createQueueKey == null || createAssignMe == null) {
            throw new TrackerApiError("not all parameters are set");
        }
        var values = new HashMap<String, String>() {{
            put("summary", createSummary);
            put("description", createDescription);
            put("queue", createQueueKey);
        }};

        if (createAssignMe.equals("true")) {
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
        var response = postResponse("https://api.tracker.yandex.net/v2/issues/",
                oauthToken, orgId, requestBody, httpCodeCreated);

        createSummary = null;
        createDescription = null;
        createQueueKey = null;
        createAssignMe = null;
        JSONObject obj = new JSONObject(response.body());
        return "https://tracker.yandex.ru/" + obj.getString("key");
    }

    @Override
    public List<String> findAssignedIssues(String oauthToken, String orgId) throws TrackerApiError {
        var userId = getUserUid(oauthToken, orgId);
        var requestBody = "{\"filter\": { \"assignee\" : \"" + userId + "\"}}";
        var uri = "https://api.tracker.yandex.net/v2/issues/_search?"
                + "order=%2BupdatedAt&scrollType=sorted&perScroll=5";
        return getIssuesList(oauthToken, orgId, uri, requestBody);
    }

    @Override
    public List<String> findNextAssignedIssues(String oauthToken, String orgId) throws TrackerApiError {
        var userId = getUserUid(oauthToken, orgId);
        if (lastResponse == null) {
            throw new TrackerApiError("Calling /next_issues before /my_issues");
        }
        if (lastResponse.headers().allValues("link").size() == 1) {
            return new ArrayList<>();
        }
        var headerLink = lastResponse.headers().allValues("link").get(1); // ref="next"
        var nextPageLink = headerLink.substring(1, headerLink.length() - lastLinkLength);
        var requestBody = "{\"filter\": { \"assignee\" : \"" + userId + "\"}}";
        return getIssuesList(oauthToken, orgId, nextPageLink, requestBody);
    }

    @NotNull
    private List<String> getIssuesList(String oauthToken, String orgId, String nextPageLink, String requestBody) throws TrackerApiError {
        var response = postResponse(nextPageLink, oauthToken, orgId, requestBody, httpCodeOk);
        lastResponse = response;
        JSONArray issues = new JSONArray(response.body());
        ArrayList<String> myIssues = new ArrayList<>();
        for (int i = 0; i < issues.length(); i++) {
            myIssues.add(issues.getJSONObject(i).getString("key"));
        }
        return myIssues;
    }
}

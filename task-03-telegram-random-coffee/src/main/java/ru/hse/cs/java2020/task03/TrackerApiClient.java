package ru.hse.cs.java2020.task03;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrackerApiClient implements TrackerApiInterface {
    private final int HTTP_CODE_OK = 200;
    private final int HTTP_CODE_CREATED = 201;
    private HttpResponse<String> lastResponse = null;

    private HttpResponse<String> getResponse(String uri, String oauthToken, String orgId) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgId)
                .build();
        return getResponse(client, request, HTTP_CODE_OK);
    }

    private HttpResponse<String> postResponse(String uri, String oauthToken, String orgId, String requestBody, int okCode) {
        System.out.println("request uri: " + uri + "\nrequest body: " + requestBody);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestCreatingIssue = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .headers("Authorization", "OAuth " + oauthToken, "X-Org-Id", orgId)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return getResponse(client, requestCreatingIssue, okCode);
    }

    private HttpResponse<String> getResponse(HttpClient client, HttpRequest request, int httpCodeOk) {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != httpCodeOk) {
                System.out.println("response ans: " + response.body());
                return null;
            }
            return response;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IssueInfo getIssueInfo(String oauthToken, String orgId, String issueId) {
        System.out.println("searching " + oauthToken + ", " + orgId + ", " + issueId);
        var response = getResponse("https://api.tracker.yandex.net/v2/issues/" + issueId,
                oauthToken, orgId);
        if (response == null) {
            return null;
        }
        System.out.println("first response: " + response.body());
        var responseComments = getResponse("https://api.tracker.yandex.net/v2/issues/" + issueId + "/comments?",
                oauthToken, orgId);
        if (responseComments == null) {
            return null;
        }
        System.out.println("second response: " + responseComments.body());

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

        JSONArray objComments = new JSONArray(responseComments.body());
        var issueComments = new ArrayList<String>();
        for (int i = 0; i < objComments.length(); i++) {
            issueComments.add(objComments.getJSONObject(i).getString("text"));
        }

        var issue = new IssueInfo(issueId, issueName, issueDescription,
                issueAuthor, issueExecutor, issueFollowers, issueComments);
        return issue;
    }

    @Override
    public String createNewIssue(String oauthToken, String orgId,
                                  String summary, String queue, String description, Boolean assignMe) {
        var objectMapper = new ObjectMapper();

        var values = new HashMap<String, String>() {{
            put("summary", summary);
            put("description", description);
            put("queue", queue);
        }};

        if (assignMe) {
            var responseMe = getResponse("https://api.tracker.yandex.net/v2/myself",
                    oauthToken, orgId);
            if (responseMe == null) {
                return null;
            }
            JSONObject obj = new JSONObject(responseMe.body());
            var creatorId = ((Long) obj.getLong("uid")).toString();
            values.put("assignee",  creatorId);
        }

        String requestBody = null;
        try {
            requestBody = objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assert(requestBody != null);
        var response = postResponse("https://api.tracker.yandex.net/v2/issues/",
                oauthToken, orgId, requestBody, HTTP_CODE_CREATED);
        if (response == null) {
            return null;
        }
        JSONObject obj = new JSONObject(response.body());
        return "https://tracker.yandex.ru/" + obj.getString("key");
    }

    @Override
    public List<String> findAssignedIssues(String oauthToken, String orgId) {
        var responseMe = getResponse("https://api.tracker.yandex.net/v2/myself",
                oauthToken, orgId);
        if (responseMe == null) {
            return null;
        }
        JSONObject obj = new JSONObject(responseMe.body());
        var userId = ((Long) obj.getLong("uid")).toString();
        var requestBody = "{\"filter\": { \"assignee\" : \"" + userId + "\"}}";
        var uri = "https://api.tracker.yandex.net/v2/issues/_search?order=%2BupdatedAt&scrollType=sorted&perScroll=5";
        var response = postResponse(uri, oauthToken, orgId, requestBody, HTTP_CODE_OK);
        if (response == null) {
            return null;
        }

        lastResponse = response;
        JSONArray issues = new JSONArray(response.body());
        ArrayList<String> myIssues = new ArrayList<>();
        for (int i = 0; i < issues.length(); i++) {
            myIssues.add(issues.getJSONObject(i).getString("key"));
        }
        return myIssues;
    }

    @Override
    public List<String> findNextAssignedIssues(String oauthToken, String orgId) {
        var responseMe = getResponse("https://api.tracker.yandex.net/v2/myself",
                oauthToken, orgId);
        if (responseMe == null) {
            return null;
        }
        JSONObject obj = new JSONObject(responseMe.body());
        var userId = ((Long) obj.getLong("uid")).toString();
        var headerLink = lastResponse.headers().allValues("link").get(1); // ref="next"
        var nextPageLink = headerLink.substring(1, headerLink.length()-13);
        var requestBody = "{\"filter\": { \"assignee\" : \"" + userId + "\"}}";
        var response = postResponse(nextPageLink, oauthToken, orgId, requestBody, HTTP_CODE_OK);
        if (response == null) {
            return null;
        }
        lastResponse = response;
        JSONArray issues = new JSONArray(response.body());
        ArrayList<String> myIssues = new ArrayList<>();
        for (int i = 0; i < issues.length(); i++) {
            myIssues.add(issues.getJSONObject(i).getString("key"));
        }
        return myIssues;
    }

}

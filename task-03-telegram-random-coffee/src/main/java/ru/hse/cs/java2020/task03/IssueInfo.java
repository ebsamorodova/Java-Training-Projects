package ru.hse.cs.java2020.task03;

import java.util.List;

public class IssueInfo {
    private String id;
    private String name; // название
    private String description; // описание
    private String author; // автор
    private String executor; // испольнитель
    private List<String> followers; // наблюдатели
    private List<String> comments; // комментарии

    public IssueInfo(String issueId, String issueName, String issueDescription,
                     String issueAuthor, String issueExecutor,
                     List<String> issueFollowers, List<String> issueComments) {
        this.id = issueId;
        this.name = issueName;
        this.description = issueDescription;
        this.author = issueAuthor;
        this.executor = issueExecutor;
        this.followers = issueFollowers;
        this.comments = issueComments;
    }

    @Override
    public String toString() {
        return "Информация по задаче:" + '\n'
                + "id = " + id
                + ", \nназвание = " + name
                + ", \nописание = " + description
                + ", \nавтор = " + author
                + ", \nисполнитель = " + executor
                + ", \nнаблюдатели = " + followers
                + ", \nкомментарии =" + comments;
    }
}

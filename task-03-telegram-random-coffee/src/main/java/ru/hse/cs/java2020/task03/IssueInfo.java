package ru.hse.cs.java2020.task03;

import java.util.List;

public class IssueInfo {
    String id;
    String name; // название
    String description; // описание
    String author; // автор
    String executor; // испольнитель
    List<String> followers; // наблюдатели
    List<String> comments; // комментарии

    public IssueInfo(String id, String name, String description, String author, String executor,
                     List<String> issueFollowers, List<String> issueComments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.executor = executor;
        this.followers = issueFollowers;
        this.comments = issueComments;
    }

    @Override
    public String toString() {
        return "Информация по задаче:" + '\n' +
                "id = " + id +
                ", \nназвание = " + name +
                ", \nописание = " + description +
                ", \nавтор = " + author +
                ", \nисполнитель = " + executor +
                ", \nнаблюдатели = " + followers +
                ", \nкомментарии =" + comments;
    }
}

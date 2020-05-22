package ru.hse.cs.java2020.task02;

public class Elem {
    private String str;
    private long id;

    Elem(long myId, String myStr) {
        str = myStr;
        id = myId;
    }

    String getStr() {
        return str;
    }

    long getId() {
        return id;
    }
}

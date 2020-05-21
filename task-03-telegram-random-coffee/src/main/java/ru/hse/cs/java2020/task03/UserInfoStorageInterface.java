package ru.hse.cs.java2020.task03;

public interface UserInfoStorageInterface {
    boolean containsUser(long id);
    String getUserToken(long id); // null if no such user
    void setUserToken(long id, String token);
    void close();
}

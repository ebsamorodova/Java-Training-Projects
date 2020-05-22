package ru.hse.cs.java2020.task03;

public interface UserInfoStorageInterface {
    boolean containsUser(long id);
    String getUserToken(long id); // null if no such user
    String getUserOrgId(long id);
    void setUserInfo(long id, String token, String orgId);
    void close();
}

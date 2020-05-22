package ru.hse.cs.java2020.task03;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.concurrent.ConcurrentMap;

public class MapDBUserInfoStorage implements UserInfoStorageInterface, AutoCloseable {
    private ConcurrentMap<Long, String> tokenMap;
    private ConcurrentMap<Long, String> orgIdMap;
    private DB db;

    public MapDBUserInfoStorage(String pathToFile) {
        db = DBMaker.fileDB(pathToFile + "/file.db").make();
        tokenMap = db.hashMap("oauthTokens", Serializer.LONG, Serializer.STRING).createOrOpen();
        orgIdMap = db.hashMap("xOrgId", Serializer.LONG, Serializer.STRING).createOrOpen();
    }

    @Override
    public void setUserInfo(long id, String token, String orgId) {
        tokenMap.put(id, token);
        orgIdMap.put(id, orgId);
    }

    @Override
    public boolean containsUser(long id) {
        return tokenMap.containsKey(id);
    }

    @Override
    public String getUserToken(long id) {
        return tokenMap.get(id);
    }

    @Override
    public String getUserOrgId(long id) {
        return orgIdMap.get(id);
    }

    @Override
    public void close() {
        db.commit();
        db.close();
    }
}

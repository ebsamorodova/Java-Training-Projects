package ru.hse.cs.java2020.task03;

import org.mapdb.*;

import java.util.concurrent.ConcurrentMap;

public class MapDBUserInfoStorage implements UserInfoStorageInterface, AutoCloseable {
    private ConcurrentMap<Long, String> myMap;
    private DB db;

    public MapDBUserInfoStorage(String pathToFile) {
        db = DBMaker.fileDB(pathToFile + "/file.db").make();
        myMap = db.hashMap("users", Serializer.LONG, Serializer.STRING).createOrOpen();
    }

    @Override
    public void setUserToken(long id, String token) {
        myMap.put(id, token);
    }

    @Override
    public boolean containsUser(long id) {
        return myMap.containsKey(id);
    }

    @Override
    public String getUserToken(long id) {
        return myMap.get(id);
    }

    @Override
    public void close() {
        db.commit();
        db.close();
    }
}

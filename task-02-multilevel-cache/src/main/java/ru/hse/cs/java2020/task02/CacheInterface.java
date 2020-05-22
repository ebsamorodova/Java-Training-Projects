package ru.hse.cs.java2020.task02;

public interface CacheInterface {
    void put(Long id, String str);
    String get(Long id);
}

package ru.hse.cs.java2020.task02;


import java.nio.file.Files;

public class Main {
    public static void main(String[] args) {
        String path;
        try {
            path = Files.createTempDirectory(null).toString();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        CacheImpl cache = new CacheImpl(18, 100, path, EvictionPolicy.LRU);
        cache.put(179L, "b");
        cache.put(178L, "d");
        cache.put(178L, "dasdad");
        System.out.println(cache.get(179L)); // b
        System.out.println(cache.get(178L)); // dasdad
    }
}

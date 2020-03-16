package ru.hse.cs.java2020.task02;

public class Main {
    public static void main(String[] args) {
        MyCache cache = new MyCache(1024, 0, Eviction.LRU);
        cache.put(1L, "aaa");
        cache.put(1L, "ccc");
        String ans = cache.get(1L);
        System.out.println("get string: " + ans);
    }
}

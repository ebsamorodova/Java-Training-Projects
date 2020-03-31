package ru.hse.cs.java2020.task02;

public class Main {
    public static void main(String[] args) {
        CacheImpl cache = new CacheImpl(18, 100, "/home/kate/hse/Java", EvictionPolicy.LRU);
        cache.put(179L, "b");
        cache.put(178L, "d");
        cache.put(178L, "dasdad");
        System.out.println(cache.get(179L)); // b
        System.out.println(cache.get(178L)); // dasdad
    }
}

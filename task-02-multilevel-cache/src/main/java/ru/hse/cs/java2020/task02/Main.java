package ru.hse.cs.java2020.task02;

public class Main {
    public static void main(String[] args) {
        MyCache cache = new MyCache(19, 100, "/home/kate/hse", EvictionPolicy.LRU);
        cache.put(179L, "b");
        cache.put(178L, "d");
        System.out.println(cache.get(179L)); // b
    }
}

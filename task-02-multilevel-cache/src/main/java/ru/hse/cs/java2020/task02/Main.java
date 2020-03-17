package ru.hse.cs.java2020.task02;

public class Main {
    public static void main(String[] args) {
        MyCache cache = new MyCache(45, 0, Eviction.LFU);
        cache.put(1L, "a");
        cache.put(1L, "b");
        cache.put(1L, "c");
        // память: 18

//        cache.printList();

        cache.put(2L, "d");
        cache.put(2L, "e");
        // память: 18, всего 36

        cache.printList();
/*
        System.out.println("занято памяти " + cache.getCurSize() + " из 45"); // 36/45, ok

        cache.put(3L, "aaa");
        System.out.println("а теперь памяти " + cache.getCurSize() + " из 45"); // 40/45, ok
        // нужно ещё 22
        // должен выкинуть один элемент id = 2 и оставить id = 1,

        System.out.println(cache.get(2L)); // e
*/
    }
}

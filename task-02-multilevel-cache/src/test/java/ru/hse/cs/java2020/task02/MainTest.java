package ru.hse.cs.java2020.task02;

import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    public void testTrueIsTrue() {
        assertTrue(true);
    }

    @Test
    public void getStringAfterUpdate() {
        MyCache cache = new MyCache(1024, 0, Eviction.LRU);
        cache.put(1L, "aaa");
        cache.put(1L, "ccc");
        String ans = cache.get(1L);
        assert(ans.equals("ccc"));
    }

}

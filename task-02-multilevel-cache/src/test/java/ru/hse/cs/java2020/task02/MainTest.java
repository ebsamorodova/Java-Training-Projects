package ru.hse.cs.java2020.task02;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    public void testTrueIsTrue() {
        assertTrue(true);
    }

    @Test
    public void testPutGet() {
        MyCache cache = new MyCache(1024, 0, Eviction.LRU);
        cache.put(179L, "bbb");
        Assert.assertEquals(cache.get(179L), "bbb");

    }

    @Test
    public void getStringAfterUpdate() {
        MyCache cache = new MyCache(1024, 0, Eviction.LRU);
        cache.put(1L, "aaa");
        cache.put(1L, "ccc");
        Assert.assertEquals(cache.get(1L), "ccc");
    }

}

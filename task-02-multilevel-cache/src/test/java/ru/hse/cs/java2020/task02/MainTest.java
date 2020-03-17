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
        cache.put(1L, "bbb");
        cache.put(1L, "ccc");
        cache.put(1L, "ddd");

        Assert.assertEquals(cache.get(1L), "ddd");
    }

    @Test
    public void getAfterRemove() {
        MyCache cache = new MyCache(33, 0, Eviction.LRU);
        cache.put(1L, "a");
        cache.put(1L, "b");
        cache.put(1L, "c");
        Assert.assertEquals(cache.get(1L), "c");

        cache.put(2L, "d");
        cache.put(1L, "aaaaaaaa");
        assertNull(cache.get(2L));
    }

    @Test
    public void testLFU() {
        MyCache cache = new MyCache(45, 0, Eviction.LFU);
        cache.put(1L, "a");
        cache.put(1L, "b");
        cache.put(1L, "c");
        cache.put(2L, "d");
        cache.put(2L, "e");

        cache.put(3L, "aaa");
        // должен выкинуть один элемент id = 2 и оставить id = 1

        Assert.assertEquals(cache.get(1L), "c"); // fail
        assertNull(cache.get(2L));
        Assert.assertEquals(cache.get(3L), "aaa");
    }

    @Test
    public void creatingList() {
        MyCache.Elem a = new MyCache.Elem(1L, "a");
        MyCache.List.Node<MyCache.Elem> a_n = new MyCache.List.Node<>(a);
        MyCache.Elem b = new MyCache.Elem(2L, "b");
        MyCache.List.Node<MyCache.Elem> b_n = new MyCache.List.Node<>(b);
        MyCache.Elem c = new MyCache.Elem(3L, "c");
        MyCache.List.Node<MyCache.Elem> c_n = new MyCache.List.Node<>(c);

        MyCache.List<MyCache.Elem> list = new MyCache.List<>();
        list.pushFront(a_n);
        list.pushFront(b_n);
        list.pushFront(c_n);

        MyCache.List.Node<MyCache.Elem> cur = list.foot;
        int cnt = 0;
        while (cur != null) {
            cur = cur.right;
            cnt++;
        }
        assert(cnt == 3);
    }

}

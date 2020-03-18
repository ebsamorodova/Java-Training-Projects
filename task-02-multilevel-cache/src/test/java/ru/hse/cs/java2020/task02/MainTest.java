package ru.hse.cs.java2020.task02;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MainTest {
    @Test
    public void testPutGet() {
        CacheImpl cache = new CacheImpl(1024, 0, "/home/kate/hse/Java", EvictionPolicy.LRU);
        cache.put(179L, "bbb");
        Assert.assertEquals(cache.get(179L), "bbb");
    }

    @Test
    public void testReadFromDisk() {
        CacheImpl cache = new CacheImpl(19, 100, "/home/kate/hse/Java", EvictionPolicy.LRU);
        cache.put(179L, "b");
        cache.put(178L, "d");
        Assert.assertEquals(cache.get(179L), "b");
    }

    @Test
    public void getStringAfterUpdate() {
        CacheImpl cache = new CacheImpl(1024, 0, "/home/kate/hse/Java", EvictionPolicy.LRU);
        cache.put(1L, "aaa");
        cache.put(1L, "bbb");
        cache.put(1L, "ccc");
        cache.put(1L, "ddd");

        Assert.assertEquals(cache.get(1L), "ddd");
    }

    @Test
    public void getAfterRemove() {
        CacheImpl cache = new CacheImpl(33, 0, "/home/kate/hse/Java", EvictionPolicy.LRU);
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
        CacheImpl cache = new CacheImpl(45, 0, "/home/kate/hse/Java", EvictionPolicy.LFU);
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
        CacheImpl.Elem a = new CacheImpl.Elem(1L, "a");
        CacheImpl.List.Node<CacheImpl.Elem> a_n = new CacheImpl.List.Node<>(a);
        CacheImpl.Elem b = new CacheImpl.Elem(2L, "b");
        CacheImpl.List.Node<CacheImpl.Elem> b_n = new CacheImpl.List.Node<>(b);
        CacheImpl.Elem c = new CacheImpl.Elem(3L, "c");
        CacheImpl.List.Node<CacheImpl.Elem> c_n = new CacheImpl.List.Node<>(c);

        CacheImpl.List<CacheImpl.Elem> list = new CacheImpl.List<>();
        list.pushFront(a_n);
        list.pushFront(b_n);
        list.pushFront(c_n);

        CacheImpl.List.Node<CacheImpl.Elem> cur = list.foot;
        int cnt = 0;
        while (cur != null) {
            cur = cur.right;
            cnt++;
        }
        assert(cnt == 3);
    }

}

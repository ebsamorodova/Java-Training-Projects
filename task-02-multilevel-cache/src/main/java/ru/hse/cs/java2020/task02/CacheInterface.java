package ru.hse.cs.java2020.task02;

import java.util.HashMap;

public interface CacheInterface {
    void put(Long id, String str);
    String get(Long id);
}

enum Eviction { LRU, LFU };

class MyCache implements CacheInterface {
    private HashMap<Long, List.Node<Elem>> myMap = new HashMap<>(); // long -> Node<Elem>
    private HashMap<Long, List.Node<List<Elem>>> listMap = new HashMap<>(); // Elem.id -> Node<List>
    private List<List<Elem>> myList = new List<>();
    private Eviction eviction;
    private int maxMemory;
    private int maxDisk;
    private int curSize = 0;

    final int CHAR_SIZE = 2;
    final int ELEM_SIZE = 16;
    final int START_FREQUENCY = 1;

    public MyCache(int memory, int disk, Eviction eviction) {
        maxMemory = memory;
        maxDisk = disk;
        if (maxDisk > 0) { // потом добавить поддержку диска
            throw new IllegalArgumentException();
        }
        this.eviction = eviction;
    }

    public int elemSize(Elem elem) {
        return elem.str.length() * CHAR_SIZE + ELEM_SIZE;
    }

    public void put(Long id, String str) {
        Elem curElem = new Elem(id, str);
        List.Node<Elem> curNode = new List.Node<>(curElem);

        if (myMap.get(id) == null) { // новый элемент
            int size = elemSize(curElem);
            while (curSize + size > maxMemory) { // пока не хватает памяти
                List.Node<List<Elem>> footList = myList.foot;
                List.Node<Elem> footElem = footList.value.foot;
                curSize -= elemSize(footElem.value);
                footList.value.extract(footElem); // достаем последний
                listMap.remove(footElem.value.id);
                if (footList.value.head == null) { // хвост кончился
                    myList.extract(footList);
                }
                myMap.remove(footElem.value.id);
            }

            // здесь уже достаточно памяти на добавление нового узла
            myMap.put(id, curNode); // long -> Node<curElem>
            List.Node<List<Elem>> footNode = myList.foot;
            if (footNode == null || footNode.value.frequency > START_FREQUENCY) { // не тот хвост
                System.out.println(str + " в не том хвосте");
                // сделаем новый хвост с частотой = 1
                List<Elem> newFootList = new List<>();
                newFootList.frequency = START_FREQUENCY;
                newFootList.pushFront(curNode);

                List.Node<List<Elem>> newFootNode = new List.Node<>(newFootList);
                myList.pushBack(newFootNode);
                listMap.put(id, newFootNode);
            } else { // можно добавить в начало хвоста
                footNode.value.pushFront(curNode);
                listMap.put(id, footNode);
            }
            curSize += size;
        } else { // такой узел уже где-то есть
            List.Node<Elem> oldNode = myMap.get(id);
            System.out.println("уже где-то есть: " + oldNode.value.str);
            List.Node<List<Elem>> oldList = listMap.get(id);
            System.out.println(oldList == null); // TRUE
            oldList.value.extract(oldNode); // удаляем старый вариант
            myMap.put(id, curNode);


            if (eviction == Eviction.LFU) { // нужно переткнуть в следующий список
                listMap.remove(id);
                int needFrequency = oldList.value.frequency + 1;
                List.Node<List<Elem>> nextList = oldList.right;

                if (nextList == null || nextList.value.frequency > needFrequency) { // не тот список
                    List<Elem> newNextList = new List<>();
                    newNextList.frequency = needFrequency;
                    newNextList.pushFront(curNode);

                    List.Node<List<Elem>> newNextNode = new List.Node<>(newNextList);
                    myList.pushAfter(oldList, newNextNode);
                    listMap.put(id, newNextNode);
                } else {
                    nextList.value.pushFront(curNode);
                    listMap.put(id, nextList);
                }
                if (oldList.value.head == null) { // если элементы в нем кончились
                    myList.extract(oldList);
                }
            } else {
                oldList.value.pushFront(curNode);
            }

        }
    }

    public String get(Long id) {
        List.Node<Elem> needElem = myMap.get(id);
        if (needElem == null) { return null; } // no such string
        String needStr = needElem.value.str;
        System.out.println("cur ans: " + needStr);
        put(id, needStr);
        return needStr;
    }

    static class Elem {
        String str;
        long id;

        public Elem(long myId, String myStr) {
            str = myStr;
            id = myId;
        }
    }

    static class List<T> {
        static class Node<T> {
            Node<T> left;
            Node<T> right;
            T value;

            public Node(T myValue) {
                left = null;
                right = null;
                value = myValue;
            }
        }

        Node<T> head;
        Node<T> foot;
        int frequency;

        public void pushFront(Node<T> elem) {
            if (head == null) {
                head = elem;
                foot = elem;
            } else {
                elem.right = head;
                head.left = elem;
                head = elem;
            }
        }

        public void pushAfter(Node<T> elem, Node<T> toPush) {
            Node<T> curRight = elem.right;
            if (curRight == null) {
                pushFront(toPush);
            } else {
                elem.right = toPush;
                toPush.left = elem;
                toPush.right = curRight;
                curRight.left = toPush;
            }
        }

        public void pushBack(Node<T> elem) {
            if (foot == null) {
                head = elem;
                foot = elem;
            } else {
                elem.left = foot;
                foot.right = elem;
                foot = elem;
            }
        }

        public void extract(Node<T> elem) {
            Node<T> curLeft = elem.left;
            Node<T> curRight = elem.right;
            elem.left = null;
            elem.right = null;
            if (curLeft == null && curRight == null) { // extract head = foot
                head = null;
                foot = null;
            } else if (curLeft == null) { // extract head
                curRight.left = null;
                head = curRight;
            } else if (curRight == null)  { // extract foot
                curLeft.right = null;
                foot = curLeft;
            } else {
                curLeft.right = curRight;
                curRight.left = curLeft;
            }
        }

    }
}
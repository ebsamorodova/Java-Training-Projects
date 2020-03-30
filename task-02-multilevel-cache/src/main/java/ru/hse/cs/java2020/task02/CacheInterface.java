package ru.hse.cs.java2020.task02;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public interface CacheInterface {
    void put(Long id, String str);
    String get(Long id);
}

enum EvictionPolicy { LRU, LFU }

class CacheImpl implements CacheInterface {
    private HashMap<Long, List.Node<Elem>> myMap = new HashMap<>(); // long -> Node<Elem>
    private HashMap<Long, List.Node<List<Elem>>> listMap = new HashMap<>(); // Elem.id -> Node<List>
    private HashMap<Long, Integer> fileMap = new HashMap<>(); // Elem.id -> file shift
    private List<List<Elem>> myList = new List<>();
    private RandomAccessFile myFile;
    private EvictionPolicy evictionPolicy;
    private String cachePath;
    private int maxMemory;
    private int maxDisk;
    private int curMemory = 0;
    private int curDisk = 0;

    private final int charSize = 2;
    private final int elemSize = 16;
    private final int startFrequency = 1;

    CacheImpl(int memory, int disk, String path, EvictionPolicy policy) {
        maxMemory = memory;
        maxDisk = disk;
        cachePath = path;
        try {
            String filePath = path + "/my_cache.txt";
            myFile = new RandomAccessFile(filePath, "rw");
        } catch (IOException e) {
            System.err.println("File creating error: " + e.getMessage());
        }
        this.evictionPolicy = policy;
    }

    public int elemSize(Elem elem) {
        return elem.str.length() * charSize + elemSize;
    }

    public void writeToFile(Elem elem) {
        fileMap.put(elem.id, curDisk);
        try {
            myFile.seek(curDisk);
            myFile.writeLong(elem.id);
            myFile.writeInt(elem.str.length());
            byte[] array = elem.str.getBytes();
            myFile.write(array, 0, elem.str.length());
            curDisk += elemSize(elem);
            myFile.seek(curDisk);
        } catch (IOException e) {
            System.err.println("Write to file error: " + e.getMessage());
        }
    }

    public void put(Long id, String str) {
        Elem curElem = new Elem(id, str);
        List.Node<Elem> curNode = new List.Node<>(curElem);
        int size = elemSize(curElem);

        if (myMap.get(id) == null) { // новый элемент
            while (curMemory + size > maxMemory) { // пока не хватает памяти
                List.Node<List<Elem>> footList = myList.foot;
                List.Node<Elem> footElem = footList.value.foot;
                curMemory -= elemSize(footElem.value);
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
            if (footNode == null || footNode.value.frequency > startFrequency) { // не тот хвост
                // сделаем новый хвост с частотой = 1
                List<Elem> newFootList = new List<>();
                newFootList.frequency = startFrequency;
                newFootList.pushFront(curNode);

                List.Node<List<Elem>> newFootNode = new List.Node<>(newFootList);
                myList.pushBack(newFootNode);
                listMap.put(id, newFootNode);
            } else { // можно добавить в начало хвоста
                footNode.value.pushFront(curNode);
                listMap.put(id, footNode);
            }
            curMemory += size;

            if (curDisk + size <= maxDisk) {
                writeToFile(curElem);
            }
        } else { // такой узел уже где-то есть
            List.Node<Elem> oldNode = myMap.get(id);
            List.Node<List<Elem>> oldList = listMap.get(id);
            oldList.value.extract(oldNode); // удаляем старый вариант
            myMap.put(id, curNode);

            if (evictionPolicy == EvictionPolicy.LFU) { // нужно переткнуть в следующий список
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

            if (!oldNode.value.str.equals(curNode.value.str) && curDisk + size < maxDisk) {
                // обновление записи и нужно перезаписать файл на диске
                RandomAccessFile myNewFile;
                try {
                    String filePath = cachePath + "/my_new_cache.txt";
                    myNewFile = new RandomAccessFile(filePath, "rw");

                    fileMap.clear();
                    int newCurDisk = 0;
                    List.Node<List<Elem>> getList = myList.head;
                    while (getList != null) {
                        List.Node<Elem> getNode = getList.value.foot;
                        while (getNode != null) {
                            fileMap.put(getNode.value.id, newCurDisk);
                            myNewFile.writeLong(getNode.value.id);
                            myNewFile.writeInt(getNode.value.str.length());
                            myNewFile.writeUTF(getNode.value.str);
                            newCurDisk += elemSize(getNode.value);
                            getNode = getNode.right;
                        }
                        getList = getList.left;
                    }
                    myFile = myNewFile;
                    curDisk = newCurDisk;
                } catch (IOException e) {
                    System.err.println("Another file error: " + e.getMessage());
                }

                if (curDisk + size <= maxDisk) {
                    writeToFile(curElem);
                }
            }
        }
    }

    public String getFromDisk(Long id) {
        Integer index = fileMap.get(id);
        if (index == null) {
            return null;
        }
        try {
            myFile.seek(index);
            myFile.readLong();
            int len = myFile.readInt();
            byte[] array = new byte[len];
            myFile.read(array, 0, len);
            myFile.seek(curDisk);
            return new String(array, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Reading from disk error: " + e.getMessage());
        }
        return null;
    }

    public String get(Long id) {
        List.Node<Elem> needElem = myMap.get(id);
        if (needElem == null) {
            return getFromDisk(id);
        }
        String needStr = needElem.value.str;
        put(id, needStr);
        return needStr;
    }

    static class Elem {
        private String str;
        private long id;

        Elem(long myId, String myStr) {
            str = myStr;
            id = myId;
        }
    }

    static class List<T> {
        static class Node<T> {
            private Node<T> left;
            private Node<T> right;
            private T value;

            Node(T myValue) {
                left = null;
                right = null;
                value = myValue;
            }
        }

        private Node<T> head;
        private Node<T> foot;
        private int frequency;

        public void pushFront(Node<T> elem) {
            if (head == null) {
                foot = elem;
            } else {
                elem.left = head;
                head.right = elem;
            }
            head = elem;
        }

        public void pushAfter(Node<T> elem, Node<T> toPush) {
            Node<T> curRight = elem.right;
            if (curRight == null) { // мы в голове
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
            } else {
                elem.right = foot;
                foot.left = elem;
            }
            foot = elem;
        }

        public void extract(Node<T> elem) {
            Node<T> curLeft = elem.left;
            Node<T> curRight = elem.right;
            elem.left = null;
            elem.right = null;
            if (curLeft == null && curRight == null) { // extract head = foot
                head = null;
                foot = null;
            } else if (curLeft == null) { // extract foot
                curRight.left = null;
                foot = curRight;
            } else if (curRight == null)  { // extract head
                curLeft.right = null;
                head = curLeft;
            } else {
                curLeft.right = curRight;
                curRight.left = curLeft;
            }
        }
    }
}

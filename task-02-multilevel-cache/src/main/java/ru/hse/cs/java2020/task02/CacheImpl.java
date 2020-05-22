package ru.hse.cs.java2020.task02;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

enum EvictionPolicy { LRU, LFU }

class CacheImpl implements CacheInterface {
    private HashMap<Long, Integer> fileMap = new HashMap<>(); // Elem.id -> file shift
    private RandomAccessFile myFile;
    private final InMemoryControlledStorage memoryStorage;
    private String cachePath;
    private int maxMemory;
    private int maxDisk;
    private int curMemory = 0;
    private int curDisk = 0;

    private final int charSize = 2;
    private final int intSize = 4;
    private final int longSize = 8;

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
        if (policy == EvictionPolicy.LFU) {
            memoryStorage = new InMemoryControlledStorageLFU();
        } else {
            memoryStorage = new InMemoryControlledStorageLRU();
        }
    }

    public int elemSize(Elem elem) {
        return elem.getStr().length() * charSize + longSize;
    } // len(str) + id

    public int diskSize(Elem elem) {
        return elem.getStr().length() * charSize + intSize;
    }

    public void writeToFile(Elem elem) {
        fileMap.put(elem.getId(), curDisk);
        try {
            myFile.seek(curDisk);
            myFile.writeInt(elem.getStr().length());
            byte[] array = elem.getStr().getBytes();
            myFile.write(array, 0, elem.getStr().length());
            curDisk += diskSize(elem);
            myFile.seek(curDisk);
        } catch (IOException e) {
            System.err.println("Write to file error: " + e.getMessage());
        }
    }

    public void put(Long id, String str) {
        Elem curElement = new Elem(id, str);
        int size = elemSize(curElement);
        String oldValue = memoryStorage.getElement(id);

        if (oldValue == null) { // новый элемент
            while (curMemory + size > maxMemory) { // пока не хватает памяти
                Elem evictedElement = memoryStorage.evictElement(); // выкинуть ненужный
                curMemory -= elemSize(evictedElement);
            }

            if (curMemory + size <= maxMemory) {
                memoryStorage.addNewElement(id, str);
                curMemory += size;
            }
            if (curDisk + diskSize(curElement) <= maxDisk) {
                writeToFile(curElement);
            }
        } else { // такой узел уже где-то есть
            int oldSize = oldValue.length() * charSize + longSize;
            while (curMemory - oldSize + size > maxMemory) {
                Elem evictedElement = memoryStorage.evictElement(); // выкинуть ненужный
                curMemory -= elemSize(evictedElement);
            }

            if (curMemory - oldSize + size <= maxMemory) {
                memoryStorage.updateElement(id, str);
                curMemory += size - oldSize;
            }

            if (!oldValue.equals(str)) { // обновление данных в файле
                if (curDisk + diskSize(curElement) > maxDisk) { // нужно перезаписать файл на диске
                    RandomAccessFile myNewFile;
                    try {
                        String filePath = cachePath + "/my_new_cache.txt";
                        myNewFile = new RandomAccessFile(filePath, "rw");

                        fileMap.clear();
                        int newCurDisk = 0;
                        for (Elem getElement : memoryStorage.getElementsList()) {
                            if (newCurDisk + diskSize(getElement) <= maxDisk) {
                                fileMap.put(getElement.getId(), newCurDisk);
                                myNewFile.writeLong(getElement.getId());
                                myNewFile.writeInt(getElement.getStr().length());
                                myNewFile.writeUTF(getElement.getStr());
                                newCurDisk += diskSize(getElement);
                            }
                        }

                        myFile = myNewFile;
                        curDisk = newCurDisk;
                    } catch (IOException e) {
                        System.err.println("Another file error: " + e.getMessage());
                    }
                }

                if (curDisk + diskSize(curElement) <= maxDisk) {
                    writeToFile(curElement);
                    curDisk += diskSize(curElement);
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
            int len = myFile.readInt();
            byte[] array = new byte[len];
            myFile.read(array, 0, len);
            myFile.seek(curDisk);
//            System.out.println("found on disk");
            return new String(array, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Reading from disk error: " + e.getMessage());
        }
        return null;
    }

    public String get(Long id) {
        String needStr = memoryStorage.getElement(id);
        if (needStr == null) {
            return getFromDisk(id);
        }
        put(id, needStr);
        return needStr;
    }
}

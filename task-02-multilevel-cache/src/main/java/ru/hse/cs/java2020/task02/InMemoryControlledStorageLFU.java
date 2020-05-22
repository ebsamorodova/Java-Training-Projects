package ru.hse.cs.java2020.task02;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.List;

// реализация взята с https://medium.com/algorithm-and-datastructure/lfu-cache-in-o-1-in-java-4bac0892bdb3
public class InMemoryControlledStorageLFU implements InMemoryControlledStorage {
    private HashMap<Long, String> myMap = new HashMap<>();
    private HashMap<Long, Integer> accessFrequency = new HashMap<>(); // счётчик
    private HashMap<Integer, LinkedHashSet<Long>> frequencyKeys = new HashMap<>();
    private int minFrequency = -1;

    InMemoryControlledStorageLFU() {
        frequencyKeys.put(1, new LinkedHashSet<>()); // новый сет для частоты 1
    }

    public Elem evictElement() {
        Long idToRemove = frequencyKeys.get(minFrequency).iterator().next();
        Elem evict = new Elem(idToRemove, myMap.get(idToRemove));
        frequencyKeys.get(minFrequency).remove(idToRemove);
        myMap.remove(idToRemove);
        accessFrequency.remove(idToRemove);
        return evict;
    }

    public void addNewElement(long id, String str) {
        myMap.put(id, str);
        accessFrequency.put(id, 1);
        minFrequency = 1; // минимальная текущая частота использования = 1
        frequencyKeys.get(minFrequency).add(id);
    }

    public void updateElement(long id, String newStr) {
        myMap.put(id, newStr);
        getElement(id); // здесь всё нужное и обновится
    }

    public List<Elem> getElementsList() {
        ArrayList<Elem> elements = new ArrayList<>();
        for (Map.Entry<Long, String> beginEntry : myMap.entrySet()) {
            Elem curElement = new Elem(beginEntry.getKey(), beginEntry.getValue());
            elements.add(curElement);
        }
        return elements;
    }

    public String getElement(long id) {
        String needStr = myMap.get(id);
        if (needStr != null) {
            int frequency = accessFrequency.get(id);
            accessFrequency.put(id, frequency + 1);
            frequencyKeys.get(frequency).remove(id);

            if (frequency == minFrequency && frequencyKeys.get(frequency).size() == 0) {
                // текущая минимальная частота использования кончилась
                minFrequency++;
            }

            if (!frequencyKeys.containsKey(frequency + 1)) {
                frequencyKeys.put(frequency + 1, new LinkedHashSet<>());
            }
            frequencyKeys.get(frequency + 1).add(id);
        }
        return needStr;
    }
}


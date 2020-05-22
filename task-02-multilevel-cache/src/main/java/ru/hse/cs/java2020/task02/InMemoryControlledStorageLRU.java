package ru.hse.cs.java2020.task02;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryControlledStorageLRU implements InMemoryControlledStorage {
    private final int defaultCapacity = 16;
    private final float defaultLoadFactor = 0.75f;
    private LinkedHashMap<Long, String> myMap = new LinkedHashMap<>(defaultCapacity, defaultLoadFactor, true);

    InMemoryControlledStorageLRU() { }

    public Elem evictElement() {
        Iterator<Map.Entry<Long, String>> begin = myMap.entrySet().iterator();
        if (begin.hasNext()) {
            Map.Entry<Long, String> beginEntry = begin.next();
            Elem evicted = new Elem(beginEntry.getKey(), beginEntry.getValue());
//            System.out.println("removed " + beginEntry.getKey() + " id");
            myMap.remove(beginEntry.getKey());
            return evicted;
        }
//        System.out.println("nothing to remove :C");
        return null;
    }

    public void addNewElement(long id, String str) {
        myMap.put(id, str);
    }

    public void updateElement(long id, String newStr) {
        myMap.put(id, newStr);
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
        return myMap.get(id);
    }
}

package ru.hse.cs.java2020.task02;

import java.util.List;

public interface InMemoryControlledStorage {
    Elem evictElement(); // выкинуть ненужный элемент

    void addNewElement(long id, String str); // добавляем новый элемент

    void updateElement(long id, String newStr); // используем старый

    List<Elem> getElementsList();

    String getElement(long id); // есть ли вообще такой элемент
}


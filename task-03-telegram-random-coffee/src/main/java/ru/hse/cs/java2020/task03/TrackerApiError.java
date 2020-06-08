package ru.hse.cs.java2020.task03;

public class TrackerApiError extends Exception {
    public TrackerApiError(String error) {
         super(error);
    }
}

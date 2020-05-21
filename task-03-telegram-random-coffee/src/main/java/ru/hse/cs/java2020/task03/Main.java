package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {

        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            TrackerApiInterface trackerClient = new TrackerApiClient();
            UserInfoStorageInterface storage = new MapDBUserInfoStorage("/home/katsam/hse");
            MyTrackerBot myNewBot = new MyTrackerBot(trackerClient, storage);
            botsApi.registerBot(myNewBot);
            Runtime.getRuntime().addShutdownHook(new Thread(myNewBot::close));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

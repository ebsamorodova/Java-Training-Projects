package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

public class MyTrackerBot extends TelegramLongPollingBot implements AutoCloseable {
    private TrackerApiInterface trackerClient;// = new TrackerApiClient();
    private UserInfoStorageInterface userInfoStorage;// = new MapDBUserInfoStorage();
//    private HashMap<Long, String> users = new HashMap<>(); // chatId -> oauthToken
    private final int TOKEN_MESSAGE_LEN = 2;
    private final int MY_ISSUES_MESSAGE_LEN = 2;
    private final int CREATE_ISSUE_MESSAGE_LEN = 6;
    private final int FIND_ISSUE_MESSAGE_LEN = 3;

    public MyTrackerBot(TrackerApiInterface trackerInterface,
                        UserInfoStorageInterface userInterface) {
        trackerClient = trackerInterface;
        userInfoStorage = userInterface;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var newText = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();
            if (!userInfoStorage.containsUser(chatId)) {
                userInfoStorage.setUserToken(chatId, "null");
            }
            String messageText;
            if (newText.equals("/start")) {
                messageText = startBot();
            } else if (newText.equals("/meow")) {
                messageText = "meow";
            } else if (newText.equals("/get_token")) {
                messageText = getUserOauthToken();
            } else if (newText.startsWith("/take_token")) {
                messageText = setUserToken(newText, chatId);
            } else if (newText.startsWith("/my_issues")) { // список назначенных задач
                messageText = getUserIssues(newText, chatId);
            } else if (newText.startsWith("/next_issues")) { // список назначенных задач
                messageText = getNextUserIssues(newText, chatId);
            } else if (newText.startsWith("/issue")) { // получить описание задачи
                messageText = findIssue(newText, chatId);
            } else if (newText.startsWith("/create")) { // создать новую задачу
                messageText = createNewIssue(newText, chatId);
            } else {
                messageText = "Неизвестный запрос, пожалуйста," +
                        "попробуйте ещё раз.";
            }
            sendMessage(messageText, update);
        }
    }

    @Override
    public String getBotUsername() {
        return "my tracker bot";
    }

    @Override
    public String getBotToken() {
        return "1113952534:AAFF2g-hWtkA9zaAJypM-wTbzOkZZiFcqAc";
    }

    public void sendMessage(String messageText, Update update) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(messageText); // send message
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String WrongFormat() {
        return "Ошибка! Неверный формат запроса.";
    }

    public String Oops() {
        return "В процессе обработки запроса что-то пошло не так.\n" +
                "Пожалуйста, попробуйте ещё раз...";
    }

    public String GetTokenPlease() {
        return "Пожалуйста, получите OAuth-токен с помощью " +
                "команды /get_token.";
    }

    public String startBot() {
        return "Привет!\nДля начала работы с ботом, пожалуйста, " +
                "авторизуйтесь с помощью комнады /get_token. " +
                "После этого введите полученный токен в формате " +
                "/take_token token.\n" +
                "После этого Вы можете получить информацию по задачу " +
                "с помощью команды /issue orgId issueId или создать " +
                "новую задачу /create orgId queueId summary description " +
                "true/false. Если Вы хотите назначить себя исполнителем, " +
                "введите в конце запроса true, иначе - false. Пример: " +
                "\"/create 1234567 QUEUE test test true\". \n" +
                "Для того, чтобы посмотреть задачи, назначенные на Вас, " +
                "введите /my_issues orgId, ответом на запрос будет не более " +
                "10 задач. Для просмотра следующих 10 задач введите " +
                "/next_issues orgId.\n" +
                "Для того, чтобы бот мяукнул, введите /meow.";
    }

    public String getUserOauthToken() { // пользователь идет и добывает токен
        String userMessage = "Пожалуйста, перейдите по ссылке " +
                "https://oauth.yandex.ru/authorize?response_type=token&client_id=08f9daa8e4d245f1aedd613d48d9885b\n" +
                "После этого введите полученный токен в формате '/take_token token'.";
        return userMessage;
    }

    public String setUserToken(String message, Long chatId) {
        var tmpArray = message.split(" ");
        if (tmpArray.length != TOKEN_MESSAGE_LEN) {// /take_token token
            return WrongFormat();
        }

        var oauthToken = tmpArray[1];
        userInfoStorage.setUserToken(chatId, oauthToken);
        return "Ваш OAuth токен успешно сохранён!";
    }

    public String createNewIssue(String message, Long chatId) {
        var oauthToken = userInfoStorage.getUserToken(chatId);
        if (oauthToken.equals("null")) {
            return GetTokenPlease();
        }

        var tmpArray = message.split(" ");
        if (tmpArray.length != CREATE_ISSUE_MESSAGE_LEN) { // /create orgid queueid summary description true/false
            return WrongFormat();
        }

        var orgId = tmpArray[1];
        var queueIssue = tmpArray[2];
        var summaryIssue = tmpArray[3];
        var descriptionIssue = tmpArray[4];
        var assignMe = tmpArray[5];
        if (!assignMe.equals("true") && !assignMe.equals("false")) {
            return WrongFormat();
        }
        var assignMeIssue = assignMe.equals("true");
        var createdIssue = trackerClient.createNewIssue(oauthToken, orgId,
                summaryIssue, queueIssue, descriptionIssue, assignMeIssue);
        if (createdIssue == null) {
            return Oops();
        }
        return "Новая задача успешно создана: " + createdIssue;
    }

    public String findIssue(String getMessage, Long chatId) {
        var oauthToken = userInfoStorage.getUserToken(chatId);
        if (oauthToken.equals("null")) {
            return GetTokenPlease();
        }
        var tmpArray = getMessage.split(" ");
        if (tmpArray.length != FIND_ISSUE_MESSAGE_LEN) { // /task orgId issueId
            return "incorrect request: " + Arrays.toString(tmpArray);
        }
        var orgId = tmpArray[1];
        var issueId = tmpArray[2];
        String issueInfo = getIssueInfo(oauthToken, orgId, issueId);
        System.out.println("find issue info: " + issueInfo);
        if (issueInfo == null) {
            return Oops();
        }
        return getIssueInfo(oauthToken, orgId, issueId);
    }

    public String getIssueInfo(String oauthToken, String orgId, String issueId) {
        String issueInfo;
        try {
            issueInfo = trackerClient.getIssueInfo(oauthToken, orgId, issueId).toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return Oops();
        }
        return issueInfo;
    }

    public String getUserIssues(String messageText, Long chatId) {
        var oauthToken = userInfoStorage.getUserToken(chatId);
        if (oauthToken.equals("null")) {
            return GetTokenPlease();
        }
        var tmpArray = messageText.split(" ");
        if (tmpArray.length != MY_ISSUES_MESSAGE_LEN) {
            return WrongFormat();
        }
        var orgId = tmpArray[1];
        var issuesKeysList = trackerClient.findAssignedIssues(oauthToken, orgId);
        if (issuesKeysList == null) {
            return Oops();
        }
        return "Список задач, назначенных на Вас:\n" + issuesKeysList.toString();
    }

    public String getNextUserIssues(String messageText, Long chatId) {
        var oauthToken = userInfoStorage.getUserToken(chatId);
        if (oauthToken.equals("null")) {
            return GetTokenPlease();
        }
        var tmpArray = messageText.split(" ");
        if (tmpArray.length != MY_ISSUES_MESSAGE_LEN) {
            return WrongFormat();
        }
        var orgId = tmpArray[1];
        var issuesKeysList = trackerClient.findNextAssignedIssues(oauthToken, orgId);
        if (issuesKeysList == null) {
            return Oops();
        }
        if (issuesKeysList.isEmpty()) {
            return "Задач, назначенных на Вас, больше нет.";
        }
        return "Список задач, назначенных на Вас:\n" + issuesKeysList.toString();
    }

    @Override
    public void close() {
        userInfoStorage.close();
    }
}

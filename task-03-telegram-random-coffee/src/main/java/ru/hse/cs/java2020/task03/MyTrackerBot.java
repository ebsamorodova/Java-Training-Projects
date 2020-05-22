package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

public class MyTrackerBot extends TelegramLongPollingBot implements AutoCloseable {
    private TrackerApiInterface trackerClient;
    private UserInfoStorageInterface userInfoStorage;
    private final int tokenMessageLen = 3;
    private final int findIssueMessageLen = 2;

    public MyTrackerBot(TrackerApiInterface trackerInterface,
                        UserInfoStorageInterface userInterface) {
        trackerClient = trackerInterface;
        userInfoStorage = userInterface;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var newMessageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (newMessageText) {
                case "/start":
                    sendMessage(startBot(), update);
                    break;
                case "/meow":
                    sendMessage("meow", update);
                    break;
                case "/get_token":
                    sendMessage(getUserInfo(), update);
                    break;
                case "/my_issues":
                    sendMessage(getUserIssues(chatId), update);
                    break;
                case "/next_issues":
                    sendMessage(getNextUserIssues(chatId), update);
                    break;
                case "/create_issue":
                    sendMessage(startCreatingIssue(chatId), update);
                    break;
                case "/done":
                    sendMessage(createNewIssue(chatId), update);
                    break;
                default:
                    if (newMessageText.startsWith("/take_token")) {
                        sendMessage(setUserInfo(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/issue")) { // получить описание задачи
                        sendMessage(findIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_summary")) {
                        sendMessage(setCreateSummaryIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_queue")) {
                        sendMessage(setCreateQueueIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_description")) {
                        sendMessage(setCreateDescriptionIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_assign")) {
                        sendMessage(setCreateAssignMeIssue(newMessageText, chatId), update);
                    } else {
                        sendMessage("Неизвестный формат запроса, "
                                + "пожалуйста, попробуйте ещё раз", update);
                    }
            }
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

    public String wrongFormat() {
        return "Ошибка! Неверный формат запроса.";
    }

    public String oops(String errorMessage) {
        return "В процессе обработки запроса что-то пошло не так:\n"
                + errorMessage;
    }

    public String getTokenPlease() {
        return "Пожалуйста, получите OAuth-токен с помощью "
                + "команды /get_token.";
    }

    public String startBot() {
        return "Привет!\nДля начала работы с ботом, пожалуйста, "
                + "авторизуйтесь с помощью команды /get_token. "
                + "После этого введите полученный токен и id Вашей "
                + "организации в формате /take_token token orgId.\n"
                + "После этого Вы можете получить информацию по задаче "
                + "с помощью команды /issue issueId или создать "
                + "новую задачу /create_issue. Для того, чтобы посмотреть "
                + "задачи, назначенные на Вас, введите /my_issues, "
                + "ответом на запрос будет не более 10 задач. "
                + "Для просмотра следующих 10 задач введите /next_issues."
                + "\nДля того, чтобы бот мяукнул, введите /meow.";
    }

    public String getUserInfo() {
        return "Пожалуйста, перейдите по ссылке "
                + "https://oauth.yandex.ru/authorize?response_type=token"
                + "&client_id=08f9daa8e4d245f1aedd613d48d9885b\n"
                + "После этого введите полученный токен и id организации "
                + "в формате /take_token token orgId.";
    }

    public String setUserInfo(String message, Long chatId) {
        var tmpArray = message.split(" ");
        if (tmpArray.length != tokenMessageLen) {
            return wrongFormat();
        }
        var oauthToken = tmpArray[1];
        var orgId = tmpArray[2];
        userInfoStorage.setUserInfo(chatId, oauthToken, orgId);
        return "Ваш OAuth и OrgId токен успешно сохранены!";
    }

    public String startCreatingIssue(long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        return "Для создания задачи укажите название задачи в формате "
                + "/create_summary summary.";
    }

    public String setCreateSummaryIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var firstIndex = "/create_summary".length() + 1;
        trackerClient.setCreateSummary(message.substring(firstIndex));
        return "Название задачи установлено. Пожалуйста, укажите "
                + "id очереди, в которой Вы хотите создать задачу: "
                + "/create_queue queue_id.";
    }

    public String setCreateQueueIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var firstIndex = "/create_queue".length() + 1;
        trackerClient.setCreateQueue(message.substring(firstIndex));
        return "Очередь задачи установлена. Пожалуйста, введите "
                + "описание задачи: /create_description description.";
    }

    public String setCreateDescriptionIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var firstIndex = "/create_description".length() + 1;
        trackerClient.setCreateDescription(message.substring(firstIndex));
        return "Описание задачи установлено. Пожалуйста, укажите, "
                + "назначить ли Вас исполнителем задачи: "
                + "/create_assign true или /create_assign false.";
    }
    public String setCreateAssignMeIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var firstIndex = "/create_assign".length() + 1;
        var assignMe = message.substring(firstIndex);
        if (!assignMe.equals("false") && !assignMe.equals("true")) {
            return oops("Unknown parameter value: \"" + assignMe + "\".");

        }
        trackerClient.setCreateAssignMe(assignMe);
        return "Все параметры задачи установлены. Пожалуйста, "
                + "введите /done для завершения создания задачи.";
    }

    public String createNewIssue(Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        try {
            String createdIssue = trackerClient.createNewIssue(oauthToken, orgId);
            return "Новая задача успешно создана: " + createdIssue;
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String findIssue(String getMessage, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);

        var tmpArray = getMessage.split(" ");
        if (tmpArray.length != findIssueMessageLen) {
            return "incorrect request: " + Arrays.toString(tmpArray);
        }
        var issueId = tmpArray[1];
        try {
            IssueInfo issueInfo = trackerClient.getIssueInfo(oauthToken, orgId, issueId);
            return issueInfo.toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String getUserIssues(Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        try {
            List<String> issuesKeysList = trackerClient.findAssignedIssues(oauthToken, orgId);
            return "Список задач, назначенных на Вас:\n" + issuesKeysList.toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String getNextUserIssues(Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        try {
            var issuesKeysList = trackerClient.findNextAssignedIssues(oauthToken, orgId);
            if (issuesKeysList.isEmpty()) {
                return "Задач, назначенных на Вас, больше нет.";
            }
            return "Список задач, назначенных на Вас:\n" + issuesKeysList.toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    @Override
    public void close() {
        userInfoStorage.close();
    }
}

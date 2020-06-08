package ru.hse.cs.java2020.task03;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class MyTrackerBot extends TelegramLongPollingBot implements AutoCloseable {
    private TrackerApiInterface trackerClient;
    private UserInfoStorageInterface userInfoStorage;
    private final int tokenMessageLen = 3;
    private final int findIssueMessageLen = 2;
    private final int pageSize = 5;

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
                    } else if (newMessageText.startsWith("/comments")) {
                        sendMessage(getIssueComments(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_summary")) {
                        sendMessage(setCreateSummaryIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_queue")) {
                        sendMessage(setCreateQueueIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_description")) {
                        sendMessage(setCreateDescriptionIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/create_assign")) {
                        sendMessage(setCreateAssignMeIssue(newMessageText, chatId), update);
                    } else if (newMessageText.startsWith("/get_issue")) {
                        sendMessage(findAssignedIssue(newMessageText, chatId), update);
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
        return "botToken";
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
                + "новую задачу /create_issue. Чтобы посмотреть "
                + "комментарии по задаче, введите /comments issue_id"
                + "Для того, чтобы посмотреть задачи, назначенные "
                + " на Вас, введите /my_issues, ответом на запрос "
                + " будет не более " + pageSize + " задач. Для просмотра "
                + "следующих 10 задач введите /next_issues."
                + "Чтобы получить информациб по N-ой выведенной задаче, "
                + "наберите /get_issue N."
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
        if (message.length() <= firstIndex) {
            return oops("Пустое название задачи");
        }
        userInfoStorage.setIssueSummary(chatId, message.substring(firstIndex));
        return "Название задачи установлено. Пожалуйста, укажите "
                + "ключ очереди, в которой Вы хотите создать задачу: "
                + "/create_queue queue_key.";
    }

    public String setCreateQueueIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        var firstIndex = "/create_queue".length() + 1;
        if (message.length() <= firstIndex) {
            return oops("Пустой ключ очереди");
        }
        try {
            trackerClient.tryQueueKey(oauthToken, orgId, message.substring(firstIndex));
            userInfoStorage.setIssueQueue(chatId, message.substring(firstIndex));
            return "Очередь задачи установлена. Пожалуйста, введите "
                    + "описание задачи: /create_description description.";
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String setCreateDescriptionIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var firstIndex = "/create_description".length() + 1;
        if (message.length() <= firstIndex) {
            return oops("Пустое описание задачи");
        }
        userInfoStorage.setIssueDescription(chatId, message.substring(firstIndex));
        return "Описание задачи установлено. Пожалуйста, укажите, "
                + "назначить ли Вас исполнителем задачи: "
                + "/create_assign true или /create_assign false.";
    }

    public String setCreateAssignMeIssue(String message, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var firstIndex = "/create_assign".length() + 1;
        if (message.length() <= firstIndex) {
            return oops("Пустое поле assign_me");
        }
        var assignMe = message.substring(firstIndex);
        if (!assignMe.equals("false") && !assignMe.equals("true")) {
            return oops("Unknown parameter value: \"" + assignMe + "\".");

        }
        userInfoStorage.setAssignMe(chatId, assignMe.equals("true"));
        return "Все параметры задачи установлены. Пожалуйста, "
                + "введите /done_issue для завершения создания задачи.";
    }

    public String createNewIssue(Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }

        if (!userInfoStorage.containsSummary(chatId)
                || !userInfoStorage.containsDescription(chatId)
                || !userInfoStorage.containsQueue(chatId)
                || !userInfoStorage.containsAssignMe(chatId)) {
            return oops("not all issue parameters are set");
        }

        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        var issueSummary = userInfoStorage.getIssueSummary(chatId);
        var issueDescription = userInfoStorage.getIssueDescription(chatId);
        var issueQueueKey = userInfoStorage.getIssueQueue(chatId);
        var issueAssignMe = userInfoStorage.getIssueAssignMe(chatId);

        try {
            String createdIssue = trackerClient.createNewIssue(oauthToken, orgId, issueSummary,
                    issueDescription, issueQueueKey, issueAssignMe);
            userInfoStorage.clearIssueInfo(chatId);
            return "Новая задача успешно создана: " + createdIssue;
        } catch (Exception e) {
            return oops(e.getMessage());
        }
    }

    public String findIssue(String getMessage, Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var tmpArray = getMessage.split(" ");
        if (tmpArray.length != findIssueMessageLen) {
            return wrongFormat();
        }

        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        var issueId = tmpArray[1];
        try {
            IssueInfo issueInfo = trackerClient.getIssueInfo(oauthToken, orgId, issueId);
            return issueInfo.toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String getIssueComments(String message, long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var tmpArray = message.split(" ");
        if (tmpArray.length != findIssueMessageLen) {
            return wrongFormat();
        }

        var issueId = tmpArray[1];
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        try {
            List<String> commentsList = trackerClient.getIssueComments(oauthToken, orgId, issueId);
            if (commentsList.isEmpty()) {
                return "У заданной задачи комментариев пока нет.";
            }
            StringBuilder commentsString = new StringBuilder("Комментарии к задаче " + issueId + ":\n");
            for (int i = 0; i < commentsList.size(); i++) {
                commentsString.append(i + 1);
                commentsString.append(". ").append(commentsList.get(i)).append('\n');
            }
            return commentsString.toString();
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
            IssuesListAndLink issuesInfo = trackerClient.findAssignedIssues(oauthToken, orgId);
            var issuesKeysString = new StringBuilder();
            var pageNumber = issuesInfo.getPageNumber();
            List issues = issuesInfo.getIssuesKeys();
            for (int i = 0; i < issues.size(); ++i) {
                issuesKeysString.append((i + 1) + pageNumber * pageSize);
                issuesKeysString.append(". ");
                issuesKeysString.append(issues.get(i));
                issuesKeysString.append("\n");
            }
            userInfoStorage.setIssuesInfo(chatId, issuesInfo);
            return "Список задач, назначенных на Вас:\n" + issuesKeysString.toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String getNextUserIssues(Long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        if (!userInfoStorage.containsIssuesInfo(chatId)) {
            return "Calling /next_issues before /my_issues";
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);
        var lastIssuesInfo = userInfoStorage.getIssuesInfo(chatId);
        var curPageLink = lastIssuesInfo.getNextPageLink();
        var curPageNumber = lastIssuesInfo.getPageNumber();
        try {
            var issuesInfo = trackerClient.findNextAssignedIssues(oauthToken, orgId, curPageLink, curPageNumber);
            userInfoStorage.setIssuesInfo(chatId, issuesInfo);
            if (issuesInfo.getIssuesKeys().isEmpty()) {
                return "Задач, назначенных на Вас, больше нет.";
            }

            var issuesKeysString = new StringBuilder();
            var pageNumber = issuesInfo.getPageNumber();
            List issues = issuesInfo.getIssuesKeys();
            for (int i = 0; i < issues.size(); ++i) {
                issuesKeysString.append((i + 1) + pageNumber * pageSize);
                issuesKeysString.append(". ");
                issuesKeysString.append(issues.get(i));
                issuesKeysString.append("\n");
            }
            userInfoStorage.setIssuesInfo(chatId, issuesInfo);
            return "Список задач, назначенных на Вас:\n" + issuesKeysString.toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    public String findAssignedIssue(String message, long chatId) {
        if (!userInfoStorage.containsUser(chatId)) {
            return getTokenPlease();
        }
        var oauthToken = userInfoStorage.getUserToken(chatId);
        var orgId = userInfoStorage.getUserOrgId(chatId);

        if (!userInfoStorage.containsIssuesInfo(chatId)) {
            return oops("Calling /get issue_number before /my_issues");
        }
        var tmpArray = message.split(" ");
        if (tmpArray.length != findIssueMessageLen) {
            return wrongFormat();
        }
        var lastIssuesInfo = userInfoStorage.getIssuesInfo(chatId);
        var pageNumber = lastIssuesInfo.getPageNumber();
        var number = Integer.valueOf(tmpArray[1]) - pageNumber * pageSize - 1;
        var issueKey = lastIssuesInfo.getIssuesKeys().get(number);
        try {
            return trackerClient.getIssueInfo(oauthToken, orgId, issueKey).toString();
        } catch (TrackerApiError trackerApiError) {
            return oops(trackerApiError.getMessage());
        }
    }

    @Override
    public void close() {
        userInfoStorage.close();
    }
}

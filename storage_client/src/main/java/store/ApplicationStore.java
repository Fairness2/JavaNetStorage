package store;

import callbacks.Callback;
import models.User;

public class ApplicationStore {
    public static User user;
    public static Callback callback;
    public static final String ROOT_PATH = "storage_client/src/main/resources/";

    public static Callback fileCallback;
}

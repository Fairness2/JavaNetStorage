package store;

import models.FileResponse;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class ApplicationStore {
    public static final String ROOT_PATH = "storage_server/src/main/resources/storage/";
    public static final HashMap<UUID, Consumer<FileResponse>> filesRequests = new HashMap<>();
}

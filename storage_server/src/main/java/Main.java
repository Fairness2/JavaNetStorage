import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            NioServer server = new NioServer();
            log.debug("Server started");
            server.run();
        }
        catch (IOException e) {
            log.error("Cant start server", e);
        }

    }
}

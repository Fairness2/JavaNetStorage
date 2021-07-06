package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShareFile implements TransmittedSignal {
    private File file;
    private String userLogin;
    private User user;
}

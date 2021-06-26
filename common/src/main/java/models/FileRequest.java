package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileRequest implements TransmittedSignal {
    private File file;
    private User user;
}

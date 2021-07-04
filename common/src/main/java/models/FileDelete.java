package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDelete implements TransmittedSignal {
    private File file;
    private User user;
}

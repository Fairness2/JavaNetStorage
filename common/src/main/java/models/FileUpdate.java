package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUpdate implements TransmittedSignal {
    private File file;
    private String newName;
    private User user;
}

package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SynchronizeRequest implements TransmittedSignal {
    private User user;
    private String path;
}

package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SynchronizeSharedFilesRequest implements TransmittedSignal {
    private User user;
}

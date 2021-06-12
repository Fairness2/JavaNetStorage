package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandardAnswer implements TransmittedSignal {
    private boolean success;
    private String message;
    private TransmittedSignal param;
}

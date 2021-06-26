package models;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FileResponse implements TransmittedSignal {
    private UUID uuid;
    private boolean status;
}

package models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileResponse implements TransmittedSignal {
    private String uuid;
    private boolean status;
}

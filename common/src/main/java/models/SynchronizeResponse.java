package models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SynchronizeResponse implements TransmittedSignal {
    private String path;
    private List<File> fileList;
}

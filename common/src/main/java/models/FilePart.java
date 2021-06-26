package models;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FilePart implements TransmittedSignal{
    private byte[] byteArray;
    private String path;
    private String name;
    private long part;
    private User user;
    private UUID uuid;
    private int countParts;
    private boolean last;
}

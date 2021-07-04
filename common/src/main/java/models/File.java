package models;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class File implements Serializable {
    public static String DIR_TYPE = "directory";
    public static String FILE_TYPE = "file";
    public static String SHARED_FILE_TYPE = "shared_file";


    private String path;
    private String name;
    private int ownerId;
    private String type;
    private boolean shared;
    private Integer fileId;
}

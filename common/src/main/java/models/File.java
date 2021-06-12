package models;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class File implements Serializable {
    public static String DIR_TYPE = "directory";
    public static String FILE_TYPE = "file";


    private String path;
    private String name;
    private int ownerId;
    private String type;
}

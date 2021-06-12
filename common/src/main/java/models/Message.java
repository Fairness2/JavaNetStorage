package models;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class Message implements TransmittedSignal {
    public String message;
    public User user;
    public ZonedDateTime time;
}

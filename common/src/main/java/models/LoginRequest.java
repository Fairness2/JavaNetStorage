package models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest implements TransmittedSignal {
    private String login;
    private String password;
}

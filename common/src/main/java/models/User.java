package models;

import java.util.Objects;

/**
 * Модель пользователя
 */
public class User implements TransmittedSignal {
    private String login;
    private String password;
    private String name;
    private int id;

    public User (String login, String name, int id) {
        this(login, null, name, id);
    }

    public User (String login, String password, String name, int id) {
        this.login = login;
        this.password = password;
        this.name = name;
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        User targetUser = (User) o;
        return Objects.equals(login, targetUser.getLogin()); //Потому что не должно быть двух пользователей с одинаковым логином
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

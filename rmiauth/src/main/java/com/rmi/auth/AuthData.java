package com.rmi.auth;

/**
 * Данные аутентификации.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
public class AuthData {

    /** Логин. */
    public final String login;
    /** Пароль. */
    public final String password;

    /**
     * Конструктор.
     *
     * @param login Логин.
     * @param password Пароль.
     */
    public AuthData(String login, String password) {
        if (login == null) {
            throw new NullPointerException("login");
        }
        if (password == null) {
            throw new NullPointerException("password");
        }

        this.login = login;
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AuthData [");
        builder.append("login=").append(login);
        builder.append(", password=").append(password);
        builder.append("]");
        return builder.toString();
    }
}

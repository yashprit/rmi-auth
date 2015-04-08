package com.rmi.auth;

/**
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
public interface Authorizer {

    /**
     * Выполняет авторизацию.
     *
     * @param authData Данные авторизации.
     *
     * @return True, если авторизация успешна, false в противном случае.
     */
    boolean authorize(AuthData authData);
}

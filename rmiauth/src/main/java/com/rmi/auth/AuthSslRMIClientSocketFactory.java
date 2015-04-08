package com.rmi.auth;

import java.io.IOException;
import java.net.Socket;
import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 * Создает сокеты, которые авторизуются на сервере, отправляя логин и пароль.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 *
 * @see AuthSslRMIServerSocketFactory
 */
public class AuthSslRMIClientSocketFactory extends SslRMIClientSocketFactory {

    /** Данные авторизации клиента. */
    private volatile AuthData authData;

    /**
     * @param authData  Данные авторизации клиента.
     */
    public void setAuthData(AuthData authData) {
        if (authData == null) {
            throw new NullPointerException("authData");
        }

        this.authData = authData;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = super.createSocket(host, port);
        new ClientSideSocketAuthorizationImpl(socket, authData).checkAuthorized();
        return socket;
    }
}

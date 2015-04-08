package com.rmi.auth;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Создает сокеты, которые авторизуются на сервере, отправляя логин и пароль.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 *
 * @see AuthSslRMIServerSocketFactory
 */
public class AuthRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {

    /** Данные авторизации клиента для хоста. */
    private static final Map<String, AuthData> hostAuthData
            = new ConcurrentHashMap<String, AuthData> ();

    /**
     * @param host Хост.
     * @param authData  Данные авторизации клиента.
     */
    public static void setHostAuthData(String host, AuthData authData) {
        if (host == null) {
            throw new NullPointerException("host");
        }
        if (authData == null) {
            throw new NullPointerException("authData");
        }

        AuthRMIClientSocketFactory.hostAuthData.put(host, authData);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);

        AuthData authData = hostAuthData.get(host);
        if (authData == null) {
            throw new SocketAuthorizationFailedException("No authentification data for host " + host);
        }
        new ClientSideSocketAuthorizationImpl(socket, authData).checkAuthorized();

        return socket;
    }
}

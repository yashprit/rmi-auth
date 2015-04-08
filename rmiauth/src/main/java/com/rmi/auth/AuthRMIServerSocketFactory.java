package com.rmi.auth;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * Создает сокеты, которые авторизуют клиентов, проверяя логин и пароль.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 *
 * @see AuthSslRMIClientSocketFactory
 */
public class AuthRMIServerSocketFactory implements RMIServerSocketFactory, Serializable {

    /** Авторизатор соединений. */
    private final Authorizer authorizer;

    /**
     * Конструктор.
     *
     * @param authorizer Авторизатор соединений.
     */
    public AuthRMIServerSocketFactory(Authorizer authorizer) {
        super();

        if (authorizer == null) {
            throw new NullPointerException("authorizer");
        }
        this.authorizer = authorizer;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocketAuthWrap(new ServerSocket(port), authorizer);
    }

}

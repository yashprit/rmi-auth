package com.rmi.auth;

import java.io.IOException;
import java.net.ServerSocket;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * Создает сокеты, которые авторизуют клиентов, проверяя логин и пароль.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 *
 * @see AuthSslRMIClientSocketFactory
 */
public class AuthSslRMIServerSocketFactory extends SslRMIServerSocketFactory {

    /** Авторизатор соединений. */
    private final Authorizer authorizer;

    /**
     * Конструктор.
     *
     * @param enabledCipherSuites
     * @param enabledProtocols
     * @param needClientAuth
     * @param authorizer Авторизатор соединений.
     *
     * @throws IllegalArgumentException
     *
     * @see SslRMIServerSocketFactory#SslRMIServerSocketFactory(java.lang.String[], java.lang.String[], boolean)
     */
    public AuthSslRMIServerSocketFactory(String[] enabledCipherSuites, String[] enabledProtocols, boolean needClientAuth, Authorizer authorizer) throws IllegalArgumentException {
        super(enabledCipherSuites, enabledProtocols, needClientAuth);

        if (authorizer == null) {
            throw new NullPointerException("authorizer");
        }
        this.authorizer = authorizer;
    }

    /**
     * Конструктор.
     *
     * @param authorizer Авторизатор соединений.
     */
    public AuthSslRMIServerSocketFactory(Authorizer authorizer) {
        super();

        if (authorizer == null) {
            throw new NullPointerException("authorizer");
        }
        this.authorizer = authorizer;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocketAuthWrap(super.createServerSocket(port), authorizer);
    }

}

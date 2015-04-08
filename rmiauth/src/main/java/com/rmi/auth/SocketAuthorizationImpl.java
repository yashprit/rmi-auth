package com.rmi.auth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Выполняет проверку авторизации сокета. Реализация потокобезопасна.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
abstract class SocketAuthorizationImpl {

    /** Ответ на запрос авторизации: авторизация прошла успешно. */
    protected static final byte AUTH_SUCCEEDED = 0;
    /** Ответ на запрос авторизации: авторизация прошла неудачно. */
    protected static final byte AUTH_FAILED = 1;
    ////
    /** Сокет. */
    protected final Socket socket;
    /** Авторизовано ли соединение. */
    protected boolean authorized;

    public SocketAuthorizationImpl(Socket socket) {
        if (socket == null) {
            throw new NullPointerException("socket");
        }

        this.socket = socket;
    }

    /**
     * Проверяет, авторизовано ли соединение. Если не авторизовано, то выполняет авторизацию.
     * В случае неудачной авторизации сокет будет закрыт.
     *
     * @throws IOException .
     */
    public abstract void checkAuthorized() throws IOException;

}

final class ClientSideSocketAuthorizationImpl extends SocketAuthorizationImpl {

    /** Данные авторизации. */
    private final AuthData authData;
    ////
    private static final Logger log = Logger.getLogger(ClientSideSocketAuthorizationImpl.class.getName());

    public ClientSideSocketAuthorizationImpl(Socket socket, AuthData authData) {
        super(socket);

        if (authData == null) {
            throw new NullPointerException("authData");
        }
        this.authData = authData;
    }

    @Override
    public void checkAuthorized() throws IOException {
        if (authorized) {
            return;
        }

        log.log(Level.FINEST, "Socket connection authorization on the client side, authData: {0}", authData);

        // авторизация еще не выполнена
        // отправка логина и пароля
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(authData.login);
        dos.writeUTF(authData.password);
        dos.flush();

        // читаем ответ на запрос авторизации от сервера
        int authResponse = socket.getInputStream().read();
        if (authResponse == AUTH_SUCCEEDED) {
            log.log(Level.FINEST, "Socket connection authorization successful");
            authorized = true;
        } else {
            log.log(Level.SEVERE, "Socket connection authorization failed");
            socket.close();
            throw new SocketAuthorizationFailedException("Wrong authorization: " + authData);
        }
    }
}

final class ServerSideSocketAuthorizationImpl extends SocketAuthorizationImpl {

    private final Authorizer authorizer;
    ////
    private static final Logger log = Logger.getLogger(ServerSideSocketAuthorizationImpl.class.getName());

    public ServerSideSocketAuthorizationImpl(Socket socket, Authorizer authorizer) {
        super(socket);

        if (authorizer == null) {
            throw new NullPointerException("authorizer");
        }
        this.authorizer = authorizer;
    }

    @Override
    public void checkAuthorized() throws IOException {
        if (authorized) {
            return;
        }

        log.log(Level.FINEST, "Socket connection authorization on the server side");

        // авторизация еще не выполнена
        // читаем логин и пароль
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String login = dis.readUTF();
        String password = dis.readUTF();

        AuthData authData = new AuthData(login, password);
        if (authorizer.authorize(authData)) {
            log.log(Level.FINEST, "Socket connection authorization succeeded with data: {0}", authData);

            // авторизация успешна
            dos.write(AUTH_SUCCEEDED);
            authorized = true;
        } else {
            log.log(Level.FINE, "Socket connection authorization failed with data: {0}", authData);

            // неверные данные авторизации
            dos.write(AUTH_FAILED);
            dos.flush();
            socket.close(); // закрываем соединение
            throw new SocketAuthorizationFailedException("Wrong authorization: " + authData);
        }
    }
}

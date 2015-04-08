package com.rmi.auth;

import com.test.TestThreadsHelper;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
public class AuthRMISocketFactoryTest {

    /** Уровень лога. */
    private static final Level DEBUG_LEVEL = Level.OFF;
    /** Будут ли все операции в тесте выполняться последовательно или параллельно. */
    private static final boolean SEQUENTIAL = false;
    /** Количество потоков. */
    private static final int NUM_THREADS = 10;
    ////
    private ServerSocket serverSocket;
    private List<Socket> clientSockets;
    private List<Socket> serverSideClientSockets;
    private AuthRMIClientSocketFactory clientSocketFactory;
    private AuthRMIServerSocketFactory serverSocketFactory;
    ////
    private final AuthData correctAuth = new AuthData("1", "1");

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("====== AuthSslRMISocketFactoryTest =====");

        // конфигурация модуля логов
        Logger.getLogger("").setLevel(DEBUG_LEVEL);
        Logger.getLogger("").getHandlers()[0].setLevel(DEBUG_LEVEL);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        clientSocketFactory = new AuthRMIClientSocketFactory();
        serverSocketFactory = new AuthRMIServerSocketFactory(new TestAuthorizer());

        clientSockets = Collections.synchronizedList(new LinkedList<Socket>());
        serverSideClientSockets = Collections.synchronizedList(new LinkedList<Socket>());

        openServerSocket();
    }

    @After
    public void tearDown() throws Exception {
        // закрываем сокеты

        closeClientSockets();
        clientSockets = null;
        serverSideClientSockets = null;

        if (serverSocket != null) {
            serverSocket.close();
            serverSocket = null;
        }

        Thread.sleep(100);
    }

    /** Корректная авторизация, клиент отправляет данные первым. */
    @Test(timeout=1000)
    public void testCorrectAuthorization() throws Throwable {
        System.out.println("testCorrectAuthorization");

        TestThreadsHelper threads = new TestThreadsHelper(NUM_THREADS, SEQUENTIAL) {

            @Override
            protected void run(int nThread) throws Exception {
                Socket clSocket = openClientSocket(correctAuth);
                clSocket.getOutputStream().write(17);
            }
        };

        threads.startThreads();

        for (int i = 0; i < NUM_THREADS; i++) {
            Socket ssSocket = accept();
            assertEquals(17, ssSocket.getInputStream().read());
        }

        threads.awaitThreads().checkThrowables();
    }

    @Test(timeout = 1000)
    public void testWrongAuthorization() throws Throwable {
        System.out.println("testWrongAuthorization");

        TestThreadsHelper threads = new TestThreadsHelper(NUM_THREADS, SEQUENTIAL) {

            @Override
            protected void run(int nThread) throws Exception {
                try {
                    openClientSocket(new AuthData("2", "3"));
                    fail("Исключение не сгенерировано");
                } catch (SocketAuthorizationFailedException e) {
                }
            }
        };

        threads.startThreads();

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                Socket ssSocket = accept();
                fail("Исключение не сгенерировано");
            } catch (SocketAuthorizationFailedException e) {
            }
        }

        threads.awaitThreads().checkThrowables();
    }

    private void openServerSocket() throws IOException {
        serverSocket = serverSocketFactory.createServerSocket(0);
    }

    private Socket openClientSocket(AuthData authData) throws IOException {
        clientSocketFactory.setHostAuthData("localhost", authData);
        Socket socket = clientSocketFactory.createSocket("localhost", serverSocket.getLocalPort());
        clientSockets.add(socket);
        return socket;
    }

    private Socket accept() throws IOException {
        Socket socket = (Socket) serverSocket.accept();
        serverSideClientSockets.add(socket);
        return socket;
    }

    private void closeClientSockets() throws IOException {
        if (clientSockets != null) {
            for (Socket socket : clientSockets) {
                socket.close();
            }
        }

        if (serverSideClientSockets != null) {
            for (Socket socket : serverSideClientSockets) {
                socket.close();
            }
        }
    }

    class TestAuthorizer implements Authorizer {

        @Override
        public boolean authorize(AuthData authData) {
            return authData.login.equals(correctAuth.login) && authData.login.equals(correctAuth.password);
        }
    }
}

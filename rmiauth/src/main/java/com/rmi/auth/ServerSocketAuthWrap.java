package com.rmi.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

/**
 * Обертка над серверным сокетом, которая выполняет аутентификацию, принимая
 * логин и пароль при первом обращении.
 *
 * @author Sergey Ponomarev (sergey.ponomarev@vistar.su)
 */
final class ServerSocketAuthWrap extends ServerSocket {

    private final ServerSocket sock;
    /** Авторизатор соединений. */
    private final Authorizer authorizer;

    public ServerSocketAuthWrap(ServerSocket sock, Authorizer authorizer) throws IOException {
        this.sock = sock;

        if (authorizer == null) {
            throw new NullPointerException("authorizer");
        }
        this.authorizer = authorizer;
    }

    public Socket accept() throws IOException {
        Socket socket = sock.accept();
        new ServerSideSocketAuthorizationImpl(socket, authorizer).checkAuthorized();
        return socket;
    }

    //<editor-fold defaultstate="collapsed" desc="Делегирование методов ServerSocket">
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        sock.setSoTimeout(timeout);
    }

    public void setReuseAddress(boolean on) throws SocketException {
        sock.setReuseAddress(on);
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        sock.setReceiveBufferSize(size);
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        sock.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    public boolean isClosed() {
        return sock.isClosed();
    }

    public boolean isBound() {
        return sock.isBound();
    }

    public synchronized int getSoTimeout() throws IOException {
        return sock.getSoTimeout();
    }

    public boolean getReuseAddress() throws SocketException {
        return sock.getReuseAddress();
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        return sock.getReceiveBufferSize();
    }

    public SocketAddress getLocalSocketAddress() {
        return sock.getLocalSocketAddress();
    }

    public int getLocalPort() {
        return sock.getLocalPort();
    }

    public InetAddress getInetAddress() {
        return sock.getInetAddress();
    }

    public ServerSocketChannel getChannel() {
        return sock.getChannel();
    }

    public void close() throws IOException {
        sock.close();
    }

    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        sock.bind(endpoint, backlog);
    }

    public void bind(SocketAddress endpoint) throws IOException {
        sock.bind(endpoint);
    }
    //</editor-fold>
}

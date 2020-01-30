package ru.mydiy.network;

import java.net.Socket;

public interface SocketThreadListener {
    void onSocketThreadStart(SocketThread socketThread);
    void onSocketReady(SocketThread socketThread, Socket socket);
    void onReceiveMessage(SocketThread socketThread, Socket socket, String msg);
    void onSocketThreadException(SocketThread socketThread, Exception e);
}

//Класс, ожидающий подключения и генерирующий Socket после подключения клиента
package ru.mydiy.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread{

    private final int port;
    private final int timeout;
    private final ServerSocketListener listener;
   //private final ServerSocketThread sst;

    public ServerSocketThread(ServerSocketListener listener, int port, int timeout){
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        listener.onThreadStart(this);
        try (ServerSocket server = new ServerSocket(port)){
            listener.onServerStart(this, server);
            server.setSoTimeout(timeout);
            Socket socket;
            while (!isInterrupted()){
               try{
                   socket = server.accept();
               } catch (SocketTimeoutException e){
                   listener.onServerAcceptTimeout(this, server);
                   continue;
               }
               listener.onSocketAccepted(server, socket);
            }
        } catch (IOException e){
            listener.onServerException(this, e);
        } finally {
            listener.onThreadStop(this);
        }
    }
}

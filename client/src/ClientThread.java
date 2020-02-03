import ru.mydiy.network.SocketThread;
import ru.mydiy.network.SocketThreadListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements SocketThreadListener {
    private SocketThread outputThread;

    public static void main(String[] args) {
        ClientThread client = new ClientThread();

        Scanner scanner = new Scanner(System.in);

        while (true){
            String msg = scanner.next();
            client.sendMessage(msg);
        }
    }

    private ClientThread(){
        Socket socket = null;
        try {
            socket = new Socket("192.168.0.123", 5277);
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputThread = new SocketThread(this, "Client", socket);
    }

    /* Socket Events*/
    @Override
    public void onSocketThreadStart(SocketThread socketThread) {

    }

    @Override
    public void onSocketReady(SocketThread socketThread, Socket socket) {

    }

    @Override
    public void onReceiveMessage(SocketThread socketThread, Socket socket, String msg) {
        System.out.println(msg);
    }

    @Override
    public void onSocketThreadException(SocketThread socketThread, Exception e) {
        System.out.println(e.getMessage());
    }

    public void sendMessage(String msg){
        outputThread.sendMessage(msg);
    }
}

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.mydiy.network.SocketThread;
import ru.mydiy.network.SocketThreadListener;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements SocketThreadListener {
    private final String host = "192.168.0.123";
    private final int port = 5277;
    private SocketThread outputThread;
    private final static Logger LOGGER = LogManager.getLogger();

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
        int i = 0;
        while (i != 10){
            try {
                //LOGGER.info("Connecting to: " + host +":" + port);
                socket = new Socket(host, port);
                outputThread = new SocketThread(this, "Client", socket);
                LOGGER.info("Подключён!");
                break;
                //LOGGER.info("Connected");
            } catch (IOException e) {
                LOGGER.info("Не могу подключиться к удалённому серверу. Попытка " + (i + 1));
                i++;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* Socket Events*/
    @Override
    public void onSocketThreadStart(SocketThread socketThread) {
        //LOGGER.info("SocketThread with name: \"" + socketThread.getName() + "\" started");
    }

    @Override
    public void onSocketReady(SocketThread socketThread, Socket socket) {
        //LOGGER.info("Socket " + socket.getRemoteSocketAddress() + ":" + socket.getPort() + " ready");
    }

    @Override
    public void onReceiveMessage(SocketThread socketThread, Socket socket, String msg){
        //LOGGER.info("Socket received message: "  + msg);
    }

    @Override
    public void onSocketThreadException(SocketThread socketThread, Exception e) {
        System.out.println(e.getMessage());
    }

    public void sendMessage(String msg){
        outputThread.sendMessage(msg);
    }
}

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("192.168.0.102", 5277);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);
            while (true){
                String msg = scanner.next();
                out.writeUTF(msg);
                System.out.println(in.readUTF());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

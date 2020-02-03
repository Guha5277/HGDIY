package ru.mydiy.server.core;

import ru.mydiy.hw.GSMListener;
import ru.mydiy.hw.MotionSensorListener;
import ru.mydiy.hw.MotionSensor;
import ru.mydiy.network.ServerSocketListener;
import ru.mydiy.network.ServerSocketThread;
import ru.mydiy.network.SocketThread;
import ru.mydiy.network.SocketThreadListener;
import com.pi4j.io.gpio.OrangePiPin;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements ServerSocketListener, SocketThreadListener, GSMListener, MotionSensorListener {
    private boolean alarmState;
    private int port = 5277;
    private int timeout = 2000;
    private SocketThread client;

    public static void main(String[] args) {
        Server server = new Server();
    }

    Server() {
        MotionSensor monitor1 = new MotionSensor(this, OrangePiPin.GPIO_01);
        //MotionSensor monitor2 = new MotionSensor(this, OrangePiPin.GPIO_02);
        //GSMModule gsmMonitor = new GSMModule();

        ServerSocketThread serverSocketThread = new ServerSocketThread(this, port, timeout);
    }

    public boolean getAlarmState() {
        return alarmState;
    }

    public void setAlarm(boolean alarm) {
        this.alarmState = alarm;
    }


    public void putLog(String msg){
        System.out.println(msg);
    }

    /*ServerSocketThread Events*/
    @Override
    public void onThreadStart(ServerSocketThread thread) {
        putLog(thread.getName() + " : started!");
    }

    @Override
    public void onServerStart(ServerSocketThread thread, ServerSocket server) {
        putLog(server.getInetAddress().getCanonicalHostName() + " : Server started!");
    }

    @Override
    public void onServerAcceptTimeout(ServerSocketThread thread, ServerSocket server) {

    }

    @Override
    public void onSocketAccepted(ServerSocket server, Socket socket) {
        //putLog(socket.getRemoteSocketAddress() + " connected!");
        client = new SocketThread(this, "dafault", socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Exception e) {

    }

    @Override
    public void onThreadStop(ServerSocketThread thread) {

    }


    /*SocketThread Events*/
    @Override
    public void onSocketThreadStart(SocketThread socketThread) {

    }

    @Override
    public void onSocketReady(SocketThread socketThread, Socket socket) {

    }

    @Override
    public void onReceiveMessage(SocketThread socketThread, Socket socket, String msg) {
        socketThread.sendMessage("Echo: " + msg);
    }

    @Override
    public void onSocketThreadException(SocketThread socketThread, Exception e) {
        putLog("ooops!");
    }


    /*Hardware Events*/
    @Override
    public void motionState(MotionSensor monitor, boolean state) {
        if (client != null){
            if (state){
                client.sendMessage("PIR IS HIGH");
            } else {
                client.sendMessage("PIR IS LOW");
            }

        }
    }

    @Override
    public void onSensorException(MotionSensor monitor, Exception e) {
        if (client != null){
            client.sendMessage("EXCEPTION IN MonitorSensor: " + e.getMessage());
        }
    }
}

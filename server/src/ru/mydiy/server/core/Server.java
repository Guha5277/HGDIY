package ru.mydiy.server.core;

import ru.mydiy.hw.GSMListener;
import ru.mydiy.hw.MotionSensorListener;
import ru.mydiy.hw.MotionSensor;
import ru.mydiy.network.ServerSocketListener;
import ru.mydiy.network.ServerSocketThread;
import ru.mydiy.network.SocketThread;
import ru.mydiy.network.SocketThreadListener;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements ServerSocketListener, SocketThreadListener, GSMListener, MotionSensorListener {
    private boolean alarmState;
    private int port = 5277;
    private int timeout = 2000;
    private SocketThread client;
    ServerSocketThread serverSocketThread;

    public static void main(String[] args) {
        Server server = new Server();
    }

    Server() {
        MotionSensor monitor1 = new MotionSensor(this, "PIR1");
        //MotionSensor monitor2 = new MotionSensor(this, OrangePiPin.GPIO_02);
        //GSMModule gsmMonitor = new GSMModule();
        serverSocketThread = new ServerSocketThread(this, port, timeout);
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
        putLog(socket.getRemoteSocketAddress() + " connected!");
        client = new SocketThread(this, "default", socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Exception e) {
        putLog("EXCEPTION in ServerSocketThread: " + e.getMessage());
        serverSocketThread.interrupt();
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
        putLog("Socket ready for exchange data");
    }

    @Override
    public void onReceiveMessage(SocketThread socketThread, Socket socket, String msg) {
        socketThread.sendMessage("Echo: " + msg);
    }

    @Override
    public void onSocketThreadException(SocketThread socketThread, Exception e) {
        putLog("EXCEPTION in SocketThread: " + e.getMessage());
        serverSocketThread.interrupt();
    }


    /*Hardware Events*/
    @Override
    public synchronized void motionState(MotionSensor monitor, boolean state) {
        if (client != null){
            if (state){
                client.sendMessage("PIR " + monitor.getName() + " IS HIGH");
            } else {
                client.sendMessage("PIR " + monitor.getName() + " IS LOW");
            }
        }
    }

    @Override
    public synchronized void onSensorException(MotionSensor monitor, Exception e) {
        if (client != null){
            client.sendMessage("EXCEPTION in MonitorSensor: " + e.getMessage());
        }
    }

    @Override
    public synchronized void activityIsGone(MotionSensor monitor, int overallTime) {
        if (client != null){
            client.sendMessage("Activity is gone. Overall time of invasion is: " + overallTime + "s");
        }
    }

    @Override
    public synchronized void debugMessage(MotionSensor monitor, String msg) {
        client.sendMessage(msg);
    }
}

package ru.mydiy.server.core;

import ru.mydiy.hw.GSMListener;
import ru.mydiy.hw.GSMModule;
import ru.mydiy.hw.MotionSensorListener;
import ru.mydiy.hw.MotionSensor;
import ru.mydiy.network.ServerSocketListener;
import ru.mydiy.network.ServerSocketThread;
import ru.mydiy.network.SocketThread;
import ru.mydiy.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server implements ServerSocketListener, SocketThreadListener, GSMListener, MotionSensorListener {
    private boolean alarmState;
    private int port = 5277;
    private int timeout = 2000;
    private SocketThread client;
    private GSMModule gsmModule;
    ServerSocketThread serverSocketThread;
    private final static Logger LOGGER = LogManager.getLogger();

    //Флаги готовности
    private boolean isServerReady = false;
    private boolean isMotionSensorReady = false;
    private boolean isGSMModuleReady = false;

    public static void main(String[] args) {
        Server server = new Server();
    }

    Server() {
        MotionSensor monitor1 = new MotionSensor(this, "PIR1", 0);
        //MotionSensor monitor2 = new MotionSensor(this, OrangePiPin.GPIO_02);
        gsmModule = new GSMModule(this);
        serverSocketThread = new ServerSocketThread(this, port, timeout);
    }

    public boolean getAlarmState() {
        return alarmState;
    }

    public void setAlarm(boolean alarm) {
        this.alarmState = alarm;
    }

    /*ServerSocketThread Events*/
    @Override
    public void onThreadStart(ServerSocketThread thread) {
        LOGGER.info(thread.getName() + " : started!");
    }

    @Override
    public void onServerStart(ServerSocketThread thread, ServerSocket server) {
        LOGGER.info(server.getInetAddress().getCanonicalHostName() + " : Server started!");
    }

    @Override
    public void onServerAcceptTimeout(ServerSocketThread thread, ServerSocket server) {

    }

    @Override
    public void onSocketAccepted(ServerSocket server, Socket socket) {
        LOGGER.info(socket.getRemoteSocketAddress() + " connected!");
        client = new SocketThread(this, "default", socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Exception e) {
        LOGGER.warn("EXCEPTION in ServerSocketThread: " + e.getMessage());
        serverSocketThread.interrupt();
    }

    @Override
    public void onThreadStop(ServerSocketThread thread) {

    }

    /*SocketThread Events*/
    @Override
    public void onSocketThreadStart(SocketThread socketThread) {
        LOGGER.info("Socket started");
    }

    @Override
    public void onSocketReady(SocketThread socketThread, Socket socket) {
        LOGGER.info("Socket ready for exchange data");
    }

    @Override
    public void onReceiveMessage(SocketThread socketThread, Socket socket, String msg) {
        LOGGER.info("Received message from client");
        switch (msg) {
            case "call":
                LOGGER.info("Command - call");
                gsmModule.call("+79994693778");
                break;
            default:
                gsmModule.sendMessage(msg, "");
        }
        //gsmModule.sendMessage(msg, "");
    }

    @Override
    public void onSocketThreadException(SocketThread socketThread, Exception e) {
        LOGGER.fatal("EXCEPTION in SocketThread: " + e.getMessage());
        socketThread.close();
        System.exit(1);
    }

    /*MotionSernsor Events*/
    @Override
    public synchronized void motionState(MotionSensor monitor, boolean state) {
        if (client != null) {
            if (state) {
                client.sendMessage("PIR " + monitor.getName() + " IS HIGH");
            } else {
                client.sendMessage("PIR " + monitor.getName() + " IS LOW");
            }
        }
    }

    @Override
    public synchronized void onException(MotionSensor monitor, Exception e) {
        if (client != null) {
            client.sendMessage("EXCEPTION in MonitorSensor: " + e.getMessage());
        }
    }

    @Override
    public synchronized void activityIsGone(MotionSensor monitor, long overallTime) {
        if (client != null) {
            client.sendMessage("Activity is gone. Overall time of invasion is: " + overallTime + "s");
        }
    }

    @Override
    public synchronized void debugMessage(MotionSensor monitor, String msg) {
        if (client != null) {
            client.sendMessage(msg);
        }
    }

    /*GSMModule events*/
    @Override
    public void onModuleStarted(GSMModule module) {
        LOGGER.info("[GSM] заущен");
    }

    @Override
    public void onException(Exception e) {
        LOGGER.warn("[GSM] EXCEPTION: " + e.getMessage() + " " + e.getCause());
    }

    @Override
    public void onReceivedMessage(GSMModule module, String msg) {
        LOGGER.info("[GSM] получено:" + msg.substring(2, msg.length() - 2));
        if (client != null) {
            client.sendMessage(msg);
        }
    }

    @Override
    public void onSendMessage(String msg) {
        LOGGER.info("[GSM] отправлено: " + msg);
    }

    @Override
    public void debugMessage(String message) {
        LOGGER.debug(message);
    }

    @Override
    public void onIncomingCall(String number) {
        LOGGER.info("Входящий звонок: " + number);
    }

    @Override
    public void onOutcomingCallDelivered(String number) {
        LOGGER.info("Исходящий звонок доставлен: " + number);
    }

    @Override
    public void onOutcomingCallFailed(String number) {
        LOGGER.info("Исходящий звонок (нет ответа): " + number);
    }
}

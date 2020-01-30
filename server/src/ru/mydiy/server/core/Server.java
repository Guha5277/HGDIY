package ru.mydiy.server.core;

import ru.mydiy.hw.GSMListener;
import ru.mydiy.hw.GSMModule;
import ru.mydiy.hw.MotionSensorListener;
import ru.mydiy.hw.MotionSensor;
import ru.mydiy.network.ServerSocketListener;
import ru.mydiy.network.ServerSocketThread;
import ru.mydiy.network.SocketThreadListener;
import com.pi4j.io.gpio.OrangePiPin;

public class Server implements ServerSocketListener, SocketThreadListener, GSMListener, MotionSensorListener {
    private boolean alarmState;
    private int port = 5277;
    private int timeout = 2000;

    public static void main(String[] args) {
        Server server = new Server();
    }

    Server() {
        MotionSensor monitor1 = new MotionSensor(this, OrangePiPin.GPIO_01);
        MotionSensor monitor2 = new MotionSensor(this, OrangePiPin.GPIO_02);
        GSMModule gsmMonitor = new GSMModule();

        ServerSocketThread serverSocketThread = new ServerSocketThread(this, port, timeout);
    }

    public boolean getAlarmState() {
        return alarmState;
    }

    public void setAlarm(boolean alarm) {
        this.alarmState = alarm;
    }
}

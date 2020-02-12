package ru.mydiy.hw;

import com.pi4j.io.serial.*;
import java.io.IOException;
import java.io.OutputStream;

public class GSMModule implements SerialDataEventListener{
    final GSMListener listener;
    final Serial serial;

    public GSMModule(GSMListener listener, int uartPort) {
        this.listener = listener;
        serial = SerialFactory.createInstance();
        serial.addListener(this);
        try {
            serial.open(OrangePiSerial.UART3_COM_PORT, Baud._9600, DataBits._8, Parity.NONE, StopBits._1, FlowControl.NONE);
            System.out.println("[GSM] module started");
        } catch (IOException e){
            listener.onException(this, e);
        }
    }

    public void sendMessage(String msg){
        try {
            serial.write(msg + "/r");
            System.out.println("[GSM] Message send: " + msg + " <CR>");
        } catch (IOException e) {
            listener.onException(this, e);
        }
    }

    public void close(){
        try {
            serial.close();
        } catch (IOException e) {
            listener.onException(this, e);
        }
    }

    @Override
    public void dataReceived(SerialDataEvent serialDataEvent) {
        String msg;
        try {
            msg = serialDataEvent.getAsciiString();
            listener.onReceivedMessage(this, msg);
            System.out.println("[GSM] Message received: " + msg + " <CR>");
        } catch (IOException e) {
            e.printStackTrace();
            listener.onException(this, e);
        }
    }
}

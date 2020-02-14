package ru.mydiy.hw;

import com.pi4j.io.serial.*;
import java.io.IOException;
import java.io.OutputStream;

public class GSMModule implements SerialDataEventListener{
    private final GSMListener listener;
    private final Serial serial;
//    private OutputStream outputStream;
//    private SerialDataWriter

    public GSMModule(GSMListener listener, int uartPort) {
        this.listener = listener;
        serial = SerialFactory.createInstance();
        serial.addListener(this);
        try {
            serial.open("/dev/ttyS1", Baud._9600, DataBits._8, Parity.NONE, StopBits._1, FlowControl.NONE);
            //outputStream = serial.getOutputStream();
            listener.onModuleStarted("[GSM] module started");
        } catch (IOException e){
            listener.onException(this, e);
        }
    }

    public void sendMessage(String msg){
        try {
//            serial.write(msg);
//            serial.write(msg + "/r");
            serial.writeln(msg);
            listener.onSendMessage(msg);
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
    public void dataReceived(SerialDataEvent event) {
        String msg;
        try {
            msg = event.getAsciiString();
            listener.onReceivedMessage(this, msg);

            int index = msg.length();
//            String str = msg.substring(1, index);
            listener.debugMessage(Integer.toBinaryString(index));

        } catch (IOException e) {
            e.printStackTrace();
            listener.onException(this, e);
        }
    }


}

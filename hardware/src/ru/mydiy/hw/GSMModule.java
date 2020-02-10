package ru.mydiy.hw;

import com.pi4j.io.serial.*;
import java.io.IOException;

public class GSMModule implements SerialDataEventListener{
    final GSMListener listener;
    final Serial serial;

    GSMModule(GSMListener listener, int uartPort) {
        this.listener = listener;
        serial = SerialFactory.createInstance();
        serial.addListener(this);
        try {
            serial.open(OrangePiSerial.UART3_COM_PORT, Baud._9600, DataBits._8, Parity.NONE, StopBits._1, FlowControl.NONE);
        } catch (IOException e){
            listener.onException(this, e);
        }
    }

    @Override
    public void dataReceived(SerialDataEvent serialDataEvent) {

    }
}

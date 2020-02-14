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

            String str = msg.substring(2, msg.length() - 2);

            if (str.contains("+")){
                if (str.contains(":")){
                    int first = str.indexOf('+');
                    int last = str.indexOf(':');

                    String result = str.substring(first, last);
                    listener.debugMessage(result);

                    switch (result){
                        case "+CLCC":
                            listener.debugMessage("INCOMING CALL DETECTED!");

                            int numberIndexStart = str.indexOf(",\"+") + 2;
                            int numberIndexEnd = numberIndexStart + 3;
                            for (int i = numberIndexStart + 3; i < str.length(); i++){
                                if(!Character.isDigit(str.charAt(i))){
                                    numberIndexEnd = i;
                                    break;
                                }
                            }

                            String number = str.substring(numberIndexStart, numberIndexEnd);
                            listener.debugMessage("Number is: " + number);
                    }

                }
            }

            //listener.debugMessage(str);

        } catch (IOException e) {
            e.printStackTrace();
            listener.onException(this, e);
        }
    }


}

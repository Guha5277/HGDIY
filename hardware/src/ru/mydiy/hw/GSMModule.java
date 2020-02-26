/*Класс имплементирующий взаимодействие с GSM-модулем*/
package ru.mydiy.hw;

import com.pi4j.io.serial.*;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.IOException;
import java.util.ArrayList;

public class GSMModule implements SerialDataEventListener {
    private final GSMListener listener;
    private final Serial serial;
    private final String SERIAL_PORT_ADDRESS = "/dev/ttyS1";
    private ArrayList<String> lastReceivedCommandList;
    private boolean availableToSendCommand;
    private StringBuilder compileString;
    private MessageKeeper msKeeper;

    public GSMModule(GSMListener listener) {
        this.listener = listener;
        serial = SerialFactory.createInstance();
        initializeModule();
    }

    private void initializeModule() {
        lastReceivedCommandList = new ArrayList<>();
        serial.addListener(this);
        try {
            serial.open(SERIAL_PORT_ADDRESS, Baud._9600, DataBits._8, Parity.NONE, StopBits._1, FlowControl.NONE);
            availableToSendCommand = true;
            compileString = new StringBuilder();
            listener.onModuleStarted(this);
        } catch (IOException e) {
            listener.onException(e);
        }
    }

    /*TODO - метод для дешифровки сообщения от GSM-модуля и вычленения главного из сообщения*/
    private synchronized void decodeMessage(String message) throws IndexOutOfBoundsException {
        String subMessage = message.substring(2, message.length() - 2);
        char firstChar = subMessage.charAt(0); //Получение первого символа, для идентификации типа уведомления от модуля

        //Если firstChar начинается с символа '+'
        if (firstChar == 0x2b) {
            String command = subMessage.substring(0, subMessage.indexOf(SIM800.COMMAND_SEPARATOR));
            switch (command) {
                case SIM800.CALL:
                    lastReceivedCommandList.add(SIM800.CALL_TO);
                    //Если в сообщении присутствует дополнительная информация
                    if (subMessage.contains("\n")) {
                        String subCommand = subMessage.substring(subMessage.indexOf("\n") + 3);
                        String number = subMessage.substring(subMessage.indexOf(SIM800.NUMBER_BEGIN_SEPARATOR) + 2, subMessage.indexOf(SIM800.NUMBER_END_SEPARATOR));
                        switch (subCommand) {
                            //Входящий звонок
                            case SIM800.INCOMING_CALL:
                                listener.onIncomingCall(number);
                                sendMessage(SIM800.DISCARD_CALL, "");
                                break;
                            //Вызов сброшен (без снятия трубки и после снятия)
                            case SIM800.BUSY:
                                listener.onOutcomingCallDelivered(number);
                                break;
                            //Нет ответа на звонок
                            case SIM800.NO_ANSWER:
                            case SIM800.NO_CARRIER:
                                listener.onOutcomingCallFailed(number);
                                break;
                        }
                    }
                    break;
                case SIM800.CALL_CONNECTED:
                    String splitString = subMessage.substring(subMessage.indexOf(SIM800.CALL));
                    String number = splitString.substring(splitString.indexOf(SIM800.NUMBER_BEGIN_SEPARATOR) + 2, splitString.indexOf(SIM800.NUMBER_END_SEPARATOR));
                    sendMessage(SIM800.DISCARD_CALL, "");
                    listener.onOutcomingCallDelivered(number);
                    break;
                case SIM800.USSD:
                    /*TODO - ответ на USSD запрос*/
                    String cussdMessage = subMessage.substring(subMessage.indexOf(SIM800.CUSD_BEGIN_SEPARATOR) + 3, subMessage.indexOf(SIM800.CUSD_END_SEPARATOR));
                    if (!cussdMessage.contains(" ")){
                        cussdMessage = UCS2toString(cussdMessage);
                    }
                    listener.debugMessage(cussdMessage);
                    float sum = stringToFloat(cussdMessage);
                    listener.debugMessage(String.valueOf(sum));

                    break;
                /*TODO - другие уведомления?*/
                case SIM800.OPERATOR:
                    lastReceivedCommandList.add(SIM800.OK);
                    break;
            }

            //Если firstChar начинается с символа A-Z или a-z
        } else if (firstChar >= 0x41 && firstChar <= 0x5a || firstChar >= 0x61 && firstChar <= 0x79) { //Если начинается с A-Z или a-z
            switch (subMessage) {
                case SIM800.OK:
                    lastReceivedCommandList.add(SIM800.OK);
                    break;
                case SIM800.READY:
                    listener.debugMessage("READY");
                    break;
                case SIM800.PWRDWN:
                    listener.debugMessage("POWER DOWN");
                    break;
                case SIM800.UNDER_VOLTAGE_PWD:
                    listener.debugMessage("UNDER VOLTAGE POWER DOWN!");
                    break;
                case SIM800.UNDER_VOLTAGE_WARN:
                    listener.debugMessage("UNDER VOLTAGE WARNING!");
                    break;
                case SIM800.OVER_VOLTAGE_PWD:
                    listener.debugMessage("OVER VOLTAGE POWER DOWN");
                    break;
                case SIM800.OVER_VOLTAGE_WARN:
                    listener.debugMessage("OVER VOLTAGE WARNING");
                    break;
            }
        } else {
            listener.debugMessage("Неизвестное незапрашевоемое уведомление от модуля");
        }
    }

    /*TODO - метод запроса баланса*/
    public void getBalance() {
        sendMessage("AT", SIM800.USSD + "=1,\"*100#\"");
    }

    /*TODO - метод запроса оператора*/
    public void operator() {
        sendMessage("AT", SIM800.OPERATOR + '?');
    }

    //Звонок по указанному номеру
    public void call(String number) {
        sendMessage(SIM800.CALL_TO, number + ";");
    }

    //Отправка сообщения модулю
    public synchronized void sendMessage(String header, String command) {
        if (availableToSendCommand) {
            try {
                availableToSendCommand = false;
                new ResponseKeeper(header).start();
                serial.writeln(header + command);
                listener.onSendMessage(header + command);
            } catch (IOException e) {
                listener.onException(e);
            }
        } else {
            if (msKeeper != null && msKeeper.isAlive()) {
                msKeeper.addCommandToQueue(header, command);
            } else {
                msKeeper = new MessageKeeper();
                msKeeper.addCommandToQueue(header, command);
                msKeeper.start();
            }
        }
    }

    //Слушатель присланных модулем сообщений
    @Override
    public void dataReceived(SerialDataEvent event) {
        try {
            String msg = event.getAsciiString();
            listener.onReceivedMessage(this, msg);
            if (msg.length() == 64 || compileString.length() > 0) {
                compileMessage(msg);
            } else {
                decodeMessage(msg);
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            listener.onException(e);
        }
    }

    private synchronized void compileMessage(String string) {
        compileString.append(string);
        if (string.length() < 64) {
            decodeMessage(compileString.toString());
            compileString.delete(0, compileString.length());
        }
    }

    private String UCS2toString(String string){
        listener.debugMessage(string.length() + "");
        if (string.length() % 4 != 0){
            return "";
        }
        StringBuilder resultString = new StringBuilder();
        for (int i = 0; i < string.length(); i+=4){
            resultString.append((char)Integer.decode("0x" + string.substring(i, i + 4)).intValue());
        }
        listener.debugMessage(resultString.toString());
        return resultString.toString();
    }

    private float stringToFloat(String string){
        StringBuilder sb = new StringBuilder();
        boolean isNegative = false;
        float result = 0.0f;
        for (int i = 0; i < string.length(); i++) {
            char temp = string.charAt(i);
            if (temp == 0x2D) {
                isNegative = true;
                continue;
            }
            if (temp >= 0x30 && temp <= 0x39 || temp == 0x2E){
                sb.append(temp);
            } else if (sb.length() > 0){
                break;
            }
        }

        if (isNegative){
            return -(Float.valueOf(sb.toString()));
        }

        return Float.valueOf(sb.toString());
    }

    //Класс проверки ответа на отправленную команду
    class ResponseKeeper extends Thread {
        String command;

        ResponseKeeper(String command) {
            this.command = command;
        }

        @Override
        public void run() {
            long timer = System.currentTimeMillis();
            while (!isInterrupted()) {
                if (lastReceivedCommandList.contains(command)) {
                    closeRequest(command);
                } else if (lastReceivedCommandList.contains(SIM800.OK)) {
                    closeRequest(SIM800.OK);
                } else if (System.currentTimeMillis() - timer > 10000) {
                    listener.debugMessage("Таймаут ожидания ответа от модуля");
                    availableToSendCommand = true;
                    interrupt();
                } else {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        listener.onException(e);
                    }
                }
            }
        }

        private void closeRequest(String command) {
            lastReceivedCommandList.remove(command);
            availableToSendCommand = true;
            interrupt();
        }
    }

    //Очередь отправки сообщений модулю
    class MessageKeeper extends Thread {
        ArrayList<String> headers;
        ArrayList<String> commands;

        MessageKeeper() {
            headers = new ArrayList<>();
            commands = new ArrayList<>();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (commands.isEmpty()) {
                    interrupt();
                } else if (availableToSendCommand) {
                    sendMessage(headers.get(0), commands.get(0));
                    headers.remove(0);
                    commands.remove(0);
                } else {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        listener.onException(e);
                        interrupt();
                    }
                }
            }
        }

        private void addCommandToQueue(String header, String command) {
            if (this.headers.contains(header) && this.commands.contains(command)) return;
            this.headers.add(header);
            this.commands.add(command);
        }
    }
}

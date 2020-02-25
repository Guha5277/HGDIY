/*Класс имплементирующий взаимодействие с GSM-модулем*/
package ru.mydiy.hw;

import com.pi4j.io.serial.*;

import java.io.IOException;
import java.util.ArrayList;

public class GSMModule implements SerialDataEventListener {
    private final GSMListener listener;
    private final Serial serial;
    private final String SERIAL_PORT_ADDRESS = "/dev/ttyS1";
    private ArrayList<String> lastReceivedCommandList;
    private boolean availableToSendCommand;
    private MessageKeeper msKeeper;

    public GSMModule(GSMListener listener) {
        this.listener = listener;
        serial = SerialFactory.createInstance();
        initModule();
    }

    private void initModule(){
        lastReceivedCommandList = new ArrayList<>();        
        serial.addListener(this);
        try {
            serial.open(SERIAL_PORT_ADDRESS, Baud._9600, DataBits._8, Parity.NONE, StopBits._1, FlowControl.NONE);
            availableToSendCommand = true;
            listener.onModuleStarted(this);
        } catch (IOException e) {
            listener.onException(e);
        }
    }
    
    /*TODO - метод для дешифровки сообщения от GSM-модуля и вычленения главного из сообщения*/
    private synchronized void decodeMessage(String message) {
        //Получение строки без символов <CR><LF> (первые два символа в начале и конце сообщения)
        String resultString = message.substring(2, message.length() - 2);
        char firstChar = resultString.charAt(0); //Получение первого символа, для идентификации типа уведомления от модуля

        //Если firstChar начинается с символа '+'
        if (firstChar == 0x2b) {
            String command = getSubString(resultString, SIM800.COMMAND_SEPARATOR);   //Получение команды
            listener.debugMessage("Command:" + command);
            switch (command) {
                case SIM800.CALL:
                    /*TODO - звонок*/
                    lastReceivedCommandList.add(SIM800.CALL_TO);
                    //Если в сообщении присутствует дополнительная информация
                    if (resultString.contains("\n")) {
                        String submessage = resultString.substring(resultString.indexOf("\n") + 3);
                        listener.debugMessage("Submessage:" + submessage);
                        switch (submessage) {
                            //Входящий звонок
                            case SIM800.INCOMING_CALL:
                                listener.onIncomingCall(getSubString(resultString, SIM800.NUMBER_BEGIN_SEPARATOR, SIM800.NUMBER_END_SEPARATOR));
                                sendMessage(SIM800.DISCARD_CALL, "");
                                break;
                            //Вызов сброшен (без снятия трубки и после снятия)
                            case SIM800.BUSY:
                            case SIM800.NO_CARRIER:
                                listener.onOutcomingCallDelivered(getSubString(resultString, SIM800.NUMBER_BEGIN_SEPARATOR, SIM800.NUMBER_END_SEPARATOR));
                                break;
                            //Нет ответа на звонок
                            case SIM800.NO_ANSWER:
                                listener.onOutcomingCallFailed(getSubString(resultString, SIM800.NUMBER_BEGIN_SEPARATOR, SIM800.NUMBER_END_SEPARATOR));
                                break;
                        }
                    }
                    break;
                case SIM800.USSD:
                    /*TODO - ответ на USSD запрос*/
                    break;
                /*TODO - другие уведомления?*/
                case SIM800.OPERATOR:
                    lastReceivedCommandList.add(SIM800.OK);
                    break;
            }

            //Если firstChar начинается с символа A-Z или a-z
        } else if (firstChar >= 0x41 && firstChar <= 0x5a || firstChar >= 0x61 && firstChar <= 0x79) { //Если начинается с A-Z или a-z
            switch (resultString) {
                case SIM800.OK:
                    lastReceivedCommandList.add(SIM800.OK);
                    break;
                case SIM800.READY:
                    /*TODO - уведомление о готовности работы, появляется только после выключения автоопределения скорости (BaudRate), после включения питания*/
                    listener.debugMessage("READY");
                    break;
                case SIM800.PWRDWN:
                    /*TODO - уведолмение о выключении устройства (после замыкания соотв. контактов или через AT команду AT+CPOWD=1) */
                    listener.debugMessage("POWER DOWN");
                    break;
                case SIM800.UNDER_VOLTAGE_PWD:
                    /*TODO - выключение модуля, низкое напряжение*/
                    listener.debugMessage("UNDER VOLTAGE POWER DOWN!");
                    break;
                case SIM800.UNDER_VOLTAGE_WARN:
                    /*TODO - предупреждение о низком напряжении*/
                    listener.debugMessage("UNDER VOLTAGE WARNING!");
                    break;
                case SIM800.OVER_VOLTAGE_PWD:
                    /*TODO - выключение модуля, высокое напряжение*/
                    listener.debugMessage("OVER VOLTAGE POWER DOWN");
                    break;
                case SIM800.OVER_VOLTAGE_WARN:
                    /*TODO - предупреждение о высоком напряжении*/
                    listener.debugMessage("OVER VOLTAGE WARNING");
                    break;
            }
        } else {
            listener.debugMessage("Неизвестное незапрашевоемое уведомление от модуля");
        }
    }

    /*TODO - метод запроса баланса*/
    public void getBalance() {
        //sendMessage();
    }

    /*TODO - метод запроса оператора*/
    public void operator() {
        sendMessage("AT", SIM800.OPERATOR + '?');
    }


    private String getSubString(String incomingString, String separator){
        int endIndex = incomingString.indexOf(separator);
        if (endIndex == -1) {
            listener.debugMessage("Ошибка декодирования сообщения от модуля (отсутствует ':' )");
            throw new IllegalArgumentException("Incorrect input string");
        }
        return incomingString.substring(0, endIndex);
    }

    private String getSubString(String incomingString, String separatorStart, String separatorEnd){
        int indexStart = incomingString.indexOf(separatorStart);
        int indexEnd = incomingString.indexOf(separatorEnd);
        if (indexStart == -1 || indexEnd == -1) {
            listener.debugMessage("Ошибка получения номера из сообщения!");
            throw new IllegalArgumentException("Incorrect input string");
        }
        return incomingString.substring(indexStart + separatorStart.length(), indexEnd);
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
            decodeMessage(msg);

        } catch (IOException e) {
            e.printStackTrace();
            listener.onException(e);
        }
    }

    //Класс проверки ответа на отправленную команду
    class ResponseKeeper extends Thread {
        String command;

        ResponseKeeper(String command) {
            this.command = command;
            listener.debugMessage("ResponseKeeper command: " + command);
        }

        @Override
        public void run() {
            long timer = System.currentTimeMillis();
            while (!isInterrupted()) {
                if (lastReceivedCommandList.contains(command)) {
                    closeRequest(command);
                } else if (lastReceivedCommandList.contains(SIM800.OK)){
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

        private void closeRequest(String command){
            listener.debugMessage("Ответ на команду: " + command);
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
                    listener.debugMessage("Очередь отправки модулю пуста");
                    interrupt();
                } else if (availableToSendCommand) {
                    listener.debugMessage("Отправка команды из очереди...");
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

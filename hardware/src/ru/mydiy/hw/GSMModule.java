/*Класс имплементирующий взаимодействие с GSM-модулем*/
package ru.mydiy.hw;

import com.pi4j.io.serial.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GSMModule implements SerialDataEventListener {
    private final GSMListener listener;
    private final Serial serial;
    private final String UART = "/dev/ttyS1";
    private HashMap<String, String> executedCommandList;
    private boolean availableToSendCommand = false;

    private MessageKeeper msKeeper;
    private ResponceKeeper rsKeeper;

    public GSMModule(GSMListener listener) {
        this.listener = listener;
        executedCommandList = new HashMap<>();
        //Установление связи с модулем, добавление слушателя на приём данных
        serial = SerialFactory.createInstance();
        serial.addListener(this);
        try {
            serial.open(UART, Baud._9600, DataBits._8, Parity.NONE, StopBits._1, FlowControl.NONE);
            listener.onModuleStarted("GSM module started");
        } catch (IOException e) {
            listener.onException(e);
        }
    }

    private synchronized void setAvailable(boolean available) {
        availableToSendCommand = available;
    }

    //Отправка сообщения модулю
    public synchronized void sendMessage(String msg) {
        if (availableToSendCommand) {
            try {
                availableToSendCommand = false;
                serial.writeln(msg);
                listener.onSendMessage(msg);
            } catch (IOException e) {
                listener.onException(e);
            }
        } else {
            if (msKeeper.isAlive()) {
                msKeeper.addCommand(msg);
            } else {
                msKeeper = new MessageKeeper();
                msKeeper.addCommand(msg);
                msKeeper.start();
            }
        }

    }

    /*TODO - метод для дешифровки сообщения от GSM-модуля и вычленения главного из сообщения*/
    private synchronized void decodeMessage(String message) {
        //Получение строки без символов <CR><LF> (перве два символа в начале и конце сообщения)
        String resultString = message.substring(2, message.length() - 2);

        char firstChar = resultString.charAt(0);

        if (firstChar == 0x2b) { //Если ответ начинается с символа '+'

            int endIndex = resultString.indexOf(':');
            if (endIndex == -1) {
                listener.debugMessage("Ошибка декодирования сообщения от модуля (отсутствует ':' )");
                return;
            }

            String command = resultString.substring(0, endIndex);
            switch (command) {
                case SIM800.CALL:
                    /*TODO - исходящий(?) звонок*/
                    int numberIndexStart = resultString.indexOf(",\"+") + 2;
                    if (numberIndexStart == -1) {
                        listener.debugMessage("Ошибка получения номера из входящего звонка");
                        return;
                    }
                    int numberIndexEnd = numberIndexStart + 3;
                    for (int i = numberIndexStart + 3; i < resultString.length(); i++) {
                        if (!Character.isDigit(resultString.charAt(i))) {
                            numberIndexEnd = i;
                            break;
                        }
                    }

                    String number = resultString.substring(numberIndexStart, numberIndexEnd);
                    listener.onIncomingCall(number);
                    break;
                case SIM800.ERROR:
                    /*TODO - ошибка от модуля*/
                    break;
                case SIM800.USSD:
                    /*TODO - ответ на USSD запрос*/
                    break;
                    /*TODO - другие уведомления?*/
            }

        } else if (firstChar >= 0x41 && firstChar <= 0x5a || firstChar >= 0x61 && firstChar <= 0x79) { //Если начинается с A-Z или a-z
            switch (resultString) {
                case SIM800.READY:
                    /*TODO - уведомление о готовности работы, появляется только после выключения автоопределения скорости (BaudRate), после включения питания*/
                case SIM800.PWRDWN:
                    /*TODO - уведолмение о выключении устройства (после замыкания соотв. контактов или через AT команду AT+CPOWD=1) */
                case SIM800.INCOMING_CALL:
                    /*TODO - уведомление о входящем звонке*/
                    break;
                case SIM800.UNDER_VOLTAGE_PWD:
                    /*TODO - выключение модуля, низкое напряжение*/
                    break;
                case SIM800.UNDER_VOLTAGE_WARN:
                    /*TODO - предупреждение о низком напряжении*/
                    break;
                case SIM800.OVER_VOLTAGE_PWD:
                    /*TODO - выключение модуля, высокое напряжение*/
                    break;
                case SIM800.OVER_VOLTAGE_WARN:
                    /*TODO - предупреждение о высоком напряжении*/
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
    public void getOperator() {

    }

    /*TODO - метод звонка*/
    public void call(String number) {
        sendMessage(SIM800.OUTCOMING_CALL + number + ";");
    }

    //Слушатель на приём сообщений от модуля
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

    /*TODO класс, следящий за получением ответа от железа, о статусе исполнения команды*/
    class ResponceKeeper extends Thread {
        String command;

        ResponceKeeper(String command) {
            this.command = command;
            start();
        }

        @Override
        public void run() {
            Long timer = System.currentTimeMillis();
            while (System.currentTimeMillis() - timer < 3000) {
                if (executedCommandList.containsKey(command)) {
                    executedCommandList.remove(command);

                    //command.equals();
                }
            }
        }
    }

        /*TODO класс, управляющий очередью отправки сообщений железу*/
        class MessageKeeper extends Thread {
            ArrayList<String> commands;

            MessageKeeper() {
                commands = new ArrayList<>();
            }

            @Override
            public void run() {
                while (!isInterrupted()) {
                    if (commands.isEmpty()) {
                        interrupt();
                    } else if (availableToSendCommand) {
                        sendMessage(commands.get(0));
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

            public void addCommand(String command) {
                this.commands.add(command);
            }
        }
    }

package ru.mydiy.hw;

public interface GSMListener {
    void onModuleStarted(GSMModule module);
    void onReceivedMessage(GSMModule module, String msg);
    void onOutcomingCallDelivered(String number);
    void onOutcomingCallFailed(String number);
    void smsSended();
    void smsSendedError();
    void onSendMessage(String msg);
    void currentBalance(float balance);
    void operatorNameReceived(String operator);
    void onIncomingCall(String number);
    void onException(Exception e);
    void debugMessage(String message);
}

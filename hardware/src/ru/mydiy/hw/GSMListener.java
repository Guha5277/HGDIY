package ru.mydiy.hw;

public interface GSMListener {
    void onModuleStarted(GSMModule module);
    void onModuleFailedToStart(GSMModule module);

    void onReceivedMessage(GSMModule module, String msg);
    void onSendMessage(String msg);

    void onIncomingCall(String number);
    void onOutcomingCallDelivered(String number);
    void onOutcomingCallFailed(String number);
    void smsSent();
    void smsSentError();
    void currentBalance(float balance);
    void operatorNameReceived(String operator);

    void onException(Exception e);
    void debugMessage(String message);
}

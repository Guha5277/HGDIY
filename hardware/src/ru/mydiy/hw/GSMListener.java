package ru.mydiy.hw;

public interface GSMListener {
    void onModuleStarted(String msg);
    void onReceivedMessage(GSMModule module, String msg);
    void onSendMessage(String msg);
    void onIncomingCall(String number);
    void onException(Exception e);
    void debugMessage(String message);
}

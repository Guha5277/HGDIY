package ru.mydiy.hw;

public interface GSMListener {
    void onModuleStarted(GSMModule module);
    void onReceivedMessage(GSMModule module, String msg);
    void onOutcomingCallDelivered(String number);
    void onOutcomingCallFailed(String number);
    void onSendMessage(String msg);
    void onIncomingCall(String number);
    void onException(Exception e);
    void debugMessage(String message);
}

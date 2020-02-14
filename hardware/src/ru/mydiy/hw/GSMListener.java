package ru.mydiy.hw;

public interface GSMListener {
    void onModuleStarted(String msg);
    void onSendMessage(String msg);
    void onReceivedMessage(GSMModule module, String msg);
    void onException(GSMModule module, Exception e);
    void debugMessage(String message);
}

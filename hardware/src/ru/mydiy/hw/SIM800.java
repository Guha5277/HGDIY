package ru.mydiy.hw;

public class SIM800 {
    //Notifications
    static final String OPERATOR = "+COPS";
    static final String CALL_CONNECTED = "+COLP";
    static final String ERROR = "+CME ERROR";
    static final String CALL = "+CLCC";
    static final String READY = "RDY";
    static final String PWRDWN = "NORMAL POWER DOWN";
    static final String INCOMING_CALL = "RING";
    static final String UNDER_VOLTAGE_WARN = "UNDER-VOLTAGE WARNNING";
    static final String UNDER_VOLTAGE_PWD = "UNDER-VOLTAGE POWER DOWN";
    static final String OVER_VOLTAGE_WARN = "OVER-VOLTAGE WARNNING";
    static final String OVER_VOLTAGE_PWD = "OVER-VOLTAGE POWER DOWN";

    //Commands
    static final String CALL_TO = "ATD";
    static final String USSD = "+CUSD";
    static final String DISCARD_CALL = "ATH";

    //Submessages (comes with main message)
    static final String OK = "OK";
    static final String BUSY = "BUSY";
    static final String NO_CARRIER = "NO CARRIER";
    static final String NO_ANSWER = "NO ANSWER";

    //Separators
    static final String COMMAND_SEPARATOR = ":";
    static final String NUMBER_BEGIN_SEPARATOR = ",\"";
    static final String NUMBER_END_SEPARATOR = "\",";
    static final String OPERATOR_BEGIN_SEPARATOR = ",\"";
    static final String OPERATOR_END_SEPARATOR = "\"\n";
}

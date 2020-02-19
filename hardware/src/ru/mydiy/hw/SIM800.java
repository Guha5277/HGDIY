package ru.mydiy.hw;

public class SIM800 {
    //Notifications
    public static final String ERROR = "+CME ERROR";
    public static final String CALL = "+CLCC";
    public static final String READY = "RDY";
    public static final String PWRDWN = "NORMAL POWER DOWN";
    public static final String INCOMING_CALL = "RING";
    public static final String UNDER_VOLTAGE_WARN = "UNDER-VOLTAGE WARNNING";
    public static final String UNDER_VOLTAGE_PWD = "UNDER-VOLTAGE POWER DOWN";
    public static final String OVER_VOLTAGE_WARN = "OVER-VOLTAGE WARNNING";
    public static final String OVER_VOLTAGE_PWD = "OVER-VOLTAGE POWER DOWN";

    //Commands
    public static final String CALL_TO = "ATD";
    public static final String USSD = "+CUSD";
    public static final String DISCARD_CALL = "ATH";

    //Other
    public static final String BUSY = "BUSY";
    public static final String NO_CARRIER = "NO CARRIER";
    public static final String NO_ANSWER = "NO ANSWER";
    public static final String OK = "OK";
}

package ru.mydiy.hw;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;

public class MotionSensor implements GpioPinListenerDigital {
    private final MotionSensorListener listener;
    private final GpioController gpio;
    private final GpioPinDigitalInput inputPin;
    private final String name;
    private final boolean HIGH = true;
    private final boolean LOW = false;
    private static final Pin[] orangePins = {OrangePiPin.GPIO_00, OrangePiPin.GPIO_01, OrangePiPin.GPIO_02, OrangePiPin.GPIO_03, OrangePiPin.GPIO_04};
    private TimeChecker timeChecker;
    private long timeCounter = 0;
    private long invasionSummaryTime = 0;
    private long timeOfLastActivity = 0;

    public MotionSensor(MotionSensorListener listener, String name, int gpioNumber){
        this.name = name;
        this.listener = listener;
        //Инициализация платформы, настройка GPIO
        try {
            PlatformManager.setPlatform(Platform.ORANGEPI);
        } catch (PlatformAlreadyAssignedException e){
            listener.onException(this, e);
        }
        gpio = GpioFactory.getInstance();
        inputPin = gpio.provisionDigitalInputPin(orangePins[gpioNumber], name, PinPullResistance.PULL_DOWN);
        inputPin.addListener(this);
        inputPin.setShutdownOptions(true);
    }

    public String getName(){
        return inputPin.getName();
    }

    private MotionSensor getMotionSensor(){
        return this;
    }

    private synchronized void updateCounterTime(){

    }

    //Обработка событий изменения состояния GPIO
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
        PinState state = gpioPinDigitalStateChangeEvent.getState();
        if (state == PinState.HIGH){
            timeCounter = System.currentTimeMillis();  //Засекаем текущее время
            timeOfLastActivity = timeCounter;          //Записываем это время
            listener.motionState(this, HIGH);
        } else {
            timeCounter = System.currentTimeMillis() - timeCounter;
            invasionSummaryTime += timeCounter;
            listener.motionState(this, LOW);

            if(timeChecker == null || !timeChecker.isAlive()){
                timeChecker = new TimeChecker();
                timeChecker.start();
            }
        }
    }

    //Анализирование времени срабатывания, для оценки уровня опасности - есть она или уже нет
    private class TimeChecker extends Thread{
        @Override
        public void run() {
            listener.debugMessage(getMotionSensor(), "THREAD TimeChecker started!");
            while (!isInterrupted()) {
                if (inputPin.isLow() && (System.currentTimeMillis() - timeOfLastActivity) > 30000) {
                        listener.activityIsGone(getMotionSensor(), invasionSummaryTime);
                        invasionSummaryTime = 0;
                        interrupt();
                } else {
                    try {
                        listener.debugMessage(getMotionSensor(), "TimeChecker: ACTIVITY IS NOT GONE! OVERALL TIME IS: " + invasionSummaryTime  + ", System time is: " + System.currentTimeMillis());
                        sleep(15000);
                    } catch (InterruptedException e) {
                        listener.onException(getMotionSensor(), e);
                    }
                }
            }
        }
    }
}

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
    private long activityTime;
    private long activitySum;
    private long timeOfLastActivity;

    public MotionSensor(MotionSensorListener listener, String name, int gpioNumber){
        this.name = name;
        this.listener = listener;
        //Инициализация платформы, настройка GPIO
        try {
            PlatformManager.setPlatform(Platform.ORANGEPI);
        } catch (PlatformAlreadyAssignedException e){
            listener.onSensorException(this, e);
        }
        gpio = GpioFactory.getInstance();
        inputPin = gpio.provisionDigitalInputPin(orangePins[gpioNumber], name, PinPullResistance.PULL_DOWN);
        inputPin.addListener(this);
        inputPin.setShutdownOptions(true);
    }

    public String getName(){
        return inputPin.getName();
    }

    private Long getLastActivityTime(){
        return System.currentTimeMillis() - timeOfLastActivity;
    }

    private MotionSensor getMotionSensor(){
        return this;
    }

    private Long getActivitySum(){
        return activitySum;
    }

    //Обработка событий изменения состояния GPIO
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
        PinState state = gpioPinDigitalStateChangeEvent.getState();
        if (state == PinState.HIGH){
            activityTime = System.currentTimeMillis();
            timeOfLastActivity = activityTime;
            listener.motionState(this, HIGH);
        } else {
            activityTime = System.currentTimeMillis() - activityTime;
            activitySum += activityTime;
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
                if (inputPin.isLow() && getLastActivityTime() > 35000) {
                        listener.debugMessage(getMotionSensor(), "TimeChecker: ACTIVITY GONE!");
                        listener.activityIsGone(getMotionSensor(), (int) (getActivitySum() / 1000));
                        interrupt();
                } else {
                    try {
                        listener.debugMessage(getMotionSensor(), "TimeChecker: ACTIVITY IS NOT GONE! OVERALL TIME IS: " + (getActivitySum() / 1000) + ", System time is: " + (System.currentTimeMillis() / 1000));
                        sleep(15000);
                    } catch (InterruptedException e) {
                        listener.onSensorException(getMotionSensor(), e);
                    }
                }
            }
        }
    }
}

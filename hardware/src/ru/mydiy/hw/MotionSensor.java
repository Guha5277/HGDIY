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
    private final int GPIO_00 = 0;
    private final int GPIO_01 = 1;
    private static final Pin[] orangePins = {OrangePiPin.GPIO_01, OrangePiPin.GPIO_00};
    private TimeChecker timeChecker;
    private long activityTime;
    private long activitySum;
    private long timeOfLastActivity;

    public MotionSensor(MotionSensorListener listener, String name){
        try {
            PlatformManager.setPlatform(Platform.ORANGEPI);
        } catch (PlatformAlreadyAssignedException e){

        }
        gpio = GpioFactory.getInstance();
        this.name = name;
        this.listener = listener;
        inputPin = gpio.provisionDigitalInputPin(orangePins[GPIO_01], name, PinPullResistance.PULL_DOWN);
        inputPin.addListener(this);
        inputPin.setShutdownOptions(true);
    }

    public void activate(){
        inputPin.addListener(this);
    }

    public void deactivate(){
        inputPin.removeAllListeners();
    }

    public String getName(){
        return inputPin.getName();
    }

    private Long getLastActivityTime(){
        return timeOfLastActivity;
    }


    private MotionSensor getMotionSensor(){
        return this;
    }

    private Long getActivitySum(){
        return activitySum;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
        PinState state = gpioPinDigitalStateChangeEvent.getState();
        if (state == PinState.HIGH){
            activityTime = System.currentTimeMillis();
            timeOfLastActivity = activityTime;
            listener.motionState(this, HIGH);
            if(timeChecker == null || !timeChecker.isAlive()){
                timeChecker = new TimeChecker();
                timeChecker.start();
            }
        } else {
            listener.motionState(this, LOW);
            activityTime = System.currentTimeMillis() - activityTime;
            activitySum += activityTime;
        }
    }

    private class TimeChecker extends Thread{


        @Override
        public void run() {
            listener.debugMessage(getMotionSensor(), "THREAD TimeChecker started!");
            while (!isInterrupted()) {
                if (getLastActivityTime() > 30000) {
                        listener.debugMessage(getMotionSensor(), "TimeChecker: ACTIVITY GONE!");
                        listener.activityIsGone(getMotionSensor(), (int) (getActivitySum() / 1000));
                        interrupt();
                } else {
                    try {
                        listener.debugMessage(getMotionSensor(), "TimeChecker: ACTIVITY IS NOT GONE! OVERALL TIME IS: " + getActivitySum() + ", System time is: " + System.currentTimeMillis());
                        sleep(5000);
                    } catch (InterruptedException e) {
                        listener.onSensorException(getMotionSensor(), e);
                    }
                }
            }
        }
    }
}

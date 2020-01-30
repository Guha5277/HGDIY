package ru.mydiy.hw;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;

public class MotionSensor implements GpioPinListenerDigital {
    private final MotionSensorListener listener;
    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalInput inputPin;
    /*TODO
        Проанализировать работу датчика и количество срабатываний. Определить, как устанавливать угрозу и нужна ли градация
        по уровням.
     */
//    private int warningLevel = 0;
//    private static final int HIGH = 1000;
//    private static final int LOW = 2000;
//    private final int sensitivity;
//    private long currentTime = System.currentTimeMillis();

    public MotionSensor(MotionSensorListener listener, Pin pin){

        try {
            PlatformManager.setPlatform(Platform.RASPBERRYPI);
        } catch (PlatformAlreadyAssignedException e){
            listener.onSensorException(this, e);
        }
        this.listener = listener;
        Pin pin2 = CommandArgumentParser.getPin(OrangePiPin.class, pin);
        inputPin = gpio.provisionDigitalInputPin(pin2);
        inputPin.setShutdownOptions(true);
    }

    public void activate(){
        inputPin.addListener(this);
    }

    public void deactivate(){
        inputPin.removeAllListeners();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
        PinState state = gpioPinDigitalStateChangeEvent.getState();
        if (state == PinState.HIGH){
            listener.onMotionDetection(this);
        }
    }
}

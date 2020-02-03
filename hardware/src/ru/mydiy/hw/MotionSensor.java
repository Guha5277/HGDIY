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
    private final GpioController gpio;
    private final GpioPinDigitalInput inputPin;
    private final boolean HIGH = true;
    private final boolean LOW = false;
    private static final Pin[] orangePins = {OrangePiPin.GPIO_01, OrangePiPin.GPIO_00};

    /*TODO
        Проанализировать работу датчика и количество срабатываний. Определить, как устанавливать угрозу и нужна ли градация
        по уровням.
     */

//    static {
//        try {
//            PlatformManager.setPlatform(Platform.ORANGEPI);
//        } catch (PlatformAlreadyAssignedException e){
//
//        }
//    }
    public MotionSensor(MotionSensorListener listener){
        try {
            PlatformManager.setPlatform(Platform.ORANGEPI);
        } catch (PlatformAlreadyAssignedException e){

        }
        gpio = GpioFactory.getInstance();
        this.listener = listener;
        inputPin = gpio.provisionDigitalInputPin(orangePins[0], PinPullResistance.PULL_DOWN);
        //Pin pin = CommandArgumentParser.getPin(OrangePiPin.class, orangePins[0]);
        //inputPin = gpio.provisionDigitalInputPin(pin);
        inputPin.addListener(this);
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
            listener.motionState(this, HIGH);
        } else {
            listener.motionState(this, LOW);
        }
    }
}

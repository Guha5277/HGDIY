package ru.mydiy.hw;

public interface MotionSensorListener {
    void motionState(MotionSensor monitor, boolean state);
    void onSensorException(MotionSensor monitor, Exception e);
}

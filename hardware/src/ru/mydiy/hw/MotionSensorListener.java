package ru.mydiy.hw;

public interface MotionSensorListener {
    void onMotionDetection(MotionSensor monitor);
    void onSensorException(MotionSensor monitor, Exception e);
}

package ru.mydiy.hw;

public interface MotionSensorListener {
    void motionState(MotionSensor monitor, boolean state);
    void onException(MotionSensor monitor, Exception e);
    void activityIsGone(MotionSensor monitor, int overallTime);
    void debugMessage(MotionSensor monitor, String msg);
}

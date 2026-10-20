package com.qualcomm.robotcore.hardware;

public class TouchSensorImpl implements TouchSensor{
    volatile boolean pressed = false;

    @Override
    public double getValue() {
        return pressed? 1.0 : 0.0;
    }

    @Override
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Internal use only. Update touch sensor state.
     * @param pressed
     */
    public void update(boolean pressed){
        this.pressed = pressed;
    }
}

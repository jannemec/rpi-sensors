/*
 * Rain sensor module
 */
package com.jannemec.sensors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import com.jannemec.tools.ActionListener;

public class Button extends Sensor {
    
    protected Pin GPIOSensor;
    
    public Button(com.jannemec.tools.Cache cache, Pin GPIOSensor) {
        super(cache);
        this.setGPIOSensor(GPIOSensor);
        this.setSensorName("Button");
        this.setValueList(new ArrayList<>(Arrays.asList("isOn")));
    }
    
    public Button(com.jannemec.tools.Cache cache) {
        this(cache, RaspiPin.GPIO_23);
    }
    
    @Override
    public int getIValue(String name) throws IllegalArgumentException, InterruptedException {
        if (this.getInteruptMode()) {
            return(this.isLastStatus() ? 0 : 1);
        } else {
            if (this.getValueList().contains(name)) {
                // Check if the value is in cache
                if (!this.getCache().hasIValue(this.getCacheCode(name))) {
                    GpioController gpio = GpioFactory.getInstance();
                    GpioPinDigitalInput sensorGPIOSensor = gpio.provisionDigitalInputPin(this.getGPIOSensor(),PinPullResistance.PULL_DOWN); // GPIOSensor pin as INPUT
                    int isMovement = sensorGPIOSensor.isHigh() ? 0 : 1;
                    //gpio.shutdown();
                    gpio.unprovisionPin(sensorGPIOSensor);
                    this.getCache().setIValue(this.getCacheCode("isOn"), isMovement);
                }
                return(this.getCache().getIValue(this.getCacheCode(name)));
            } else {
                throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
            }
        }
    }
    
    protected boolean lastStatus = false;

    public boolean isLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(boolean lastStatus) {
        this.lastStatus = lastStatus;
    }
    protected boolean interuptMode = false;
    protected GpioPinDigitalInput myButton = null;
    protected Date lastChangeDate = null;
    protected Sensor mySelf = null;
    
    public Date getLastChangeDate() {
        return(this.lastChangeDate);
    }
    
    protected void setLastChangeDate() {
        this.setLastChangeDate(new Date());
    }
    
    protected void setLastChangeDate(Date dt) {
        this.lastChangeDate = dt;
    }
    
    public void setInteruptMode(boolean interupMode) {
        this.setLastChangeDate(null);
        if (interupMode) {
            // set listener
            if (this.myButton == null) {
                // create gpio controller
                final GpioController gpio = GpioFactory.getInstance();

                // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
                this.myButton = gpio.provisionDigitalInputPin(this.getGPIOSensor(), PinPullResistance.PULL_DOWN);

                // set shutdown state for this input pin
                this.myButton.setShutdownOptions(true);
            }
            this.setLastStatus(this.myButton.getState().isHigh());
            this.mySelf = this;
            // create and register gpio pin listener
            myButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    // display pin state on console
                    if (event.getState().isHigh()) {
                        setLastChangeDate();
                    }
                    setLastStatus(event.getState().isHigh());
                    if (!(actionListener == null)) {
                        actionListener.handleAction(mySelf);
                    }
                }
            });
        } else {
            // remove litener
            if (!(this.myButton == null)) {
                this.myButton.removeAllListeners();
            }
        }
        this.interuptMode = interupMode;
    }
    
    private ActionListener actionListener;
     
    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
    
    public void clearActionListener() {
        this.actionListener = null;
    }
    
    public boolean getInteruptMode() {
        return(this.interuptMode);
    }
    
    public boolean isOn() throws Exception {
        return(this.getIValue("isOn") == 1);
    }
    
    public int getIsOn() throws Exception {
        return(this.getIValue("isOn"));
    }
    
    @Override
    public String getSValue(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getDValue(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the GPIOSensor
     */
    public Pin getGPIOSensor() {
        return this.GPIOSensor;
    }

    /**
     * @param GPIOSensor the GPIOSensor to set
     */
    public void setGPIOSensor(Pin GPIOSensor) {
        this.GPIOSensor = GPIOSensor;
    }
}

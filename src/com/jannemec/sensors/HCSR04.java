/*
 * Distance sensor module
 */
package com.jannemec.sensors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import java.util.ArrayList;
import java.util.Arrays;

public class HCSR04 extends Sensor {
    
    protected Pin GPIOTrig;
    protected Pin GPIOEcho;
    
    public HCSR04(com.jannemec.tools.Cache cache, Pin GPIOTrig, Pin GPIOEcho) {
        super(cache);
        this.setGPIOTrig(GPIOTrig);
        this.setGPIOEcho(GPIOEcho);
        this.setSensorName("HCSR04");
        this.setValueList(new ArrayList<>(Arrays.asList("distance")));
    }
    
    public HCSR04(com.jannemec.tools.Cache cache) {
        this(cache, RaspiPin.GPIO_28, RaspiPin.GPIO_29);
    }
    
    @Override
    public double getDValue(String name) throws IllegalArgumentException, InterruptedException {
        if (this.getValueList().contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasDValue(this.getCacheCode(name))) {
                // Create I2C bus
                GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalOutput sensorTriggerPin =  gpio.provisionDigitalOutputPin(this.getGPIOTrig()); // Trigger pin as OUTPUT
		GpioPinDigitalInput sensorEchoPin = gpio.provisionDigitalInputPin(this.getGPIOEcho(),PinPullResistance.PULL_UP); // Echo pin as INPUT
                sensorTriggerPin.high(); // Make trigger pin HIGH
                Thread.sleep((long) 1);// Delay for 10 microseconds
                sensorTriggerPin.low(); //Make trigger pin LOW
                
                while(sensorEchoPin.isLow()){ //Wait until the ECHO pin gets HIGH
                }
                long startTime= System.nanoTime(); // Store the surrent time to calculate ECHO pin HIGH time.
                while(sensorEchoPin.isHigh()){ //Wait until the ECHO pin gets LOW
                }
                long endTime= System.nanoTime(); // Store the echo pin HIGH end time to calculate ECHO pin HIGH time.

                double distance = ((((double) (endTime-startTime))/1000000000 * 343)/2);
                /*gpio.shutdown();*/
                gpio.unprovisionPin(sensorTriggerPin);
                gpio.unprovisionPin(sensorEchoPin);
                this.getCache().setDValue(this.getCacheCode("distance"), distance);
            }
            return(this.getCache().getDValue(this.getCacheCode(name)));
        } else {
            throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
        }
    }
    
    public double getDistance() throws Exception {
        return(this.getDValue("distance"));
    }
    
    @Override
    public String getSValue(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIValue(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the GPIOTrig
     */
    public Pin getGPIOTrig() {
        return this.GPIOTrig;
    }

    /**
     * @param GPIOTrig the GPIOTrig to set
     */
    public void setGPIOTrig(Pin GPIOTrig) {
        this.GPIOTrig = GPIOTrig;
    }

    /**
     * @return the GPIOEcho
     */
    public Pin getGPIOEcho() {
        return this.GPIOEcho;
    }

    /**
     * @param GPIOEcho the GPIOEcho to set
     */
    public void setGPIOEcho(Pin GPIOEcho) {
        this.GPIOEcho = GPIOEcho;
    }
}

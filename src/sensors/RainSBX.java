/*
 * Rain sensor module
 */
package sensors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import java.util.ArrayList;
import java.util.Arrays;

public class RainSBX extends Sensor {
    
    protected Pin GPIOSensor;
    
    public RainSBX(tools.Cache cache, Pin GPIOSensor) {
        super(cache);
        this.setGPIOSensor(GPIOSensor);
        this.setSensorName("RainSBX");
        this.setValueList(new ArrayList<>(Arrays.asList("isRain")));
    }
    
    public RainSBX(tools.Cache cache) {
        this(cache, RaspiPin.GPIO_27);
    }
    
    @Override
    public int getIValue(String name) throws IllegalArgumentException, InterruptedException {
        if (this.getValueList().contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasIValue(this.getCacheCode(name))) {
                // Create I2C bus
                GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalInput sensorGPIOSensor = gpio.provisionDigitalInputPin(this.getGPIOSensor(),PinPullResistance.PULL_UP); // GPIOSensor pin as INPUT
                int isRain = sensorGPIOSensor.isHigh() ? 0 : 1;
                gpio.shutdown();
                gpio.unprovisionPin(sensorGPIOSensor);
                this.getCache().setIValue(this.getCacheCode("isRain"), isRain);
            }
            return(this.getCache().getIValue(this.getCacheCode(name)));
        } else {
            throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
        }
    }
    
    public boolean isRain() throws Exception {
        return(this.getIValue("isRain") == 1);
    }
    
    public int getIsRain() throws Exception {
        return(this.getIValue("isRain"));
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

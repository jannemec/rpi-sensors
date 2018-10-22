/*
 * Light sensor module
 */
package com.jannemec.sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.util.ArrayList;
import java.util.Arrays;

public class TSL2561 extends Sensor {
    
    public TSL2561(com.jannemec.tools.Cache cache, int address) {
        super(cache);
        this.setAddress(address);
        this.setSensorName("TSL2561");
        this.setValueList(new ArrayList<>(Arrays.asList("infrared", "visible", "full")));
    }
    
    public TSL2561(com.jannemec.tools.Cache cache) {
        this(cache, 0x39);
    }
    
    @Override
    public double getDValue(String name) throws Exception {
        if (this.getValueList().contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasDValue(this.getCacheCode(name))) {
                // Create I2C bus
		I2CBus Bus = I2CFactory.getInstance(I2CBus.BUS_1);
		// Get I2C device
		I2CDevice device = Bus.getDevice(this.getAddress());

		// Select control register
		// Power ON mode
		device.write(0x00 | 0x80, (byte)0x03);
		// Select timing register
		// Nominal integration time = 402ms
		device.write(0x01 | 0x80, (byte)0x02);
		Thread.sleep(500);

		// Read 4 bytes of data
		// ch0 lsb, ch0 msb, ch1 lsb, ch1 msb
		byte[] data=new byte[4];
		device.read(0x0C | 0x80, data, 0, 4);

		// Convert the data
		double ch0 = ((data[1] & 0xFF)* 256 + (data[0] & 0xFF));
		double ch1 = ((data[3] & 0xFF)* 256 + (data[2] & 0xFF));
                
                // Store it to the cache and return
                this.getCache().setDValue(this.getCacheCode("full"), ch0);
                this.getCache().setDValue(this.getCacheCode("infrared"), ch1);
                this.getCache().setDValue(this.getCacheCode("visible"), (ch0 - ch1));
                
                // Output data to screen
		//System.out.printf("Full Spectrum(IR + Visible) : %.2f lux %n", ch0);
		//System.out.printf("Infrared Value : %.2f lux %n", ch1);
		//System.out.printf("Visible Value : %.2f lux %n", (ch0 - ch1));
            }
            return(this.getCache().getDValue(this.getCacheCode(name)));
        } else {
            throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
        }
    }
    
    public double getInfrared() throws Exception {
        return(this.getDValue("infrared"));
    }
    
    public double getVisible() throws Exception {
        return(this.getDValue("visible"));
    }
    
    public double getFull() throws Exception {
        return(this.getDValue("full"));
    }

    @Override
    public String getSValue(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIValue(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

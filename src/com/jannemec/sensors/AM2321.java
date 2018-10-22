/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AM2321 extends Sensor {
    
    public AM2321(com.jannemec.tools.Cache cache, int address) {
        super(cache);
        this.setAddress(address);
        this.setSensorName("AM2321");
        this.setValueList(new ArrayList<>(Arrays.asList("temperature", "humidity")));
    }
    
    public AM2321(com.jannemec.tools.Cache cache) {
        this(cache, 0x5c);
    }
    
    @Override
    public double getDValue(String name) throws Exception {
        if (this.valueList.contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasDValue(this.getCacheCode(name))) {
                byte[] wb = new byte[] {(byte)0x03,(byte)0x00,(byte)0x04};
                byte[] rb = new byte[8];
                double hum = 0;
                double temp = 0;
                try {
                    I2CBus bus=I2CFactory.getInstance(I2CBus.BUS_1);
                    I2CDevice dev = bus.getDevice(this.getAddress());
                    // wecken
                    try{
                        dev.read();
                    } catch (IOException e) {
                        Thread.sleep(100);
                    }
                    // messen
                    dev.read(wb, 0, 3, rb, 0, 8);
                    //int crc = ((rb[6] & 0xff ) << 8) | rb[7] & 0xff;
                    hum = (((rb[2] & 0xff ) << 8) | rb[3] & 0xff);
                    temp = (((rb[4] & 0xff) << 8) | rb[5] & 0xff);
                    hum = hum / 10;
                    temp = temp / 10;
                } catch (I2CFactory.UnsupportedBusNumberException | IOException | InterruptedException e) {
                    System.out.println(e.toString());
                }
                
                // Store it to the cache and return
                this.getCache().setDValue(this.getCacheCode("temperature"), temp);
                this.getCache().setDValue(this.getCacheCode("humidity"), hum);
                
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
    
    public double getTemperature() throws Exception {
        return(this.getDValue("temperature"));
    }
    
    public double getHumidity() throws Exception {
        return(this.getDValue("humidity"));
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
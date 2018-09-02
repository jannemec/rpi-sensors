/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BMP180 extends Sensor {
    
    public BMP180(tools.Cache cache, int address) {
        super(cache);
        this.setAddress(address);
        this.setSensorName("BMP180");
        this.setValueList(new ArrayList<>(Arrays.asList("temperature", "altitude", "pressure")));
    }
    
    public BMP180(tools.Cache cache) {
        this(cache, 0x77);
    }

    
    /**
     *
     * @param name
     * @return
     * @throws UnsupportedBusNumberException
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public double getDValue(String name) throws Exception {
        if (this.valueList.contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasDValue(this.getCacheCode(name))) {
                // We have to read the value
                // Create I2C bus
		I2CBus bus;
                bus = I2CFactory.getInstance(I2CBus.BUS_1);
		// Get I2C device, BMP180 I2C address is 0x77(119)
		I2CDevice device = bus.getDevice(this.getAddress());

		// Calibration Cofficients stored in EEPROM of the device
		// Read 22 bytes of data from address 0xAA(170)
		byte[] data = new byte[22];
		device.read(0xAA, data, 0, 22);

		// Convert the data
		int AC1 = data[0] * 256 + data[1];
		int AC2 = data[2] * 256 + data[3];
		int AC3 = data[4] * 256 + data[5];
		int AC4 = ((data[6] & 0xFF) * 256) + (data[7] & 0xFF);
		int AC5 = ((data[8] & 0xFF) * 256) + (data[9] & 0xFF);
		int AC6 = ((data[10] & 0xFF) * 256) + (data[11] & 0xFF);
		int B1 = data[12] * 256 + data[13];
		int B2 = data[14] * 256 + data[15];
		int MB = data[16] * 256 + data[17];
		int MC = data[18] * 256 + data[19];
		int MD = data[20] * 256 + data[21];
		Thread.sleep(500);
		
		// Select measurement control register
		// Enable temperature measurement
		device.write(0xF4, (byte)0x2E);
		Thread.sleep(100);
		
		// Read 2 bytes of data from address 0xF6(246)
		// temp msb, temp lsb
		device.read(0xF6, data, 0, 2);
		
		// Convert the data
		int temp = ((data[0] & 0xFF) * 256 + (data[1] & 0xFF));
		
		// Select measurement control register
		// Enable pressure measurement, OSS = 1
		device.write(0xF4, (byte)0x74);
		Thread.sleep(100);
		
		// Read 3 bytes of data from address 0xF6(246)
		// pres msb1, pres msb, pres lsb
		device.read(0xF6, data, 0, 3);
		
		// Convert the data
		double pres = (((data[0] & 0xFF) * 65536) + ((data[1] & 0xFF) * 256) + (data[2] & 0xFF)) / 128;

		// Callibration for Temperature
		double X1 = (temp - AC6) * AC5 / 32768.0;
		double X2 = (MC * 2048.0) / (X1 + MD);
		double B5 = X1 + X2;
		double cTemp = ((B5 + 8.0) / 16.0) / 10.0;
		double fTemp = cTemp * 1.8 + 32;
		
		// Calibration for Pressure
		double B6 = B5 - 4000;
		X1 = (B2 * (B6 * B6 / 4096.0)) / 2048.0;
		X2 = AC2 * B6 / 2048.0;
		double X3 = X1 + X2;
		double B3 = (((AC1 * 4 + X3) * 2) + 2) / 4.0;
		X1 = AC3 * B6 / 8192.0;
		X2 = (B1 * (B6 * B6 / 2048.0)) / 65536.0;
		X3 = ((X1 + X2) + 2) / 4.0;
		double B4 = AC4 * (X3 + 32768) / 32768.0;
		double B7 = ((pres - B3) * (25000.0));
		double pressure;
		if(B7 < 2147483648L) {
			pressure = (B7 * 2) / B4;
		} else {
			pressure = (B7 / B4) * 2;
		}
		X1 = (pressure / 256.0) * (pressure / 256.0);
		X1 = (X1 * 3038.0) / 65536.0;
		X2 = ((-7357) * pressure) / 65536.0;
		pressure = (pressure + (X1 + X2 + 3791) / 16.0) / 100;
		
		// Calculate Altitude
		double altitude = 44330 * (1 - Math.pow((pressure / 1013.25), 0.1903));
                
                // Store it to the cache and return
                this.getCache().setDValue(this.getCacheCode("temperature"), cTemp);
                this.getCache().setDValue(this.getCacheCode("ftemperature"), fTemp);
                this.getCache().setDValue(this.getCacheCode("altitude"), altitude);
                this.getCache().setDValue(this.getCacheCode("pressure"), pressure);
            }
            return(this.getCache().getDValue(this.getCacheCode(name)));
        } else {
            throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
        }
    }
    
    public double getTemperature() throws Exception {
        return(this.getDValue("temperature"));
    }
    
    public double getPressure() throws Exception {
        return(this.getDValue("pressure"));
    }
    
    public double getAltitude() throws Exception {
        return(this.getDValue("altitude"));
    }
    
    @Override
    public String getSValue(String name) {
        throw new IllegalArgumentException("This sensor does not support String value.");
    }

    @Override
    public int getIValue(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } 
}
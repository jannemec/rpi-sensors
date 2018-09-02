/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;

public class HYT939
{
	public static void main(String args[]) throws Exception
	{
		// Create I2CBus
		I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
		// Get I2C device, HYT939 I2C address is 0x28(40)
		I2CDevice device = bus.getDevice(0x28);

		// Send normal mode command 
		device.write((byte)0x80);
		Thread.sleep(500);

		// Read 4 bytes of data
		// humidity msb, humidity lsb, temp msb, temp lsb
		byte[] data = new byte[4];
		device.read(data,0,4);

		// Convert the data to 14-bits
		double humidity = (((data[0] & 0x3F) * 256) + (data[1] & 0xFF)) * (100.0 / 16383.0);
		double cTemp = ((((data[2] & 0xFF) * 256) + (data[3] & 0xFC)) / 4) * (165.0 / 16383.0) - 40;
		double fTemp = (cTemp * 1.8 ) + 32;

		// Output data to screen
		System.out.printf("Relative Humidity is : %.2f %%RH %n", humidity);
		System.out.printf("Temperature in Celsius is : %.2f C %n", cTemp);
		System.out.printf("Temperature in Fahrenheit is : %.2f F %n", fTemp);
	}
}

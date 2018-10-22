/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.tst;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.jannemec.sensors.BMP180;
import com.jannemec.sensors.AM2321;
import com.jannemec.sensors.TSL2561;
import com.jannemec.sensors.TCS3200;
import com.jannemec.sensors.HCSR04;
import com.jannemec.sensors.RainSBX;
import com.jannemec.tools.MemCache;


// To run
// cd Dokumenty/rpi/sensors
// java -cp Sensors.jar com.jannemec.tst.Test
// java -cp ~/Dokumenty/rpi/sensors/Sensors.jar com.jannemec.tst.Test
/**
 *
 * @author u935
 */
public class Test {

    public static void main(String args[]) throws Exception {
        com.jannemec.tools.MemCache mCache = new MemCache();
        
        RainSBX rainSBX = new RainSBX(mCache);
        for(int i = 0; i < 2; i++) {
            System.out.printf("Prší : %s %n", rainSBX.isRain() ? "ANO" : "NE");
            Thread.sleep(2000);
        }
    
    }
    
    public static void removed() throws Exception {
        com.jannemec.tools.MemCache mCache = new MemCache();
        
        HCSR04 hcsr04 = new HCSR04(mCache);
        for(int i = 0; i < 2; i++) {
            System.out.printf("Distance : %.2f m %n", (double) (hcsr04.getDistance()));
            Thread.sleep(2000);
        }
        
        TCS3200 tcs3200 = new TCS3200(mCache);
        tcs3200.setFrequency(1);
        for(int i = 0; i < 2; i++) {
            System.out.printf("Red : %.2f prct %n", (double) (tcs3200.getRed() / 10.0));
            System.out.printf("Green : %.2f prct %n", (double) (tcs3200.getGreen() / 10.0));
            System.out.printf("Blue : %.2f prct %n", (double) (tcs3200.getBlue() / 10.0));
            System.out.printf("Clear : %.2f prct %n", (double) (tcs3200.getClear() / 10.0));
            System.out.printf("Light : %d %n", tcs3200.getTotal());
            System.out.printf("Color : %s %n", tcs3200.getColor());
            Thread.sleep(2000);
        }
    
        //tools.MemCache mCache = new MemCache();
        /*tcs3200.setFrequency(1);
        System.out.printf("Red : %.2f prct %n", (double) (tcs3200.getRed() / 10.0));
        System.out.printf("Green : %.2f prct %n", (double) (tcs3200.getGreen() / 10.0));
        System.out.printf("Blue : %.2f prct %n", (double) (tcs3200.getBlue() / 10.0));
        System.out.printf("Light : %d %n", tcs3200.getTotal());
        System.out.printf("Color : %s %n", tcs3200.getColor());*/
        
        /*tcs3200.setFrequency(2);
        System.out.printf("Red : %d %n", tcs3200.getRed());
        System.out.printf("Green : %d %n", tcs3200.getGreen());
        System.out.printf("Blue : %d %n", tcs3200.getBlue());
        System.out.printf("Light : %d %n", tcs3200.getTotal());
        System.out.printf("Color : %s %n", tcs3200.getColor());*/
        
        /*tcs3200.setFrequency(2);
        System.out.printf("Red : %d %n", tcs3200.getRed());
        System.out.printf("Green : %d %n", tcs3200.getGreen());
        System.out.printf("Blue : %d %n", tcs3200.getBlue());
        System.out.printf("Light : %d %n", tcs3200.getTotal());
        System.out.printf("Color : %s %n", tcs3200.getColor());*/
        
        AM2321 am2321;
        am2321 = new AM2321(mCache);

        // Output data to screen
        for(int i = 0; i < 2; i++) {
            System.out.printf("Humidity : %.2f prct %n", am2321.getHumidity());
            System.out.printf("Temperature in Celsius : %.2f C %n", am2321.getTemperature());
            Thread.sleep(2000);
        }
        
        
        BMP180 bmp180;
        bmp180 = new BMP180(mCache);

        // Output data to screen
        for(int i = 0; i < 1; i++) {
            System.out.printf("Altitude : %.2f m %n", bmp180.getAltitude());
            System.out.printf("Pressure : %.2f hPa %n", bmp180.getPressure());
            System.out.printf("Temperature in Celsius : %.2f C %n", bmp180.getTemperature());
            Thread.sleep(2000);
        }
        
        TSL2561 tsl2561;
        tsl2561 = new TSL2561(mCache);

        // Output data to screen
        for(int i = 0; i < 1; i++) {
            System.out.printf("Full : %.2f lux %n", tsl2561.getFull());
            System.out.printf("Infrared : %.2f lux %n", tsl2561.getInfrared());
            System.out.printf("Visible : %.2f lux %n", tsl2561.getVisible());
            Thread.sleep(2000);
        }
        //System.out.printf("Temperature in Fahrenheit : %.2f F %n", fTemp);
        
        // GPIO test
        System.out.println("<--Pi4J--> GPIO Control Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "MyLED", PinState.HIGH);

        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);

        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(500);

        // turn off gpio pin #01
        pin.low();
        System.out.println("--> GPIO state should be: OFF");

        
        gpio.shutdown();
        gpio.unprovisionPin(pin);
        Thread.sleep(500);
    }

}

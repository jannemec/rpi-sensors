/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tst;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterruptCallback;

/**
 *
 * @author marcandreuf
 */
// java -cp Dokumenty/rpi/sensors/Sensors.jar tst.Ex13_ButtonISR
public class Ex13_ButtonISR extends BaseSketch {    

    private static final int btnPin = 0;
    private static final int ledPin = 1;
    
    public static void main(String[] args) throws InterruptedException {
        Ex13_ButtonISR sketch = new Ex13_ButtonISR(GpioFactory.getInstance());
        sketch.run(args);
    }

    
    /**
     * @param gpio controller 
     */
    public Ex13_ButtonISR(GpioController gpio){
        super(gpio);
    }
    
    @Override
    protected void setup(String[] args) {
        gpioIsrSetup();
        System.out.println("Button ISR ready.");
    }
        
    
    private static volatile long lastTime = System.currentTimeMillis();;
    private static void gpioIsrSetup() {
        wiringPiSetup();
        
        Gpio.pinMode(btnPin, Gpio.INPUT);
        Gpio.pullUpDnControl(btnPin, Gpio.PUD_UP);
        
        Gpio.pinMode(ledPin, Gpio.OUTPUT);
        Gpio.pullUpDnControl(ledPin, Gpio.PUD_DOWN);
        Gpio.digitalWrite(ledPin, false);
        
        Gpio.wiringPiISR(btnPin, Gpio.INT_EDGE_FALLING, new GpioInterruptCallback() {
            private final long debounceTime = 200;
            @Override
            public void callback(int pin) {
                Gpio.delay(10);
                long currentTime = System.currentTimeMillis();
                if(currentTime > lastTime+debounceTime){
                    Gpio.digitalWrite(ledPin, Gpio.digitalRead(ledPin)==0?1:0);                
                    System.out.println("GPIO PIN 0 detected. Led state: "+Gpio.digitalRead(ledPin));
                }else{
                    System.out.println("Discard event "+currentTime);
                }              
                lastTime=currentTime;
            }
        });
    }
    
    
    @Override
    protected void loop(String[] args) {
        try {
            countDownLatchEndSketch.await();
        } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        Gpio.digitalWrite(ledPin, 0);
    }
    
    
}
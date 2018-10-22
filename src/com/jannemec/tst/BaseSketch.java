/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.tst;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.wiringpi.Gpio;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Super class with sketch abstraction methods.
 * 
 * @author marcandreuf
 */
public abstract class BaseSketch {

    protected final GpioController gpio;
    protected static Thread threadCheckInputStream;
    protected boolean isNotInterrupted = true;
    protected static final CountDownLatch countDownLatchEndSketch = new CountDownLatch(1);

    protected abstract void setup(String[] args);
    protected abstract void loop(String[] args) throws InterruptedException;   
    
    protected void loop() throws InterruptedException{
        loop(new String[0]);
    }

    public BaseSketch() {
        this(null);
    }
    
    public BaseSketch(GpioController gpio) {
        this.gpio = gpio;
    }

    protected void run(String[] args) throws InterruptedException {
        setup(args);
        startThreadToCheckInputStream();
        loop(args);
        tearDown();
    }

    private void startThreadToCheckInputStream() {
        CheckEnd checkend = new CheckEnd();
        threadCheckInputStream = new Thread(checkend);
        threadCheckInputStream.start();
    }

    protected void tearDown() {
        if(gpio != null){
            for(GpioPin pin : gpio.getProvisionedPins()){
                if(pin.getMode().equals(PinMode.DIGITAL_OUTPUT)){
                    pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
                }
            }                
            gpio.shutdown();
        }
    }

    protected void delayMilliseconds(long miliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(miliseconds);
        } catch (InterruptedException ex) {
            System.out.println("Sleep Milliseconds delay interrupted " + ex.getMessage());
        }
    }
    
    protected void delayMicrosendos(long microseconds){
        try {
            TimeUnit.MICROSECONDS.sleep(microseconds);
        } catch (InterruptedException ex) {
            System.out.println("Sleep Microseconds delay interrupted " + ex.getMessage());
        }
    }

    protected void setSketchInterruption() {
        if (threadCheckInputStream != null && threadCheckInputStream.isAlive()) {
            threadCheckInputStream.interrupt();
        }
        isNotInterrupted = false;
    }

    private class CheckEnd implements Runnable {
        Scanner scanner = new Scanner(System.in);

        @Override
        @SuppressWarnings("empty-statement")
        public void run() {
            while (!scanner.hasNextLine()) {};
            System.out.println("Sketch interrupted.");
            scanner.close();
            isNotInterrupted = false;
            countDownLatchEndSketch.countDown();
        }
    }
    
    
    protected static void wiringPiSetup(){
        if (Gpio.wiringPiSetup() == -1) {
            String msg = "==>> GPIO SETUP FAILED";
            System.out.println(msg);
            throw new RuntimeException(msg);
        }
    }

}

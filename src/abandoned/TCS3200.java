/*
 * Light sensor module
 */
package abandoned;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.ArrayList;
import java.util.Arrays;
import sensors.Sensor;

public class TCS3200 extends Sensor {

    /**
     * @return the intervalLength
     */
    public int getIntervalLength() {
        return intervalLength;
    }

    /**
     * @param intervalLength the intervalLength to set
     */
    public void setIntervalLength(int intervalLength) {
        this.intervalLength = intervalLength;
    }
    
    public TCS3200(tools.Cache cache, Pin s1, Pin s2, Pin s3, Pin s4, Pin oe, Pin out) {
        super(cache);
        this.setAddress(address);
        this.setSensorName("TCS3200");
        this.setValueList(new ArrayList<>(Arrays.asList("red", "green", "blue", "clear", "total", "color")));
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.oe = oe;
        this.out = out;
    }
    
    public TCS3200(tools.Cache cache) {
        //this(cache, null, null, RaspiPin.GPIO_04, RaspiPin.GPIO_05, null, RaspiPin.GPIO_06);
        this(cache, RaspiPin.GPIO_02, RaspiPin.GPIO_03, RaspiPin.GPIO_04, RaspiPin.GPIO_05, null, RaspiPin.GPIO_01);
    }
    
    @Override
    public void finalize() throws Throwable {
        try {
            if (this.gpio != null) {
                this.gpio.shutdown();
                this.gpio = null;
            } } finally {
            super.finalize();
        }
    }

    protected Pin s1 = null;
    protected Pin s2 = null;
    protected Pin s3 = null;
    protected Pin s4 = null;
    protected Pin oe = null;
    protected Pin out = null;
    protected int intervalLength = 500;
    protected int counter = 0;
    protected int frequency = 1;
    final private double[] delay = { 0.3, 0.3, 0.3, 0.3 };
    final private double[] count = { 10, 10, 10, 10 };
    protected GpioPinDigitalOutput ps1;
    protected GpioPinDigitalOutput ps2;
    protected GpioPinDigitalOutput ps3;
    protected GpioPinDigitalOutput ps4;
    protected GpioPinDigitalInput inputPin;
    protected GpioController gpio;
    protected long timeLimit = 0;
    protected long finishTime = 0;
    
    /**
     * Set the frequency of the sensor - possible values are
     * 0    L   L   0%      --
     * 1    L   H   2%      10-12kHz
     * 2    H   L   20%     100-120kHz
     * 3    H   H   100%    500-600kHz
     * @param f 
     */
    public void setFrequency(int f) throws IndexOutOfBoundsException {
        if ((f >= 0) && (f <= 3)) {
            if (this.frequency != f) {
                this.frequency = f;
                this.getCache().clearIValue(this.getCacheCode("red"));
                this.getCache().clearIValue(this.getCacheCode("green"));
                this.getCache().clearIValue(this.getCacheCode("blue"));
                this.getCache().clearIValue(this.getCacheCode("total"));
                this.getCache().clearSValue(this.getCacheCode("color"));
            }
        } else {
            throw new IndexOutOfBoundsException("Value of frequency has to be from set 0, 1, 2, 3");
        }
    }
    
    public int getFrequency() {
        return(this.frequency);
    }
    
    protected void setFrequencyPins() {
        this.setFrequencyPins(this.getFrequency());
    }
    
    protected void setFrequencyPins(int f) {
        switch(f) {
                case 0:
                    this.ps1.setState(PinState.LOW);
                    this.ps2.setState(PinState.LOW);
                    break;
                case 1:
                    this.ps1.setState(PinState.LOW);
                    this.ps2.setState(PinState.HIGH);
                    break;
                case 2:
                    this.ps1.setState(PinState.HIGH);
                    this.ps2.setState(PinState.LOW);
                    break;
                case 3:
                default :
                    this.ps1.setState(PinState.HIGH);
                    this.ps2.setState(PinState.HIGH);
                    break;
            }
    }
    
    protected synchronized void clearCounter() {
        this.counter = 0;
    }
    
    protected synchronized int getCounter() {
        return(this.counter);
    }
    
    protected synchronized void addCounter(int i) {
        this.counter += i;
    }
    
    protected synchronized void addCounter() {
        this.counter++;
    }
    
    @Override
    public int getIValue(String name) throws Exception {
        if (this.getValueList().contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasIValue(this.getCacheCode(name))) {
                if (this.gpio == null) {
                    this.gpio = GpioFactory.getInstance();
                }
                if (this.ps1 == null) {
                    this.ps1 = this.gpio.provisionDigitalOutputPin(this.s1, this.getSensorName() + " S1", PinState.LOW);
                    this.ps2 = this.gpio.provisionDigitalOutputPin(this.s2, this.getSensorName() + " S2", PinState.LOW);
                    this.ps3 = this.gpio.provisionDigitalOutputPin(this.s3, this.getSensorName() + " S3", PinState.LOW);
                    this.ps4 = this.gpio.provisionDigitalOutputPin(this.s4, this.getSensorName() + " S4", PinState.LOW);
                    this.inputPin = gpio.provisionDigitalInputPin(this.out, PinPullResistance.PULL_DOWN);
                    this.inputPin.setShutdownOptions(true);
                }
                this.setFrequencyPins();
                //GpioUtil.setEdgeDetection(this.out.getAddress(), GpioUtil.EDGE_RISING);
                
                // RED
                this.ps3.setState(PinState.LOW);
                this.ps4.setState(PinState.LOW);
                Thread.sleep((int) this.delay[0] * 1000);
                inputPin.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
                    if (event.getState().isHigh()) {
                        addCounter();
                    }
                });               
                
                this.clearCounter();
                Thread.sleep(this.getIntervalLength());
                int red = this.getCounter();

                //System.out.printf("Red: %d %d %n", (int) Math.round(this.counter), (int) this.counter * 1000 / this.intervalLength);
                        
                // GREEN
                this.ps3.setState(PinState.HIGH);
                this.ps4.setState(PinState.HIGH);
                Thread.sleep((int) this.delay[1] * 1000);
                
                this.clearCounter();
                Thread.sleep(this.getIntervalLength());
                int green = this.getCounter();
                //System.out.printf("Green: %d %d %n", (int) Math.round(this.counter), (int) this.counter * 1000 / this.intervalLength);
                
                // BLUE
                this.ps3.setState(PinState.LOW);
                this.ps4.setState(PinState.HIGH);
                Thread.sleep((int) this.delay[2] * 1000);
                
                this.clearCounter();
                Thread.sleep(this.getIntervalLength());
                int blue = this.getCounter();
                //System.out.printf("Blue: %d %d %n", (int) Math.round(this.counter), (int) this.counter * 1000 / this.intervalLength);
                
                // CLEAR
                this.ps3.setState(PinState.HIGH);
                this.ps4.setState(PinState.LOW);
                Thread.sleep((int) this.delay[3] * 1000);
                this.clearCounter();
                Thread.sleep(this.getIntervalLength());
                int clear = this.getCounter();
                
                this.ps3.setState(PinState.LOW);
                this.ps4.setState(PinState.LOW);
                this.inputPin.removeAllListeners();
                this.inputPin.removeAllTriggers();
                
                setFrequencyPins(0);
                
                if (this.ps1 != null) {
                    this.gpio.unprovisionPin(this.ps1);
                    this.ps1 = null;
                    this.gpio.unprovisionPin(this.ps2);
                    this.ps2 = null;
                    this.gpio.unprovisionPin(this.ps3);
                    this.ps3 = null;
                    this.gpio.unprovisionPin(this.ps4);
                    this.ps4 = null;
                    this.gpio.unprovisionPin(this.inputPin);
                    this.inputPin = null;
                }
                //this.gpio.shutdown();
                
                this.getCache().setIValue(this.getCacheCode("total"), red + green + blue);
                if (red + green + blue > 0) {
                    this.getCache().setIValue(this.getCacheCode("red"), (int) (1000 * red / (red + green + blue)));
                    this.getCache().setIValue(this.getCacheCode("green"),  (int) (1000 * green / (red + green + blue)));
                    this.getCache().setIValue(this.getCacheCode("blue"),  (int) (1000 * blue / (red + green + blue)));
                    this.getCache().setIValue(this.getCacheCode("clear"),  (int) (1000 * clear / (red + green + blue)));
                } else {
                    this.getCache().setIValue(this.getCacheCode("red"), 0);
                    this.getCache().setIValue(this.getCacheCode("green"),  0);
                    this.getCache().setIValue(this.getCacheCode("blue"),  0);
                    this.getCache().setIValue(this.getCacheCode("clear"),  0);
                }
                
                if ((red > blue) && (red > green)) {
                    this.getCache().setSValue(this.getCacheCode("color"), "red");
                } else if ((blue > red) && (blue > green)) {
                    this.getCache().setSValue(this.getCacheCode("color"), "blue");
                } else if ((green > red) && (green > blue)) {
                    this.getCache().setSValue(this.getCacheCode("color"), "green");
                } else {
                    this.getCache().setSValue(this.getCacheCode("color"), "???");
                }
            }
            return(this.getCache().getIValue(this.getCacheCode(name)));
        } else {
            throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
        }
    }
    
    public int getRed() throws Exception {
        return(this.getIValue("red"));
    }
    
    public int getGreen() throws Exception {
        return(this.getIValue("green"));
    }
    
    public int getBlue() throws Exception {
        return(this.getIValue("blue"));
    }
    
    public int getClear() throws Exception {
        return(this.getIValue("clear"));
    }
    
    public int getTotal() throws Exception {
        return(this.getIValue("total"));
    }
    
    public String getColor() throws Exception {
        return(this.getSValue("color"));
    } 

    @Override
    public String getSValue(String name) throws Exception {
        if (this.getValueList().contains(name)) {
            // Check if the value is in cache
            if (!this.getCache().hasSValue(this.getCacheCode(name))) {
                this.getTotal();
            }
            return(this.getCache().getSValue(this.getCacheCode(name)));
        } else {
            throw new IllegalArgumentException("Parameter must be one of " + Arrays.toString(this.valueList.toArray()));
        }
    }

    @Override
    public double getDValue(String name) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

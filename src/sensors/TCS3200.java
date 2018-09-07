/*
 * Light sensor module
 */
package sensors;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterruptCallback;
import com.pi4j.wiringpi.GpioUtil;
import java.util.ArrayList;
import java.util.Arrays;

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
        this.setSensorName("TCS3200W");
        this.setValueList(new ArrayList<>(Arrays.asList("red", "green", "blue", "clear", "total", "color")));
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.oe = oe;
        this.out = out;
    }
    
    public TCS3200(tools.Cache cache) {
        this(cache, RaspiPin.GPIO_02, RaspiPin.GPIO_03, RaspiPin.GPIO_04, RaspiPin.GPIO_05, RaspiPin.GPIO_07, RaspiPin.GPIO_06);
    }
    
    @Override
    public void finalize() throws Throwable {
        super.finalize();
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
    final private double[] delay = { 1, 0.6};
    final private double[] count = { 10000, 10000};
    
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
                    Gpio.digitalWrite(this.s1.getAddress(), 0);
                    Gpio.digitalWrite(this.s2.getAddress(), 0);
                    break;
                case 1:
                    Gpio.digitalWrite(this.s1.getAddress(), 0);
                    Gpio.digitalWrite(this.s2.getAddress(), 1);
                    break;
                case 2:
                    Gpio.digitalWrite(this.s1.getAddress(), 1);
                    Gpio.digitalWrite(this.s2.getAddress(), 0);
                    break;
                case 3:
                default :
                    Gpio.digitalWrite(this.s1.getAddress(), 1);
                    Gpio.digitalWrite(this.s2.getAddress(), 1);
                    break;
            }
    }
    
    protected void setOnOff(boolean state) {
        Gpio.pinMode(this.oe.getAddress(), Gpio.OUTPUT);
        Gpio.pullUpDnControl(this.oe.getAddress(), Gpio.PUD_DOWN);
        Gpio.digitalWrite(this.oe.getAddress(), state ? 1 : 0);
    }
    
    public void turnOff() {
        this.setOnOff(false);
    }
    public void turnOn() {
        this.setOnOff(true);
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
                    int status = Gpio.wiringPiSetup();
                    if (status == -1) {
                        String msg = "==>> GPIO SETUP FAILED";
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    }
                    Gpio.pinMode(this.s1.getAddress(), Gpio.OUTPUT);
                    Gpio.pullUpDnControl(this.s1.getAddress(), Gpio.PUD_DOWN);
                    Gpio.digitalWrite(this.s1.getAddress(), 0);
                    Gpio.pinMode(this.s2.getAddress(), Gpio.OUTPUT);
                    Gpio.pullUpDnControl(this.s2.getAddress(), Gpio.PUD_DOWN);
                    Gpio.digitalWrite(this.s2.getAddress(), 0);
                    Gpio.pinMode(this.s3.getAddress(), Gpio.OUTPUT);
                    Gpio.pullUpDnControl(this.s3.getAddress(), Gpio.PUD_DOWN);
                    Gpio.digitalWrite(this.s3.getAddress(), 0);
                    Gpio.pinMode(this.s4.getAddress(), Gpio.OUTPUT);
                    Gpio.pullUpDnControl(this.s4.getAddress(), Gpio.PUD_DOWN);
                    Gpio.digitalWrite(this.s4.getAddress(), 0);
                     
                    Gpio.pinMode(this.out.getAddress(), Gpio.INPUT);
                    Gpio.pullUpDnControl(this.out.getAddress(), Gpio.PUD_UP);

                this.setFrequencyPins();
                //GpioUtil.setEdgeDetection(this.out.getAddress(), GpioUtil.EDGE_RISING);
                
                // RED
                Gpio.digitalWrite(this.s3.getAddress(), 0);
                Gpio.digitalWrite(this.s4.getAddress(), 0);
                Thread.sleep((int) this.delay[0] * 1000);
                this.timeLimit = System.currentTimeMillis() + (long) (this.delay[1] * 1000);
                this.finishTime = this.timeLimit;
                this.clearCounter();

                Gpio.wiringPiISR(this.out.getAddress(), Gpio.INT_EDGE_RISING, new GpioInterruptCallback() {
                    @Override
                    public void callback(int pin) {
                        //Gpio.delay(10);
                        long currentTime = System.currentTimeMillis();
                        if(currentTime > timeLimit){
                            finishTime = currentTime;
                        } else {
                            if (Gpio.digitalRead(pin) == 1) {
                                addCounter();             
                            }
                        }
                    }
                });
                        
                this.clearCounter();
                while((this.getCounter() < this.count[0]) && (System.currentTimeMillis() < this.timeLimit)) {
                    Thread.sleep((int) this.delay[1]);
                }
                //Gpio.wiringPiISR(this.out.getAddress(), Gpio.INT_EDGE_FALLING, null);
                //Gpio.wiringPiClearISR(this.out.getAddress());
                //System.out.printf("Red: %d %d", (int) Math.round(this.getCounter()), (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000)));
                //System.out.println();
                int red = (int) (this.getCounter() > 0 ? (this.getCounter() * 1000.0 / (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000))) : 0);
                //System.out.printf("Red: %d", red);
                //System.out.println();
                        
                // GREEN
                Gpio.digitalWrite(this.s3.getAddress(), 1);
                Gpio.digitalWrite(this.s4.getAddress(), 1);
                Thread.sleep((int) this.delay[0] * 1000);
                this.timeLimit = System.currentTimeMillis() + (long) (this.delay[1] * 1000);
                this.finishTime = this.timeLimit;

                this.clearCounter();
                while((this.getCounter() < this.count[0]) && (System.currentTimeMillis() < this.timeLimit)) {
                    Thread.sleep((int) this.delay[1]);
                }
                //Gpio.wiringPiISR(this.out.getAddress(), Gpio.INT_EDGE_FALLING, null);
                //Gpio.wiringPiClearISR(this.out.getAddress());
                //System.out.printf("Green: %d %d", (int) Math.round(this.getCounter()), (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000)));
                //System.out.println();
                int green = (int) (this.getCounter() > 0 ? (this.getCounter() * 1000.0 / (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000))): 0);
                //System.out.printf("Green: %d", green);
                //System.out.println();                
                
                // BLUE
                Gpio.digitalWrite(this.s3.getAddress(), 0);
                Gpio.digitalWrite(this.s4.getAddress(), 1);
                Thread.sleep((int) this.delay[0] * 1000);
                this.timeLimit = System.currentTimeMillis() + (long) (this.delay[1] * 1000);
                this.finishTime = this.timeLimit;

                this.clearCounter();
                while((this.getCounter() < this.count[0]) && (System.currentTimeMillis() < this.timeLimit)) {
                    Thread.sleep((int) this.delay[1]);
                }
                //Gpio.wiringPiISR(this.out.getAddress(), Gpio.INT_EDGE_FALLING, null);
                //Gpio.wiringPiClearISR(this.out.getAddress());
                //System.out.printf("Blue: %d %d", (int) Math.round(this.getCounter()), (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000)));
                //System.out.println();
                int blue = (int) (this.getCounter() > 0 ? (this.getCounter() * 1000.0 / (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000)))  : 0);
                //System.out.printf("Blue: %d", blue);
                //System.out.println();
                
                // CLEAR
                Gpio.digitalWrite(this.s3.getAddress(), 1);
                Gpio.digitalWrite(this.s4.getAddress(), 0);
                Thread.sleep((int) this.delay[0] * 1000);
                this.timeLimit = System.currentTimeMillis() + (long) (this.delay[1] * 1000);
                this.finishTime = this.timeLimit;

                this.clearCounter();
                while((this.getCounter() < this.count[1]) && (System.currentTimeMillis() < this.timeLimit)) {
                    Thread.sleep((int) this.delay[1]);
                }
                Gpio.wiringPiISR(this.out.getAddress(), Gpio.INT_EDGE_FALLING, null);
                Gpio.wiringPiClearISR(this.out.getAddress());
                //System.out.printf("Clear: %d %d", (int) Math.round(this.getCounter()), (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000)));
                //System.out.println();
                int clear = (int) (this.getCounter() > 0 ? (this.getCounter() * 1000.0 / (this.finishTime - this.timeLimit + (long) (this.delay[1] * 1000)))  : 0);
                
                Gpio.digitalWrite(this.s3.getAddress(), 0);
                Gpio.digitalWrite(this.s4.getAddress(), 0);
                
                setFrequencyPins(0);
                
                GpioUtil.unexport(this.out.getAddress());
                
                
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

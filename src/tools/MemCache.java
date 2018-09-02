/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Calendar;
import java.util.HashMap;

/**
 *
 * @author u935
 */
public class MemCache implements Cache {

    public MemCache() {
        this.delay = 10;
    }
    
    public MemCache(int delay) {
        this.delay = delay;
    }
    
    /**
     * @return the delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    private int delay;
    
    private HashMap<String, DoubleCache> dmap = new HashMap<>();
    private HashMap<String, IntCache>    imap = new HashMap<>();
    private HashMap<String, StringCache> smap = new HashMap<>();
    
    /**
     * Get Calndar value - according to predefined delay
     * @return 
     */
    protected Calendar getValidTo() {
        return this.getValidTo(this.delay);
    }
    
    protected Calendar getValidTo(int delay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delay);
        return(calendar);
    }
    
    protected boolean isValid(Calendar calendar) {
        Calendar now = Calendar.getInstance();
        return(now.compareTo(calendar) <= 0);
    }
    
    @Override
    public double getDValue(String key) {
        if (this.hasDValue(key)) {
            DoubleCache v = this.dmap.get(key);
            if (this.isValid(v.validTo)) {
                return v.value;
            }
        } else {
            throw new IllegalArgumentException ("The key is not stored in the cache");
        }
        return(Double.NaN);
    }

    @Override
    public void setDValue(String key, double val) {
        DoubleCache value = new DoubleCache();
        value.value = val;
        value.validTo = this.getValidTo();
        this.dmap.put(key, value);
    }
    
    @Override
    public boolean hasDValue(String key) {
        if (this.dmap.containsKey(key)) {
            DoubleCache v = this.dmap.get(key);
            if (this.isValid(v.validTo)) {
                return(true);
            } else {
                return(false);
            }
        } else {
            return false;
        }
    }
    
    @Override
    public void clearDValue(String key) {
        if (this.dmap.containsKey(key)) {
            dmap.remove(key);
        }
    }
    
    @Override
    public int getIValue(String key) {
        if (this.hasIValue(key)) {
            IntCache v = this.imap.get(key);
            if (this.isValid(v.validTo)) {
                return v.value;
            }
        } else {
            throw new IllegalArgumentException ("The key is not stored in the cache");
        }
        return(Integer.MAX_VALUE);
    }

    @Override
    public void setIValue(String key, int val) {
        IntCache value = new IntCache();
        value.value = val;
        value.validTo = this.getValidTo();
        this.imap.put(key, value);
    }
    
    @Override
    public boolean hasIValue(String key) {
        if (this.imap.containsKey(key)) {
            IntCache v = this.imap.get(key);
            if (this.isValid(v.validTo)) {
                return(true);
            } else {
                return(false);
            }
        } else {
            return false;
        }
    }
    
    @Override
    public void clearIValue(String key) {
        if (this.imap.containsKey(key)) {
            imap.remove(key);
        }
    }
    
    @Override
    public String getSValue(String key) {
        if (this.hasSValue(key)) {
            StringCache v = this.smap.get(key);
            if (this.isValid(v.validTo)) {
                return v.value;
            }
        } else {
            throw new IllegalArgumentException ("The key is not stored in the cache");
        }
        return(null);
    }

    @Override
    public void setSValue(String key, String val) {
        StringCache value = new StringCache();
        value.value = val;
        value.validTo = this.getValidTo();
        this.smap.put(key, value);
    }
    
    @Override
    public boolean hasSValue(String key) {
        if (this.smap.containsKey(key)) {
            StringCache v = this.smap.get(key);
            if (this.isValid(v.validTo)) {
                return(true);
            } else {
                return(false);
            }
        } else {
            return false;
        }
    }
    
    @Override
    public void clearSValue(String key) {
        if (this.smap.containsKey(key)) {
            smap.remove(key);
        }
    }
    
    @Override
    public void clearCache(boolean onlyExpired) {
        if (onlyExpired) {
            smap.forEach((k,v) -> {if (!this.isValid(v.validTo)) {smap.remove(k);}});
            imap.forEach((k,v) -> {if (!this.isValid(v.validTo)) {smap.remove(k);}});
            dmap.forEach((k,v) -> {if (!this.isValid(v.validTo)) {smap.remove(k);}});
        } else {
            // Cler all values
            this.dmap.clear();
            this.imap.clear();
            this.smap.clear();
        }
    }
    
    @Override
    public void clearCache() {
        this.clearCache(false);
    }
}

class DoubleCache {
    public double value;
    public Calendar validTo;
}

class IntCache {
    public int value;
    public Calendar validTo;
}

class StringCache {
    public String value;
    public Calendar validTo;
}

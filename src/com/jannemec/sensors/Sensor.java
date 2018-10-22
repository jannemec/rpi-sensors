/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.sensors;

import java.util.ArrayList;

/**
 *
 * @author u935
 */
abstract public class Sensor {
    
    protected com.jannemec.tools.Cache cache;
    protected String sensorName = "SENSOR";
    protected int address;
    protected ArrayList<String> valueList;
    
    public abstract double getDValue(String name) throws Exception;
    public abstract int getIValue(String name) throws Exception;
    //public abstract void setValue(String name, double d);
    public abstract String getSValue(String name) throws Exception;
    
    public void setCache(com.jannemec.tools.Cache cache) {
        this.cache = cache;
    }
    
    public com.jannemec.tools.Cache getCache() {
        return(this.cache);
    }
    
    public String getCacheCode(String name) {
        return(this.getSensorName() + "_" + name);
    }
    
    public Sensor(com.jannemec.tools.Cache cache) {
        this.setCache(cache);
    }
    
    /**
     * The time for the value refresh - before this time the value from cache will be taken
     */
    private int valueRefresh = 1;

    /**
     * @return the valueRefresh
     */
    public int getValueRefresh() {
        return valueRefresh;
    }

    /**
     * @param valueRefresh the valueRefresh to set
     */
    public void setValueRefresh(int valueRefresh) {
        this.valueRefresh = valueRefresh;
    }

    /**
     * @return the sensorName
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * @param sensorName the sensorName to set
     */
    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    /**
     * @return the address
     */
    public int getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(int address) {
        this.address = address;
    }
    
    /**
     * @return the valueList
     */
    public ArrayList<String> getValueList() {
        return valueList;
    }

    /**
     * @param valueList the valueList to set
     */
    public void setValueList(ArrayList<String> valueList) {
        this.valueList = valueList;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

/**
 *
 * @author u935
 */
public interface Cache {

    /**
     * Gets the double value from cache, NaN in case the value is not stored in cache or the value expired
     * @param key String
     * @return double
     */
    public double  getDValue(String key);
    
    /**
     * Sets the double value to cache
     * @param key String
     * @param val double
     */
    public void    setDValue(String key, double val);
    
    /**
     * Checks if the double value is in cache and is valid
     * @param key String
     * @return boolean
     */
    public boolean hasDValue(String key);
    
    /**
     * Removes the value from cache if exists
     * @param key String
     */
    public void clearDValue(String key);
    
    /**
     * Gets the int value from cache, min number in case the value is not stored in cache or the value expired
     * @param key String
     * @return int
     */
    public int     getIValue(String key);
    
    /**
     * Sets the int value to cache
     * @param key int
     * @param val double
     */
    public void    setIValue(String key, int val);
    
    /**
     * Checks if the int value is in cache and is valid
     * @param key String
     * @return boolean
     */
    public boolean hasIValue(String key);
    
    /**
     * Removes the value from cache if exists
     * @param key String
     */
    public void clearIValue(String key);
    
    /**
     * Gets the String value from cache, null in case the value is not stored in cache or the value expired
     * @param key String
     * @return String
     */
    public String  getSValue(String key);
    
    /**
     * Sets the String value to cache
     * @param key String
     * @param val String
     */
    public void    setSValue(String key, String val);
    
    /**
     * Checks if the String value is in cache and is valid
     * @param key String
     * @return boolean
     */
    public boolean hasSValue(String key);
    
    /**
     * Removes the value from cache if exists
     * @param key String
     */
    public void clearSValue(String key);
    
    /**
     * Clear all caches - removes all items from cache or only expired if the parameter onlyExpired is true
     * @param onlyExpired boolean
     */
    public void clearCache(boolean onlyExpired);
    
    /**
     * Clear all caches - removes all items from cache same as clearCache(false)
     */
    public void clearCache();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jannemec.dblog;

import java.util.Date;
import java.util.Properties;

/**
 *
 * @author u935
 */
public interface DataLogger {
    
    public DataLogger createConnection(Properties props);
    
    public void storeValues(Date dt, String store);
    
}

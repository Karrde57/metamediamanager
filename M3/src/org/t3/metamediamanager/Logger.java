/*Copyright 2014  M3Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/package org.t3.metamediamanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Allow the developer to write in a log file
 *
 */
public class Logger {
	//Pour le Singleton
    private static volatile Logger instance = null;
    
    private FileWriter _fileWriter;
    
    private Logger()
    {
    	try {
			_fileWriter = new FileWriter(M3Config.getInstance().getUserConfDirectory() + File.separator + "m3.log",true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void write(String elem)
    {
    	Date date = new Date();
    	DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
    			DateFormat.SHORT,
    			DateFormat.SHORT);
    	elem = "[" + shortDateFormat.format(date) + "] " + elem + "\n";
    	try {
			_fileWriter.write(elem);
			_fileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Méthode permettant de renvoyer une instance de la classe Singleton
     * @return Retourne l'instance du singleton.
     */
    public final static Logger getInstance() {
        if (Logger.instance == null) {
           synchronized(Logger.class) {
             if (Logger.instance == null) {
            	 Logger.instance = new Logger();
             }
           }
        }
        return Logger.instance;
    }
    
    public void close()
    {
    	try {
			_fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

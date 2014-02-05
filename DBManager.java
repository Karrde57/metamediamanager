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
*/package com.t3.metamediamanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Singleton class used to open the database and create statements
 * @author vincent
 *
 */
public final class DBManager {
	//Pour le Singleton
    private static volatile DBManager instance = null;

    private Connection _connection = null;
    

    /**
     * Constructeur de l'objet.
     */
    private DBManager() {
        super();
    }

    /**
     * Méthode permettant de renvoyer une instance de la classe Singleton
     * @return Retourne l'instance du singleton.
     */
    public final static DBManager getInstance() {
        if (DBManager.instance == null) {
           synchronized(DBManager.class) {
             if (DBManager.instance == null) {
            	 DBManager.instance = new DBManager();
             }
           }
        }
        return DBManager.instance;
    }
    
    
    public class DBException extends Exception
    {
		private static final long serialVersionUID = 1L;

		public DBException(String msg)
    	{
    		super("Erreur cache : " + msg);
    	}
    }
 
    /*
     * Se connecte à la base de données
     */
    public void connect(String DBPath) {    	
        try {
        	DBPath = M3Config.getInstance().getUserConfDirectory() + DBPath;
        	File f = new File(DBPath);
        	if(f.exists())
        	{
        		Class.forName("org.sqlite.JDBC");
                _connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
        	} else { //Si le fichier n'existe pas, c'est qu'on doit créer toutes les tables
        		Class.forName("org.sqlite.JDBC");
                _connection = DriverManager.getConnection("jdbc:sqlite:" + DBPath);
                createTables();
        	}
        	
        	enableForeignKeys();
            
            System.out.println("Connexion a " + DBPath + " avec succès");
        } catch (ClassNotFoundException notFoundException) {
            System.out.println("Erreur de connexion");
        } catch (SQLException sqlException) {
            System.out.println("Erreur de connexion");
        }
    }
    
   
    
    public Statement getStatement() {
    	Statement res = null;
    	try {
			res = _connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return res;
    }
    
    public PreparedStatement preparedStatement(String sql)
    {
    	PreparedStatement s = null;
    	try {
			s = _connection.prepareStatement(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return s;
    }
    
    public Connection getConnection() {
    	return _connection;
    }
    
    private void createTables()
    {
    	
    	try {
        	InputStream file = ClassLoader.getSystemResourceAsStream("com/t3/metamediamanager/database.sql");
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int length = 0;
	        while ((length = file.read(buffer)) != -1) {
	            baos.write(buffer, 0, length);
	        }
	        
	        String bigQuery = new String(baos.toByteArray(), "UTF-8");
	        String[] queries = bigQuery.split(";");
	        
	        for(String query : queries)
	        {
	        	Statement s = getStatement();
		        s.execute(query+";");
	        }
	        
    	} catch(UnsupportedEncodingException e)
    	{
    		System.out.println("Erreur lors de la création du cache");
    	} catch (IOException e) {
    		System.out.println("Erreur lors de la création du cache");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la création du cache : " + e.getMessage());
		}
    }
    
    private void enableForeignKeys()
    {
    	Statement s = getStatement();
    	try {
			s.execute("PRAGMA foreign_keys=ON;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

 
    public void close() {
        try {
            _connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    



}
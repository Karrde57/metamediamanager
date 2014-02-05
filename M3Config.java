Copyright 2014  M3Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
package com.t3.metamediamanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public final class M3Config {
	//Pour le Singleton
    private static volatile M3Config instance = null;
    

    private Document _document;
    private Element _root;
    
    private HashMap<String,String> _params = new HashMap<String,String>();
    private Vector<String> _filmsDirectories = new Vector<String>();
    private Vector<String> _seriesDirectories = new Vector<String>();
    
    String _confDir;
    
    File _fileConfig;
    
    /**
     * Constructeur de l'objet.
     */
    private M3Config() {
        super();
        
        _confDir =  System.getProperty("user.home") + File.separator;
        if(System.getProperty("os.name").startsWith("Windows"))
        {
        	_confDir += "MetaMediaManager";
        } else {
        	_confDir += ".m3";
        }
        _confDir += File.separator;
        
        File f = new File(_confDir);
        if(!f.exists())
        {
        	f.mkdirs();
        }
    }

    /**
     * Méthode permettant de renvoyer une instance de la classe Singleton
     * @return Retourne l'instance du singleton.
     */
    public final static M3Config getInstance() {
        if (M3Config.instance == null) {
           synchronized(DBManager.class) {
             if (M3Config.instance == null) {
            	 M3Config.instance = new M3Config();
             }
           }
        }
        return M3Config.instance;
    }
    
    /**
     * Loads the XML configuration file
     * @param filename
     */
    public void loadFile(String filename) {
    	_fileConfig = new File(getUserConfDirectory() + filename);
    	
    	if(!_fileConfig.exists())
    		copyResourceToUserDir(filename, filename);
    	
    	
        //On crée une instance de SAXBuilder
        SAXBuilder sxb = new SAXBuilder();
        try
        {
           //On crée un nouveau document JDOM avec en argument le fichier XML
           //Le parsing est terminé ;)
           _document = sxb.build(_fileConfig);
        }
        catch(Exception e){
        	System.out.println("Erreur lors de l'ouverture du fichier de configuration.");
        }

        //On initialise un nouvel élément racine avec l'élément racine du document.
        _root = _document.getRootElement();
        
        //Remplissage des paramètres
        List<Element> listParams = _root.getChildren("param");
        
        Iterator<Element> i = listParams.iterator();
        while(i.hasNext())
        {
           Element current = i.next();
           
           _params.put(current.getAttributeValue("name"), current.getAttributeValue("value"));           
        }
        
        //Remplissage des dossiers de films/séries
        List<Element> listDirs = _root.getChildren("dir");
        
        i = listDirs.iterator();
        while(i.hasNext())
        {
           Element current = i.next();
           if(current.getAttributeValue("type").equals("films"))
           {
        	   _filmsDirectories.add(current.getAttributeValue("url"));
           } else if(current.getAttributeValue("type").equals("series"))
           {
        	   _seriesDirectories.add(current.getAttributeValue("url"));
           }
        }
    }
    
    /**
     * Returns the value of the parameter matching with the parameter
     * @param name name of the parameter
     * @return value of the parameter
     */
    public String getParam(String name) {
    	if(_params.containsKey(name))
    		return _params.get(name);
    	else
    		return "";
    }
    
    /**
     * Set a parameter which will be saved in the XML config file
     * @param name
     * @param value
     */
    public void setParam(String name, String value)
    {
    	_params.put(name, value);
    }
    
    public Vector<String> getFilmsDirectories()
    {
    	return _filmsDirectories;
    }
    
    public Vector<String> getSeriesDirectories()
    {
    	return _seriesDirectories;
    }
    
    public void setFilmsDirectories(Vector<String> dir)
    {
    	if(dir==null)
    		throw new IllegalArgumentException();
    	_filmsDirectories = dir;
    }
    
    public void setSeriesDirectories(Vector<String> dir)
    {
    	if(dir==null)
    		throw new IllegalArgumentException();
    	_seriesDirectories = dir;
    }
    
    public void save()
    {
    	try
        {
    		Element root = new Element("config");
    		Document doc = new Document(root);
    		for(Entry<String,String> entry : _params.entrySet())
    		{
    			Element p = new Element("param");
    			p.setAttribute("name", entry.getKey());
    			p.setAttribute("value", entry.getValue());
    			root.addContent(p);
    		}
    		
    		//Film directories
    		for(String dir : _filmsDirectories)
    		{
    			Element d = new Element("dir");
    			d.setAttribute("type", "films");
    			d.setAttribute("url", dir);
    			root.addContent(d);
    		}
    		
    		//Series directories
    		for(String dir : _seriesDirectories)
    		{
    			Element d = new Element("dir");
    			d.setAttribute("type", "series");
    			d.setAttribute("url", dir);
    			root.addContent(d);
    		}
    		
    		//Save
    	    XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
    	    sortie.output(doc, new FileOutputStream(_fileConfig));
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally
    	{
    		
    	}
    }
    
    
    /**
     * Returns the path to the directory containing all user files (ex : /home/user/.m3/
     * @return the configuration directory
     */
    public String getUserConfDirectory()
    {
        return _confDir;
    }
    
    /**
     * Copy a file from "conf" to user directory
     * @param src source file relative to "conf" directory
     * @param des destination file relative to the user directory
     */
    public void copyResourceToUserDir(String src, String des)
    {
    	File file = new File(getUserConfDirectory() + des);
    	if(!file.exists())
    	{
    		InputStream r = null;
    		OutputStream os = null;
    		
    		
    		try {
    			file.createNewFile();
        		
    			r = getClass().getResourceAsStream("/com/t3/metamediamanager/conf/"+src);
	    		os = new FileOutputStream(file.getAbsolutePath());
	            byte[] buffer = new byte[1024];
	            int length;
	            while ((length = r.read(buffer)) > 0) {
	                os.write(buffer, 0, length);
	            }
    		} catch (IOException e) {
    			System.out.println(file.getAbsolutePath());
				e.printStackTrace();
			}
    		finally {
    			try {
					r.close();
					os.close();
				} catch (IOException e) {}
    			
    		}
    	}
    }
}
    

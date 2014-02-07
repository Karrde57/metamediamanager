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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Loads XBMC Saver, MediaBrowser Saver etc...
 * Saver can be plugins
 * @author vincent
 *
 */
public class SaverManager {
    private static volatile SaverManager instance = null;
    
    private Vector<Saver> _savers = new Vector<Saver>();
    
    /**
     * Constructeur de l'objet.
     */
    private SaverManager() {
        super();
    }

    /**
     * Méthode permettant de renvoyer une instance de la classe Singleton
     * @return Retourne l'instance du singleton.
     */
    public final static SaverManager getInstance() {
        if (SaverManager.instance == null) {
           synchronized(SaverManager.class) {
             if (SaverManager.instance == null) {
            	 SaverManager.instance = new SaverManager();
             }
           }
        }
        return SaverManager.instance;
    }
    
    /**
     * Loads internal savers and plugins
     */
    public void loadSavers()
    {
    	_savers.add(new XBMCSaver());
    	_savers.add(new MediaBrowserSaver());
    	
    	//Plugin Savers
    	//They are stored in /home/user/.m3/plugins
    	File pluginsDir = new File(M3Config.getInstance().getUserConfDirectory() + File.separator + "plugins");
    	if(pluginsDir.exists())
    	{
    		File[] potentialPlugins = pluginsDir.listFiles();
    		List<String> jarFileList = new ArrayList<String>();
    		//If jar file, we add to the list of Savers to load
    		for(File p : potentialPlugins)
    		{
    			if(p.getAbsolutePath().endsWith(".jar"))
    				jarFileList.add(p.getAbsolutePath());
    		}
    		String[] jarFilesTab = new String[jarFileList.size()];
    		jarFileList.toArray(jarFilesTab);
    		PluginsLoader loader = new PluginsLoader(jarFilesTab);
    		
    		try {
				Saver[] loadedSavers = loader.loadAllSaver();
				
				for(Saver p : loadedSavers)
					_savers.add(p);
			} catch (Exception e) {
e.printStackTrace();
			}
    		
    		
    	}
    }
    
    /**
     * Returns all loaded savers
     * @return all savers
     */
    public Vector<Saver> getSavers()
    {
    	return _savers;
    }
    
    
    /**
     * Use M3Config to know which savers are enabled (using their name), and returns the list
     * @return list of enabled savers
     */
    private List<Saver> getEnabledSavers()
    {
    	List<Saver> savers = new ArrayList<Saver>();
    	Vector<String> names = M3Config.getInstance().getEnabledSavers();
    	for(Saver s : _savers)
    	{
    		if(names.contains(s.getName()))
    			savers.add(s);
    	}
    	return savers;
    }
    
    
    /**
     * Saves the searchable (film, episode, series...)
     * with all enabled savers
     * @param searchable
     */
    public void save(Searchable searchable)
    {
    	List<Saver> enabledSavers = getEnabledSavers(); //From xml configuration file
    	for(Saver s : enabledSavers)
    	{
    		try {
				if(searchable instanceof Film)
				{
					Film media = (Film) searchable;
					s.save(media.getInfo(), media.getFilename());
				} else if(searchable instanceof Series)
				{
					Series series = (Series) searchable;
					s.saveSeries(series.getInfo(), series.getDirectory());
					
				} else if(searchable instanceof SeriesEpisode)
				{
					SeriesEpisode ep = (SeriesEpisode) searchable;
					s.saveEpisode(ep.getInfo(), ep.getFilename());
				}
			} catch (ProviderException e) {
				Logger.getInstance().write("Erreur Saver :" + e.getMessage());
			}
    	}
    }
}

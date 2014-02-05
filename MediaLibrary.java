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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
 * Utility class allowing to link database and local files
 * @author vincent
 *
 */
public class MediaLibrary {
	//Pour le Singleton
    private static volatile MediaLibrary instance = null;
    /**
     * Constructeur de l'objet.
     */
    private MediaLibrary() {
        super();
    }

    /**
     * Méthode permettant de renvoyer une instance de la classe Singleton
     * @return Retourne l'instance du singleton.
     */
    public final static MediaLibrary getInstance() {
        if (MediaLibrary.instance == null) {
           synchronized(MediaLibrary.class) {
             if (MediaLibrary.instance == null) {
            	 MediaLibrary.instance = new MediaLibrary();
             }
           }
        }
        return MediaLibrary.instance;
    }
    
    private static void getFiles(File file, Collection<File> all) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                all.add(child);
                getFiles(child, all);
            }
        }
    }
    
    /**
     * Search in the media directories every film or episode to add to the database
     */
    public void refreshDB()
    {
    	List<String> films = M3Config.getInstance().getFilmsDirectories();
    	List<String> series = M3Config.getInstance().getSeriesDirectories();
    	
    	for(String fdir : films)
    	{
 		    Collection<File> all = new ArrayList<File>();
		    getFiles(new File(fdir), all);
		    for(File f : all)
		    {
		    	String filename = f.getPath();
		    	if(filename.endsWith(".mkv") || filename.endsWith(".avi"))
		    	{
		    		Media media = Media.getByFilename(filename);
		    		if(media==null)
		    		{
		    			String name = f.getName();
		    			name = name.replaceAll(".mkv", "");
		    			name = name.replaceAll(".avi", "");

		    			media=new Film(name,filename);
		    			media.save();
		    		}
		    	}
		    }
    	}
    	
    	
    	for(String fdir : series)
    	{
 		    Collection<File> all = new ArrayList<File>();
		    getFiles(new File(fdir), all);
		    for(File f : all)
		    {
		    	String filename = f.getPath();
		    	if(filename.endsWith(".mkv") || filename.endsWith(".avi"))
		    	{
		    		Media media = Media.getByFilename(filename);
		    		if(media==null)
		    		{
		    			String name = f.getName();
		    			name = name.replaceAll(".mkv", "");
		    			name = name.replaceAll(".avi", "");
		    			
		    			String seriesPath = new File(fdir).toURI().relativize(new File(f.getAbsolutePath()).toURI()).getPath();
		    			
		    			int i;
		    			i=seriesPath.indexOf("/");
		    			if(i<0)
		    				i=seriesPath.indexOf("\\");
		    			if(i>0)
		    				seriesPath = seriesPath.substring(0, i);

		    			
		    			media=SeriesEpisode.fromFilename(name,filename, new File(fdir + "/" + seriesPath.trim()).getAbsolutePath());
		    			if(media != null)
		    				media.save();
		    		}
		    	}
		    }
    	}
    }
    
    
    
}

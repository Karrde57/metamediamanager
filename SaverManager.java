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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;


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
           synchronized(ProviderManager.class) {
             if (SaverManager.instance == null) {
            	 SaverManager.instance = new SaverManager();
             }
           }
        }
        return SaverManager.instance;
    }
    
    public void loadSavers()
    {
    	_savers.add(new XBMCSaver());
    }
    
    public void save(Media media)
    {
    	for(Saver s : _savers)
    	{
    		s.save(media);
    	}
    }
}

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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarFile;

/**
 * Classe gérant le chargement et la validation des plugins
 * @author Lainé Vincent (dev01, http://vincentlaine.developpez.com/ )
 *
 */
public class PluginsLoader {

	private String[] files;
	
	private ArrayList<Class<?>> classProvider;
	private ArrayList<Class<?>> classSaver;
	
	/**
	 * Constructeur par défaut
	 *
	 */
	public PluginsLoader(){
		this.classProvider = new ArrayList<Class<?>>();
		this.classSaver = new ArrayList<Class<?>>();
	}
	
	/**
	 * Constucteur initialisant le tableau de fichier à charger.
	 * @param files Tableau de String contenant la liste des fichiers à charger.
	 */
	public PluginsLoader(String[] files){
		this();
		this.files = files;
	}
	
	/**
	 * Défini l'ensemble des fichiers à charger
	 * @param files
	 */
	public void setFiles(String[] files ){
		this.files = files;
	}

	
	/**
	 * Fonction de chargement de tout les plugins de type Provider
	 * @return Une collection de Provider contenant les instances des plugins
	 * @throws Exception si file = null ou file.length = 0
	 */
	public Provider[] loadAllProvider() throws Exception {
		
		this.initializeLoader();
		
		Provider[] tmpPlugins = new Provider[this.classProvider.size()];
		
		for(int index = 0 ; index < tmpPlugins.length; index ++ ){
			
			//On créer une nouvelle instance de l'objet contenu dans la liste grâce à newInstance() 
			//et on le cast en StringPlugins. Vu que la classe implémente StringPlugins, le cast est toujours correct
			tmpPlugins[index] = (Provider)((Class<?>)this.classProvider.get(index)).newInstance() ;
			
		}
		
		return tmpPlugins;
		
	}
	
	/**
	 * Fonction de chargement de tout les plugins de type Saver
	 * @return Une collection de Provider contenant les instances des plugins
	 * @throws Exception si file = null ou file.length = 0
	 */
	public Saver[] loadAllSaver() throws Exception {
		
		this.initializeLoader();
		
		Saver[] tmpPlugins = new Saver[this.classSaver.size()];
		
		for(int index = 0 ; index < tmpPlugins.length; index ++ ){
			
			//On créer une nouvelle instance de l'objet contenu dans la liste grâce à newInstance() 
			//et on le cast en StringPlugins. Vu que la classe implémente StringPlugins, le cast est toujours correct
			tmpPlugins[index] = (Saver)((Class<?>)this.classSaver.get(index)).newInstance() ;
			
		}
		
		return tmpPlugins;
		
	}
	
	
	private void initializeLoader() throws Exception{
		//On vérifie que la liste des plugins à charger à été initialisé
		if(this.files == null || this.files.length == 0 ){
			throw new Exception("Pas de fichier spécifié");
		}

		//Pour eviter le double chargement des plugins
		if(this.classProvider.size() != 0 || this.classSaver.size() != 0 ){
			return ;
		}
		
		File[] f = new File[this.files.length];
//		Pour charger le .jar en memoire
		URLClassLoader loader;
		//Pour la comparaison de chaines
		String tmp = "";
		//Pour le contenu de l'archive jar
		Enumeration<?> enumeration;
		//Pour déterminé quels sont les interfaces implémentées
		Class<?> tmpClass = null;
		
		for(int index = 0 ; index < f.length ; index ++ ){
			
			f[index] = new File(this.files[index]);
			
			if( !f[index].exists() ) {
				break;
			}
			
			@SuppressWarnings("deprecation")
			URL u = f[index].toURL();
			//On créer un nouveau URLClassLoader pour charger le jar qui se trouve ne dehors du CLASSPATH
			loader = new URLClassLoader(new URL[] {u}); 
			
			//On charge le jar en mémoire
			JarFile jar = new JarFile(f[index].getAbsolutePath());
			
			//On récupére le contenu du jar
			enumeration = jar.entries();
			
			while(enumeration.hasMoreElements()){
				
				tmp = enumeration.nextElement().toString();

				//On vérifie que le fichier courant est un .class (et pas un fichier d'informations du jar )
				if(tmp.length() > 6 && tmp.substring(tmp.length()-6).compareTo(".class") == 0) {
					
					tmp = tmp.substring(0,tmp.length()-6);
					tmp = tmp.replaceAll("/",".");
					
					tmpClass = Class.forName(tmp ,true,loader);
					
					for(int i = 0 ; i < tmpClass.getInterfaces().length; i ++ ){
						
						//Une classe ne doit pas appartenir à deux catégories de plugins différents. 
						//Si tel est le cas on ne la place que dans la catégorie de la première interface correct
						// trouvée
							if( tmpClass.getInterfaces()[i].getName().toString().equals("org.t3.metamediamanager.Provider") ) {
								this.classProvider.add(tmpClass);
							}
							if( tmpClass.getInterfaces()[i].getName().toString().equals("org.t3.metamediamanager.Saver") ) {
								this.classSaver.add(tmpClass);
							}
					}
					
				}
			}
			
			
			jar.close();
			
		
		}
		
	}
	
	
}

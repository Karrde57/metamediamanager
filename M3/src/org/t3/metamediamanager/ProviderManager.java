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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Load and manages providers
 * @author vincent
 *
 */
public class ProviderManager {
	private class ProviderPriority {
		public String providerName;
		public int priority;
	}
	
	//Pour le Singleton
    private static volatile ProviderManager instance = null;
    
    private Vector<Provider> _providers = new Vector<Provider>();
    private HashMap<String, String[]> _priorities = new HashMap<String, String[]>();
    
    private File _configFile;
    
    private String _errorLog ="";
    
    /**
     * Constructeur de l'objet.
     */
    private ProviderManager() {
        super();
    }

    /**
     * Méthode permettant de renvoyer une instance de la classe Singleton
     * @return Retourne l'instance du singleton.
     */
    public final static ProviderManager getInstance() {
        if (ProviderManager.instance == null) {
           synchronized(ProviderManager.class) {
             if (ProviderManager.instance == null) {
            	 ProviderManager.instance = new ProviderManager();
             }
           }
        }
        return ProviderManager.instance;
    }
    
    /**
     * Load the XML configuration file containing especially the priorities
     * @param basename
     */
    public void loadConfig(String basename)
    {
    	String filename = M3Config.getInstance().getUserConfDirectory() + basename;
    	
    	_configFile = new File(filename);
    	if(!_configFile.exists())
    		M3Config.getInstance().copyResourceToUserDir(basename, basename);
    	
    	//On crée une instance de SAXBuilder
        SAXBuilder sxb = new SAXBuilder();
        try
        {
           //On crée un nouveau document JDOM avec en argument le fichier XML
           //Le parsing est terminé ;)
           Document document = sxb.build(_configFile);


        //On initialise un nouvel élément racine avec l'élément racine du document.
        Element root = document.getRootElement();
        
        //Remplissage des paramètres
        List<Element> listFields = root.getChildren("field");
        
        Iterator<Element> i = listFields.iterator();
        while(i.hasNext())
        {
           Element current = i.next();
           List<ProviderPriority> providersNames = new ArrayList<ProviderPriority>();
           
           List<Element> listProviders = current.getChildren("provider");
           Iterator<Element> i2 = listProviders.iterator();
           
           while(i2.hasNext())
           {
        	   Element currentP = i2.next();
        	   ProviderPriority pp = new ProviderPriority();
        	   pp.providerName = currentP.getAttributeValue("name");
        	   pp.priority = Integer.parseInt(currentP.getAttributeValue("priority"));
        	   providersNames.add(pp);
           }
           
           //Tri
           Collections.sort(providersNames, new Comparator<ProviderPriority>(){
				@Override
				public int compare(ProviderPriority a, ProviderPriority b) {
					return a.priority - b.priority;
				}  	   
           });
           
          
           //Build a simple array
           String[] providersNamesString = new String[providersNames.size()];
           for(int j=0; j<providersNames.size(); j++)
        	   providersNamesString[j] = providersNames.get(j).providerName;
           
           _priorities.put(current.getAttributeValue("name"), providersNamesString);
        }
        
        }
        catch(Exception e){
        	System.out.println("Erreur lors de l'ouverture du fichier de configuration.");
        }
        
    }
    
    /**
     * This method returns only the providers enabled in the XML configuration file
     * @return enabled providers names
     */
    private Vector<Provider> getEnabledProviders()
    {
    	Vector<Provider> res = new Vector<Provider>();
    	Vector<String> enabledProvidersNames = M3Config.getInstance().getEnabledProviders();
    	for(Provider p: _providers)
    	{
    		if(enabledProvidersNames.contains(p.getName()))
    			res.add(p);
    	}
    	return res;
    }
    
    /**
     * Loads and creates an instance of each provider
     */
    public void loadProviders()
    {
    	//Base Providers
    	_providers.add(new OmdbProvider());
    	_providers.add(new ProviderXBMC());
    	_providers.add(new TheMovieDBProvider());
    	_providers.add(new OpenSubtitlesProvider());
    	_providers.add(new TheTvdbProvider());
    	_providers.add(new AllocineProvider());
    	_providers.add(new MediaBrowserProvider());
    	
    	//Plugin Providers
    	//They are stored in /home/user/.m3/plugins
    	File pluginsDir = new File(M3Config.getInstance().getUserConfDirectory() + File.separator + "plugins");
    	if(pluginsDir.exists())
    	{
    		File[] potentialPlugins = pluginsDir.listFiles();
    		List<String> jarFileList = new ArrayList<String>();
    		//If jar file, we add to the list of providers to load
    		for(File p : potentialPlugins)
    		{
    			if(p.getAbsolutePath().endsWith(".jar"))
    				jarFileList.add(p.getAbsolutePath());
    		}
    		String[] jarFilesTab = new String[jarFileList.size()];
    		jarFileList.toArray(jarFilesTab);
    		PluginsLoader loader = new PluginsLoader(jarFilesTab);
    		
    		try {
				Provider[] loadedProviders = loader.loadAllProvider();
				
				for(Provider p : loadedProviders)
					_providers.add(p);
			} catch (Exception e) {

			}
    		
    		
    	}
    }
    
    //Give all field names of every mediainfo
    private List<String> getInfosNames(HashMap<Provider, MediaInfo> mediaInfos)
    {
    	List<String> res = new ArrayList<String>();
    	for(Entry<Provider, MediaInfo> entry : mediaInfos.entrySet()) {
    		MediaInfo infos = entry.getValue();
    		for(Entry<String,String> field : infos.entrySet())
    		{
    			if(!res.contains(field.getKey()))
        		{
        			res.add(field.getKey());
        		}
    		}
    		
    		
    	}
    	return res;
    }
    
    /**
     * Returns the provider object from its string name
     * @param name
     * @return provider object or null if not found
     */
    private Provider providerFromName(String name)
    {
    	for(Provider p: _providers)
    	{
    		if(p.getName().equals(name))
    			return p;
    	}
    	return null;
    }
    
    
    /**
     * Send a copy of the request to every provider, and sort the results depending on the priorities
     * @param request
     * @return response of the request
     */
    public ProviderResponse getInfo(MediaInfo res, ProviderRequest request)
    {
    	
		
		String errors = "";
		
		List<Provider> providersWithoutAnswer = new ArrayList<Provider>();
		HashMap<Provider, MediaInfo> mediaInfos = new HashMap<Provider,MediaInfo>();
		
		List<String> suggested = new ArrayList<String>();
		
		Vector<Provider> enabledProviders = this.getEnabledProviders();
		
		//On fait une requete sur tous les providers
		for(Provider provider : enabledProviders)
		{
		
			ProviderResponse response;
			try {
				response = provider.query(request);
				if(response.getType() == ProviderResponse.Type.FOUND)
					mediaInfos.put(provider, response.getResponse());
				else if(response.getType() == ProviderResponse.Type.SUGGESTED) {
					suggested.addAll(Arrays.asList(response.getSuggested()));
					providersWithoutAnswer.add(provider);
				} else
					providersWithoutAnswer.add(provider);
			} catch (ProviderException e) {
				errors+=e.getMessage()+"\n";
			}

		}
		
		if(providersWithoutAnswer.size() > 0)
		{
			if(mediaInfos.size() > 0 && mediaInfos.values().iterator().next().containsKey("title"))
			{
				String perfectName = mediaInfos.values().iterator().next().get("title");
				for(Provider provider : providersWithoutAnswer)
				{
					ProviderRequest newRequest = new ProviderRequest(request.getType(), perfectName, request.getFilename(), request.getLanguage());

					ProviderResponse response;
					try {
						response = provider.query(newRequest);
						if(response.getType() == ProviderResponse.Type.FOUND)
							mediaInfos.put(provider, response.getResponse());
						else if(response.getType() == ProviderResponse.Type.SUGGESTED)
							suggested.addAll(Arrays.asList(response.getSuggested()));
					} catch (ProviderException e) {
						errors+=e.getMessage()+"\n";
					}

				}
			}
			
			//Si on a vraiment rien trouvé
			if(mediaInfos.size() == 0)
			{
				if(suggested.size() > 0)
				{
					String[] array = new String[suggested.size()];
					suggested.toArray(array);
					return new ProviderResponse(array);
				} else {
					return new ProviderResponse();
				}
			}
		}
		
		
		//On mix le tout en prenant en compte les priorités
		List<String> infosNames = getInfosNames(mediaInfos);
		for(String fieldName : infosNames)
		{
			if(_priorities.containsKey(fieldName))
			{
				if(fieldName.startsWith("img_")) //Cas image : on les mets ensemnle
				{
					List<String> urls = new ArrayList<String>();
					String[] ppliste = _priorities.get(fieldName);
					for(String pp : ppliste) //Rappel : ppliste est trié par priorité.
					{
						Provider provider = providerFromName(pp);
						//On vérifie que ce provider là a trouvé qqchose
						if(mediaInfos.containsKey(provider) && mediaInfos.get(provider).containsKey(fieldName))
						{
							String[] urlsLocalProvider = mediaInfos.get(provider).getImages(fieldName);
							for(String url : urlsLocalProvider)
							{
								urls.add(url);
							}
								
						}
					}
					String[] urlsArray = new String[urls.size()];
					urls.toArray(urlsArray);
					res.putImages(fieldName, urlsArray);
				} else { //Cas autre information, on cherche par priorité
					String[] ppliste = _priorities.get(fieldName);
					for(String pp : ppliste) //Rappel : ppliste est trié par priorité.
					{
						Provider provider = providerFromName(pp);
						//On vérifie que ce provider là a trouvé qqchose
						if(mediaInfos.containsKey(provider) && mediaInfos.get(provider).containsKey(fieldName))
						{
							res.put(fieldName, mediaInfos.get(provider).get(fieldName));
							break; //On sort de la boucle, cette info a été validée
						}
					}
				}
				
			}
		}
		
		//Gestion des acteurs
		String[] ppliste = _priorities.get("actor");
		for(String pp : ppliste)
		{
			Provider provider = providerFromName(pp);
			if(mediaInfos.containsKey(provider) && mediaInfos.get(provider).getActors().length > 0)
			{
				res.setActors(mediaInfos.get(provider).getActors());
				break;
			}
		}
		
		_errorLog = errors;
		
		//Log
		String log = "RECHERCHE " + request;
		for(Entry<Provider,MediaInfo> mi : mediaInfos.entrySet())
		{
			log+="Résultat " + mi.getKey().getName() + "\n";
			log+=mi.getValue()+"\n";
		}
		Logger.getInstance().write(log);
		if(!_errorLog.isEmpty())
		{
			Logger.getInstance().write("[ERROR] " + _errorLog);
		}
		
		System.out.println("errors : " + _errorLog);
		
		return new ProviderResponse(res);
    }
    
    public String getErrorLog()
    {
    	return _errorLog;
    }
    
    public void cleanErrorLog()
    {
    	_errorLog="";
    }
    
    public HashMap<String, String[]> getPriorities()
    {
    	return _priorities;
    }
    
    public void setPriorities(HashMap<String, String[]> p)
    {
    	_priorities = p;
    }
    
    public String[] getProvidersNameList()
    {
    	String[] names = new String[_providers.size()];
    	for(int i=0; i<_providers.size(); i++)
    		names[i] = _providers.get(i).getName();
    	return names;
    }
    
    public Vector<Provider> getProviders()
    {
    	return _providers;
    }
    
    
    public void save()
    {
    	try
        {
    		Element root = new Element("fields");
    		Document doc = new Document(root);
    		
    		for(Entry<String,String[]> infoType : _priorities.entrySet())
    		{
    			Element infoTypeElement = new Element("field");
    			infoTypeElement.setAttribute("name", infoType.getKey());
    			int priority = 1;
    			for(String providerName : infoType.getValue())
    			{
    				Element providerElement = new Element("provider");
    				providerElement.setAttribute("name", providerName);
    				providerElement.setAttribute("priority", ""+priority);
    				infoTypeElement.addContent(providerElement);
    				priority++;
    			}
    			root.addContent(infoTypeElement);
    		}
    		
    		//Save
    	    XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
    	    sortie.output(doc, new FileOutputStream(_configFile));
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
    
}

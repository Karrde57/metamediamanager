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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import org.json.*;

/**
 *Allow to handle the Omdb API
 * @author Nicolas
 *
 */
public class OmdbProvider implements Provider {
	
	private String url;
	private String charset;
	private String query;
	
	private FieldsConfig _config = new FieldsConfig(getName());
	

	public OmdbProvider ()
	{
		url ="http://www.omdbapi.com/";
		charset = "UTF-8";
	}
	
	
	/**
	 * Connects to the omdb API and return the JSONObject associates with the query.
	 * @param s
	 * 		The query.
	 * @param mode
	 * 		The way of your query :
	 * 			-s = general query (with the MovieTitle)
	 * 			-i = precise query (with the MovieId)
	 * @return A JSONObject which contains the data.
	 * @throws ProviderException
	 */
	private JSONObject getJSON(String s, char mode) throws ProviderException
	{
		try {
			if (mode =='i')
			{
				query = String.format("i=%s", URLEncoder.encode(s, charset));
			}
			else if (mode == 's')
			{
				query = String.format("s=%s", URLEncoder.encode(s, charset));
			}
			URLConnection connection = new URL (url+"?"+query ).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();
			String rep = convertStreamToString(response);
			return convertStringToJSON(rep);
		}
		catch (IOException e) {
			throw new ProviderException("Erreur d'accès à l'API !");
		}
	}
	
	/**
	 * Converts an InputStream object into a String object.
	 * @param is
	 * 		The InputStream you want to convert.
	 * @return
	 * 		A String Object.
	 */
	private static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is);
	    Scanner s2 = s.useDelimiter("\\A");
	    String res = s.hasNext() ? s.next() : "";
	    s2.close();
	    s.close();
	    return res;
	}
	
	/**
	 * Converts a String object into a JSONObject object.
	 * @param s
	 * 		The String object you want to convert.
	 * @return
	 * 		A JSONObject.
	 * @throws ProviderException
	 */
	private static JSONObject convertStringToJSON (String s) throws ProviderException
	{
		JSONObject json;
		try {
			json = new JSONObject (s);
		} catch (JSONException e) {
			throw new ProviderException("Problème API : erreur JSON !");
		}
		return json;
	}

	/**
	 * @return
	 * 		A String Object which contains the name of the provider ("omdb").
	 */
	@Override
	public String getName() {
		return "omdb";
	}
	
	@Override
	public String[] getConfigFiles()
	{
		String[] tab = new String[1];
		tab[0] = getName();
		return tab;
	}

	/**
	 * Returns the MediaInfos object associates with HTTP request.
	 * @param id
	 * 		The movie id.
	 * @return
	 * 		A MediaInfos Object.
	 * @throws ProviderException
	 */
	private MediaInfo getInfoI(String id) throws ProviderException {
		JSONObject json = getJSON(id,'i');
				try {
					if ((json.getString("Response").compareTo("True"))==0)			//vérifie que l'API a trouvé quelque chose !
					{
						MediaInfo infos = new MediaInfo();
						HashMap<String, String> mapInfos = _config.getFieldsAssociation();
						
						for(Entry<String, String> entry : mapInfos.entrySet()) {
								 String value = entry.getValue();
								 String cle = entry.getKey();
								 String imagePath;
								 String[] tab = new String[1];
								 if (json.has(value))
								 {
									 if (cle.equals("img_poster") && !json.getString(value).equals("N/A"))	//gère les images
									 {
										 imagePath=json.getString(value);
										 tab[0] = createPicture(imagePath);
										 infos.putImages(cle, tab);
									 }
									 
									 else if (cle.equals("actor") && !json.getString(value).equals("N/A"))	//gère les acteurs
									 {
										 String s = json.getString(value);
										 String[] tabName = s.split(", ");
										 ActorInfo[] tabActor = new ActorInfo[tabName.length];
											for (int i =0; i<tabName.length; i++)
											{
												ActorInfo acteur = new ActorInfo(tabName[i]);
												tabActor[i]=acteur;
											}
											infos.setActors(tabActor);
									 }
									 
									 else if(!json.getString(value).equals("N/A"))		//autres valeurs
									 {
										 infos.put(cle, json.getString(value));
									 }
								 }
						}
						return infos;
					}
				} catch (JSONException e) {
					throw new ProviderException ("Erreur JSON : renvoi NULL");
				}
				return null;
	}
	
	/**
	 * Returns a list who contains all the movies which match with the title given in parameter.
	 * The list contains two Strings : the movieID associates with the movieTitle
	 * @param titre
	 * 		The movieTitle.
	 * @return A list who contains two Strings : the movieID associates with the movieTitle.
	 * @throws ProviderException
	 */
	//renvoi une liste d'une paire de deux chaines de carac
	private List<AbstractMap.SimpleEntry<String, String>> getInfoS(String titre) throws ProviderException 
	{
		List<AbstractMap.SimpleEntry<String, String>> tab =  new ArrayList<AbstractMap.SimpleEntry<String, String>>();
		JSONObject json = getJSON(titre,'s');	//requete JSon
		
		if (json.has("Search"))			//vérifie que l'API a trouvé quelque chose !
		{
				JSONArray jtab;
					try {
						jtab = json.getJSONArray("Search");
					
						for(int i=0; i<jtab.length(); i++) //pour chaques films trouvés (qui se trouvent dans le JSONArray)
						{
							//ajoute l'id et le titre du film dans la liste (id en premier et nom en deuxième)
							tab.add(new AbstractMap.SimpleEntry<String,String>(((JSONObject)jtab.get(i)).getString("imdbID"), ((JSONObject)jtab.get(i)).getString("Title")));
						}
					} catch (JSONException e) {
						throw new ProviderException ("Erreur JSON : renvoi NULL");
					}
		}
		return tab;
	}
	
	/**
	 * Creates a PictureFile in the default temporary-file directory.
	 * @param path
	 * 		The URL of the Picture.
	 * @return A String object which contains the Temporary-file AbsolutePathname.
	 * @throws ProviderException
	 */
	private String createPicture (String path) throws ProviderException				
	{
		URL urlPic;
			try {
				urlPic = new URL(path);
				InputStream fileIn = urlPic.openStream();  
				File filePic = File.createTempFile("image",".jpg");  
				String pathPic = filePic.getAbsolutePath();
				BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(filePic));
				int b;  
				while ((b = fileIn.read()) != -1)  
				{
					fileOut.write(b);
				}
				fileOut.flush();  
				fileOut.close();  
				fileIn.close();  
				return pathPic; 
			} catch (IOException e) {
				throw new ProviderException("Erreur image : Erreur pendant l'import ou l'ajout du poster");
			}
	}


	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {
		//appelle getInfoS (renvoi tab string)
		//si 1 seul string est renvoyé, appelle getInfoI (avec string id) 
		//sinon renvoi le tab dans provider response
		
		if(r.getType() == ProviderRequest.Type.FILM)	//si on cherche un film
		{
			String name = r.getName();
				List<AbstractMap.SimpleEntry<String, String>> tab = getInfoS(name);	//récupère la liste des paires imdbid/titre
				
				if(tab.size() == 0)	//si la liste est vide
					return new ProviderResponse();	//retourne ProviderResponse NOT_FOUND
				
				int i=0;
				String[] suggests = new String[tab.size()];	//tableau de la taille de la liste
				for(AbstractMap.SimpleEntry<String, String> entry : tab) {
					 String title = entry.getValue();
					 String id = entry.getKey();
					 if (title.equalsIgnoreCase(name))	//si un des titre de la liste est exactement le même (sauf casse) que le titre de la requête (var name)
					 {
						MediaInfo info = getInfoI(id);
						return new ProviderResponse(info);	//renvoi un ProviderResponse d'un MediaInfo
					 }
					 
					 suggests[i]=title;	//sinon rempli le tableau avec les titres des films
					 i++;
				}
				return new ProviderResponse(suggests);// renvoi le tableau des suggestions des films
			}

		else {
			return new ProviderResponse();	//enfin renvoi un ProviderResponse NOT_FOUND si le type n'est pas correct
		}
	}
}

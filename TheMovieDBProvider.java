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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Allow to handle theMovieDB API
 * @author Nicolas
 *
 */
public class TheMovieDBProvider implements Provider {

	private String url;
	private String charset;
	private String query;
	private JSONObject json;
	private String apiKey;
	
	private FieldsConfig _config = new FieldsConfig(getName());
	
	//http://api.themoviedb.org/3/movie/550?api_key=ebb392e0588bc0b5ab9d9a6100711a8c&append_to_response=casts&language=fr
	public TheMovieDBProvider() {
		url ="http://api.themoviedb.org/";
		apiKey = "ebb392e0588bc0b5ab9d9a6100711a8c";
		charset = "UTF-8";
	}
	
	
	/**
	 * 
	 * @param s
	 * 		The query.
	 * @param i
	 * The way of your query :
	 * 			-s = general query (with the MovieTitle)
	 * 			-i = precise query (with the MovieId)
	 * @param language
	 * 		The language in which one you want the informations.
	 * @return A JSONObject which contains the data.
	 * @throws ProviderException
	 */
	public JSONObject getJSON(String s, int i, String language) throws ProviderException
	{
		try {
			if (i =='i')
			{
				//http://api.themoviedb.org/3/movie/550?api_key=ebb392e0588bc0b5ab9d9a6100711a8c
				query = String.format("http://api.themoviedb.org/3/movie/%s?api_key=ebb392e0588bc0b5ab9d9a6100711a8c&append_to_response=casts&language=%s", 
						URLEncoder.encode(s, charset), URLEncoder.encode(language,charset));
			}
			else if (i == 's')
			{
				query = String.format("http://api.themoviedb.org/3/search/movie?api_key=ebb392e0588bc0b5ab9d9a6100711a8c&query=%s", URLEncoder.encode(s, charset));
			}
			URLConnection connection = new URL (query).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();
			String rep = convertStreamToString(response);
			return json = convertStringToJSON(rep);
		}
		catch (IOException e) {
			throw new ProviderException("Erreur d'accès à l'API !" +e.getMessage());
		}
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
	 * Converts an InputStream object into a String object.
	 * @param is
	 * 		The InputStream you want to convert.
	 * @return
	 * 		A String Object.
	 */
	private static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	/**
	 * Returns the MediaInfos object associates with HTTP request.
	 * @param id
	 * 		The movie ID.
	 * @param lang
	 * 		The language in which one you want the informations.
	 * @return
	 * 		A MediaInfos Object.
	 * @throws ProviderException
	 */
	public MediaInfo getInfoI(String id, String lang, boolean allImages) throws ProviderException {
		JSONObject json = getJSON(id,'i', lang);
		JSONArray jtabCast;
		

		
				try {

						MediaInfo infos = new MediaInfo();
						HashMap<String, String> mapInfos = _config.getFieldsAssociation();
						
						for(Entry<String, String> entry : mapInfos.entrySet()) {
								 String value = entry.getValue();
								 String cle = entry.getKey();
								 String imagePath;
								 String[] tab = new String[1];
								 if (json.has(value))
								 {
									 if (cle.equals("genre") && !json.isNull(value))	//gère les images
									 {
										JSONArray jtab = json.getJSONArray("genres");
										String val= "";
										for (int i =0; i< jtab.length(); i++)
										{
											
											if(((JSONObject) jtab.get(i)).getString("name") != null)
											{
												if (i != 0)
												{
													val += ", ";
												}
												val += (jtab.getJSONObject(i)).getString("name");
											}
										}
										infos.put(cle, val);
									 }
									 
									 
									 else if(cle.equals("director"))
									 {
										jtabCast = json.getJSONObject("casts").getJSONArray("crew");
										String res = "";
										int cpt =0;
										for (int i =0; i<jtabCast.length();i++)
										{
											
											JSONObject jsondir = jtabCast.getJSONObject(i);
												if (jsondir.getString("job").equals("Director"))
												{
													if(cpt!=0)
													{
														res += ", ";
													}
													res += jsondir.getString("name");
													cpt ++;
												}
										}
										infos.put(cle, res);
									 }
									 
									 
									 else if(cle.equals("writer"))
									 {
										jtabCast = json.getJSONObject("casts").getJSONArray("crew");
										String res = "";
										int cpt =0;
										for (int i =0; i<jtabCast.length();i++)
										{
											
											JSONObject jsondir = jtabCast.getJSONObject(i);
												if (jsondir.getString("job").equals("Author"))
												{
													if(cpt!=0)
													{
														res += ", ";
													}
													res += jsondir.getString("name");
													cpt ++;
												}
										}
										infos.put(cle, res);
									 }
									 
									else if(cle.equals("actor"))
									 {
										jtabCast = json.getJSONObject("casts").getJSONArray("cast");
										ActorInfo[] tabActor = new ActorInfo[jtabCast.length()];
										for(int j =0; j<jtabCast.length(); j++)
										{
											JSONObject acteur = jtabCast.getJSONObject(j);
											String nom = acteur.getString("name");
											String role = acteur.getString("character");
											String imageActeur = "";
											if (!acteur.isNull("profile_path"))
											{
												imageActeur = createPicture(acteur.getString("profile_path"));
											}
											ActorInfo AF = new ActorInfo(nom,role,imageActeur);
											tabActor[j] = AF;
										}
										infos.setActors(tabActor);
									 }
									 
										 else if ((cle.equals("img_poster")|| cle.equals("img_backdrop")) && !json.getString(value).equals("null"))
									 {
										 String path = json.getString(value);
										 String pathTempFile = createPicture(path);
										 infos.put(cle, pathTempFile);
									 }

									 else if(!json.getString(value).equals("null"))		//autres valeurs
									 {
										 infos.put(cle, json.getString(value));
									 }
								 }
							 }
						
						if(allImages)
						{
							HashMap<String,String[]> img = getAllImages(id, lang);
							for(Entry<String,String[]> entry : img.entrySet())
							{
								//Note : themoviedb use "posters" and "backdrop" instead of "poster" and "backdrop". Removes the "s"
								String nameImgType = entry.getKey();
								if(nameImgType.endsWith("s"))
									nameImgType=nameImgType.substring(0, nameImgType.length()-1);
								
								infos.putImages("img_"+nameImgType, entry.getValue());
							}
						}
						
						return infos;
				}
					catch (JSONException e) {
					throw new ProviderException ("Erreur JSON : renvoi NULL" + e.getMessage());
				}
	}
	
	/**
	 * Returns a list who contains all the movies which match with the title given in parameter.
	 * The list contains two Strings : the movieID associates with the movieTitle
	 * @param titre
	 * 		The movieTitle.
	 * @return A list who contains two Strings : the movieID associates with the movieTitle.
	 * @throws ProviderException
	 */
	private List<AbstractMap.SimpleEntry<String, String>> getInfoS(String titre) throws ProviderException 
	{
		List<AbstractMap.SimpleEntry<String, String>> tab =  new ArrayList<AbstractMap.SimpleEntry<String, String>>();
		JSONObject json = getJSON(titre,'s', "");	//requete JSon
		
		if (json.has("results"))			//vérifie que l'API a trouvé quelque chose !
		{
				JSONArray jtab;
					try {
						jtab = json.getJSONArray("results");
						
					
						for(int i=0; i<jtab.length(); i++) //pour chaques films trouvés (qui se trouvent dans le JSONArray)
						{
							//ajoute l'id et le titre du film dans la liste (id en premier et nom en deuxième)
							if (!jtab.isNull(i) && jtab.getJSONObject(i).has("id") && jtab.getJSONObject(i).has("original_title"))
							{
								tab.add(new AbstractMap.SimpleEntry<String,String>(jtab.getJSONObject(i).getString("id"), jtab.getJSONObject(i).getString("original_title")));
							}
						}
					} catch (JSONException e) {
						throw new ProviderException ("Erreur JSON : renvoi NULL" +e.getMessage());
					}
		}
		return tab;
	}
	
	private HashMap<String, String[]> getAllImages(String id, String language)
	{
		HashMap<String,String[]> res = new HashMap<String,String[]>(); //Final hashmap to be returned
		

		String query = String.format("http://api.themoviedb.org/3/movie/"+id+"/images?api_key="+apiKey);

		try {
			URLConnection connection = new URL (query).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();
			String rep = convertStreamToString(response);


			JSONObject json = convertStringToJSON(rep);
			
			String[] types = {"backdrops", "posters"};
			
			//We only select the 10 most voted images. We use this class to do the final sort
			class ImageInfo implements Comparable<ImageInfo> {
				String name;
				double priority;
				@Override
				public int compareTo(ImageInfo o) {
					return (int)(priority - o.priority);
				}
			}
			
			//Use of a temp hashmap with a list to be sorted
			HashMap<String, List<ImageInfo>> tmpRes = new HashMap<String, List<ImageInfo>>();
			
			
			//For each type of image (poster or backdrop)
			for(String type : types)
			{
				if(json.has(type))
				{
					JSONArray typeJson = json.getJSONArray(type);
					
					List<ImageInfo> imgList = new ArrayList<ImageInfo>();
					
					tmpRes.put(type, imgList);

					int nbImages = typeJson.length();
					for(int i=0; i<nbImages; i++) //For every image
					{
						JSONObject imgObject = typeJson.getJSONObject(i);
						
						if(imgObject.has("file_path") && imgObject.has("vote_average") && imgObject.has("iso_639_1"))
						{
							if(imgObject.isNull("iso_639_1") || imgObject.getString("iso_639_1").equals(language))
							{
								ImageInfo ii = new ImageInfo();
								ii.name = imgObject.getString("file_path");
								ii.priority = imgObject.getDouble("vote_average");
								imgList.add(ii);
							}
							
							
						}
					}
					
					//We sort by priority/vote
					Collections.sort(imgList, Collections.reverseOrder());

					int nbImagesFinal = (imgList.size() > 10) ? 10 : imgList.size();
					res.put(type, new String[nbImagesFinal]);
					for(int i=0; i<nbImagesFinal; i++)
					{
						res.get(type)[i] = createPicture(imgList.get(i).name); //Download the image, and keep the filename of the temp file
					}
				}
				
				
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return res;
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
				String query = "http://d3gtl9l2a4fn1j.cloudfront.net/t/p/w185" + path;
				urlPic = new URL(query);
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
				throw new ProviderException("Erreur image : Erreur pendant l'import ou l'ajout de l'image");
			}
	}
	
	
	
	
	/**
	 * @return
	 * 		A String Object which contains the name of the provider ("omdb").
	 */
	@Override
	public String getName() {
		return "theMovieDB";
	}
	
	@Override
	public String[] getConfigFiles()
	{
		String[] tab = new String[1];
		tab[0] = getName();
		return tab;
	}
	

	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {
		//appelle getInfoS (renvoi tab string)
		//si 1 seul string est renvoyé, appelle getInfoI (avec string id) 
		//sinon renvoi le tab dans provider response
		
		if(r.getType() == ProviderRequest.Type.FILM)	//si on cherche un film
		{
			String name = r.getName();
			String langage = r.getLanguage();
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
						MediaInfo info = getInfoI(id, langage, r.getAdditions().contains(ProviderRequest.Additions.JACKETS));
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

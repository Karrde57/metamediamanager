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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Get the information from Allocine.fr
 *
 */
public class AllocineProvider implements Provider {

	private FieldsConfig _config = new FieldsConfig(getName());
	
	private final String API_URL = "http://api.allocine.fr/rest/v3";
	private final String PARTNER_KEY = "100043982026";
	private final String SECRET_KEY = "29d185d98c984a359e6e6f26a0474269";
	private final String CHARSET = "UTF-8";
	
	/**
	 * Transforms a map of parameter into a part of url "key1=value1&key2=value2"
	 * @param params
	 * @return part of the url
	 * @throws ProviderException
	 */
	private String paramsToString(HashMap<String,String> params) throws ProviderException
	{
		String res = "";
		//try {
			
			int i = 0;
			for(Entry<String,String> entry : params.entrySet())
			{
				res += entry.getKey() + "=" + /*URLEncoder.encode(*/entry.getValue()/*, CHARSET)*/;
				if(i!=params.size()-1)
					res+="&";
				i++;
			}
			
		return res;
	}
	
	private byte[] SHA(String md5) {
		   try {
		        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
		        byte[] array = md.digest(md5.getBytes());
		        return array;
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
		}
	
	/**
	 * Generates the url of the request and download the json result
	 * @param method
	 * @param data
	 * @return text content (json string)
	 * @throws ProviderException
	 */
	public String makeRequest(String method, LinkedHashMap<String,String> data) throws ProviderException
	{
		
		
		String queryUrl = API_URL + "/" + method;

        Date date = new Date();
        String sed = (new SimpleDateFormat("yMMdd")).format(date);
        
        String builtQuery = paramsToString(data);
		
        String sig;

        sig = new String(Base64.encodeBase64(SHA(SECRET_KEY + builtQuery + "&sed=" + sed)));
		try {
			queryUrl += "?" + builtQuery + "&sed=" + sed + "&sig=" + URLEncoder.encode(sig, CHARSET).replace("+", "%20");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println(queryUrl);
		
	    //We know have the url of the json to download
		
		
		try {
			URLConnection connection = new URL (queryUrl).openConnection();
			connection.setRequestProperty("Accept-Charset", CHARSET);
			InputStream response;
			response = connection.getInputStream();
			String rep = "";
			
			BufferedReader in = new BufferedReader(new InputStreamReader(response));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
			    rep += inputLine;
			
			System.out.println(rep);
			
			return rep;
		} catch (IOException e) {
			throw new ProviderException("Allociné : erreur lors de la connexion : " + e.getMessage());
		}
        
	}
	/**
	 * Downloads an image and returns a temp file path
	 * @param path
	 * @return path of the temp file
	 * @throws ProviderException
	 */
	private String downloadImage (String path) throws ProviderException				
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
	
	private ProviderResponse getMovieData(String code) throws ProviderException
	{
		LinkedHashMap<String,String> params = new LinkedHashMap<String,String>();
		params.put("partner", PARTNER_KEY);
		params.put("format", "json");
		params.put("code", code);
		params.put("filter", "movie");
		//params.put("striptags", "synopsis,synopsisshort");
		//params.put();

		String res = makeRequest("movie", params);

		MediaInfo info = new MediaInfo();
		
		try {
			JSONObject obj = new JSONObject(res);
			
			obj = obj.getJSONObject("movie");
			
			HashMap<String,String> fieldsAssoc = _config.getFieldsAssociation();
			for(Entry<String,String> entry : fieldsAssoc.entrySet())
			{
				if(obj.has(entry.getValue()))
				{
					String value = obj.getString(entry.getValue());
					System.out.println(value);
					if(entry.getKey().equals("runtime")) 
					{
						//Allocine runtime is in seconds, we convert that in minutes
						value = ""+Integer.parseInt(value)/60;
						info.put(entry.getKey(), value);
					}
					else if(entry.getKey().equals("img_poster"))
					{
						if(obj.getJSONObject(entry.getValue()).has("href"))
						{
							String[] posters = new String[1];
							posters[0] = downloadImage(obj.getJSONObject(entry.getValue()).getString("href"));
							info.putImages("img_poster", posters);
						}
					} else if(entry.getKey().equals("genre"))
					{
						JSONArray tab = obj.getJSONArray(entry.getValue());
						int nbGenres = tab.length();
						String genres = "";
						for(int i=0; i<nbGenres; i++)
						{
							genres += tab.getJSONObject(i).getString("$");
							if(i != nbGenres -1)
								genres += ", ";
						}
						info.put(entry.getKey(), genres);
					} else if(entry.getKey().equals("actor"))
					{
						JSONArray tab = obj.getJSONArray(entry.getValue());
						int nbPers = tab.length();
						
						List<ActorInfo> actorList = new ArrayList<ActorInfo>();
						
						
						for(int i=0; i<nbPers; i++)
						{
							JSONObject actorObj = tab.getJSONObject(i);
							String activityCode = actorObj.getJSONObject("activity").getString("code");
							if(activityCode.equals("8001")) //8001 is the code for actors
							{
								String actorName = actorObj.getJSONObject("person").getString("name");
								String role = actorObj.getString("role");
								
								ActorInfo ai = new ActorInfo();
								ai.setName(actorName);
								ai.setRole(role);
								actorList.add(ai);
							}
						}
						
						ActorInfo[] aiTab = new ActorInfo[actorList.size()];
						actorList.toArray(aiTab);
						
						info.setActors(aiTab);
						
					} else {
						info.put(entry.getKey(), value);
					}
				}
			}
		} catch (JSONException e) {
			throw new ProviderException("Allociné : JSON du film inccorect : " + e.getMessage());
		}
		return new ProviderResponse(info);
	}
	
	@Override
	public String getName() {
		return "allocine";
	}

	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {
		if(r.getType() != ProviderRequest.Type.FILM)
			return new ProviderResponse();
		
		
		//Send a request to get a list of matching movies
		LinkedHashMap<String, String> params = new LinkedHashMap<String,String>();
		try {
		params.put("partner", URLEncoder.encode(PARTNER_KEY, CHARSET).replace("+", "%20"));
		params.put("filter", "movie,tvseries,theater,person,news");
		params.put("count", "5");
		params.put("page", "1");
		
			params.put("q", URLEncoder.encode(r.getName(), CHARSET).replace("+", "%20"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		params.put("format", "json");

		String res = makeRequest("search", params);


		try {
			//We parse the result
			JSONObject obj = new JSONObject(res);

			obj = obj.getJSONObject("feed");
			
			if(!obj.has("movie"))
				return new ProviderResponse(); //No movie has been found
			
			
			JSONArray movies = obj.getJSONArray("movie");
			int nbMovies = movies.length();
			
			if(nbMovies == 0)
				return new ProviderResponse(); //No movie has been found
			
			//For each movie found
			String[] movieNames = new String[nbMovies];
			for(int i=0; i<nbMovies; i++)
			{
				JSONObject movieObj = movies.getJSONObject(i);
				String title = "";
				if(movieObj.has("title"))
					title = movieObj.getString("title");
				else if(movieObj.has("originalTitle"))
					title = movieObj.getString("originalTitle");
				
				if(r.getName().toLowerCase().equals(title.toLowerCase()))
				{
					return getMovieData(movieObj.getString("code"));
				}
				
				movieNames[i] = title;
				
				
			}
			
			//We send suggestions
			return new ProviderResponse(movieNames);
			
		} catch (JSONException e) {
			throw new ProviderException("Allociné : le json retourné est incorrect " + e.getMessage());
		}
		
	}

	@Override
	public String[] getConfigFiles() {
		String[] conf = new String[1];
		conf[0] = "allocine";
		return conf;
	}

}

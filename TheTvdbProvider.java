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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


/**
 * Provider of the website TheTvDb for the series
 * @author jmey
 *
 */
public class TheTvdbProvider implements Provider {
	
	private String charset;
	private String apiKey;
	
	private FieldsConfig _seriesConfig = new FieldsConfig(getName()+"_series");
	private FieldsConfig _episodeConfig = new FieldsConfig(getName());
	
	public TheTvdbProvider ()
	{
		apiKey = "B89CE93890E9419B";
		charset = "UTF-8";
	}
	
	private class SeriesRef {
		String id;
		String name;
	}
	
	
	
	private Document downloadXml(String url) throws ProviderException
	{
		try {
			URLConnection connection = new URL (url ).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();
			String rep = convertStreamToString(response);

			 //On crée une instance de SAXBuilder
	        SAXBuilder sxb = new SAXBuilder();
	        Document doc;
	        try
	        {
	           //On crée un nouveau document JDOM avec en argument le fichier XML
	           //Le parsing est terminé ;)
	           doc = sxb.build(new StringReader(rep));
	           
	           return doc;

	        }
	        catch(Exception e){
	        	throw new ProviderException("API TvDb incoréhente");
	        }
		}
		catch (IOException e) {
			throw new ProviderException("Erreur d'accès à l'API TvDB!");
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
	
	
	
	@Override
	public String getName() {
		return "thetvdb";
	}
	
	@Override
	public String[] getConfigFiles()
	{
		String[] tab = new String[2];
		tab[0] = getName();
		tab[1] = getName() + "_series";
		return tab;
	}

	private SeriesRef[] getSeriesRef(String name) throws ProviderException
	{
		Document doc;
		try {
			String url = "http://thetvdb.com/api/GetSeries.php?seriesname="+URLEncoder.encode(name, charset);
			Logger.getInstance().write("TvDb : getSeriesRef : " + url);
			doc = downloadXml(url);
		} catch (UnsupportedEncodingException e) {
			throw new ProviderException("charset non supporté");
		}
		Element root = doc.getRootElement();
		
		List<Element> seriesElemList = root.getChildren("Series");
		
		//Check if contains info
		if(seriesElemList.size() == 0)
			return new SeriesRef[0]; //No information
		
		//Suggested series
		Vector<SeriesRef> suggested = new Vector<SeriesRef>();
			
		for(Element seriesElem : seriesElemList)
		{
			SeriesRef ref = new SeriesRef();
			ref.id = seriesElem.getChild("seriesid").getText();
			ref.name = seriesElem.getChild("SeriesName").getText();
			suggested.add(ref);
		}
		
		SeriesRef[] res = new SeriesRef[suggested.size()];
		suggested.toArray(res);

		return res;
	}
	
	private String downloadImage(String url) throws ProviderException
	{
		URL urlPic;
		try {
			urlPic = new URL(url);
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
	
	private MediaInfo getSeriesInfo(String seriesId, String language, boolean allImages) throws ProviderException
	{
		String url = "http://thetvdb.com/api/"+apiKey+"/series/"+seriesId+"/"+language+".xml";
		
		Document doc = downloadXml(url);

		Logger.getInstance().write("TvDb : getSeriesInfo url : " + url);
		
		Element root = doc.getRootElement();
		
		
		
		if(root == null || root.getChild("Series") == null)
			throw new ProviderException("recherche sur une série d'un id inconnu");
		
		Element seriesElem = root.getChild("Series");
		
		return parseXml(seriesElem, _seriesConfig, allImages, seriesId);
		
	}
	
	private MediaInfo getEpisodeInfo(String seriesId, int season, int episode, String language, boolean allImages) throws ProviderException
	{
		String url = "http://thetvdb.com/api/"+apiKey+"/series/"+seriesId+"/default/"+season+"/"+episode+"/"+language+".xml";
			
		Document doc = downloadXml(url);
		
		Logger.getInstance().write("TvDb Request : " + url);

		Element root = doc.getRootElement();
		
		
		
		if(root == null || root.getChild("Episode") == null)
			throw new ProviderException("recherche sur un épisode d'un id inconnu");
		
		Element seriesElem = root.getChild("Episode");
		
		return parseXml(seriesElem, _episodeConfig, allImages, seriesId);
	}
	
	private MediaInfo parseXml(Element rootElem, FieldsConfig configXML, boolean allImages, String seriesId) throws ProviderException
	{
		MediaInfo info = new MediaInfo();
		
		//Using thetvdb.xml
		HashMap<String,String> fields = configXML.getFieldsAssociation();
		
		//Additionnal images
		HashMap<String,String[]> allImagesMap = null;
		if(allImages)
		{
			allImagesMap = bannersInfo(seriesId);
			
		}
		
		for(Entry<String,String> field : fields.entrySet())
		{
			String name = field.getKey();
			String markup = field.getValue();
					
			
			Element elem = rootElem.getChild(markup);
			if(elem != null) //If the information actually exists in the downloaded xml file
			{
				String value = elem.getText();
					
				if(name.equals("genre"))
				{
					if(value.startsWith("|"))
						value = value.substring(1);
					if(value.endsWith("|"))
						value = value.substring(0, value.length()-2);
					value = value.replace('|', ',');
					
					info.put(name, value);
				} else if(name.startsWith("img_"))
				{
					if(allImages && name.equals("img_poster"))
					{
						String[] downloaded = new String[allImagesMap.get("poster").length];
						for(int i=0; i<allImagesMap.get("poster").length; i++)
						{
							downloaded[i] = downloadImage("http://thetvdb.com/banners/" + allImagesMap.get("poster")[i]);
						}
						info.putImages(name, downloaded);
					}
					else
					{
						value = downloadImage("http://thetvdb.com/banners/" + value);
						info.put(name, value);
					}
					
					
					
					
					
				} else {
					info.put(name, value);
				}
				
				
			}
		
		}		
		return info;
	}
	
	/**
	 * Download and parse banners.xml
	 * Map key : banner type (season, fanart, series...)
	 * Map value : vector of urls
	 * @return map
	 * @throws ProviderException 
	 */
	public HashMap<String, String[]> bannersInfo(String seriesId) throws ProviderException
	{
		class BannerInfo implements Comparable<BannerInfo> {
			String url;
			double rating;
			
			@Override
			public int compareTo(BannerInfo o)
			{
				return (int) (rating - ((BannerInfo) o).rating);
			}
		}
		
		
		HashMap<String, List<BannerInfo>> res = new HashMap<String, List<BannerInfo>>();
		
		Document doc = downloadXml("http://thetvdb.com/api/"+apiKey+"/series/"+seriesId+"/banners.xml");
	
		Element root = doc.getRootElement();
		
		List<Element> banners = root.getChildren();
		
		for(Element bannerElem : banners)
		{
			
			String type = bannerElem.getChildText("BannerType");
			String url = bannerElem.getChildText("BannerPath");
			
			String rating = (bannerElem.getChildText("Rating").isEmpty()) ? "0" : bannerElem.getChildText("Rating");
			
			if(!res.containsKey(type))
				res.put(type, new ArrayList<BannerInfo>());
			BannerInfo bi = new BannerInfo();
			bi.url = url;
			bi.rating = Double.parseDouble(rating);
			res.get(type).add(bi);
		}
		
		HashMap<String, String[]> bannersMap = new HashMap<String, String[]>();
		
		//We sort banners info and limit 5 banners by type
		for(Entry<String, List<BannerInfo>> entry : res.entrySet())
		{
			Collections.sort(entry.getValue(), Collections.reverseOrder());

			int size = (entry.getValue().size() > 10) ? 10 : entry.getValue().size();
			bannersMap.put(entry.getKey(), new String[size]);
			for(int i=0; i<size; i++)
			{
				bannersMap.get(entry.getKey())[i] = entry.getValue().get(i).url;
			}
		}
		
		return bannersMap;
	}

	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {

		//On ne s'occupe pas des films
		if(r.getType() == ProviderRequest.Type.FILM)
			return new ProviderResponse();
		
		if(r.getType() == ProviderRequest.Type.SERIES)
		{
			SeriesRef[] references = getSeriesRef(r.getSeriesName());
			
			if(references.length == 0)
				return new ProviderResponse(); //not found
			
			String[] suggested = new String[references.length];
			
			//Buidling suggestion list
			for(int i=0; i<references.length; i++)
			{
				//But if one the series is perfectly matching with the series name, we download its information (no suggestion)
				if(references[i].name.toLowerCase().trim().equals(r.getSeriesName().toLowerCase().trim()))
				{
					return new ProviderResponse(getSeriesInfo(references[i].id, r.getLanguage(), r.getAdditions().contains(ProviderRequest.Additions.JACKETS)));
				}
				suggested[i] = references[i].name;
			}
			
			return new ProviderResponse(suggested);
		}
		
		if(r.getType() == ProviderRequest.Type.EPISODE)
		{
			SeriesRef[] references = getSeriesRef(r.getSeriesName());
			
			if(references.length == 0 || (references.length > 1 && !references[0].name.trim().toLowerCase().equals(r.getSeriesName().trim().toLowerCase())))
				return new ProviderResponse(); //not found
			
			String seriesId = references[0].id;
			
			
			bannersInfo(seriesId);
			
			return new ProviderResponse(getEpisodeInfo(seriesId, r.getSeason(), r.getEpisode(), r.getLanguage(), r.getAdditions().contains(ProviderRequest.Additions.JACKETS)));
		}
			
		
		
		return null;
	}
}

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * MediaCenterDataMediaBrowser contains all the functions for the media center mediabrowser
 * @author jmey
 */
public class MediaCenterDataMediaBrowser extends MediaCenterData {

	@Override
	public String getName() {
		return "MediaBrowser";
	}
	
	protected FieldsConfig _saverconfig = new FieldsConfig(getName());

	
	@Override
	public MediaInfo open(String filename) throws ProviderException {
	//***********Open + parse***********
	File mediaFile = new File(filename);								// film
	File directoryFile = mediaFile.getParentFile();						// directory
	File JsonFile = new File(directoryFile, "MBMovie.json");			// json
	String content = "";
	MediaInfo mediainfo = new MediaInfo();
	if(!JsonFile.exists())
	{
		return null;
	}
	try 
	{
		content = new Scanner(JsonFile).useDelimiter("\\Z").next();	// json -> string
	}
	catch (Exception e) {
		e.printStackTrace();
	}
	JSONObject json = null;
	try 
	{
		json = new JSONObject(content);
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	//***********END open + parse***********
	Iterator<?> i = json.keys();
	ArrayList<ActorInfo> actors = new ArrayList<ActorInfo>();
	HashMap<String, String> structinfo =  _saverconfig.getFieldsAssociation();
	
	HashMap<String, String> hashinfo = new HashMap<String,String>();

	while(i.hasNext())
	{
		
		try {
			Object childkey = i.next();
			Object childvalue = json.get(String.valueOf(childkey));
			String genre="";
			if(childkey.equals("genres"))
			{			
				JSONArray jsonarray = new JSONArray(childvalue.toString());
				for(int i2=0;i2<jsonarray.length();i2++)
				{
					genre = genre + jsonarray.getJSONObject(i2).get("name") + ',';
				}
				genre = genre.substring(0, genre.length()-1);
				hashinfo.put("genres", genre);	
			}
			else if(childkey.equals("production_companies"))
			{
				String production_companies="";
				JSONArray jsonarray = new JSONArray(childvalue.toString());
				for(int i2=0;i2<jsonarray.length();i2++)
				{
					production_companies = production_companies + jsonarray.getJSONObject(i2).get("name") + ',';
				}
				production_companies = production_companies.substring(0, production_companies.length()-1);
				hashinfo.put("production_companies", production_companies);
			}
			else if(childkey.equals("production_countries"))
			{
				String production_countries="";
				JSONArray jsonarray = new JSONArray(childvalue.toString());
				for(int i2=0;i2<jsonarray.length();i2++)
				{
					production_countries = production_countries + jsonarray.getJSONObject(i2).get("name") + ',';
				}
				production_countries = production_countries.substring(0, production_countries.length()-1);
				hashinfo.put("production_companies", production_countries);
			}
			else if(childkey.equals("spoken_languages"))
			{
				String spoken_languages="";
				JSONArray jsonarray = new JSONArray(childvalue.toString());
				for(int i2=0;i2<jsonarray.length();i2++)
				{
					spoken_languages = spoken_languages + jsonarray.getJSONObject(i2).get("name") + ',';
				}
				spoken_languages = spoken_languages.substring(0, spoken_languages.length()-1);
				hashinfo.put("spoken_languages", spoken_languages);
			}
			else if(childkey.equals("cast"))
			{
				JSONArray jsonarray = new JSONArray(childvalue.toString());
				for(int i2=0;i2<jsonarray.length();i2++)
				{
					String name = jsonarray.getJSONObject(i2).get("name") + "";
					String role = jsonarray.getJSONObject(i2).getString("character") + "";
					actors.add(new ActorInfo(name, role, ""));
				} 
			}
			else if(childkey.equals("crew"))
			{
				String crew = "";
				JSONArray jsonarray = new JSONArray(childvalue.toString());
				for(int i2=0;i2<jsonarray.length();i2++)
				{
					crew = crew + jsonarray.getJSONObject(i2).get("name") + ',';
				}
				crew = crew.substring(0, crew.length()-1);
				hashinfo.put("crew", crew);
			}
			else
			{
				hashinfo.put(childkey.toString(), childvalue.toString());
			}
			
		}
		catch(Exception e)
		{
			
		}
		
	}
	for(Entry<String, String> entry : structinfo.entrySet()) 					//add into mediainfo<hashmap>
	{
	   String  key = entry.getKey();
	   String value= entry.getValue();
	    for(Entry<String, String> entry2 : hashinfo.entrySet()) 
	    {
	    	String key2 = entry2.getKey();
	    	String value2 = entry2.getValue();
	    	//System.out.println(key2 + "++++++++++++++" + value);
	    	if(value.equals(key2) && !value2.equals(""))
	    	{
	    		mediainfo.put(key, value2);	
	    	}
	    }   
	}
	ActorInfo[] actorsinfo = new ActorInfo[actors.size()]; //adding actors into mediainfo
	actors.toArray(actorsinfo);
	mediainfo.setActors(actorsinfo);
	
	//images
	String posterName = "folder.jpg";
	ArrayList<String> imagesNames = new ArrayList<String>();
	String fanartName = "backdrop1.jpg";
	File f1 = new File(mediaFile.getParent(), fanartName);
	int i2=1;
	while(f1.exists())
	{
		fanartName = "backdrop" + i2 + ".jpg";
		f1 = new File(mediaFile.getParent(), fanartName);
		if(f1.exists())
		{
			
			imagesNames.add((f1.getAbsolutePath()));
			
			
		}
		i2++;
	}
	String[] castimagesNames =  imagesNames.toArray(new String[imagesNames.size()]);
	mediainfo.putImages("img_fanart", castimagesNames);
	String[] imagesNames2 = new String[1];
	File f2 = new File(mediaFile.getParent(), posterName);
	if(f2.exists())
	{
		
		imagesNames2[0] = f2.getAbsolutePath();
		mediainfo.putImages("img_poster", imagesNames2);
	}

	
	return mediainfo;
}
	@Override
	public void save(MediaInfo mediainfo, String filename) throws ProviderException {
		HashMap<String, String> structinfo =  _saverconfig.getFieldsAssociation();
		JSONObject jsonobject = new JSONObject();
		
		for(Entry<String, String> entry : mediainfo.entrySet())
		{
			   String  key = entry.getKey();
			   String value= entry.getValue();
			   
			   if(key.equals("type"))
			   {
				   JSONArray jsontab = new JSONArray();
				   try {
					   
					   String[] tab = value.split(",");
					   for(int i=0;i<tab.length;i++)
					   {
						   
						   JSONObject child = new JSONObject();
						   child.put("id", 1);
						   child.put("name", tab[i]);
						   jsontab.put(child);
					   }
					   jsonobject.put("genres", jsontab);
				   } catch (Exception e)
				   {
					   e.getStackTrace();
				   }
			   }
			   else if(key.equals("studio"))
			   {
				   JSONArray jsontab = new JSONArray();
				   try {
					   
					   String[] tab = value.split(",");
					   for(int i=0;i<tab.length;i++)
					   {
						   JSONObject child = new JSONObject();
						   child.put("name", tab[i]);
						   child.put("id", 1);
						   jsontab.put(child);
					   }
					   jsonobject.put("production_companies", jsontab);
				   } catch (Exception e)
				   {
					   e.getStackTrace();
				   } 
			   }
			   else if(key.equals("country"))
			   {
				   JSONArray jsontab = new JSONArray();
				   try {
					   
					   String[] tab = value.split(",");
					   for(int i=0;i<tab.length;i++)
					   {
						   JSONObject child = new JSONObject();
						   child.put("iso_3166_1", "unknown");
						   child.put("name", tab[i]);
						   jsontab.put(child);
					   }
					   jsonobject.put("production_countries", jsontab);
				   } catch (Exception e)
				   {
					   e.getStackTrace();
				   } 
			   }
			   else if(key.equals("language"))
			   {
				   JSONArray jsontab = new JSONArray();
				   try {
					   
					   String[] tab = value.split(",");
					   for(int i=0;i<tab.length;i++)
					   {
						   JSONObject child = new JSONObject();
						   child.put("iso_639_1", "unknown");
						   child.put("name", tab[i]);
						   jsontab.put(child);
					   }
					   jsonobject.put("spoken_languages", jsontab);
				   } catch (Exception e)
				   {
					   e.getStackTrace();
				   } 
			   }
			   else if(key.equals("director"))
			   {
				   JSONArray jsontab = new JSONArray();
				   try {
					   
					   String[] tab = value.split(",");
					   for(int i=0;i<tab.length;i++)
					   {
						   JSONObject child = new JSONObject();
						   child.put("id", 0);
						   child.put("name", tab[i]);
						   child.put("department", "unknown");
						   child.put("job", "unknown");
						   child.put("profile_path", "null");
						   
						   jsontab.put(child);
					   }
					   jsonobject.put("crew", jsontab);
				   } catch (Exception e)
				   {
					   e.getStackTrace();
				   } 
			   }
			   else
			   {

				   for(Entry<String, String> entry2 : structinfo.entrySet()) 
				   {
					   String key2 = entry2.getKey();
					   String value2 = entry2.getValue();
					   if(key2.equals(key))
					   {
						   try {
								jsonobject.put(value2, value);
							} catch (JSONException e) {
								e.printStackTrace();
							}
					   }
				   }

			   }

			  
		}
		   //acteurs
		   ActorInfo[] actors = mediainfo.getActors();
		   JSONArray jsontab = new JSONArray();
		   for(int i =0;i<actors.length;i++)
		   {
			  
			   try {
				   
				   JSONObject child = new JSONObject();
				   child.put("id", 0);
				   child.put("name", actors[i].getName() );
				   child.put("character", actors[i].getRole());
				   child.put("order", i);
				   child.put("cast_id", 0);
				   child.put("profile_path", "null");
				   
				   jsontab.put(child);
			   } catch(Exception e) {
				   e.getStackTrace();
			   }
			   
		   }
		   try {
			jsonobject.put("cast", jsontab);
		} catch (JSONException e) {
			e.printStackTrace();
		}
			String images[];
			String f = new File(filename).getParent();
			images = mediainfo.getImages("img_poster");
			if(images != null && images[0] != null && images[0] != "")
			{
			copy_images(images[0],  f + File.separator + "folder.jpg");
			}
			images = mediainfo.getImages("img_fanart");
			if(images != null && images[0] != null && images[0] != "")
			{
				for(int i=0;i<images.length;i++)
				{
					System.out.println(images[i] + "->>>>>" + f + File.separator + "backdrop" + i +".jpg" );
					copy_images(images[i],  f + File.separator + "backdrop" + i +".jpg");
					
				}
			}
			FileOutputStream fop = null;
			try 
			{
				File file = new File("MBMovie.json");
				fop = new FileOutputStream(file);
				if (!file.exists()) {
					file.createNewFile();
				}
				byte[] contentInBytes = jsonobject.toString().getBytes();
				 
				fop.write(contentInBytes);
				fop.flush();
				fop.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try {
					if (fop != null) {
						fop.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		
	}
	
/********************************************************************
*********************************************************************
******************************SERIES*********************************
*********************************************************************
*********************************************************************/
	
	@Override
	public void saveSeries(MediaInfo mediainfo, String directory) {
		
		HashMap<String, String> hashinfo = _saverconfig.getFieldsAssociation();
		Element root = new Element("Data");
		Element series = new Element("Series");
		for(Entry<String, String> entry : mediainfo.entrySet()) 
		{
			String key =  entry.getKey();
			String value = entry.getValue();
			
			for(Entry<String, String> entry2 : hashinfo.entrySet())
			{
				String key2 = entry2.getKey();
				String value2 = entry2.getValue();
				
				if(key.equals(key2))
				{
					Element newchild = new Element(value2);
					newchild.setText(value);
					
					series.addContent(newchild);
				}
			}
			
		}
		//actors
		Element allactorselement = new Element("Actors");
		String allactors="";
		ActorInfo[] actors = mediainfo.getActors();
		Element actorselement = new Element("Actors");
		for(int i=0;i<actors.length;i++)
		{
			Element actorelement = new Element("Actor");
			Element name = new Element("Name");
			name.setText(actors[i].getName());
			Element role = new Element("Role");
			role.setText(actors[i].getRole());
			actorelement.addContent(name);
			actorelement.addContent(role);
			actorselement.addContent(actorelement);
			allactors += actors[i].getName() + " | ";
			
		}
		if(allactors != "" && allactors.length() >= 2)
		{
			allactorselement.setText(allactors.substring(0, allactors.length()-2));
		}
		series.addContent(allactorselement);
		series.addContent(actorselement);
		
		root.addContent(series);
		
		Document document = new Document(root);
		 XMLOutputter exit = new XMLOutputter(Format.getPrettyFormat());
			String newFileNameNfo = new File(directory, "Series.xml").getAbsolutePath();
			System.out.println(newFileNameNfo);
		 try {
			exit.output(document, new FileOutputStream(newFileNameNfo));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		//images
		 String images[];
			
			
			images = mediainfo.getImages("img_banner");
			if(images[0] != "")
			{
				copy_images(images[0], new File(directory, "banner.jpg").getAbsolutePath());
			}
				
			
			images = mediainfo.getImages("img_poster");
			if(images[0] != "")
			{
				copy_images(images[0], new File(directory, "folder.jpg").getAbsolutePath());
			}
			images = mediainfo.getImages("img_fanart");
			
			if(images[0] != "")
			{
				for(int i=0;i<images.length;i++)
				{
					int j = i+1;
					copy_images(images[i], new File(directory, "backdrop" + j + ".jpg").getAbsolutePath());
				}
			}
			
	}
	
	@Override
	public MediaInfo openSeries(String filename) throws ProviderException {
		System.out.println("Entre dans open serie");
		System.out.println("open serie filename :" +filename);
		MediaInfo mediainfo = new MediaInfo();
		org.jdom2.Document document = null;		
		HashMap<String, String> hashinfo = _saverconfig.getFieldsAssociation();
		File directory = new File(filename);
		File xmlfile = new File(directory.getAbsolutePath() + File.separator +  "Series.xml");
		//System.out.println(xmlfile.getAbsolutePath());
		if(!xmlfile.exists())
		{
			return null;
		}
		SAXBuilder sxb = new SAXBuilder();
		try
		{
			
			document = sxb.build(xmlfile);		
		}
		catch(Exception e) 
		{
			
			throw(new ProviderException("Erreur lors de l'ouverture du fichier"));

		}
		
		Element root = document.getRootElement().getChild("Series"); // root is the 2nd element of the xml file : series
		List<Element> childlist = root.getChildren();
		Iterator<Element> i = childlist.iterator();
		ArrayList<ActorInfo> actors = new ArrayList<ActorInfo>();
		
		while(i.hasNext())
		{
			
			Element child = (Element) i.next();
			String key = child.getName();
			String value = child.getValue();
			
			if(key.equals("Actors"))
			{
				List<Element> childlist2 = child.getChildren();
				Iterator<Element> i2 = childlist2.iterator();
				int i3=0;
				String name = null, role = null;
				while(i2.hasNext())
				{
					Element childchild = (Element) i2.next();
					if(childchild.getChild("Name") != null)
					{
						
						name = childchild.getChild("Name").getValue();
						role = childchild.getChild("Role").getValue();
						
						i3++;
					}

					if(i3>=1)
					{
						
						actors.add(new ActorInfo(name, role, null));
						name = ""; role = "";
						i3=0;
					}
				}

			}
			else
			{
				for(Entry<String, String> entry : hashinfo.entrySet())
				{
					
					String key2 = entry.getKey();
					String value2 = entry.getValue();
					System.out.println(key + "->"+ value2);
					if(key.equals(value2))
					{
						System.out.println(key2 + "->"+ value);
						mediainfo.put(key2,  value);
					}
				}
			}
		}
		System.out.println(mediainfo.toString());
		ActorInfo[] actorsi = new ActorInfo[actors.size()];
		actors.toArray(actorsi);
		mediainfo.setActors(actorsi);
		
		//images
		String posterName = "folder.jpg";
		ArrayList<String> imagesNames = new ArrayList<String>();
		String fanartName = "backdrop1.jpg";
		String bannerName = "banner.jpg";
		File f1 = new File(directory, fanartName);
		int i2=1;
		while(f1.exists())
		{
			fanartName = "backdrop" + i2 + ".jpg";
			f1 = new File(directory, fanartName);
			if(f1.exists())
			{
				
				imagesNames.add((f1.getAbsolutePath()));
				
				
			}
			i2++;
		}
		String[] castimagesNames =  imagesNames.toArray(new String[imagesNames.size()]);
		
		mediainfo.putImages("img_fanart", castimagesNames);
		String[] imagesNames2 = new String[1];
		File f2 = new File(directory, posterName);
		if(f2.exists())
		{
			
			imagesNames2[0] = f2.getAbsolutePath();
			mediainfo.putImages("img_poster", imagesNames2);
		}
		String[] imagesNames3 = new String[1];
		File f3 = new File(directory, bannerName);
		if(f3.exists())
		{
			
			imagesNames3[0] = f3.getAbsolutePath();
			mediainfo.putImages("img_banner", imagesNames3);
		}
		//images
		 String images[];
			
			
			images = mediainfo.getImages("img_banner");
			if(images[0] != "")
			{
			
				copy_images(images[0], new File(directory, "banner.jpg").getAbsolutePath());
			}
				
			
			images = mediainfo.getImages("img_poster");
			if(images[0] != "")
			{
				copy_images(images[0], new File(directory, "folder.jpg").getAbsolutePath());
			}
			images = mediainfo.getImages("img_fanart");
			
			if(images[0] != "")
			{
				for(int i1=0;i1<images.length;i1++)
				{
					int j = i1+1;
					copy_images(images[i1], new File(directory, "backdrop" + j + ".jpg").getAbsolutePath());
				}
			}
		String genre = mediainfo.get("type");
		genre = genre.replace('|', ',');
		if(genre != null && genre != "" && genre.length() > 2)
		{
			if(genre.charAt(0) == ',')
				genre = genre.substring(1);
			if(genre.charAt(genre.length()-1) == ',')
				genre = genre.substring(0, genre.length()-1);
			mediainfo.put("type", genre);
		}
		return mediainfo;
	}

	@Override
	public MediaInfo openEpisode(String filename)
	{
		System.out.println("Entre dans open ep");
		System.out.println("open ep :" + filename);
		org.jdom2.Document document = null;	
		File directory = new File(filename).getParentFile();
		File metadatafile = new File(directory.getAbsolutePath(), "metadata");
		if(!metadatafile.exists())
		{
			return null;
		}
		File infofile = new File(metadatafile, filename.substring(filename.lastIndexOf(File.separator), filename.lastIndexOf("."))+".xml");
		
		if(!infofile.exists())
		{
			return null;
		}
		SAXBuilder sxb = new SAXBuilder();
		try
		{
			document = sxb.build(infofile);		
		}
		catch(Exception e) 
		{
			
			return null;

		}
		MediaInfo mediainfo = new MediaInfo();
		Element root = document.getRootElement().getChild("Episode");
		List<Element> elements =  root.getChildren();
		Iterator<Element> i = elements.iterator();
		HashMap<String, String> hashinfo = _saverconfig.getFieldsAssociation();
		while(i.hasNext())
		{
			Element child = (Element) i.next();
			String key = child.getName();
			String value = child.getValue();
			for(Entry<String, String> entry : hashinfo.entrySet())
			{
				
				String key2 = entry.getKey();
				String value2 = entry.getValue();
				if(key.equals(value2))
				{
					mediainfo.put(key2,  value);
				}
			}
		}
		//images
		String posterName = "folder.jpg";
		String bannerName = "banner.jpg";
		
		//images
		String[] imagesNames2 = new String[1];
		File f2 = new File(directory, posterName);
		if(f2.exists())
		{
			imagesNames2[0] = f2.getAbsolutePath();
			mediainfo.putImages("img_poster", imagesNames2);
		}
		String[] imagesNames3 = new String[1];
		File f3 = new File(directory, bannerName);
		if(f3.exists())
		{
			imagesNames3[0] = f3.getAbsolutePath();
			mediainfo.putImages("img_banner", imagesNames3);
		}
		
		//System.out.println(mediainfo.toString());
		return mediainfo;
		
	}
	/**
	 * use for copy images given in an array in a folder given in parameters
	 * @param images
	 * @param newFileName
	 */
	public void copy_images(String images, String newFileName)
	{

			FileChannel in = null; // canal d'entrée
			FileChannel out = null; // canal de sortie
			try 
			{
				  // Init
				  in = new FileInputStream(images).getChannel();
				  out = new FileOutputStream(newFileName).getChannel();
				 
				  // Copy in->out
				  in.transferTo(0, in.size(), out);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace(); 
			}
			finally
			{ 
				if(in != null) {
				  	try
				  	{
					  in.close();
					} 
				  	catch (Exception e)
					{
				  		e.printStackTrace();
					}
				  }
				  if(out != null)
				  {
				  	try
				  	{
					  out.close();
					} 
				  	catch (Exception e) 
				  	{
				  		e.printStackTrace();
				  	}
				  }		
			}
			
			
	
	}
	@Override
	public void saveEpisode(MediaInfo mediainfo, String filename) {
		HashMap<String, String> hashinfo = _saverconfig.getFieldsAssociation();
		Element root = new Element("Data");
		Element series = new Element("Episode");
		for(Entry<String, String> entry : mediainfo.entrySet()) 
		{
			String key =  entry.getKey();
			String value = entry.getValue();
			
			for(Entry<String, String> entry2 : hashinfo.entrySet())
			{
				String key2 = entry2.getKey();
				String value2 = entry2.getValue();
				
				if(key.equals(key2))
				{
					Element newchild = new Element(value2);
					newchild.setText(value);
					
					series.addContent(newchild);
				}
			}
			
		}
		root.addContent(series);
		
		Document document = new Document(root);
		 XMLOutputter exit = new XMLOutputter(Format.getPrettyFormat());
		 	String directory = new File(filename).getParentFile().toString();
		 	String directorymeta = directory + File.separator + "metadata";
		 	File directorymetafile = new File(directorymeta);
		 	if(!directorymetafile.exists())
		 		directorymetafile.mkdir();
			File mediafile = new File(filename);
			String newFileNameNfo = directorymetafile.getAbsolutePath() + File.separator + mediafile.getName().substring(0, mediafile.getName().lastIndexOf('.')) + ".nfo";
		 try {
			exit.output(document, new FileOutputStream(newFileNameNfo));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
			//images
		 String images[];
			
			
			images = mediainfo.getImages("img_banner");
			if(images[0] != "")
			{
			
				copy_images(images[0], new File(directory, "banner.jpg").getAbsolutePath());
			}
				
			
			images = mediainfo.getImages("img_poster");
			if(images[0] != "")
			{
				copy_images(images[0], new File(directory, "folder.jpg").getAbsolutePath());
				System.out.println(directory);
			}
			images = mediainfo.getImages("img_fanart");
		
	}




}

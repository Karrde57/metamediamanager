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
*/
package com.t3.metamediamanager;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jdom2.input.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.*;
/**
 * class which has all methods for the data center XBMC (saver and provider)
 * @author jmey
 */
public class MediaCenterDataXBMC extends MediaCenterData {


	protected FieldsConfig _saverconfig = new FieldsConfig(getName());

	
	@Override
	public MediaInfo open(String filename) throws ProviderException {  
		org.jdom2.Document document = null;												
		Element root;																	// root of the media xml		
		HashMap<String,String> fieldsAssociation = _saverconfig.getFieldsAssociation(); // HashMap between XBMC fields and DB fields
		HashMap<String, String> xmlAssociation = new HashMap<String, String>();			// HashMap between XBMC fields and the content
		String key, value, key2, value2; 												// key, value -> fieldsAssociation ||| key2, value2 -> xmlAssociation
		SAXBuilder sxb = new SAXBuilder();
		MediaInfo mediainfo = new MediaInfo();
		
		String newFileName = filename.substring(0, filename.lastIndexOf('.'));
		newFileName += ".nfo";
		File f = new File(newFileName);
		if(!f.exists())
		{
			return null;
		}
		
		try
		{
			document = sxb.build(f);		
		}
		catch(Exception e) 
		{
			
			throw(new ProviderException("Erreur lors de l'ouverture du fichier"));

		}
		
		root = document.getRootElement();
		List<Element> elements =  root.getChildren();									// all elements with "movie" as father
		List<Element> childelements;
		Iterator<Element> i = elements.iterator();
		Iterator<Element> ichild;
		Element current, currentchild;
		String credits = "", genre = "";
		ArrayList<ActorInfo> actors = new ArrayList<ActorInfo>();
		while(i.hasNext()) // create HashMap with media information (example "name" -> "moviename")
		{
			current = (Element)i.next();
			childelements = null;
			ichild = null;
			int i2;
			if(current.getName().equals("actor"))									//special traitment for actor
			{
				childelements = current.getChildren();
				ichild = childelements.iterator();
				String name = "", role = "", imgurl = "";
				i2=0;
				File dirActors = new File(new File(filename).getParentFile(), ".actors");
				while(ichild.hasNext())
				{
					currentchild = (Element)ichild.next();
					
					if(currentchild.getName().equals("name"))
					{
						name = currentchild.getValue();
						String newActorName = name.replace(' ', '_');
						
						File actor = new File(dirActors, newActorName + ".jpg");
						if(actor.exists())
						{
							imgurl = actor.getAbsolutePath();
							
						}
						i2++;
																		
					}	
					else if(currentchild.getName().equals("role"))
					{
						role = currentchild.getValue();
						i2++;
						
					}
					else if(currentchild.getName().equals("thumb"))
					{
						
					}
					if(i2>=2)
					{
						actors.add(new ActorInfo(name, role, imgurl));
						name = ""; role = ""; imgurl="";
					}
						
					
				}
			}
			else if(current.getName().equals("genre"))
			{
				genre += current.getValue() + ",";
			}
			else if(current.getName().equals("credits"))
			{
				credits += current.getValue() + ",";
			}
			else if(current.getName().equals("fanart"))								// same
			{
				//do nothing
			}
			else if(current.getName().equals("fileinfo"))							//same because of multiple childrens
			{
				childelements = current.getChild("streamdetails").getChild("video").getChildren();
				ichild = childelements.iterator();
				while(ichild.hasNext())
				{
					currentchild = (Element)ichild.next();
					if(currentchild.getName().equals("codec"))			//exception for codec beaucause same attribut for audio and video codec
					{
						mediainfo.put("videoCodec", currentchild.getText());
					}
					else
					{
						xmlAssociation.put(currentchild.getName(), currentchild.getText());
					}
				}
				childelements = current.getChild("streamdetails").getChild("audio").getChildren();
				ichild = childelements.iterator();
				while(ichild.hasNext())
				{
					currentchild = (Element)ichild.next();
					if(currentchild.getName().equals("codec")) //exception for codec beaucause same attribut for audio and video codec
					{
						mediainfo.put("audioCodec", currentchild.getText());
					}
					else
					{
						xmlAssociation.put(currentchild.getName(), currentchild.getText());
					}
				}
			}
			else																			//simple traitment
			{
				xmlAssociation.put(current.getName(), current.getText());
			}
					
					
		}
		if(genre.length() >= 1) // add genre
		{
			mediainfo.put("genre", genre.substring(0, genre.length()-1));
		}
		if(credits.length() >= 1) // add credits
		{
			mediainfo.put("credits", credits.substring(0, credits.length()-1));
		}
		ActorInfo[] actorsinfo = new ActorInfo[actors.size()]; //adding actors into mediainfo
		actors.toArray(actorsinfo);
		mediainfo.setActors(actorsinfo);
		for(Entry<String, String> entry : fieldsAssociation.entrySet()) 					//add into mediainfo<hashmap>
		{
		    key = entry.getKey();
		    value= entry.getValue();
		    for(Entry<String, String> entry2 : xmlAssociation.entrySet()) 
		    {
		    	key2 = entry2.getKey();
		    	value2 = entry2.getValue();
		    	
		    	if(value.equals(key2) && !value2.equals(""))
		    	{
		    		mediainfo.put(key, value2);
		    		
		    	}
		    }
		    
		}

		// Recuperation des images
		File movieFile = new File(filename);
		String movieName = movieFile.getName().substring(0, movieFile.getName().lastIndexOf('.'));
		String fanartName = movieName + "-fanart.jpg";
		String posterName = movieName +"-poster.jpg";
		String thumbName = movieName + "-thumb.jpg";
		
		String[] imagesNames = new String[1];
		File f1 = new File(movieFile.getParent(), fanartName);
		if(f1.exists())
		{
			imagesNames[0] = f1.getAbsolutePath();
			mediainfo.putImages("img_fanart", imagesNames);
			
		}
		
		File f2 = new File(movieFile.getParent(), posterName);
		if(f2.exists())
		{
			imagesNames[0] = f2.getAbsolutePath();
			mediainfo.putImages("img_realposter", imagesNames);
		}
		File f3 = new File(movieFile.getParent(), thumbName);
		if(f3.exists())
		{
			imagesNames[0] = f3.getAbsolutePath();
			mediainfo.putImages("img_poster", imagesNames);
		}
		
		return mediainfo;
	}

/********************
*********SAVE********
********************/
	
	@Override
	public void save(MediaInfo info, String filename) {
		Element root = new Element("movie");
		Document document = new Document(root);
		Element child, child2, child3;
		String key, value, key2, value2;
		HashMap<String,String> fieldsAssociation = _saverconfig.getFieldsAssociation();
		for(Entry<String, String> entry : info.entrySet()) 
		{
			
	    	key = entry.getKey();
	    	value = entry.getValue();
			for(Entry<String, String> entry2 : fieldsAssociation.entrySet()) 
			{
				key2 = entry2.getKey();
				value2 = entry2.getValue();
				if(key.equals(key2))
				{
					child = new Element(value2);
					child.setText(value);
					root.addContent(child);			
				}
			}
		}
		ActorInfo[] actors = info.getActors();
		HashMap<String, String> actorsimg = new HashMap<String, String>();
		if(actors.length>=1)
		{
			for(int i =0;i<actors.length;i++)
			{
				
				child = new Element("actor");
				
				child2 = new Element("name");
				child2.setText(actors[i].getName());	
				child.addContent(child2);
				//*****
				child3 = new Element("role");
				child3.setText(actors[i].getRole());
				child.addContent(child3);
				//*****
				if(!actors[i].getImgUrl().isEmpty())
				{
					
					actorsimg.put(actors[i].getName(), M3Config.getInstance().getUserConfDirectory() + actors[i].getImgUrl());
				}
				root.addContent(child);
			}
		}
			
		 XMLOutputter exit = new XMLOutputter(Format.getPrettyFormat());
			String newFileName = filename.substring(0, filename.lastIndexOf('.'));
			String newFileNameNfo = newFileName + ".nfo";
		 try {
			exit.output(document, new FileOutputStream(newFileNameNfo));
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
 
		} 
		String images[];
		
		images = info.getImages("img_poster");
		if(images[0] != null && images[0] != "")
			copy_images(images[0], newFileName +"-poster.jpg");
		images = info.getImages("img_fanart");
		System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + Arrays.toString(images));
		if(images[0] != null && images[0] != "")
			copy_images(images[0], newFileName+"-fanart.jpg");
		File dir = new File(new File(filename).getParentFile(), ".actors");
		dir.mkdir();
		String newActorName;
		if(actorsimg != null && !actorsimg.isEmpty())
		{
			for(Entry<String, String> entry : actorsimg.entrySet()) 
			{
				
				String newimage = entry.getValue();
				newActorName = entry.getKey().replace(' ', '_');
				if(newimage != null && newimage != "" && new File(newimage).exists())
					copy_images(newimage, new File(dir, newActorName + ".jpg").toString());
				System.out.println(newimage +"++++++++++++++++++++++++"+new File(dir, newActorName + ".jpg").toString());
			}
		}
		
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
	
/****************************************
****************SERIES*******************
****************************************/

/********************
*********OPEN********
********SERIES******/
	
	public MediaInfo openSeries(String filename) throws ProviderException
	{
		
		org.jdom2.Document document = null;												
		Element root;																	// root of the media xml		
		HashMap<String,String> fieldsAssociation = _saverconfig.getFieldsAssociation(); // HashMap between XBMC fields and DB fields
		HashMap<String, String> xmlAssociation = new HashMap<String, String>();			// HashMap between XBMC fields and the content
		String key, value, key2, value2; 												// key, value -> fieldsAssociation ||| key2, value2 -> xmlAssociation
		SAXBuilder sxb = new SAXBuilder();
		MediaInfo mediainfo = new MediaInfo();
		ArrayList<ActorInfo> actors = new ArrayList<ActorInfo>();
		
		File f = new File(filename, "tvshow.nfo");
		if(!f.exists())
		{
			
			return null;
		}
		
		try
		{
			document = sxb.build(f);		
		}
		catch(Exception e) 
		{
			
			throw(new ProviderException("Erreur lors de l'ouverture du fichier"));

		}
		
		root = document.getRootElement();
		List<Element> elements =  root.getChildren();									
		List<Element> childelements;
		Iterator<Element> i = elements.iterator();
		Iterator<Element> ichild;
		Element current, currentchild;
		String genre="", credits="";
		while(i.hasNext()) // create HashMap with media information (example "name" -> "moviename")
		{
			current = (Element)i.next();
			childelements = null;
			ichild = null;
			int i2;
			if(current.getName().equals("actor"))									//special traitment for actor
			{
				childelements = current.getChildren();
				ichild = childelements.iterator();
				String name = "", role = "", imgurl = "";
				i2=0;
				File dirActors = new File(new File(filename).getParentFile(), ".actors");
				while(ichild.hasNext())
				{
					currentchild = (Element)ichild.next();
					
					if(currentchild.getName().equals("name"))
					{
						name = currentchild.getValue();
						String newActorName = name.replace(' ', '_');
						
						File actor = new File(dirActors, newActorName + ".jpg");
						if(actor.exists())
						{
							imgurl = actor.getAbsolutePath();
							
						}
						i2++;
																		
					}	
					else if(currentchild.getName().equals("role"))
					{
						role = currentchild.getValue();
						i2++;
						
					}
					else if(currentchild.getName().equals("thumb"))
					{

					}
					if(i2>=2)
					{
						actors.add(new ActorInfo(name, role, imgurl));
						name = ""; role = ""; imgurl="";
					}
						
					
				}
			}
			else if(current.getName().equals("fanart"))
			{
			}
			else if(current.getName().equals("genre"))
			{
				genre += current.getValue() + ',';
			}
			else if(current.getName().equals("credits"))
			{
				credits += current.getValue() + ',';
			}
			else																			
			{
				xmlAssociation.put(current.getName(), current.getText());
			}
					
					
		}
		if(genre.length() >=2)
		{
			mediainfo.put("type", genre.substring(0, genre.length()-1));
		}
		if(credits.length()>=2)
		{
			mediainfo.put("credits", credits.substring(0, credits.length()-1));
		}
		ActorInfo[] actorsinfo = new ActorInfo[actors.size()];							//adding actoes into mediainfo
		actors.toArray(actorsinfo);
		mediainfo.setActors(actorsinfo);
		for(Entry<String, String> entry : fieldsAssociation.entrySet()) 					//add into mediainfo<hashmap>
		{
		    key = entry.getKey();
		    value= entry.getValue();
		    for(Entry<String, String> entry2 : xmlAssociation.entrySet()) 
		    {
		    	key2 = entry2.getKey();
		    	value2 = entry2.getValue();;
		    	if(value.equals(key2) && !value2.equals(""))
		    	{
		    		mediainfo.put(key, value2);
		    		
		    	}
		    }
		    
		}
		// Recuperation des images;
		String[] imagesNames = new String[1];
		File f1 = new File(filename, "poster.jpg");
		
		if(f1.exists())
		{
			imagesNames[0] = f1.getAbsolutePath();
			mediainfo.putImages("img_poster", imagesNames);
		}
		
		File f2 = new File(filename, "season-all-poster.jpg");
		if(f2.exists())
		{
			imagesNames[0] = f2.getAbsolutePath();
			mediainfo.putImages("img_poster", imagesNames);
		}
		File f3 = new File(filename, "banner.jpg");
		if(f3.exists())
		{
			
			imagesNames[0] = f3.getAbsolutePath();
			mediainfo.putImages("banner_series", imagesNames);
		}
		
		return mediainfo;
	}
	/********************
	*********SAVE********
	********EPISODE*****/
	public void saveEpisode(MediaInfo info, String filename) {
		Element root = new Element("episodedetails");
		Document document = new Document(root);
		Element child;
		String key, value, key2, value2;
		HashMap<String,String> fieldsAssociation = _saverconfig.getFieldsAssociation();
		for(Entry<String, String> entry : info.entrySet()) 
		{
			
	    	key = entry.getKey();
	    	value = entry.getValue();
			for(Entry<String, String> entry2 : fieldsAssociation.entrySet()) 
			{
				key2 = entry2.getKey();
				value2 = entry2.getValue();
				if(key.equals(key2))
				{
					child = new Element(value2);
					child.setText(value);
					root.addContent(child);
					
					
				}
			}
		}
		 XMLOutputter exit = new XMLOutputter(Format.getPrettyFormat());
			String newFileName = filename.substring(0, filename.lastIndexOf('.'));
			String newFileNameNfo = newFileName + ".nfo";
		 try {
			exit.output(document, new FileOutputStream(newFileNameNfo));
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
 
		} 
		String images[];
		
		
		images = info.getImages("img_poster");
		System.out.println(Arrays.toString(images));
		if(images[0] != null && images[0] != "")
			copy_images(images[0], newFileName+"-thumb.jpg");
	}
	
	/********************
	*********SAVE********
	********SERIES******/
	public void saveSeries(MediaInfo info, String directory) {
		Element root = new Element("tvshow");
		Document document = new Document(root);
		Element child;
		String key, value, key2, value2;
		HashMap<String,String> fieldsAssociation = _saverconfig.getFieldsAssociation();
		for(Entry<String, String> entry : info.entrySet()) 
		{
			
	    	key = entry.getKey();
	    	value = entry.getValue();
			for(Entry<String, String> entry2 : fieldsAssociation.entrySet()) 
			{
				key2 = entry2.getKey();
				value2 = entry2.getValue();
				if(key.equals(key2))
				{
					child = new Element(value2);
					child.setText(value);
					root.addContent(child);
					
					
				}
			}
		}
		 XMLOutputter exit = new XMLOutputter(Format.getPrettyFormat());
			String newFileNameNfo = new File(directory, "tvshow.nfo").getAbsolutePath();
		 try {
			exit.output(document, new FileOutputStream(newFileNameNfo));
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
 
		} 
		String images[];
		
		
		images = info.getImages("img_poster");
		if(images[0] != null && images[0] != "")
			copy_images(images[0], new File(directory, "posterAllSeason.jpg").getAbsolutePath());

			
		
		images = info.getImages("img_fanart");
		if(images[0] != null && images[0] != "")
			copy_images(images[0], new File(directory, "fanart.jpg").getAbsolutePath());
		
		images = info.getImages("img_banner");
		if(images[0] != null && images[0] != "")
			copy_images(images[0], new File(directory, "banner.jpg").getAbsolutePath());
	}

	@Override
	public String getName()
	{
		return "XBMC";
	}

	@Override
	public MediaInfo openEpisode(String filename) {
		
		try {
			return this.open(filename);
		} catch (ProviderException e) {
			return null;
		}
		
	}
	
}

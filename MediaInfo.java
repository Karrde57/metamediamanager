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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class MediaInfo extends HashMap<String,String> {
	
	private boolean _imagesModified=false;
	private boolean _actorsModified=false;
	private ActorInfo[] _actors;
	private HashSet<String> _modifiedFields = new HashSet<String>();
	private HashSet<String> _newFields = new HashSet<String>();
	private static final long serialVersionUID = 1L;

	/**
	 * return informations about a film ( search by id )
	 * manage also actors 
	 * @param table table where informations will be save
	 * @param id id of the film
	 * @return MediaInfo MediaInfo with informations loaded
	 */
	public static MediaInfo load(String table, int id)
	{
		Statement statement = DBManager.getInstance().getStatement();
		MediaInfo info = null;
		try {
			ResultSet rsinfo;
			rsinfo = statement.executeQuery("select * from fields where id_"+table+"=" + id);
			
		    while(rsinfo.next())
		    {
		    	if(info == null)
		    	{
		    		info = new MediaInfo();
		    	}
		       info.put(rsinfo.getString("name"), rsinfo.getString("value"));
		       
		       if(table.equals("media"))
			    {
			    	Actor[] actors = Actor.getAllByMedia(id);
			    	if(actors != null)
			    	{
			    		info._actors = new ActorInfo[actors.length];
				    	for(int i=0; i<actors.length; i++)
				    		info._actors[i] = actors[i].getActorInfo(id);
			    	}
			    	
			    }
		    }
		    
		    
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return info;
	}
	/**
	 * save informations into the database 
	 * manage also the images and actors
	 * @param table table where informations will be save
	 * @param id id of the media
	 */
	public void save(String table, int id)
	{
		//DEBUG
		long time = System.nanoTime();
		System.out.println("1 id : " + id + " temps : " + (System.nanoTime()-time));
		
		Statement s;
		try {
			//En cas de sauvegarde d'images
			if(_imagesModified)
			{
				for(Entry<String, String> entry : this.entrySet()) {
				    String cle = entry.getKey();
				    String valeur = entry.getValue();
				    
				    if(cle.startsWith("img_"))
				    {
				    	String[] fileNames = getImages(cle);
				    	
				    	String destinationDir = M3Config.getInstance().getUserConfDirectory() + "img"+File.separatorChar+table+"_"+id+File.separatorChar+cle;
				    	File dir = new File(destinationDir);
				    	File tmpDir = null;
				    	if(dir.exists())
				    	{
				    		tmpDir = new File(destinationDir+"_old");
				    		if(tmpDir.exists())
				    			Utility.deleteFolder(tmpDir);
				    		dir.renameTo(tmpDir);
				    		for(int i=0; i<fileNames.length; i++)
				    		{
				    			fileNames[i] = fileNames[i].replace(destinationDir, tmpDir.getAbsolutePath());
				    		}
				    	}
				    	dir.mkdirs();
				    	
				    	
			    		
			    		
			    		
				    	
				    	
				    	String[] newFilenames = new String[fileNames.length];
				    	
				    	int i=0;
				    	for(String filename : fileNames)
				    	{
				    		File f = new File(filename);
				    		if(f.exists())
				    		{
					    		(new File(dir.getAbsolutePath() +File.separatorChar)).mkdirs();
					    		
					    		
					    		String destination = dir.getAbsolutePath() +File.separatorChar+i+".png";
					    		newFilenames[i] = destination;
					    		try {
									Files.copy(Paths.get(filename), Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				    		}
				    		i++;
				    	}
				    	
				    	if(tmpDir != null)
				    	{
				    		Utility.deleteFolder(tmpDir);
				    	}
				    	
				    	this.putImages(cle, newFilenames);
				    }
				    
				}
			}
			
			//DEBUG
			System.out.println("2 id : " + id + " temps : " + (System.nanoTime()-time));
			
			if(_actorsModified && table.equals("media") && _actors != null)
			{
				
				int i=0;
				
				//Actor.deleteRolesFromMedia(id);
				for(ActorInfo a : _actors)
				{
					if(i==10)
						break;
					
					Actor actor = Actor.getByName(a.getName());
					if(actor==null)
					{
						actor = new Actor(a.getName());
						actor.save();
					}
					
					actor.setRoleAndSave(id, a.getRole());

					if(!a.getImgUrl().isEmpty())
					{
						try {
							File dir = new File(M3Config.getInstance().getUserConfDirectory() +  "img"+File.separatorChar+"actors");
				    		dir.mkdirs();
							Files.copy(Paths.get(a.getImgUrl()), Paths.get(M3Config.getInstance().getUserConfDirectory() + actor.generateImageUrl()), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
						}
					}
					
					
					i++;
				}
			}
			
			//DEBUG
			System.out.println("3 id : " + id + " temps : " + (System.nanoTime()-time));
				
			
			//Insertion de nouvelles données
			for(String cle : _newFields) {
			    String valeur = get(cle);	    
			    PreparedStatement ps = DBManager.getInstance().getConnection().prepareStatement("insert into fields(id_"+table+",name,value) values(?,?,?)");
			    ps.setInt(1,id);
			    ps.setString(2, cle);
			    ps.setString(3,valeur);
			    ps.execute();
			    
			}
			
			//Modification des données existantes
			for(String cle : _modifiedFields) {
				System.out.println(cle);
				
			    String valeur = get(cle);	    
			    PreparedStatement ps = DBManager.getInstance().getConnection().prepareStatement("update fields set value=? where name=? and id_"+table+"=?");
			    ps.setString(1, valeur);
			    ps.setString(2, cle);
			    ps.setInt(3,id);
			    ps.execute();
			    
			}
			
			_newFields.clear();
			_modifiedFields.clear();
			
			//DEBUG
			System.out.println("4 id : " + id + " temps : " + (System.nanoTime()-time));
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//TODO Save images in cache
	}
	
	@Override
	public String toString() {
		String res = "";
		for(Entry<String, String> entry : this.entrySet()) {
		    String cle = entry.getKey();
		    String valeur = entry.getValue();
		    
		    res += "Info " + cle + " : " + valeur + "\n";
		}
		if(_actors != null)
		{
			res+="Acteurs : ";
			for(ActorInfo ai : _actors)
			{
				res += ai.getName();
			}
		}
		return res;
	}
	/**
	 * return the name of the key if it exists
	 * @param name
	 * @return String name of the key
	 */
	public String get(String name)
	{
		if(this.containsKey(name))
			return super.get(name);
		else
			return "";
	}
	
	
	@Override
	public String put(String name, String value)
	{
		if(!containsKey(name))
			_newFields.add(name);
		else if(!_newFields.contains(name) && !value.equals(get(name)))
			_modifiedFields.add(name);

		return super.put(name, value);
	}
	
	/**
	 * return a list of images sored in the field
	 * @param name
	 * @return array of images
	 */
	public String[] getImages(String name)
	{

		return get(name).split(";");

	}
	
	/**
	 * add images to mediaInfo
	 * @param name
	 * @param imagesNames imagesNames must be a local file
	 */
	public void putImages(String name, String[] imagesNames)
	{
		String res = "";
		for(int i = 0; i<imagesNames.length; i++)
		{
			res += imagesNames[i];
			if(i != imagesNames.length-1)
				res+=";";
		}
		
		put(name, res);
		_imagesModified=true;
	}
	
	
	/**
	 * normal setter
	 * @param actors
	 */
	public void setActors(ActorInfo[] actors)
	{
		_actorsModified = true;
		_actors=actors;
	}
	/**
	 * normal getter
	 * @return actorInfo of an actor
	 */
	public ActorInfo[] getActors()
	{
		if(_actors != null)
			return _actors;
		else
			return new ActorInfo[0];
	}

}

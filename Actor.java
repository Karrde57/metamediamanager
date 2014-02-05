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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * Class allowing the user to select and update an actor in database
 */
public class Actor {
	private String _name = "";
	private int _cacheID = 0;
	
	private static HashMap<String, Actor> _cacheName = new HashMap<String, Actor>();
	
	HashMap<Integer, String> _roles = null;
	
	private Actor()
	{
		
	}
	
	public Actor(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Generate the url of the image relative to the cache
	 * @return relative path
	 */
	public String generateImageUrl()
	{
		return "img/actors/"+_cacheID+".jpg";
	}
	
	/**
	 * Load and returns the roles of the actor
	 * key = media id
	 * string = description of the role
	 * @return
	 */
	public HashMap<Integer, String> getRoles()
	{
		if(_roles == null)
			loadRoles();
		if(_roles == null)
			_roles = new HashMap<Integer, String>();
		
		return _roles;
	}
	
	/**
	 * Replaces the local roles of the actor with new ones
	 * key = media id
	 * string = description of the role
	 * @param roles
	 */
	public void setRoles(HashMap<Integer, String> roles)
	{
		_roles = roles;
	}
	
	private void loadRoles()
	{
		_roles = new HashMap<Integer, String>();
		
		PreparedStatement s = DBManager.getInstance().preparedStatement("SELECT * from role WHERE id_actor=?");
		try {
			s.setInt(1, _cacheID);
			ResultSet rs = s.executeQuery();
			while(rs.next()) //Acteur trouvé
			{
				_roles.put(rs.getInt("id_media"), rs.getString("description"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Replaces or adds the role of the actor for the given media
	 * @param id_media ID of the media
	 * @param description description of the role
	 */
	public void setRoleAndSave(int id_media, String description)
	{
		if(_cacheID == 0)
			throw new RuntimeException("actor.setRoleAndSave : the actor is not in the database");
		try {
			PreparedStatement s;
			if(getRoles().containsKey(id_media))
			{
				s = DBManager.getInstance().preparedStatement("UPDATE role SET description=? WHERE id_actor=? AND id_media=?");
			} else {
				s = DBManager.getInstance().preparedStatement("INSERT INTO role(description, id_actor, id_media) VALUES(?,?,?)");
			}
			s.setString(1, description);
			s.setInt(2, _cacheID);
			s.setInt(3, id_media);
			s.execute();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}

		getRoles().put(id_media, description);
		
	}
	
	/*
	 * Returns the description of the role of the actor in the movie
	 * @return String of the description if role found or null
	 */
	public String getRoleByMedia(int media)
	{
		if(_roles == null)
			loadRoles();
		
		if(_roles.containsKey(media))
			return _roles.get(media);
		else
			return null;
	}
	
	/**
	 * Finds the actor with a matching name
	 * @param name name of the actor
	 * @return the actor or null if not found
	 */
	public static Actor getByName(String name)
	{
		Actor actor = null;
		
		if(_cacheName.containsKey(name))
			return _cacheName.get(name);
		
		PreparedStatement s = DBManager.getInstance().preparedStatement("SELECT * from actors WHERE lower(trim(actor_name)) = lower(trim(?))");
		try {
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			if(rs.next()) //Acteur trouvé
			{
				actor = new Actor();
				actor._cacheID = rs.getInt("id");
				actor._name = rs.getString("actor_name");
				_cacheName.put(name, actor);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return actor;
	}
	
	/**
	 * Returns the list of the actors playing in the entered media
	 * @param ID of the media
	 * @return Array of the actors
	 */
	public static Actor[] getAllByMedia(int media)
	{
		Vector<Actor> v = new Vector<Actor>();
		
		PreparedStatement s = DBManager.getInstance().preparedStatement("SELECT * FROM actors, role WHERE id_actor=id AND id_media=?");
		try {
			s.setInt(1, media);
			ResultSet rs = s.executeQuery();
			while(rs.next()) //Acteur trouvé
			{
				Actor actor = new Actor();
				actor._cacheID = rs.getInt("id");
				actor._name = rs.getString("actor_name");
				
				v.add(actor);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Actor[] res = new Actor[v.size()];
		v.toArray(res);
		return res;
	}
	
	/**
	 * Delete the role of an actor for a given media
	 * @param id_media ID of the media
	 * @param id_actor ID of the actor
	 */
	public static void deleteRole(int id_media, int id_actor)
	{
		Statement s = DBManager.getInstance().getStatement();
		try {
			s.execute("DELETE FROM role WHERE id_actor="+id_actor+" AND id_media="+id_media);
		} catch (SQLException e) {
		}
	}
	
	/**
	 * Delete every role of a media
	 * @param id_media ID of the media
	 */
	public static void deleteRolesFromMedia(int id_media)
	{
		Statement s = DBManager.getInstance().getStatement();
		try {
			s.execute("DELETE FROM role WHERE id_media="+id_media);
		} catch (SQLException e) {
		}
	}
	
	/**
	 * Adds a role with a description for the current actor
	 * @param description
	 * @param id_media ID of the media
	 */
	public void addRole(String description, int id_media)
	{
		if(_roles == null)
			loadRoles();
		if(_roles == null)
			_roles = new HashMap<Integer,String>();
		
		_roles.put(id_media, description);
	}
	
	/**
	 * Update the current actor in the database
	 */
	public void save()
	{
		try {
			PreparedStatement s;
			if(_cacheID == 0)
			{
				s = DBManager.getInstance().preparedStatement("INSERT INTO actors(actor_name) VALUES(?)");
				
					s.setString(1, _name);
					s.executeUpdate();
					
					ResultSet rs = s.getGeneratedKeys();
					rs.next();
					_cacheID = rs.getInt(1);
					
					
				
				
			}
			
			if(_roles != null)
			{
				s = DBManager.getInstance().preparedStatement("DELETE FROM role WHERE id_actor="+_cacheID);
				s.executeUpdate();
				
				for(Entry<Integer,String> entry  : _roles.entrySet())
				{
					int id_media = entry.getKey();
					String description = entry.getValue();
					
					s = DBManager.getInstance().preparedStatement("INSERT INTO role(id_media, id_actor, description) VALUES(?,?,?)");
					s.setInt(1, id_media);
					s.setInt(2, _cacheID);
					s.setString(3, description);
					s.executeUpdate();
				}
			}
			
			
			
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns known information about the current actor and a media
	 * @param id_media
	 * @return actor information in this media
	 */
	public ActorInfo getActorInfo(int id_media)
	{
		ActorInfo r = new ActorInfo(_name, getRoleByMedia(id_media), generateImageUrl());
		return r;
	}
	
	
	@Override
	public String toString()
	{
		return "Acteur : " + _name + " (id = " + _cacheID + ")";
	}
}

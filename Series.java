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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

public class Series implements Searchable {
	private MediaInfo _info = null; //null = infos non chargées
	private String _name;
	private String _seriesDirectory = "";
	private int _cacheID = 0; //id=0 : pas encore dans la bdd
	
	/**
	 * Select the series in the database 
	 * @param id ID of the series
	 * @return a Series object or null if not found
	 */
	public static Series loadById(int id) {
		Statement statement = DBManager.getInstance().getStatement();
		ResultSet rs;
		Series res = null;
		try {
			rs = statement.executeQuery("select * from series where id=" + id);
			if(rs.next()) //Série trouvée
			{	
				res = new Series();
				res._cacheID=id;
				res._name=rs.getString("series_name");
				res._seriesDirectory=rs.getString("series_path");
			} else {
				throw new Exception("Série " + id + " non trouvé");
			}
			
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Search in the database a series with this name
	 * @param name
	 * @return a Series object or null if not found
	 */
	public static Series loadByName(String name)
	{
		PreparedStatement statement;
		ResultSet rs;
		Series res = null;
		try {
			statement = DBManager.getInstance().getConnection().prepareStatement("select * from series where lower(trim(series_name))=lower(trim(?))");
			statement.setString(1, name);
			
			rs = statement.executeQuery();
			if(rs.next()) //Série trouvée
			{	
				res = new Series();
				res._cacheID=rs.getInt("id");
				res._name=name;
				res._seriesDirectory=rs.getString("series_path");
				
				return res;
			}
			
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Search in the database a series stored in this directory
	 * @param path
	 * @return a Series object or null if not found
	 */
	public static Series loadByPath(String path)
	{
		PreparedStatement statement;
		ResultSet rs;
		Series res = null;
		try {
			statement = DBManager.getInstance().getConnection().prepareStatement("select * from series where lower(trim(series_path))=lower(trim(?))");
			statement.setString(1, path);
			
			rs = statement.executeQuery();
			if(rs.next()) //Série trouvée
			{	
				res = new Series();
				res._cacheID=rs.getInt("id");
				res._seriesDirectory=path;
				res._name=rs.getString("series_path");
				return res;
			}
			
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public int getId()
	{
		return _cacheID;
	}
	
	/**
	 * Update the series in the database
	 */
	public void save()
	{
		if(getInfo().containsKey("title"))
		{
			_name = getInfo().get("title");
		}
		
		if(_cacheID == 0) //Création d'un nouveau champ
		{
			try {
				PreparedStatement statement = DBManager.getInstance().getConnection().prepareStatement("insert into series(series_name, series_path) values (?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
				statement.setString(1, _name);
				statement.setString(2, _seriesDirectory);
				statement.executeUpdate();
				
				ResultSet rs = statement.getGeneratedKeys();
				rs.next();
				_cacheID = rs.getInt(1);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else { //Mise à jour de la série
			try {
				PreparedStatement statement = DBManager.getInstance().getConnection().prepareStatement("update series set series_name=? where id=?");
				statement.setString(1, _name);
				statement.setInt(2, _cacheID);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(_info != null)
		{
			_info.save("series", _cacheID);
		}
	}
	
	public void delete() {
		Statement s;
		try {
			//Suppression
			s = DBManager.getInstance().getConnection().createStatement();
			s.execute("delete from series where id="+_cacheID);
			s.execute("delete from fields where series_id="+_cacheID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		_cacheID=0;
	}
	
	public String getDirectory()
	{
		return _seriesDirectory;
	}
	
	
	public Series(String path) {
		_seriesDirectory=path;
		File f = new File(path);
		_name = f.getName();
	}
	
	private Series()
	{
		
	}
	
	public MediaInfo getInfo() {
		loadInfo();
		return _info;
	}
	
	public void setInfo(MediaInfo mi) {
		_info = mi;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getPath()
	{
		return _seriesDirectory;
	}
	
	private void loadInfo() {
		if(_info == null)
			_info = MediaInfo.load("series", _cacheID);
		if(_info == null)
			_info = new MediaInfo();
	}

	@Override
	public String getFilename() {
		return _seriesDirectory;
	}

	@Override
	public String generateSimpleName() {
		return getName();
	}
}

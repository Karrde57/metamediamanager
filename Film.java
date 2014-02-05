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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * Class representing a film
 * @author vincent
 *
 */
public class Film extends Media{

	public Film(String name, String filename) {
		super(name, filename);
	}
	
	@Override
	public String getType() {
		return "film";
	}
	
	protected Film(ResultSet rs)
	{
		super(rs);
	}
	
	@Override
	public void save()
	{
		super.save();
		if(_cacheID == 0) //Création d'un nouveau champ
		{
			try {
				PreparedStatement statement = DBManager.getInstance().getConnection().prepareStatement("insert into medias(name, filename, type, jacket) values (?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
				statement.setString(1,_name);
				statement.setString(2, _filename);
				statement.setString(3, this.getType());
				statement.setString(4, _jacket);
				statement.executeUpdate();
				
				ResultSet rs = statement.getGeneratedKeys();
				rs.next();
				_cacheID = rs.getInt(1);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else { //Mise à jour du media
			try {
				PreparedStatement statement = DBManager.getInstance().getConnection().prepareStatement("update medias set name=?, filename=?, jacket=? where id=?");
				statement.setString(1,_name);
				statement.setString(2, _filename);
				statement.setString(3, _jacket);
				statement.setInt(4, _cacheID);
				
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
	}
}

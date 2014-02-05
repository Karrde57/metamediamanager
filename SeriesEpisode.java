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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeriesEpisode extends Media {
	
	private Series _series = null;
	private int _seriesId = 0;
	private int _season = 0;
	private int _episodeNum = 0;
	


	public SeriesEpisode(ResultSet rs) {
		super(rs);
		try {
			_seriesId = rs.getInt("id_series");
			_season = rs.getInt("numseason");
			_episodeNum = rs.getInt("numepisode");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Creates a SeriesEpisode using its filename. If the filename is incorrect (ex : qzefzef.avi instead of S01E01.avi), null is returned
	 * @param name
	 * @param filename
	 * @param seriesPath
	 * @return
	 */
	public static SeriesEpisode fromFilename(String name, String filename, String seriesPath)
	{
		Matcher matcher = Pattern.compile("^.*[sS]([0-9]+)\\.?[eE]([0-9]+)").matcher(name);
		if (matcher.find()) {
			SeriesEpisode se = new SeriesEpisode(name, filename);
			
		    se._season = Integer.parseInt(matcher.group(1));
		    se._episodeNum = Integer.parseInt(matcher.group(2));
		    //Try to find if the series is known
		    Series s = Series.loadByPath(seriesPath);
		    if(s != null) //Found
		    {
		    	se._seriesId=s.getId();
		    } else {
		    	s = new Series(seriesPath);
		    	s.save();
		    	se._seriesId = s.getId();
		    }
		    
		    return se;
		}
		
		
		return null;
	}
	
	private SeriesEpisode(String name, String filename)
	{
		super(name, filename);
	}

	
	public int getEpisodeNumber()
	{
		return _episodeNum;
	}
	
	public int getSeasonNumber()
	{
		return _season;
	}

	@Override
	public String getType() {
		return "seriesEpisode";
	}
	
	@Override
	public void save()
	{
		super.save();
		if(_cacheID == 0) //Création d'un nouveau champ
		{
			try {
				PreparedStatement statement = DBManager.getInstance().getConnection().prepareStatement("insert into medias(name, filename, type, numseason, numepisode, id_series, jacket) values (?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
				statement.setString(1, _name);
				statement.setString(2, _filename);
				statement.setString(3, this.getType());
				statement.setInt(4, _season);
				statement.setInt(5, _episodeNum);
				if(_seriesId != 0)
					statement.setInt(6, _seriesId);
				else
					statement.setNull(6, java.sql.Types.INTEGER);
				statement.setString(7, _jacket);
				statement.executeUpdate();
				
				ResultSet rs = statement.getGeneratedKeys();
				rs.next();
				_cacheID = rs.getInt(1);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else { //Mise à jour du media
			try {
				PreparedStatement statement = DBManager.getInstance().getConnection().prepareStatement("update medias set name=?, filename=?, numseason=?, numepisode=?, id_series=?, jacket=? where id=?");
				statement.setString(1, _name);
				statement.setString(2, _filename);
				statement.setInt(3, _season);
				statement.setInt(4, _episodeNum);
				if(_seriesId != 0)
					statement.setInt(5, _seriesId);
				else
					statement.setNull(5, java.sql.Types.INTEGER);
				statement.setString(6, _jacket);
				statement.setInt(7, _cacheID);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	public Series getSeries() {
		if(_series == null)
		{ //Load Series information
			_series = Series.loadById(_seriesId);
		}
		return _series;
	}
	
	public int getSeriesId()
	{
		return _seriesId;
	}

}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Vector;
import com.t3.metamediamanager.Series;
import com.t3.metamediamanager.SeriesEpisode;
/**
 * Class representing a film or an episode stored in the database
 */
public class Media implements Searchable {
	protected MediaInfo _info = null; //null = infos non chargées
	protected String _filename;
	protected String _name;
	protected String _jacket = "";
	protected int _cacheID = 0; //id=0 : pas encore dans la bdd
	
	

	/**
	 * Select the media (film or episode) stored in database with its id
	 * @param id ID of the media
	 * @return	the wanted media
	 */
	public static Media getById(int id) {
		Statement statement = DBManager.getInstance().getStatement();
		ResultSet rs;
		Media res = null;
		try {
			rs = statement.executeQuery("select * from medias where id=" + id);
			if(rs.next()) //Media trouvé
			{
				String filename = rs.getString("filename");
				String type = rs.getString("type");		
				
				if(type.compareTo("film") == 0)
				{
					res = new Film(rs);
				} else if(type.compareTo("series") == 0) {
					res = new SeriesEpisode(rs);
				} else {
					throw new Exception("Media " + id + " n'est ni un film ni une série");
				}

			} else {
				throw new Exception("Media " + id + " non trouvé");
			}
					
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Select all media matching with the request, using the name
	 * @param name Partial name of the media
	 * @param filter Filter (only films, only series...)
	 * @returns vector of the matching medias
	 */
	public static Vector<Media> searchByName(String name, MediaFilter filter)
	{
		PreparedStatement statement;
		ResultSet rs;
		Vector<Media> liste = new Vector<Media>();
		try {
			String conditions = "";
			if(filter != null)
			{
				if(filter.type == MediaFilter.Type.NONE)
					return new Vector<Media>();
				if(filter.type == MediaFilter.Type.FILMS)
					conditions += " AND type='film'";
				else if(filter.type == MediaFilter.Type.EPISODES)
					conditions += " AND type='seriesEpisode'";
				
				if(filter.completion == MediaFilter.Completion.COMPLETE)
					conditions += " AND (SELECT count(*) FROM fields WHERE id_media=id) > 0";
				else if(filter.completion == MediaFilter.Completion.NOT_COMPLETE)
					conditions += " AND (SELECT count(*) FROM fields WHERE id_media=id) = 0";
			}
			
			statement = DBManager.getInstance().getConnection().prepareStatement("select * from medias where name like ? " + conditions + " order by name asc");

			statement.setString(1, "%" + name + "%");
			
			rs = statement.executeQuery();
			while(rs.next()) //Media trouvé
			{
				String type = rs.getString("type");		
				Media res;
				if(type.compareTo("film") == 0)
				{
					res = new Film(rs);
				} else if(type.compareTo("seriesEpisode") == 0) {
					res = new SeriesEpisode(rs);
				} else {
					throw new Exception("Media " + rs.getString("name") + " n'est ni un film ni une série");
				}
				liste.add(res);

			}
			
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return liste;
	}
	
	/**
	 * Finds the media with the given filename
	 * @param filename filename
	 * @return the media or null if not found
	 */
	public static Media getByFilename(String filename)
	{
		PreparedStatement statement;
		ResultSet rs;
		Media res = null;
		try {
			statement = DBManager.getInstance().getConnection().prepareStatement("select * from medias where filename=?");
			statement.setString(1, filename);
			
			rs = statement.executeQuery();
			if(rs.next()) //Media trouvé
			{
				String type = rs.getString("type");		
				
				if(type.compareTo("film") == 0)
				{
					res = new Film(rs);
				} else if(type.compareTo("seriesEpisode") == 0) {
					res = new SeriesEpisode(rs);
				} else {
					throw new Exception("Media " + filename + " n'est ni un film ni une série");
				}

			}
			
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	
	

	protected Media(ResultSet rs)
	{
		try {
			_filename = rs.getString("filename");
			_name = rs.getString("name");
			_cacheID = rs.getInt("id");
			_jacket = rs.getString("jacket");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Updates the media in database
	 */
	public void save()
	{
		if(_info != null)
		{
			_info.save("media", _cacheID);
			if(_info.containsKey("img_poster"))
			{
				String[] jackets = _info.getImages("img_poster");
				if(jackets.length>0)
					_jacket = jackets[0];
			}
			if(_info.containsKey("title"))
			{
				_name = _info.get("title");
			}
		}
	}
	
	/**
	 * Check if the media has information in database
	 * @return true if the medias has information
	 */
	public boolean hasInfos()
	{
		if(_cacheID != 0 && _info == null) //Si les infos ne sont pas chargées mais qu'elles sont en bdd
			loadInfo();
		
		return _info!=null;
	}
	
	/**
	 * Delete the media from the database
	 */
	public void delete() {
		Statement s;
		try {
			//Suppression
			s = DBManager.getInstance().getConnection().createStatement();
			s.execute("delete from medias where id="+_cacheID);
			s.execute("delete from fields where media_id="+_cacheID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		_cacheID=0;
	}
	
	/**
	 * Creates a new media
	 * @param name
	 * @param filename
	 */
	public Media(String name, String filename) {
		_name = name;
		_filename = filename;
	}
	
	/**
	 * Select all known information from the database
	 * @return MediaInfo containing all known
	 */
	public MediaInfo getInfo() {
		if(_cacheID != 0 && _info == null) //Si les infos ne sont pas chargées mais qu'elles sont en bdd
			loadInfo();
		if(_info == null) //Si nouveau media
			_info = new MediaInfo();		
		return _info;
	}
	
	/**
	 * Replace local information by a new MediaInfo
	 * @param i new information
	 */
	public void setInfo(MediaInfo i)
	{
		_info = i;
	}
	
	public int getId()
	{
		return _cacheID;
	}

	/**
	 * Removes every useless parts from the media's name
	 * @return simple name of the name
	 */
	public String generateSimpleName()
    {
		String strTitleAndYear, strFileName, strYear;
		
		strFileName = _name.toLowerCase();
		
		strTitleAndYear = strFileName;

		String[] regexps = {"[ _\\,\\.\\(\\)\\[\\]\\-](ac3|dts|custom|dc|french|remastered|divx|divx5|dsr|dsrip|dutch|dvd|dvd5|dvd9|dvdrip|dvdscr|dvdscreener|screener|dvdivx|cam|fragment|fs|hdtv|hdrip|hdtvrip|internal|limited|multisubs|ntsc|ogg|ogm|pal|pdtv|proper|repack|rerip|retail|r3|r5|bd5|se|svcd|swedish|german|read.nfo|nfofix|unrated|extended|ws|telesync|ts|telecine|tc|brrip|bdrip|480p|480i|576p|576i|720p|720i|1080p|1080i|3d|hrhd|hrhdtv|hddvd|bluray|x264|h264|xvid|xvidvd|xxx|www.www|cd[1-9]|\\[.*\\])([ _\\,\\.\\(\\)\\[\\]\\-]|$)", "(\\[.*\\])"};

		String regCleanDate = "(.*[^ _\\,\\.\\(\\)\\[\\]\\-])[ _\\.\\(\\)\\[\\]\\-]+(19[0-9][0-9]|20[0-1][0-9])([ _\\,\\.\\(\\)\\[\\]\\-]|[^0-9]$)";
		
		Matcher matcher = Pattern.compile(regCleanDate).matcher(strTitleAndYear);
		
		if (matcher.find())
		    {
		      strTitleAndYear = matcher.group(1);
		      strYear = matcher.group(2);
		    }
		
		if(strTitleAndYear.contains("."))
		  strTitleAndYear = strTitleAndYear.substring(0, strTitleAndYear.lastIndexOf('.')); //remove extension

		  for (int i = 0; i < regexps.length; i++)
		  {
			 matcher = Pattern.compile(regexps[i]).matcher(strTitleAndYear);

		    int j=0;
		    if (matcher.find() && (j=matcher.end()-1) > 0)
		      strTitleAndYear = strTitleAndYear.substring(0, j);
		  }
		  

		  // final cleanup - special characters used instead of spaces:
		  // all '_' tokens should be replaced by spaces
		  // if the file contains no spaces, all '.' tokens should be replaced by
		  // spaces - one possibility of a mistake here could be something like:
		  // "Dr..StrangeLove" - hopefully no one would have anything like this.

		    boolean initialDots = true;
		    boolean alreadyContainsSpace = strTitleAndYear.contains(" ");

		    StringBuilder sb = new StringBuilder(strTitleAndYear);
		    
		    for (int i = 0; i < (int)sb.length(); i++)
		    {
		      char c = sb.charAt(i);

		      if (c != '.')
		        initialDots = false;

		      if ((c == '_') || ((!alreadyContainsSpace) && !initialDots && (c == '.')))
		      {
		        sb.setCharAt(i, ' ');
		      }
		    }
		  

		  strTitleAndYear = sb.toString();
		  
		  return strTitleAndYear;
    }
	
	
	public static Media[] getAll()
	{
		Vector<Media> vec = new Vector<Media>();
		
		PreparedStatement statement;
		ResultSet rs;
		Media res = null;
		try {
			statement = DBManager.getInstance().getConnection().prepareStatement("select * from medias");
			
			rs = statement.executeQuery();
			while(rs.next()) //Media trouvé
			{
				String type = rs.getString("type");		
				
				if(type.compareTo("film") == 0)
				{
					res = new Film(rs);
				} else if(type.compareTo("seriesEpisode") == 0) {
					res = new SeriesEpisode(rs);
				} else {
					throw new Exception("Media " + rs.getInt("id") + " n'est ni un film ni une série");
				}
				
				vec.add(res);

			}
			
			statement.close();	
			    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Media[] array = new Media[vec.size()];
		vec.toArray(array);
		return array;
	}
	
	
	
	
	public void renameMediaString (String newname)
	{
		System.out.println("nom recu par renamemediastring : " + newname);
		File directory = new File(this.getFilename()).getParentFile();
		List<File> fileliste = new ArrayList<File>();
		fileliste = Arrays.asList(directory.listFiles()); 
		Iterator<File> i = fileliste.iterator();
		String name = new File(this.getFilename()).getName().substring(0,new File(this.getFilename()).getName().lastIndexOf('.'));
		File metadatafile = null;
		while(i.hasNext())	//rename first all the file which contains the name of the media
		{
			File file = (File)i.next();
			if(file.getName().contains(name))
			{
				System.out.println("detection du film");
				String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.'));	//return extensions
				File newfile = new File(directory, newname + ext);
				System.out.println("renommage du fichier en " + newname+ext);
				file.renameTo(newfile);
				System.out.println("Avant ->>>>>>>>>>>>>>>" + this._filename);
				this._filename = replace(this._filename, name, newname);
				System.out.println("Après ->>>>>>>>>>>>>>>" + this._filename);
				this.save();
			}
			if(file.getName().contains("metadata")) // for mediabrowser
			{
				System.out.println("dossier metadata présent");
				metadatafile = file;
			}

		}
		if(metadatafile != null && metadatafile.exists()) // same
		{
			System.out.println("renommage des fichiers dans metadata");
			List<File> fileliste2 = new ArrayList<File>();
			fileliste2 = Arrays.asList(metadatafile.listFiles());
			Iterator<File> i2 = fileliste2.iterator();
			while(i2.hasNext())
			{
				File file = (File)i2.next();
				if(file.getName().contains(name))
				{
					String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.'));	
					File newfile = new File(metadatafile, newname + ext);
					file.renameTo(newfile);
				}
			}
		}
	}
	
	
	
	public String getFilename() {
		return _filename;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getJacket()
	{
		return _jacket;
	}
	
	public String getType() {
		return "";
	}
	
	/**
	 * Search subtitles located in the movie directory
	 * TODO : process multiple subtitles files
	 * @return
	 */
	public File[] searchLocalSubtitles()
	{
		String noExt = _filename.substring(0, _filename.lastIndexOf('.'));
		
		File subFile = new File(noExt + ".srt");
		
		if(subFile.exists())
		{
			File[] files = new File[1];
			files[0] = subFile;
			return files;
		}
		
		return new File[0];
	}
	
	
	private void loadInfo() {
		_info = MediaInfo.load("media", _cacheID);
	}
	public String getAllocineUrl()
	{
		String id;
		if(this instanceof Film)
		{
			id = this.getInfo().get("AllocineId");
			if(id != "" || id != null)
				return "http://www.allocine.fr/film/fichefilm_gen_cfilm="+id+".html";
		}
		else if(this instanceof SeriesEpisode)
		{
			id  = ((SeriesEpisode)this).getSeries().getInfo().get("AllocineId");
			if(id != "" || id != null)
				return "http://www.allocine.fr/series/ficheserie_gen_cserie="+id+".html";
		}
			return "";
	}
    private static String replace(String originalText, String subStringToFind, String subStringToReplaceWith) {
		int s = 0;
		int e = 0;
		
		StringBuffer newText = new StringBuffer();
		
		while ((e = originalText.indexOf(subStringToFind, s)) >= 0) {
		
		   newText.append(originalText.substring(s, e));
		   newText.append(subStringToReplaceWith);
		   s = e + subStringToFind.length();
		
		}
		
		newText.append(originalText.substring(s));
		return newText.toString();

} // end replace(String, String, String)
}


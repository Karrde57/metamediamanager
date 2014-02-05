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

import java.util.EnumSet;

/**
 * Represents the request sent to the provider
 * (Series, episode, film, season...)
 */
public class ProviderRequest {
	public enum Type {FILM, SERIES, SEASON, EPISODE};
	public enum Additions {JACKETS, SUBTITLES};
	
	private Type _type;
	private String _name ="";
	private String _filename = "";
	private int _season = 0;
	private int _episode = 0;
	private String _seriesName = "";
	private String _language = "en";
	private String _hash = "";
	private EnumSet<Additions> _additions = EnumSet.noneOf(Additions.class);
	private String _imdbID = "";
	
	/**
	 * Request with film parameters
	 * @param type request type
	 * @param name media name
	 * @param filename media filename
	 * @param l language
	 */
	public ProviderRequest(Type type, String name, String filename, String l)
	{
		_type = type;
		_filename = filename;
		_language = l;
		_name = name;
		_seriesName = name;
	}
	
	/**
	 * Request with episode parameters
	 * @param type request type
	 * @param seriesName name of the series
	 * @param filename path of the directory of the series
	 * @param l language
	 * @param season season of the episode
	 * @param episode episode number
	 */
	public ProviderRequest(Type type, String seriesName, String filename, String l, int season, int episode)
	{
		this(type, "", filename, l);
		_season = season;
		_episode = episode;
		_seriesName = seriesName;
	}
	
	/**
	 * Request with series parameters
	 * @param type request type
	 * @param seriesName name of the series
	 * @param l language
	 */
	public ProviderRequest(Type type, String seriesName, String l)
	{
		_type=type;
		_seriesName = seriesName;
		_language = l;
	}

	public String getName() {
		return _name;
	}
	public String getFilename() {
		return _filename;
	}
	public int getSeason() {
		return _season;
	}
	public String getSeriesName() {
		return _seriesName;
	}
	public String getLanguage() {
		return _language;
	}
	public int getEpisode() {
		return _episode;
	}
	public Type getType()
	{
		return _type;
	}
	public String getHash()
	{
		return _hash;
	}
	public EnumSet getAdditions()
	{
		return _additions;
	}
	public void setAdditions(EnumSet s)
	{
		_additions = s;
	}
	
	public void setHash(String hash)
	{
		_hash=hash;
	}
	
	public void setImdbID(String id)
	{
		_imdbID = id;
	}
	
	public String getImdbID()
	{
		return _imdbID;
	}
	
}

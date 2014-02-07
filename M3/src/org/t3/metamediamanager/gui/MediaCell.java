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
*/package org.t3.metamediamanager.gui;

import java.io.File;

import org.t3.metamediamanager.Media;
import org.t3.metamediamanager.Series;
import org.t3.metamediamanager.SeriesEpisode;


/**
 * Cell displaying the jacket of the media, in the main media grid
 * @author vincent
 *
 */
public class MediaCell extends ImageCell {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Media _media = null;
	private Series _series = null;
	
	public enum Type {MEDIA, SERIES, SEASON};
	private Type _type;
	
	/**
	 * Constructs the cell representing a media
	 * @param media
	 */
	public MediaCell(Media media)
	{
		super(new File(media.getJacket()), (media instanceof SeriesEpisode) ? "S"+((SeriesEpisode)media).getSeasonNumber() + "E" + ((SeriesEpisode)media).getEpisodeNumber() + " " +media.getName() : media.getName());
		_media = media;
		_type = Type.MEDIA;
	}
	
	private MediaCell(File f, String name)
	{
		super(f, name);
	}
	
	
	/**
	 * Constructs the cell representing a season 
	 */
	public static MediaCell makeFromSeason(Series s, int season)
	{
		SeriesEpisode episode = SeriesEpisode.getBySeasonAndEpisodeNumber(s.getId(), season, 1);
		MediaCell mc;
		if(episode != null)
			mc = new MediaCell(new File(episode.getJacket()), "Saison " + season);
		else
			mc = new MediaCell(null, "Saison " + season);
		mc._type = Type.SEASON;
		mc._series = s;
		return mc;
	}
	
	public static MediaCell makeFromSeries(Series s)
	{
		MediaCell mc = new MediaCell(new File(s.getPoster()), s.getName());
		mc._type = Type.SERIES;
		mc._series = s;
		return mc;
	}
	
	public Media getMedia()
	{
		return _media;
	}
	
	public Series getSeries()
	{
		return _series;
	}
	
	public Type getType()
	{
		return _type;
	}


}

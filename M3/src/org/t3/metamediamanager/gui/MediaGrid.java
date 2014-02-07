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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.t3.metamediamanager.Media;
import org.t3.metamediamanager.MediaFilter;
import org.t3.metamediamanager.Series;
import org.t3.metamediamanager.SeriesEpisode;



/**
 * Grid of pictures. The user can select it and scroll. Pictures are posters of Media.
 * @author vincent
 *
 */
public class MediaGrid extends ImageGrid {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public MediaGrid()
	{
		super();
	
		addCellSelectedListener(new CellSelectedListener() {

			@Override
			public void imageChanged(CellSelectedEvent e) {
				MediaCell mc = (MediaCell) e.getCell();
				
				LibTreeSelectionEvent event;
				if(mc.getType() == MediaCell.Type.MEDIA)
				{
					event = new LibTreeSelectionEvent(MediaGrid.this, LibTreeSelectionEvent.Type.MEDIA);
					event.media = mc.getMedia();
				} else if(mc.getType() == MediaCell.Type.SERIES)
				{
					event = new LibTreeSelectionEvent(MediaGrid.this, LibTreeSelectionEvent.Type.SERIES);
					event.series = mc.getSeries();
				} else //season
				{
					event = new LibTreeSelectionEvent(MediaGrid.this, LibTreeSelectionEvent.Type.SEASON);
					event.series = mc.getSeries();

					event.season = Integer.parseInt(mc.getText().replace("Saison ", ""));
				}
				
				fireEvent(event);
			}
			
		});
		
	}
	
	public void addLibTreeSelectionListener(LibTreeSelectionListener l)
	{
		listenerList.add(LibTreeSelectionListener.class, l);
	}
	
	public void removeLibTreeSelectionListener(LibTreeSelectionListener l)
	{
		listenerList.remove(LibTreeSelectionListener.class, l);
	}
	
	
	private void fireEvent(LibTreeSelectionEvent a)
	{
		Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i = i+2) {
	      if (listeners[i] == LibTreeSelectionListener.class) {
	        ((LibTreeSelectionListener) listeners[i+1]).selected(a);
	      }
	    }
	}

	
	/**
	 * Fills the grid by making a request to the cache
	 * @param search
	 * @param filter
	 */
	public void search(String search, MediaFilter filter)
	{
		Vector<Media> medias = Media.searchByName(search, filter);
		_list.clear();
		container.removeAll();
		
		for(Media media : medias)
		{
			MediaCell mc = new MediaCell(media);
			addCell(mc);
			
		}
		
		updateUI();
	}
	
	public void showSeries(Series s)
	{
		container.removeAll();
		
		int[] seasons = s.getAvalaibleSeason();
		
		for(int i=0; i<seasons.length; i++)
		{
			MediaCell mc = MediaCell.makeFromSeason(s, seasons[i]);
			addCell(mc);
		}
		
		updateUI();
	}
	
	public void showAllSeries()
	{
		container.removeAll();
		
		Series[] series = Series.getAll();
				
		for(Series s : series)
		{
			MediaCell mc = MediaCell.makeFromSeries(s);
			addCell(mc);
		}
		
		updateUI();
	}
	
	public void showSeason(Series s, int season)
	{
		container.removeAll();
		
		SeriesEpisode[] episodes = SeriesEpisode.getAllBySeason(s.getId(), season);
		for(SeriesEpisode se : episodes)
		{
			MediaCell mc = new MediaCell(se);
			addCell(mc);
		}
		
		updateUI();
	}
	
	public Media[] getAllMedias()
	{
		Component[] components = container.getComponents();
		List<Media> mediaList = new ArrayList<Media>();
		for(Component c: components)
		{
			if(c instanceof MediaCell)
			{
				MediaCell cell = (MediaCell) c;
				if(cell.getMedia() != null)
					mediaList.add(cell.getMedia());
			}
		}
		
		Media[] tab = new Media[mediaList.size()];
		mediaList.toArray(tab);
		
		return tab;
	}
	
	/**
	 * Returns the current selected media (null if nothing selected)
	 * @return the media or null if nothing is selected
	 */
	public Media getSelectedMedia()
	{
		ImageCell cell = getSelectedCell();
		if(cell != null)
		{
			return ((MediaCell) cell).getMedia();
		}
		return null;
	}
	
	
	public void setSelectedMedia(Media media)
	{
		Component[] components = container.getComponents();

		for(Component c: components)
		{
			if(c instanceof MediaCell)
			{
				MediaCell cell = (MediaCell) c;
				if(cell.getMedia() != null)
				{
					if(cell.getMedia().equals(media))
						cell.setSelected(true);
					else
						cell.setSelected(false);
				}
					
			}
		}
		
		updateUI();
	}
	
}

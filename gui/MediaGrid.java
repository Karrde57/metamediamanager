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
package com.t3.metamediamanager.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox.KeySelectionManager;

import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.MediaFilter;

class MediaChangedEvent extends EventObject
{
	private Media _media;
	public MediaChangedEvent(Object source, Media media) {
		super(source);
		_media=media;
	}
	
	public Media getMedia()
	{
		return _media;
	}
}

interface MediaChangedListener extends EventListener
{
	public void mediaChanged(MediaChangedEvent e);
}

public class MediaGrid extends ImageGrid {
	
	
	public MediaGrid()
	{
		super();
	
		addCellSelectedListener(new CellSelectedListener() {

			@Override
			public void imageChanged(CellSelectedEvent e) {
				MediaCell mc = (MediaCell) e.getCell();
				
				Object[] listeners = listenerList.getListenerList();
			    for (int i = 0; i < listeners.length; i = i+2) {
			      if (listeners[i] == MediaChangedListener.class) {
			        ((MediaChangedListener) listeners[i+1]).mediaChanged(new MediaChangedEvent(this, mc.getMedia()));
			      }
			    }
			}
			
		});
		
	}
	
	public void addMediaChangedListener(MediaChangedListener mcl)
	{
		listenerList.add(MediaChangedListener.class, mcl);
	}
	
	public void removeMediaChangedListener(MediaChangedListener mcl)
	{
		listenerList.remove(MediaChangedListener.class, mcl);
	}
	
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
	
	public Media[] getAllMedias()
	{
		Media[] list = new Media[_list.size()];
		for(int i=0; i<_list.size(); i++)
		{
			list[i]=((MediaCell)(_list.get(i))).getMedia();
		}
		return list;
	}
	
	public Media getSelectedMedia()
	{
		ImageCell cell = getSelectedCell();
		if(cell != null)
		{
			return ((MediaCell) cell).getMedia();
		}
		return null;
	}
	
}

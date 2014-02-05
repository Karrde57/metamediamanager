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

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import com.t3.metamediamanager.Film;
import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.SeriesEpisode;

public class LibraryTree extends JTree {
	
	private class MyRenderer extends DefaultTreeCellRenderer {

	    private Icon mediaIcon = getIcon(getClass().getClassLoader().getResource("com/t3/metamediamanager/gui/icons/Media.png"));
	    private Icon folderIcon = getIcon(getClass().getClassLoader().getResource("com/t3/metamediamanager/gui/icons/Folder.png"));

	    
	    private ImageIcon getIcon(URL url)
	    {
			try {
				BufferedImage img = ImageIO.read(url);
				Image resized = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
		    	return new ImageIcon(resized);
			} catch (IOException e) {
				System.out.println("erreur récup icone");
			}
	    	return null;
	    }

	    @Override
	    public Component getTreeCellRendererComponent(JTree tree, Object value,
	        boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
	        super.getTreeCellRendererComponent(
	            tree, value, sel, exp, leaf, row, hasFocus);
	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	        if (node instanceof MediaNode) {
	            setIcon(mediaIcon);
	        } else {
	        	setIcon(folderIcon);
	        }
	        return this;
	    }
	}
	
	
	private class MediaNode extends DefaultMutableTreeNode
	{
		private Media _media;
		public MediaNode(Media media)
		{
			super(media.getName());
			_media=media;
		}
		public Media getMedia()
		{
			return _media;
		}
	}
	
	private class LibraryTreeModel extends DefaultTreeModel {
		private class SeriesNode extends DefaultMutableTreeNode
		{
			private HashMap<Integer, DefaultMutableTreeNode> _seasons = new HashMap<Integer, DefaultMutableTreeNode>();
			
			public SeriesNode(String name)
			{
				super(name);
			}
			
			public void addToSeason(DefaultMutableTreeNode node, int season)
			{
				if(!_seasons.containsKey(season))
				{
					DefaultMutableTreeNode seasonNode = new DefaultMutableTreeNode("Saison " + season);
					_seasons.put(season, seasonNode);
					add(seasonNode);
					
				}
				_seasons.get(season).add(node);;
			}
		}
		
		

		
		public LibraryTreeModel()
		{
			super(new DefaultMutableTreeNode("root"));
			
			
			
			DefaultMutableTreeNode filmsNode = new DefaultMutableTreeNode("Films");
			DefaultMutableTreeNode seriesNode = new DefaultMutableTreeNode("Séries");
			
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
			root.add(filmsNode);
			root.add(seriesNode);
			
			//Load all
			Media[] all = Media.getAll();
			
			HashMap<Integer, SeriesNode> seriesNameNodes = new HashMap<Integer, SeriesNode>();
			
			
			for(Media media : all)
			{
				if(media instanceof Film)
				{
					MediaNode filmNode = new MediaNode(media);
					filmsNode.add(filmNode);
				} else {
					SeriesEpisode episode = (SeriesEpisode) media;
					MediaNode sNode = new MediaNode(media);
					if(!seriesNameNodes.containsKey(episode.getSeriesId()))
					{
						seriesNameNodes.put(episode.getSeriesId(), new SeriesNode(episode.getSeries().getName()));
						seriesNode.add(seriesNameNodes.get(episode.getSeriesId()));
					}
					
					seriesNameNodes.get(episode.getSeriesId()).addToSeason(sNode, episode.getSeasonNumber());
				}
			}
		}
		
	}
	
	
	public LibraryTree()
	{
		reloadLibrary();
		setRootVisible(false);
		
		this.setCellRenderer(new MyRenderer());
		
		addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		                           LibraryTree.this.getLastSelectedPathComponent();

		    /* if nothing is selected */ 
		        if (node == null) return;

		    /* retrieve the node that was selected */ 
		    
		        if(node instanceof MediaNode)
		        {
		        	MediaNode mediaNode = (MediaNode) node;
		        	
		        	//Fire event
		        	Object[] listeners = listenerList.getListenerList();
				    for (int i = 0; i < listeners.length; i = i+2) {
				      if (listeners[i] == MediaChangedListener.class) {
				        ((MediaChangedListener) listeners[i+1]).mediaChanged(new MediaChangedEvent(this, mediaNode.getMedia()));
				      }
				    }
		        }
		    }
		});
		
			
	}
	
	public void reloadLibrary()
	{
		setModel(new LibraryTreeModel());
		for (int i = 0; i < getRowCount(); i++) {
		    expandRow(i);
		}
	}
	
	public void addMediaChangedListener(MediaChangedListener mcl)
	{
		listenerList.add(MediaChangedListener.class, mcl);
	}
	
	public void removeMediaChangedListener(MediaChangedListener mcl)
	{
		listenerList.remove(MediaChangedListener.class, mcl);
	}

}

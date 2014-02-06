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
*/package com.t3.metamediamanager.gui;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.t3.metamediamanager.Film;
import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.Series;
import com.t3.metamediamanager.SeriesEpisode;




/**
 * Tree displayed at left of the main interface.
 * Tree structure :
 * 	-root
 * 	  -films
 *      -film 1
 *      -film 2
 *    -series
 *    	-series 1
 *    	  -season 1
 *    		-episode 1
 * @author vincent
 *
 */
public class LibraryTree extends JTree {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Renderer used to apply differents icons to the nodes, depending on its nature ("directory", media)..
	 * @author vincent
	 *
	 */
	private class MyRenderer extends DefaultTreeCellRenderer {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Icon mediaIcon = getIcon(getClass().getClassLoader().getResource("com/t3/metamediamanager/gui/icons/Media.png"));
	    private Icon mediaRedIcon = getIcon(getClass().getClassLoader().getResource("com/t3/metamediamanager/gui/icons/MediaRed.png"));
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
	            MediaNode mediaNode = (MediaNode) node;
	            if(mediaNode.getMedia().hasInfos())
	            	setIcon(mediaIcon);
	            else
	            	setIcon(mediaRedIcon);
	        } else {
	        	setIcon(folderIcon);
	        }
	        return this;
	    }
	}
	
	/**
	 * Node containing a reference to a Media
	 * @author vincent
	 *
	 */
	private class MediaNode extends DefaultMutableTreeNode
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Media _media;
		public MediaNode(Media media)
		{
			super((media instanceof SeriesEpisode) ? ((SeriesEpisode)media).getEpisodeNumber() + " " +media.getName() : media.getName());
			_media=media;
		}
		public Media getMedia()
		{
			return _media;
		}
	}
	
	/**
	 * Node used to represent a series
	 * @author vincent
	 *
	 */
	private class SeriesNode extends DefaultMutableTreeNode
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private HashMap<Integer, DefaultMutableTreeNode> _seasons = new HashMap<Integer, DefaultMutableTreeNode>();
		private Series _series;
		public SeriesNode(Series series)
		{
			super(series.getName());
			_series = series;
		}
		
		public Series getSeries()
		{
			return _series;
		}
		
		/**
		 * Add a node to a "season" node (and create it if not already done)
		 * @param node
		 * @param season
		 */
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
	
	
	/**
	 * Model used by the tree
	 * @author vincent
	 *
	 */
	private class LibraryTreeModel extends DefaultTreeModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		DefaultMutableTreeNode _filmsNode = new DefaultMutableTreeNode("Films");
		DefaultMutableTreeNode _seriesNode = new DefaultMutableTreeNode("Séries");
		
		/**
		 * Inits the tree model
		 */
		public LibraryTreeModel()
		{
			super(new DefaultMutableTreeNode("Bibliothèque"));
			
			
			//We create the 2 main nodes

			
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
			root.add(_filmsNode);
			root.add(_seriesNode);
			
			//Load all
			Media[] all = Media.getAll();
			
			HashMap<Integer, SeriesNode> seriesNameNodes = new HashMap<Integer, SeriesNode>();
			
			
			for(Media media : all)
			{
				if(media instanceof Film)
				{
					MediaNode filmNode = new MediaNode(media);
					_filmsNode.add(filmNode);
				} else {
					SeriesEpisode episode = (SeriesEpisode) media;
					MediaNode sNode = new MediaNode(media);
					if(!seriesNameNodes.containsKey(episode.getSeriesId()))
					{
						seriesNameNodes.put(episode.getSeriesId(), new SeriesNode(episode.getSeries()));
						_seriesNode.add(seriesNameNodes.get(episode.getSeriesId()));
					}
					
					seriesNameNodes.get(episode.getSeriesId()).addToSeason(sNode, episode.getSeasonNumber());
				}
			}
		}
		
		public DefaultMutableTreeNode getSeriesNode()
		{
			return _seriesNode;
		}
		
		public DefaultMutableTreeNode getFilmsNode()
		{
			return _filmsNode;
		}
		
		/**
		 * Returns the node assiociated to a media.
		 * This is used when we want to select a media's node
		 * @param media
		 * @return
		 */
		public MediaNode getNodeByMedia(Media media)
		{
			List<DefaultMutableTreeNode> leaves = new ArrayList<DefaultMutableTreeNode>();
			getLeaves((DefaultMutableTreeNode) getRoot(), leaves);
			
			for(DefaultMutableTreeNode node : leaves)
			{
				if(node instanceof MediaNode)
				{
					MediaNode mediaNode = (MediaNode) node;
					if(mediaNode.getMedia().equals(media))
						return mediaNode;
				}
			}
			
			return null;
		}
		
		/**
		 * Puts in the list "leaves" all the leaves from the node "node"
		 * @param node
		 * @param leaves
		 */
		public void getLeaves(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> leaves)
		{
			if(node.isLeaf())
				leaves.add(node);
			else {
				for(int i=0; i<node.getChildCount(); i++)
				{
					getLeaves((DefaultMutableTreeNode)node.getChildAt(i), leaves);
				}
			}
		}
		

		
	}
	
	
	
	/**
	 * Constructs the tree
	 */
	public LibraryTree()
	{
		reloadLibrary();
		setRootVisible(true);
		
		this.setCellRenderer(new MyRenderer());
		
		addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		                           LibraryTree.this.getLastSelectedPathComponent();

		    /* if nothing is selected */ 
		        if (node == null) return;
		        
		        LibraryTreeModel model = (LibraryTreeModel) LibraryTree.this.getModel();

		    /* retrieve the node that was selected */ 
		    
		        if(node == LibraryTree.this.getModel().getRoot())
		        {
		        	//Fire event
		        	LibTreeSelectionEvent action = new LibTreeSelectionEvent(this, LibTreeSelectionEvent.Type.EVERYTHING);
		        	fireEvent(action);
		        }
		        else if(node instanceof MediaNode)
		        {
		        	MediaNode mediaNode = (MediaNode) node;
		        	
		        	//Fire event
		        	LibTreeSelectionEvent action = new LibTreeSelectionEvent(this, LibTreeSelectionEvent.Type.MEDIA);
		        	action.media = mediaNode.getMedia();
		        	fireEvent(action);
		        } else if(node instanceof SeriesNode)
		        {
		        	SeriesNode seriesNode = (SeriesNode) node;
		        	
		        	//Fire event
		        	LibTreeSelectionEvent action = new LibTreeSelectionEvent(this, LibTreeSelectionEvent.Type.SERIES);
		        	action.series = seriesNode.getSeries();
		        	fireEvent(action);
		        } else if(node.getParent() == LibraryTree.this.getModel().getRoot())
		        {
		        	//Case 1 : the user clicked on "Series"
		        	
		        	if(node == model.getSeriesNode())
		        	{
		        		LibTreeSelectionEvent action = new LibTreeSelectionEvent(this, LibTreeSelectionEvent.Type.ALL_SERIES);
			        	fireEvent(action);
		        	
			        } else {
			        	//Fire event
			        	LibTreeSelectionEvent action = new LibTreeSelectionEvent(this, LibTreeSelectionEvent.Type.ALL_FILMS);
			        	fireEvent(action);
			        }
		        } else if(node.getParent() instanceof SeriesNode) //The node is a season
		        {
		        	//Fire event
		        	LibTreeSelectionEvent action = new LibTreeSelectionEvent(this, LibTreeSelectionEvent.Type.SEASON);
		        	action.series = ((SeriesNode) node.getParent()).getSeries();
		        	action.season = Integer.parseInt(node.toString().replaceAll("Saison ", ""));
		        	fireEvent(action);
		        }
		    }
		});
		
			
	}
	/**
	 * Reconstructs the tree
	 */
	public void reloadLibrary()
	{
		
		setModel(new LibraryTreeModel());
		expandRow(2); //expand series
		expandRow(1); //expand films
		
	}
	
	/**
	 * 
	 * @param e
	 */
	private void fireEvent(LibTreeSelectionEvent e)
	{
		Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i = i+2) {
	      if (listeners[i] == LibTreeSelectionListener.class) {
	        ((LibTreeSelectionListener) listeners[i+1]).selected(e);
	      }
	    }
	}
	
	public void addLibTreeSelectionListener(LibTreeSelectionListener mcl)
	{
		listenerList.add(LibTreeSelectionListener.class, mcl);
	}
	
	public void removeLibTreeSelectionListener(LibTreeSelectionListener mcl)
	{
		listenerList.remove(LibTreeSelectionListener.class, mcl);
	}
	
	/**
	 * Select the entered node
	 * @param node
	 */
	private void selectNode(TreeNode node)
	{
		if(node != null)
		{
			 //make the node visible by scroll to it    
	        TreeNode[] nodes = ((DefaultTreeModel) getModel()).getPathToRoot(node);    
	        TreePath path = new TreePath(nodes);    
	        System.out.println(path.toString());    // Able to get the exact node here    
	        setExpandsSelectedPaths(true);                  
	        setSelectionPath(new TreePath(nodes));  
		}
	}
	
	/**
	 * Select the node "Séries"
	 */
	public void selectAllSeries()
	{
		LibraryTreeModel model = (LibraryTreeModel) getModel();
		selectNode(model.getSeriesNode());		 
	}
	
	/**
	 * Select the node "Films"
	 */
	public void selectAllFilms()
	{
		LibraryTreeModel model = (LibraryTreeModel) getModel();
		selectNode(model.getFilmsNode());		 
	}
	
	/**
	 * Select the node representing the entered Media
	 * @param media
	 */
	public void selectMedia(Media media)
	{
		LibraryTreeModel model = (LibraryTreeModel) getModel();
		MediaNode node = model.getNodeByMedia(media);
		selectNode(node);
	}
	
	/**
	 * Select the node representing the entered series
	 * @param s
	 */
	public void selectSeries(Series s)
	{
		LibraryTreeModel model = (LibraryTreeModel) getModel();
		DefaultMutableTreeNode allSeriesNode = model.getSeriesNode();
		for(int i=0; i<allSeriesNode.getChildCount(); i++)
		{
			SeriesNode seriesNode = (SeriesNode) allSeriesNode.getChildAt(i);
			if(seriesNode.getSeries().equals(s))
			{
				selectNode(seriesNode);
				return;
			}
		}
	}
	
	/**
	 * Select the node representing the season
	 * @param s
	 * @param season
	 */
	public void selectSeason(Series s, int season)
	{
		LibraryTreeModel model = (LibraryTreeModel) getModel();
		DefaultMutableTreeNode allSeriesNode = model.getSeriesNode();
		for(int i=0; i<allSeriesNode.getChildCount(); i++)
		{
			SeriesNode seriesNode = (SeriesNode) allSeriesNode.getChildAt(i);
			if(seriesNode.getSeries().equals(s))
			{
				for(int j=0; j<seriesNode.getChildCount(); j++)
				{
					DefaultMutableTreeNode seasonNode = (DefaultMutableTreeNode) seriesNode.getChildAt(j);
					if(Integer.parseInt(seasonNode.toString().replace("Saison ", "")) == season)
						selectNode(seasonNode);
				}
				return;
			}
		}
	}


}

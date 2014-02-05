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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import com.t3.metamediamanager.ActorInfo;
import com.t3.metamediamanager.Film;
import com.t3.metamediamanager.M3Config;
import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.MediaInfo;
import com.t3.metamediamanager.ProviderManager;
import com.t3.metamediamanager.ProviderRequest;
import com.t3.metamediamanager.ProviderResponse;
import com.t3.metamediamanager.Series;
import com.t3.metamediamanager.SeriesEpisode;
import com.t3.metamediamanager.ThumbCreator;
import com.t3.metamediamanager.ThumbException;

import java.awt.FlowLayout;

class MediaModifiedEvent extends EventObject
{
	private Media _media;
	public MediaModifiedEvent(Object source, Media media) {
		super(source);
		_media=media;
	}
	
	public Media getMedia()
	{
		return _media;
	}
}

interface MediaModifiedListener extends EventListener
{
	public void mediaModified(MediaModifiedEvent e);
}

/**
 * Component used to display information from the selected media
 * @author vincent
 *
 */
public class InfosPane extends JPanel {
	private static Media _currentMedia = null;
	private JTabbedPane _pane = new JTabbedPane();
	private JTextPane textPane;
	private Searcher _searcher;
	private JPanel panel_1;
	private JPanel truc;
	private static MediaInfo mi;
	private ImageGrid ig, igJaquettes, igBackdrop;
	private SuperMigLayout _smlFilm, _smlSerie;
	private Boolean _film, _edit = false;
	private SubtitlesInfo _subtitlesInfo;
	JScrollPane _scrollPane;
	JButton _btnEdit;

	
	
	public InfosPane(Searcher searcher)
	{
		super(new BorderLayout());

		_searcher = searcher;
		_subtitlesInfo = new SubtitlesInfo(searcher);
		
		truc = new JPanel (new BorderLayout());
		_pane.addTab("Générales", null, truc, null);
		
		

		panel_1 = new JPanel();
			truc.add(panel_1, BorderLayout.SOUTH);
			panel_1.setLayout(new BorderLayout(0, 0));
			
			textPane = new JTextPane();
			panel_1.add(textPane);
			
			JButton btnNewButton_2 = new JButton("Recherche série");
			btnNewButton_2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Series s = ((SeriesEpisode) _currentMedia).getSeries();
					ProviderRequest pr = new ProviderRequest(ProviderRequest.Type.SERIES, s.getName(), s.getPath(), "en");
					ProviderResponse resp = ProviderManager.getInstance().getInfo(pr);
					s.setInfo(resp.getResponse());
					s.save();
					textPane.setText(resp.getResponse().toString());
				}
			});
			panel_1.add(btnNewButton_2);
			
			_smlFilm = new SuperMigLayout(9, "");
			_smlSerie = new SuperMigLayout(5, "");
			_scrollPane = new JScrollPane(_smlFilm);
			_film = true;
			_scrollPane.getVerticalScrollBar().setUnitIncrement(16);	//vitesse de scrolling
			
			
			JPanel inter2 = new JPanel(new BorderLayout());
			inter2.add(_scrollPane, BorderLayout.CENTER);
			truc.add(inter2, BorderLayout.CENTER);
			
		
		ig = new ImageGrid();
		JPanel inter = new JPanel(new BorderLayout());
		inter.add(ig, BorderLayout.CENTER);
		_pane.addTab("Acteurs", null, inter, null);	
		
		igJaquettes = new ImageGrid();
		JPanel interJaquettes = new JPanel(new BorderLayout());
		interJaquettes.add(igJaquettes, BorderLayout.CENTER);
		_pane.addTab("Jaquettes", null, interJaquettes, null);
		igJaquettes.addCellSelectedListener(new CellSelectedListener() {
			@Override
			public void imageChanged(CellSelectedEvent e) {
				setJaquettePréféré(e.getCell().getImageFile());
			}
		});
		
		igBackdrop = new ImageGrid();
		igBackdrop.setDefaultCellSize(new Dimension(530, 300));
		JPanel interBackdrop = new JPanel(new BorderLayout());
		interBackdrop.add(igBackdrop, BorderLayout.CENTER);
		_pane.addTab("Miniatures", null, interBackdrop, null);

		
		//JLabel lblNewLabel_6 = new JLabel("New label");
		//_pane.addTab("Bandes annonces", null, lblNewLabel_6, null);
		
		_pane.addTab("Sous-titres", null, _subtitlesInfo, null);
		
		add(_pane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		
		JToggleButton btnEdit = new JToggleButton ("Edit");
		btnEdit.setIcon(new ImageIcon(InfosPane.class.getResource("/com/t3/metamediamanager/gui/icons/Document.png")));

		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(_film)
				{
					_smlFilm.SwitchAllCompos();
				}
				else
				{
					_smlSerie.SwitchAllCompos();
				}
				
				if (_edit == true)
				{
					save();
					_edit = false;
				}
				else
				{
					_edit = true;
				}
			}
		});
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel.add(btnEdit);
		
		JButton btnMtn = new JButton("Miniatures");
		btnMtn.setIcon(new ImageIcon(InfosPane.class.getResource("/com/t3/metamediamanager/gui/icons/My Images.png")));
		btnMtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final Media media  = _currentMedia;
				if(media != null)
				{
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							File f = new File(media.getFilename());
							ThumbCreator tc = new ThumbCreator();
							String[] img;
							try {
								img = tc.create(f, 5);
								media.getInfo().putImages("img_backdrop", img);
								media.save();
							} catch (final ThumbException e) {
								SwingUtilities.invokeLater(new Runnable(){
									@Override
									public void run() {
										JOptionPane.showMessageDialog(null,
											    "Erreur miniature : " + e.getMessage(),
											    "Erreur",
											    JOptionPane.ERROR_MESSAGE);
									}
								});
							}
						}
					});
					
					t.run();
					
					setCurrentMedia(_currentMedia);
				}
			}
		});
		
		panel.add(btnMtn);
		
		
		
		JButton btnAllImg = new JButton("+ Jaquettes");
		btnAllImg.setIcon(new ImageIcon(InfosPane.class.getResource("/com/t3/metamediamanager/gui/icons/Download.png")));

		btnAllImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final Media media  = _currentMedia;
				if(media != null)
				{
					Media[] tab = new Media[1];
					tab[0] = media;
					_searcher.searchList(tab, EnumSet.of(ProviderRequest.Additions.JACKETS));
					
					setCurrentMedia(_currentMedia);
				}
			}
		});
		
		panel.add(btnAllImg);
	}
	
	public void setCurrentMedia(Media media)
	{
		_currentMedia = media;
		
		_subtitlesInfo.setMedia(media);
		
		mi = _currentMedia.getInfo();
		media.getInfo().get("title");
		
		if (media instanceof Film)
		{
			setFilm();
		}
		else if (media instanceof SeriesEpisode)
		{
			setSeriesEpisode();
		}
		setActors();
		setJaquette();
		setBackdrop();
		setGeneralThumbnail();
	}
	
	public void setGeneralThumbnail()
	{
		if (_film)
		{
			_smlFilm.setThumbnail();
		}
	}
	
	public void setFilm()
	{
		if(_film == false)
		{
			_scrollPane.setViewportView(_smlFilm);
			_film = true;
		}
			_smlFilm.modifCompo(0,mi.get("title"));
			if( _smlFilm.getLength() <= 1)
			{
				
				_smlFilm.setMedia(_currentMedia);
				JLabel filename = new JLabel("<html>"+_currentMedia.getFilename()+"</html>");
				filename.setName("filename");
				_smlFilm.setComponent(1,"Filename", filename);
				JLabel genre = new JLabel("<html>"+mi.get("genre")+"</html>");
				genre.setName("genre");
				_smlFilm.setComponent(2, "Genres", genre);
				JLabel synopsis = new JLabel("<html>"+mi.get("synopsis")+"</html>");
				synopsis.setName("synopsis");
				_smlFilm.setComponent(3, "Synopsis",synopsis);
				JLabel duree = new JLabel("<html>"+mi.get("runtime")+"</html>");
				duree.setName("runtime");
				_smlFilm.setComponent(4, "Durée",duree);
				JLabel rating = new JLabel("<html>"+mi.get("rating")+"</html>");
				rating.setName("rating");
				_smlFilm.setComponent(5, "Note",rating);
				JLabel writer = new JLabel("<html>"+mi.get("writer")+"</html>");
				writer.setName("writer");
				_smlFilm.setComponent(6, "Writer",writer);
				JLabel director = new JLabel("<html>"+mi.get("director")+"</html>");
				director.setName("director");
				_smlFilm.setComponent(7, "Director",director);
				JLabel release = new JLabel("<html>"+mi.get("release")+"</html>");
				release.setName("release");
				_smlFilm.setComponent(8, "Date de sortie", release);
				JLabel imdbID = new JLabel("<html>"+mi.get("imdbID")+"</html>");
				imdbID.setName("imdbID");
				_smlFilm.setComponent(9, "imdbID",imdbID);
			}
			else
			{
				_smlFilm.setMedia(_currentMedia);
				_smlFilm.modifCompo(1,_currentMedia.getFilename());
				_smlFilm.modifCompo(2,mi.get("genre"));
				_smlFilm.modifCompo(3,mi.get("synopsis"));
				_smlFilm.modifCompo(4,mi.get("runtime"));
				_smlFilm.modifCompo(5,mi.get("rating"));
				_smlFilm.modifCompo(6,mi.get("writer"));
				_smlFilm.modifCompo(7,mi.get("director"));
				_smlFilm.modifCompo(8,mi.get("release"));
				_smlFilm.modifCompo(9,mi.get("imdbID"));
			}
	}
		
	
	public void setSeriesEpisode()
	{
		if(_film == true)
		{
			_scrollPane.setViewportView(_smlSerie);
			_film = false;
		}
		_smlSerie.modifCompo(0,mi.get("title"));
		if( _smlSerie.getLength() <= 1)
		{
			_smlSerie.setMedia(_currentMedia);
			JLabel filename = new JLabel("<html>"+_currentMedia.getFilename()+"</html>");
			filename.setName("filename");
			_smlSerie.setComponent(1,"Filename",filename);
			JLabel synopsis = new JLabel("<html>"+mi.get("synopsis")+"</html>");
			synopsis.setName("synopsis");
			_smlSerie.setComponent(2, "Synopsis", synopsis);
			JLabel rating = new JLabel("<html>"+mi.get("rating")+"</html>");
			rating.setName("rating");
			_smlSerie.setComponent(3, "Note", rating);
			JLabel writer = new JLabel("<html>"+mi.get("writer")+"</html>");
			writer.setName("writer");
			_smlSerie.setComponent(4, "Writer",writer);
			JLabel director = new JLabel("<html>"+mi.get("director")+"</html>");
			director.setName("director");
			_smlSerie.setComponent(5, "Director", director);
		}
		else
		{
			_smlSerie.setMedia(_currentMedia);
			_smlSerie.modifCompo(1,_currentMedia.getFilename());
			_smlSerie.modifCompo(2,mi.get("synopsis"));
			_smlSerie.modifCompo(3,mi.get("rating"));
			_smlSerie.modifCompo(4,mi.get("writer"));
			_smlSerie.modifCompo(5,mi.get("director"));
		}
		
	}
	
	public void setActors()
	{
		ActorInfo[] tabActors = mi.getActors();
		ig.clean();
		
		for(ActorInfo a : tabActors)
		{
			File f = new File(M3Config.getInstance().getUserConfDirectory() + a.getImgUrl());
			ImageCell cellule = new ImageCell(f,"<html>"+a.getName()+"<br /><FONT size='4'>"+a.getRole()+"</FONT></html>");
			ig.addCell(cellule);
			ig.updateUI();
		}
	}
	
	public void setBackdrop()
	{
		String[] tabBackdrop = mi.getImages("img_backdrop");
		igBackdrop.clean();
		
		for(String s : tabBackdrop)
		{
			File f = new File(s);
			ImageCell cellule = new ImageCell(f,"");
			igBackdrop.addCell(cellule);
			igBackdrop.updateUI();
		}
	}
	
	public void setJaquette()
	{
		String[] tabJaquettes = mi.getImages("img_poster");
		igJaquettes.clean();
		
		for(String s : tabJaquettes)
		{
			File f = new File(s);
			ImageCell cellule = new ImageCell(f,"");
			igJaquettes.addCell(cellule);
			igJaquettes.updateUI();
		}
	}
	
	public void save()
	{
		Component[] tab;
		if(_film)
		{
			tab = _smlFilm.getTab();
		}
		else
		{
			tab = _smlSerie.getTab();
		}
		
		for (Component c : tab)
		{
			 mi.put(c.getName(), ((JTextComponent) c).getText());
		}
		_currentMedia.save();
	}
	
	public void setJaquettePréféré(File jaquette)	//à l'air de marcher
	{
				
		String[] tabJaquette = mi.getImages("img_poster");
		
		int num = -1;
		
		//We search the index
		for(int i=0; i<tabJaquette.length; i++)
		{
			if(tabJaquette[i].equals(jaquette.getAbsolutePath()))
			{
				num = i;
				break;
			}
				
		}
		
		if(num==-1) //Erreur
			throw new RuntimeException("Jaquette non trouvée lors d'une sélection");
		
		//debug
		for(String s : tabJaquette)
		{
			System.out.println(s+"\n");
		}
		//fin debug
		
		String jaquetteRemp = tabJaquette[num];
		String jaquetteVire = tabJaquette[0];
		
		//debug
		System.out.println("jaquette à remplacer : "+jaquetteRemp);
		System.out.println("jaquette remplacée : "+jaquetteVire);
		//fin debug
		
		tabJaquette[0] = jaquetteRemp;
		tabJaquette[num] = jaquetteVire;
		
		
		
		//debug
		System.out.println("jaquette à remplacer : "+tabJaquette[0]);
		System.out.println("jaquette remplacée : "+tabJaquette[num]);
		
		for(String s : tabJaquette)
		{
			System.out.println("changement"+s+"\n");
		}
		//fin debug
		
		
		
		mi.putImages("img_poster", tabJaquette);
		_currentMedia.save();
		
		fireMediaModifiedEvent(_currentMedia);
		setJaquette();
	}
	
	
	public void addMediaModifiedListener(MediaModifiedListener mcl)
	{
		listenerList.add(MediaModifiedListener.class, mcl);
	}
	
	public void removeMediaModifiedListener(MediaModifiedListener mcl)
	{
		listenerList.remove(MediaModifiedListener.class, mcl);
	}
	
	private void fireMediaModifiedEvent(Media m)
	{
		Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i = i+2) {
	      if (listeners[i] == MediaModifiedListener.class) {
	        ((MediaModifiedListener) listeners[i+1]).mediaModified(new MediaModifiedEvent(this, m));
	      }
	    }
	}
}









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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.t3.metamediamanager.Actor;
import org.t3.metamediamanager.ActorInfo;
import org.t3.metamediamanager.Film;
import org.t3.metamediamanager.M3Config;
import org.t3.metamediamanager.Media;
import org.t3.metamediamanager.MediaInfo;
import org.t3.metamediamanager.ProviderRequest;
import org.t3.metamediamanager.Series;
import org.t3.metamediamanager.SeriesEpisode;
import org.t3.metamediamanager.ThumbCreator;
import org.t3.metamediamanager.ThumbException;

class MediaModifiedEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
 * @author vincent & nicolas
 *
 */
public class InfosPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Media _currentMedia = null;
	private JTabbedPane _pane = new JTabbedPane();
	private JTextPane textPane;
	private Searcher _searcher;
	private JPanel _panelSwitchInfos;
	private JPanel interGeneral;
	private static MediaInfo mi, miSerie;
	private ImageGrid ig, igJaquettes, igBackdrop;
	private SuperMigLayout _smlFilm, _smlEpisode, _smlSerie;
	private Boolean _edit = false;
	private SubtitlesInfo _subtitlesInfo;
	
	private HashMap<File, String> _actorsPictureHash;
	
	JScrollPane _scrollPane;
	JButton _btnEdit;
	private Series _currentSerie;

	
	/**Enum which describes the current panel
	 * 
	 */
	public enum TypeInfos
	{
		FILM,
		SERIE,
		EPISODE;
	}
	private TypeInfos _type;
	
	
	/**
	 * 
	 * @param searcher
	 * 		
	 */
	public InfosPane(Searcher searcher)
	{
		super(new BorderLayout());

		_searcher = searcher;
		_subtitlesInfo = new SubtitlesInfo(searcher);
		
		
		//panel general tab
		interGeneral = new JPanel (new BorderLayout());
		_pane.addTab("Générales", null, interGeneral, null);
		
		
		
		_panelSwitchInfos = new JPanel();
			interGeneral.add(_panelSwitchInfos, BorderLayout.SOUTH);
			_panelSwitchInfos.setLayout(new BorderLayout(0, 0));
			
			textPane = new JTextPane();
			_panelSwitchInfos.add(textPane);
			
			JButton btnSwitchInfos = new JButton("Afficher informations série");
			btnSwitchInfos.addActionListener(new ActionListener() {					//compléter le bouton pour afficher le smlSérie !
				public void actionPerformed(ActionEvent arg0) {
					
					if (_type == TypeInfos.EPISODE)				//if panel = EPISODE
					{
						_currentSerie =  ((SeriesEpisode) _currentMedia).getSeries();
						miSerie = _currentSerie.getInfo();
						System.out.println(miSerie.toString());
						setSeries();
					}
					else										// else : panel = SERIES
					{
						setSeriesEpisode();
					}
				 
				}
			});
			_panelSwitchInfos.add(btnSwitchInfos);
			
			//SuperMigLayout tab general (Switch them depending on the media)
			_smlFilm = new SuperMigLayout(9, "");
			_smlEpisode = new SuperMigLayout(5, "");
			_smlSerie = new SuperMigLayout(4, "");
			_scrollPane = new JScrollPane(_smlFilm);
			
			//default case : _type = film
			_type = TypeInfos.FILM;
			_panelSwitchInfos.setVisible(false);
			
			_scrollPane.getVerticalScrollBar().setUnitIncrement(16);	//vitesse de scrolling
			
			//2nd interPanel in generalInfos
			JPanel interGeneral2 = new JPanel(new BorderLayout());
			interGeneral2.add(_scrollPane, BorderLayout.CENTER);
			interGeneral.add(interGeneral2, BorderLayout.CENTER);
			
		//panel Actors tab
		JPanel interActors = new JPanel(new BorderLayout());
		ig = new ImageGrid();
		ig.addCellSelectedListener(new CellSelectedListener(){

			@Override
			public void imageChanged(CellSelectedEvent e) {
				//when the users clicks on an actor, we show more information about him
				Actor a = Actor.getByName(_actorsPictureHash.get(e.getCell().getImageFile()));
				if(a != null)
				{
					ActorInfoFrame frame = new ActorInfoFrame(a);
					frame.setVisible(true);
					frame.pack();
				}
			}
			
		});
		interActors.add(ig, BorderLayout.CENTER);
		_pane.addTab("Acteurs", null, interActors, null);	
		
		//panel Jaquettes tab
		JPanel interJaquettes = new JPanel(new BorderLayout());
		igJaquettes = new ImageGrid();
		interJaquettes.add(igJaquettes, BorderLayout.CENTER);
		_pane.addTab("Jaquettes", null, interJaquettes, null);
		igJaquettes.addCellSelectedListener(new CellSelectedListener() {
			@Override
			public void imageChanged(CellSelectedEvent e) {
				setJaquettePrefere(e.getCell().getImageFile());
			}
		});
		
		//panel Backdrop tab
		JPanel interBackdrop = new JPanel(new BorderLayout());
		igBackdrop = new ImageGrid();
		igBackdrop.setDefaultCellSize(new Dimension(530, 300));
		interBackdrop.add(igBackdrop, BorderLayout.CENTER);
		_pane.addTab("Miniatures", null, interBackdrop, null);

		
		//panel Subtitles tab
		_pane.addTab("Sous-titres", null, _subtitlesInfo, null);
		
		
		add(_pane, BorderLayout.CENTER);
		
		
		
		
		//place the buttons at the bottom of the infosPane
		JPanel panelBtnBas = new JPanel();
		add(panelBtnBas, BorderLayout.SOUTH);
		
		JToggleButton btnEdit = new JToggleButton ("Edit");
		btnEdit.setIcon(new ImageIcon(InfosPane.class.getResource("/org/t3/metamediamanager/gui/icons/Document.png")));

		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(_type == TypeInfos.FILM)
				{
					_smlFilm.SwitchAllCompos();
				}
				else if(_type == TypeInfos.EPISODE)
				{
					_smlEpisode.SwitchAllCompos();
				}
				else if(_type == TypeInfos.SERIE)
				{
					_smlSerie.SwitchAllCompos();		//à vérifier !
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
		panelBtnBas.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panelBtnBas.add(btnEdit);
		
		JButton btnMtn = new JButton("Miniatures");
		btnMtn.setIcon(new ImageIcon(InfosPane.class.getResource("/org/t3/metamediamanager/gui/icons/My Images.png")));
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
		
		panelBtnBas.add(btnMtn);
		
		
		
		JButton btnAllImg = new JButton("+ Jaquettes");
		btnAllImg.setIcon(new ImageIcon(InfosPane.class.getResource("/org/t3/metamediamanager/gui/icons/Download.png")));

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
		
		panelBtnBas.add(btnAllImg);
	}
	
	
	
	
	/**
	 * //Set the different tabs of the InfosPane depending on the type of the media
	 * @param media
	 * 		Set the informations about this media
	 */
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
	
	
	/**Choose the method of SML to execute depending on the type of the media.
	 * Set the thumbnails
	 * 
	 */
	public void setGeneralThumbnail()
	{
		if (_type == TypeInfos.FILM)
		{
			_smlFilm.setThumbnail();
		}
		else if (_type == TypeInfos.EPISODE)
		{
			_smlEpisode.setThumbnail();
		}
	}
	
	
	/**Set the information in general Tab (for a movie)
	 *
	 */
	public void setFilm()
	{
		if(_type != TypeInfos.FILM)
		{
			_scrollPane.setViewportView(_smlFilm);
			_type = TypeInfos.FILM;
			_panelSwitchInfos.setVisible(false);
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
		
	

	/**Set the information in general Tab (for an episode)
	 * 
	 */
	public void setSeriesEpisode()
	{
		if(_type != TypeInfos.EPISODE)
		{
			_scrollPane.setViewportView(_smlEpisode);
			_type = TypeInfos.EPISODE;
			_panelSwitchInfos.setVisible(true);
		}
		_smlEpisode.modifCompo(0,mi.get("title"));
		_smlEpisode.setMedia(_currentMedia);
		if( _smlEpisode.getLength() <= 1)
		{
			JLabel filename = new JLabel("<html>"+_currentMedia.getFilename()+"</html>");
			filename.setName("filename");
			_smlEpisode.setComponent(1,"Filename",filename);
			JLabel synopsis = new JLabel("<html>"+mi.get("synopsis")+"</html>");
			synopsis.setName("synopsis");
			_smlEpisode.setComponent(2, "Synopsis", synopsis);
			JLabel rating = new JLabel("<html>"+mi.get("rating")+"</html>");
			rating.setName("rating");
			_smlEpisode.setComponent(3, "Note", rating);
			JLabel writer = new JLabel("<html>"+mi.get("writer")+"</html>");
			writer.setName("writer");
			_smlEpisode.setComponent(4, "Writer",writer);
			JLabel director = new JLabel("<html>"+mi.get("director")+"</html>");
			director.setName("director");
			_smlEpisode.setComponent(5, "Director", director);
		}
		else
		{
			_smlEpisode.modifCompo(1,_currentMedia.getFilename());
			_smlEpisode.modifCompo(2,mi.get("synopsis"));
			_smlEpisode.modifCompo(3,mi.get("rating"));
			_smlEpisode.modifCompo(4,mi.get("writer"));
			_smlEpisode.modifCompo(5,mi.get("director"));
		}
		
	}
	
	/**Set the information in general tab (for a serie)
	 * 
	 * @param se
	 */
	public void setSeries (SeriesEpisode se)
	{
		_currentSerie =  ((SeriesEpisode) se).getSeries();
		miSerie = _currentSerie.getInfo();
		System.out.println(miSerie.toString());
		setSeries();
	}
	
	public void setSeries (Series s)
	{
		_currentSerie = s;
		miSerie = _currentSerie.getInfo();
		setSeries();
	}
	
	
	/**Set the information in general tab (for a serie)
	 * 
	 */
	public void setSeries()
	{
		if(_type != TypeInfos.SERIE)
		{
			_scrollPane.setViewportView(_smlSerie);
			_type = TypeInfos.SERIE;
			_panelSwitchInfos.setVisible(true);
		}
		_smlSerie.modifCompo(0,miSerie.get("title"));
		_smlSerie.setMedia(_currentSerie.getInfo());					//problème ici !
		if(_smlSerie.getLength() <= 1)
		{	
			System.out.println(miSerie.get("genre"));
			JLabel genre = new JLabel("<html>"+miSerie.get("genre")+"</html>");
			genre.setName("genre");
			_smlSerie.setComponent(1,"Genre(s)",genre);
			JLabel synopsis = new JLabel("<html>"+miSerie.get("synopsis")+"</html>");
			synopsis.setName("synopsis");
			_smlSerie.setComponent(2, "Synopsis", synopsis);
			JLabel rating = new JLabel("<html>"+miSerie.get("rating")+"</html>");
			rating.setName("rating");
			_smlSerie.setComponent(3, "Note", rating);
			JLabel runtime = new JLabel("<html>"+miSerie.get("runtime")+"</html>");
			runtime.setName("runtime");
			_smlSerie.setComponent(4, "Durée", runtime);
		}
		else
		{
			_smlSerie.modifCompo(1,miSerie.get("genre"));
			_smlSerie.modifCompo(2,miSerie.get("synopsis"));
			_smlSerie.modifCompo(3,miSerie.get("rating"));
			_smlSerie.modifCompo(4,miSerie.get("runtime"));
		}
		
	}
	
	/**set the actors (pictures + role) in actors tab
	 * 
	 */
	public void setActors()
	{
		ActorInfo[] tabActors = mi.getActors();
		ig.clean();
		_actorsPictureHash = new HashMap<File,String>();
		for(ActorInfo a : tabActors)
		{
			File f = new File(M3Config.getInstance().getUserConfDirectory() + a.getImgUrl());
			_actorsPictureHash.put(f, a.getName());
			ImageCell cellule = new ImageCell(f,"<html>"+a.getName()+"<br /><FONT size='4'>"+a.getRole()+"</FONT></html>");
			ig.addCell(cellule);
			ig.updateUI();
		}
	}
	
	/**set the backdrops in the backdrops tab
	 * 
	 */
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
	
	
	/**set the jackets in the jackets tab
	 * 
	 */
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
	
	
	/**save the informations of the current media
	 * 
	 */
	public void save()
	{
		Component[] tab = null;
		if(_type == TypeInfos.FILM)
		{
			tab = _smlFilm.getTab();
		}
		else if (_type == TypeInfos.EPISODE)
		{
			tab = _smlEpisode.getTab();
		}
		else if (_type == TypeInfos.SERIE)
		{
			tab = _smlSerie.getTab();
		}
		
		for (Component c : tab)
		{
			if (_type == TypeInfos.SERIE)
			{
			System.out.println("Nom de compo :" + c.getName() + "texte : " +((JTextComponent) c).getText());
			 miSerie.put(c.getName(), ((JTextComponent) c).getText());
			}
			else
			{
				mi.put(c.getName(), ((JTextComponent) c).getText());
			}
		}
		if (_type == TypeInfos.SERIE)
		{
			_currentSerie.save();
		}
		else
			_currentMedia.save();
	}	
	
	/**allow the user to choose his favorite jacket
	 * 
	 * @param jaquette
	 * 		The path of the jacket
	 * @throws RuntimeException
	 */
	public void setJaquettePrefere(File jaquette)
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









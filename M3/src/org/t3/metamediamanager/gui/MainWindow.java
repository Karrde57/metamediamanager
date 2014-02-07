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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.t3.metamediamanager.DBManager;
import org.t3.metamediamanager.Film;
import org.t3.metamediamanager.Logger;
import org.t3.metamediamanager.M3Config;
import org.t3.metamediamanager.Media;
import org.t3.metamediamanager.MediaFilter;
import org.t3.metamediamanager.MediaLibrary;
import org.t3.metamediamanager.MediaRenamer;
import org.t3.metamediamanager.ProviderManager;
import org.t3.metamediamanager.SaverManager;
import org.t3.metamediamanager.Series;
import org.t3.metamediamanager.SeriesEpisode;

/**
 * Main class of the GUI application
 * GUI generated with WindowBuilder (Eclipse). You should use it.
 * @author vincent
 *
 */
public class MainWindow {

	private JFrame frame;
	private MediaGrid _mediaGrid;
	private JIconTextField _txtSearch;
	private JToggleButton _btnFilterComplete, _btnFilterNotComplete;
	private InfosPane _infosPane;
	private Searcher _searcher = new Searcher();
	private LibraryTree _libTree = new LibraryTree();
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
	    
		
		M3Config.getInstance().loadFile("config.xml");
		DBManager.getInstance().connect("database.db");
		
		
		ProviderManager.getInstance().loadConfig("providers.xml");
		ProviderManager.getInstance().loadProviders();	
		SaverManager.getInstance().loadSavers();
		
		MediaLibrary.getInstance().refreshDB();

		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Gui Theme
					try
					{
						String osname = System.getProperty ("os.name").toLowerCase();
						if(osname.contains("windows"))
						{
							UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
						} else if(osname.contains("mac"))
						{
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						}
						else {
							UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
						}
					    Logger.getInstance().write("Look and feel choisi");
					}
					catch (Exception e)
					{
					System.out.println("Unable to load Windows look and feel");
					}
					
					
					//If it's the first time the user starts the application, we show the "First Run" window
					if(!M3Config.getInstance().getParam("firstrun").equals("false"))
					{
						FirstRunFrame of = new FirstRunFrame();
						of.pack();
						of.setVisible(true);
					}
					
					MainWindow window = new MainWindow();
					window.frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("org/t3/metamediamanager/gui/m3logo.png")));
					window.frame.setVisible(true);
					window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					window.frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
					        Logger.getInstance().close();
					        DBManager.getInstance().close();
					        System.exit(0);
					    }
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}
	
	private void refreshMediaGrid()
	{
		refreshMediaGrid(false);
	}
	
	/**
	 * Reloads the main grid depending on the filters (only movies, only series ...)
	 */
	private void refreshMediaGrid(boolean onlyFilms)
	{
		MediaFilter filter;

		if(!onlyFilms)
			filter = new MediaFilter(MediaFilter.Type.ALL);
		else
			filter = new MediaFilter(MediaFilter.Type.FILMS);
		
		if(_btnFilterNotComplete.isSelected() && _btnFilterComplete.isSelected())
		{
			filter.completion = MediaFilter.Completion.ALL;
		} else if(_btnFilterComplete.isSelected())
		{
			filter.completion = MediaFilter.Completion.COMPLETE;
		} else if(_btnFilterNotComplete.isSelected())
		{
			filter.completion = MediaFilter.Completion.NOT_COMPLETE;
		} else {
			filter = new MediaFilter(MediaFilter.Type.NONE);
		}
		
		
		if(!_btnFilterNotComplete.isSelected() && !_btnFilterComplete.isSelected())
			filter = new MediaFilter(MediaFilter.Type.NONE);
		
		_mediaGrid.search(_txtSearch.getText(), filter);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 708, 581);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setTitle("MetaMediaManager");
		
		//GENERATED BY WINDOWBUILDER
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.65);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel3 = new JPanel(new BorderLayout());
		
		JSplitPane splitPane2 = new JSplitPane();
		splitPane.setLeftComponent(panel3);
		splitPane2.setDividerLocation(220);
		
		panel3.add(splitPane2, BorderLayout.CENTER);
		
		_infosPane = new InfosPane(_searcher);
		
		//We the user edit the current media, we reload the main grid (the poster may have been changed)
		_infosPane.addMediaModifiedListener(new MediaModifiedListener() {
			@Override
			public void mediaModified(MediaModifiedEvent e) {
				System.out.println("test");
				refreshMediaGrid();
				_libTree.reloadLibrary();
			}
		});
		
		splitPane.setRightComponent(_infosPane);
		
		//When a search is ended, we reload the main grid, and reload the information at the right
		_searcher.addSearchableModifiedListener(new SearchableModifiedListener() {
			@Override
			public void onSearchableModified(SearchableModifiedEvent e) {
				refreshMediaGrid();
				_libTree.reloadLibrary();
				
				if(e.getSearchable() != null && e.getSearchable() instanceof Media)
				{
					Media m = (Media) e.getSearchable();
					MainWindow.this._infosPane.setCurrentMedia(m);
				}
				
				
			}	
		});
		
		
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 50, 10, 50));
		panel3.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new MigLayout("", "[grow][][20:30:40][][grow]", "[10px][]"));
		
		_txtSearch = new JIconTextField(new ImageIcon(ClassLoader.getSystemResource("org/t3/metamediamanager/gui/icons/Search.png")));
		panel_2.add(_txtSearch, "cell 0 0 5 1,growx,aligny top");
		_txtSearch.setHorizontalAlignment(SwingConstants.LEFT);
		_txtSearch.setFont(new Font(_txtSearch.getFont().getName(), Font.PLAIN, 24));
		
		_txtSearch.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				MainWindow.this.refreshMediaGrid();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				MainWindow.this.refreshMediaGrid();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				MainWindow.this.refreshMediaGrid();
			}
			
		});
		
		

		
		ActionListener actionFilters = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MainWindow.this.refreshMediaGrid();
			}
			
		};
		

		
		
		_btnFilterComplete = new JToggleButton("Complets");
		_btnFilterComplete.setSelected(true);
		
		_btnFilterNotComplete = new JToggleButton("Incomplets");
		_btnFilterNotComplete.setSelected(true);
		
		panel_2.add(_btnFilterComplete, "cell 1 1");
		
		panel_2.add(_btnFilterNotComplete, "cell 3 1");
		
		JScrollPane scrollPane = new JScrollPane();
		
		scrollPane.setViewportView(_libTree);
		
		splitPane2.setLeftComponent(scrollPane);
		


		
		_mediaGrid = new MediaGrid();
		splitPane2.setRightComponent(_mediaGrid);
		
		_btnFilterComplete.addActionListener(actionFilters);
		_btnFilterNotComplete.addActionListener(actionFilters);
		
		
		refreshMediaGrid();

		
		JToolBar toolBar = new JToolBar();
		JButton refreshButton = new JButton("");
		refreshButton.setToolTipText("Effectuer une recherche globale");
		refreshButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Download.png")));
		toolBar.add(refreshButton);
		refreshButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//We check if there is at least 1 enabled provider
				if(M3Config.getInstance().getEnabledProviders().size() == 0)
				{
					JOptionPane.showMessageDialog(frame,
						    "Vous n'avez aucun fournisseur activé (voir les options)",
						    "Recherche",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				_searcher.searchList(_mediaGrid.getAllMedias());
			}
		});
		JButton saveButton = new JButton("");
		saveButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Save.png")));
		saveButton.setToolTipText("Effectuer une sauvegarde vers les media centers");
		toolBar.add(saveButton);
		
		
		//When the user wants to export the data to media centers
		saveButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//We check if there is at least 1 enabled saver
				if(M3Config.getInstance().getEnabledSavers().size() == 0)
				{
					JOptionPane.showMessageDialog(frame,
						    "Vous n'avez aucun enregistreur activé (voir les options)",
						    "Export",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(M3Config.getInstance().getEnabledSavers().contains("MediaBrowser"))
				{
					int retour = JOptionPane.showConfirmDialog(new Frame(), "<html><b>Attention, pour le media center mediabrowser : </b><br /> - les films doivent être dans des dossiers différents (vous pouvez utiliser le bouton juste à côté pour cela) <br /> - Les épisodes des séries doivent être rangés par saison (Cette fonction n'est malheureusement pas encore disponible) <br /><br /> sinon l'export ne fonctionnera pas correctement. </html>");
					if (retour != JOptionPane.OK_OPTION)
						return;
						
				}
				
				Media[] medias = _mediaGrid.getAllMedias();
				HashSet<Integer> series = new HashSet<Integer>();
				for(Media m  : medias)
				{
					if(m.hasInfos())
					{
						SaverManager.getInstance().save(m);
						
						if(m instanceof SeriesEpisode)
							series.add(((SeriesEpisode) m).getSeriesId());
					}
				}
				
				for(Integer seriesId : series)
				{
					SaverManager.getInstance().save(Series.loadById(seriesId));
				}
				
				
				String txt = "L'export a été effectué vers les media centers suivants : ";
				for(String str : M3Config.getInstance().getEnabledSavers())
					txt+=str +" ";
				JOptionPane.showMessageDialog(frame,txt);
				
			}
		});
		
		JButton optionButton = new JButton("");
		optionButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Gears.png")));
		optionButton.setToolTipText("Ouvrir les options");
		toolBar.add(optionButton);
		optionButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				OptionsFrame frame = new OptionsFrame();
				frame.pack();
				frame.setVisible(true);
				
				frame.addWindowListener(new WindowAdapter()
		        {
		            public void windowClosing(WindowEvent we)
		            {
		                MainWindow.this.refreshMediaGrid();
		                MainWindow.this._libTree.reloadLibrary();
		            }
		        });
			}
		});
		
		JButton refreshLibButton = new JButton("");
		refreshLibButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Refresh.png")));
		refreshLibButton.setToolTipText("Actualiser la bibliothèque");
		toolBar.add(refreshLibButton);
		refreshLibButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				RefreshLibraryWorker w = new RefreshLibraryWorker();
				
		    	w.pack();
				w.setVisible(true);
				
				refreshMediaGrid();
				MainWindow.this._libTree.reloadLibrary();
				
			}
		});
		
		JButton renamerButton = new JButton();
		renamerButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Pen.png")));
		renamerButton.setToolTipText("Renommer les médias");
		toolBar.add(renamerButton);
		//Opens the "renamer" window
		renamerButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Renamer frame = new Renamer();
				frame.pack();
				frame.setVisible(true);
				
				frame.addWindowListener(new WindowAdapter()
		        {
		            public void windowClosing(WindowEvent we)
		            {
		                MainWindow.this.refreshMediaGrid();
		                MainWindow.this._libTree.reloadLibrary();
		            }
		        });
			}
		});
		
		JButton eachfilmfolderButton = new JButton();
		eachfilmfolderButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Folder Open.png")));
		//optionButton.setIcon(new ImageIcon(MainWindow.class.getResource("/org/t3/metamediamanager/gui/icons/Gears.png")));
		eachfilmfolderButton.setToolTipText("1 film = 1 dossier [BETA]");
		toolBar.add(eachfilmfolderButton);
		eachfilmfolderButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int retour = JOptionPane.showConfirmDialog(new Frame(), "[BETA] Etes vous sûr de vouloir mettre chacun de vos films dans un dossier séparé ?");
				if (retour == JOptionPane.OK_OPTION)
					MediaRenamer.folderEachFilm();
		       
			}
		});
		
		
		
		
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		//When something is selected either in the treeview or the grid
		LibTreeSelectionListener selectListener = new LibTreeSelectionListener(){

			@Override
			public void selected(LibTreeSelectionEvent e) {
				System.out.println(e.getType());
				
				if(e.getType() == LibTreeSelectionEvent.Type.EVERYTHING)
				{
					_txtSearch.setText("");
					refreshMediaGrid();
				}
				
				if(e.getType() == LibTreeSelectionEvent.Type.ALL_FILMS)
				{
					MainWindow.this._libTree.selectAllFilms();
					MainWindow.this.refreshMediaGrid(true);
				} else if(e.getType() == LibTreeSelectionEvent.Type.MEDIA)
				{
					
					if(e.media instanceof Film)
					{
						if(MainWindow.this._mediaGrid.getSelectedMedia() != null && !MainWindow.this._mediaGrid.getSelectedMedia().equals(e.media))
							MainWindow.this.refreshMediaGrid(true);
					} else {
						
						//If series episode, we show the grid of the season
						SeriesEpisode episode = (SeriesEpisode) e.media;
						MainWindow.this._mediaGrid.showSeason(episode.getSeries(), episode.getSeasonNumber());
					}
					MainWindow.this._mediaGrid.setSelectedMedia(e.media);
					MainWindow.this._infosPane.setCurrentMedia(e.media);
					MainWindow.this._libTree.selectMedia(e.media);
				} else if(e.getType() == LibTreeSelectionEvent.Type.SERIES)
				{
					MainWindow.this._libTree.selectSeries(e.series);
					MainWindow.this._mediaGrid.showSeries(e.series);
					MainWindow.this._infosPane.setSeries(e.series);
				} else if(e.getType() == LibTreeSelectionEvent.Type.ALL_SERIES)
				{
					MainWindow.this._mediaGrid.showAllSeries();
					MainWindow.this._libTree.selectAllSeries();
				} else if(e.getType() == LibTreeSelectionEvent.Type.SEASON)
				{
					MainWindow.this._libTree.selectSeason(e.series, e.season);
					MainWindow.this._mediaGrid.showSeason(e.series, e.season);
				}
			}
			
		};
		
		//When the user clicks on a media in the tree, we show the information
		_libTree.addLibTreeSelectionListener(selectListener);
		_mediaGrid.addLibTreeSelectionListener(selectListener);
				
	}

}

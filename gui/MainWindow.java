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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import com.t3.metamediamanager.DBManager;
import com.t3.metamediamanager.Logger;
import com.t3.metamediamanager.M3Config;
import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.MediaFilter;
import com.t3.metamediamanager.MediaLibrary;
import com.t3.metamediamanager.ProviderManager;
import com.t3.metamediamanager.ProviderRequest;
import com.t3.metamediamanager.SaverManager;
import com.t3.metamediamanager.Searchable;
import com.t3.metamediamanager.Series;
import com.t3.metamediamanager.SeriesEpisode;
import com.t3.metamediamanager.ThumbCreator;
import com.t3.metamediamanager.ThumbException;


public class MainWindow {

	private JFrame frame;
	private MediaGrid _mediaGrid;
	private JIconTextField _txtSearch;
	private JToggleButton _btnFilterFilm, _btnFilterSeries, _btnFilterComplete, _btnFilterNotComplete;
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
					
					if(!M3Config.getInstance().getParam("firstrun").equals("false"))
					{
						FirstRunFrame of = new FirstRunFrame();
						of.pack();
						of.setVisible(true);
					}
					
					MainWindow window = new MainWindow();
					window.frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("com/t3/metamediamanager/gui/m3logo.png")));
					window.frame.setVisible(true);
					window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					window.frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
					        Logger.getInstance().close();
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
		MediaFilter filter;
		if(_btnFilterFilm.isSelected() && _btnFilterSeries.isSelected())
			filter = new MediaFilter(MediaFilter.Type.ALL);
		else if(_btnFilterFilm.isSelected())
			filter = new MediaFilter(MediaFilter.Type.FILMS);
		else if(_btnFilterSeries.isSelected())
			filter = new MediaFilter(MediaFilter.Type.EPISODES);
		else
			filter = new MediaFilter(MediaFilter.Type.NONE);
		
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
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.65);
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panel3 = new JPanel(new BorderLayout());
		
		JSplitPane splitPane2 = new JSplitPane();
		splitPane.setLeftComponent(panel3);
		
		panel3.add(splitPane2, BorderLayout.CENTER);
		
		_infosPane = new InfosPane(_searcher);
		
		_infosPane.addMediaModifiedListener(new MediaModifiedListener() {
			@Override
			public void mediaModified(MediaModifiedEvent e) {
				System.out.println("test");
				refreshMediaGrid();
				_libTree.reloadLibrary();
			}
		});
		
		splitPane.setRightComponent(_infosPane);
		
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
		
		JPanel panel = new JPanel();
		splitPane2.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 50, 10, 50));
		panel3.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new MigLayout("", "[grow][][20:30:40][][grow]", "[10px][]"));
		
		_txtSearch = new JIconTextField(new ImageIcon(ClassLoader.getSystemResource("com/t3/metamediamanager/gui/icons/Search.png")));
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
		
		
		 _btnFilterFilm = new JToggleButton("Films");
		_btnFilterFilm.setSelected(true);
		panel_2.add(_btnFilterFilm, "cell 1 1");
		
		_btnFilterSeries = new JToggleButton("Séries");
		_btnFilterSeries.setSelected(true);
		panel_2.add(_btnFilterSeries, "cell 3 1");
		
		ActionListener actionFilters = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MainWindow.this.refreshMediaGrid();
			}
			
		};
		

		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new MigLayout("", "[grow][125px,grow,center][grow]", "[70px][70px][50px,grow]"));
		
		_btnFilterComplete = new JToggleButton("Complets");
		_btnFilterComplete.setSelected(true);
		panel_1.add(_btnFilterComplete, "cell 1 0,grow");
		
		_btnFilterNotComplete = new JToggleButton("Incomplets");
		_btnFilterNotComplete.setSelected(true);
		panel_1.add(_btnFilterNotComplete, "cell 1 1,grow");
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, "cell 0 2 3 1,grow");
		
		scrollPane.setViewportView(_libTree);
		_libTree.addMediaChangedListener(new MediaChangedListener() {

			@Override
			public void mediaChanged(MediaChangedEvent e) {
				_infosPane.setCurrentMedia(e.getMedia());
			}
			
		});
		
		_mediaGrid = new MediaGrid();
		splitPane2.setRightComponent(_mediaGrid);
		
		_btnFilterFilm.addActionListener(actionFilters);
		_btnFilterSeries.addActionListener(actionFilters);
		_btnFilterComplete.addActionListener(actionFilters);
		_btnFilterNotComplete.addActionListener(actionFilters);
		
		_mediaGrid.addMediaChangedListener(new MediaChangedListener(){
			@Override
			public void mediaChanged(MediaChangedEvent e) {
				_infosPane.setCurrentMedia(e.getMedia());
			}
		});
		
		refreshMediaGrid();

		
		JToolBar toolBar = new JToolBar();
		JButton refreshButton = new JButton("");
		refreshButton.setToolTipText("Effectuer une recherche globale");
		refreshButton.setIcon(new ImageIcon(MainWindow.class.getResource("/com/t3/metamediamanager/gui/icons/Download.png")));
		toolBar.add(refreshButton);
		refreshButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				_searcher.searchList(_mediaGrid.getAllMedias());
			}
		});
		JButton saveButton = new JButton("");
		saveButton.setIcon(new ImageIcon(MainWindow.class.getResource("/com/t3/metamediamanager/gui/icons/Save.png")));
		saveButton.setToolTipText("Effectuer une sauvegarde vers les media centers");
		toolBar.add(saveButton);
		saveButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Media[] medias = _mediaGrid.getAllMedias();
				for(Media m  : medias)
				{
					if(m.hasInfos())
					{
						SaverManager.getInstance().save(m);
					}
				}
			}
		});
		
		JButton optionButton = new JButton("");
		optionButton.setIcon(new ImageIcon(MainWindow.class.getResource("/com/t3/metamediamanager/gui/icons/Gears.png")));
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
		refreshLibButton.setIcon(new ImageIcon(MainWindow.class.getResource("/com/t3/metamediamanager/gui/icons/Refresh.png")));
		refreshLibButton.setToolTipText("Actualiser la bibliothèque");
		toolBar.add(refreshLibButton);
		refreshLibButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				RefreshLibraryWorker w = new RefreshLibraryWorker();
				
		    	w.pack();
				w.setVisible(true);
				
				refreshMediaGrid();
				
			}
		});
		
		JButton renamerButton = new JButton("Renommer");
		//optionButton.setIcon(new ImageIcon(MainWindow.class.getResource("/com/t3/metamediamanager/gui/icons/Gears.png")));
		optionButton.setToolTipText("Renommer les médias");
		toolBar.add(renamerButton);
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
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
				
	}

}

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

import java.util.Vector;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.t3.metamediamanager.M3Config;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Panel used in Options window and First Use window allowing the user to add and remove the directories to be searched.
 * Two swing lists are used, and it generates two vector<string> used in M3Config (M3Config loads and save these vectors)
 * @author vincent
 *
 */
public class DirectoriesChooserPanel extends JPanel {
	private JList<?> _lstFilms, _lstSeries;
	private DefaultListModel<ListItem> _mdlFilms, _mdlSeries;
	
	private JPopupMenu _menu = new JPopupMenu();
	
	private ListItem _selectedItem;
	
	private class ListItem {
		public String path;
		
		public ListItem(String path, boolean isFilm)
		{
			this.path = path;
		}
		
		@Override
		public String toString()
		{
			return path;
		}
	}
	
	/**
	 * Get the directories list and add them to the list
	 */
	private void load()
	{
		Vector<String> films = M3Config.getInstance().getFilmsDirectories();
		Vector<String> series = M3Config.getInstance().getSeriesDirectories();
		
		_mdlFilms.clear();
		_mdlSeries.clear();
		
		for(String film : films)
		{
			_mdlFilms.addElement(new ListItem(film, true));
		}
		
		for(String serie : series)
		{
			_mdlSeries.addElement(new ListItem(serie, false));
		}
		
		
	}
	
	/**
	 * Internal method called when the user wants to add a directory (series or movies)
	 * @param filmsDir
	 */
	private void addDirectory(boolean filmsDir)
	{
		JFileChooser j = new JFileChooser();
		j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		Integer opt = j.showOpenDialog(this);

		if(opt == JFileChooser.APPROVE_OPTION)
		{
			if(filmsDir)
			{
				_mdlFilms.addElement(new ListItem(j.getSelectedFile().getAbsolutePath(), true));
			} else {
				_mdlSeries.addElement(new ListItem(j.getSelectedFile().getAbsolutePath(), false));
			}
		}
	}
	
	public DirectoriesChooserPanel() {
		setLayout(new MigLayout("", "[grow][240px:n][grow][240px:n][grow][:200px:200px]", "[][100px:n,grow][][100px:n,grow][]"));
		
		//Menu
		JMenuItem itemDelete = new JMenuItem("Enlever");
		_menu.add(itemDelete);
		
		itemDelete.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				_mdlFilms.removeElement(_selectedItem);
				_mdlSeries.removeElement(_selectedItem);
			}
		});
		
		JLabel lblNewLabel = new JLabel("Vos dossiers de films :");
		add(lblNewLabel, "cell 0 0 4 1");
		
		_mdlFilms = new DefaultListModel<ListItem>();
		_mdlSeries = new DefaultListModel<ListItem>();
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "cell 0 1 5 1,grow");
				
		_lstFilms = new JList<ListItem>(_mdlFilms);
		
		//Contextual menu for movies
		_lstFilms.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)  {check(e);}
			public void mouseReleased(MouseEvent e) {check(e);}

			public void check(MouseEvent e) {
			    if (e.isPopupTrigger() && _lstFilms.getSelectedIndex() != -1) { //if the event shows the menu
			    	_selectedItem = _mdlFilms.getElementAt(_lstFilms.getSelectedIndex());
			    	_lstFilms.setSelectedIndex(_lstFilms.locationToIndex(e.getPoint())); //select the item
			    	_menu.show(_lstFilms, e.getX(), e.getY()); //and show the menu
			    }
			}
		});
		
		
		
		scrollPane.setViewportView(_lstFilms);
		
		JLabel lblNewLabel_2 = new JLabel("<html>Vos films peuvent être organisés en \"vrac\" dans ces dossiers. <br /> <br /> <b> Pour supprimer un dossier : clic droit puis \"supprimer\" </b></html>");
		add(lblNewLabel_2, "cell 5 1");
		
		JLabel lblNewLabel_1 = new JLabel("Vos dossiers de séries :");
		add(lblNewLabel_1, "cell 0 2 4 1");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		add(scrollPane_1, "cell 0 3 5 1,grow");
		
		_lstSeries = new JList<ListItem>(_mdlSeries);
		scrollPane_1.setViewportView(_lstSeries);
				
		
		//Contextual menu for series
		_lstSeries.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e)  {check(e);}
			public void mouseReleased(MouseEvent e) {check(e);}

			public void check(MouseEvent e) {
			    if (e.isPopupTrigger() && _lstSeries.getSelectedIndex() != -1) { //if the event shows the menu
			    	_selectedItem = _mdlSeries.getElementAt(_lstSeries.getSelectedIndex());
			    	_lstSeries.setSelectedIndex(_lstSeries.locationToIndex(e.getPoint())); //select the item
			    	_menu.show(_lstSeries, e.getX(), e.getY()); //and show the menu
			    }
			}
		});
		
		JButton btnNewButton = new JButton("Nouveau dossier de films");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addDirectory(true);
			}
		});
		
		JLabel lblNewLabel_3 = new JLabel("<html>Un dossier de séries doit respecter l'arboresence suivante :<br>-Nom Série<br>    -xxxS01E01.avi<br>    -xxxS01E02.avi<br>-xxx1x3.avi<br>...<br><br>Vous pouvez organiser, ou non, les épisodes par dossier de saison du moment que le format de nom des épisodes est respecté (format changeable dans les options)</html>");
		add(lblNewLabel_3, "cell 5 3");
		add(btnNewButton, "cell 1 4");
		
		JButton btnNewButton_1 = new JButton("Nouveau dossier de séries");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addDirectory(false);
			}
		});
		add(btnNewButton_1, "cell 3 4");
		
		load();
		
		
	}
	
	/**
	 * Construct a list of all film directories entered by the user
	 */
	public Vector<String> getFilmsDirectories()
	{
		Vector<String> res = new Vector<String>();
		for(int i=0; i<_mdlFilms.size(); i++)
		{
			res.add(_mdlFilms.get(i).path);
		}
		return res;
	}
	
	/**
	 * Construct a list of all series directories entered by the user
	 */
	public Vector<String> getSeriesDirectories()
	{
		Vector<String> res = new Vector<String>();
		for(int i=0; i<_mdlSeries.size(); i++)
		{
			res.add(_mdlSeries.get(i).path);
		}
		return res;
	}
	
	
	private static final long serialVersionUID = 1L;

}

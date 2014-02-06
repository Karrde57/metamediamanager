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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EnumSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.t3.metamediamanager.Film;
import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.ProviderRequest;
import com.t3.metamediamanager.Searchable;


/**
 * Panel displayed in the tab "Subtitles" from the InfoPane.
 * @author vincent
 *
 */
public class SubtitlesInfo extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Media _currentMedia = null;
	private JList<ListItem> _lstSubtitles;
	private Searcher _searcher;
	
	/**
	 * Item of the list representing a subtitle
	 * @author vincent
	 *
	 */
	class ListItem 
	{
		private File _file;
		private String _name="";
		
		ListItem(File f)
		{
			_file = f;
			_name = f.getName();
		}
		
		@Override
		public String toString()
		{
			return _name;
		}
		
		public File getFile()
		{
			return _file;
		}
	}
	
	public SubtitlesInfo(Searcher s) {
		if(s == null)
			throw new IllegalArgumentException("Searcher is null");
		
		_searcher = s;
		
		setLayout(new MigLayout("", "[grow][][grow]", "[][grow][][]"));
		
		JLabel lblSoustitresDisponibles = new JLabel("Sous-titres disponibles :");
		add(lblSoustitresDisponibles, "cell 0 0 3 1");
		
		_lstSubtitles = new JList<ListItem>();
		add(_lstSubtitles, "cell 0 1 3 1,grow");
		
		JButton btnSearch = new JButton("Chercher des sous-titres");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(_currentMedia != null && _currentMedia instanceof Film)
				{
					Searchable[] mediaList = new Searchable[1];
					mediaList[0] = _currentMedia;
					
					_searcher.searchList(mediaList, EnumSet.of(ProviderRequest.Additions.SUBTITLES));
					
					setMedia(_currentMedia);
				}
				
			}
		});
		
		JLabel lblPensezVrifier = new JLabel("<html>Pensez à vérifier la langue de la recherche (voir les options)</html>");
		add(lblPensezVrifier, "cell 0 2 3 1");
		add(btnSearch, "cell 1 3");
	}
	
	
	/**
	 * Search the subtitles associated with the entered media
	 * @param m
	 */
	public void setMedia(Media m)
	{
		_currentMedia = m;
		
		File[] files = _currentMedia.searchLocalSubtitles();
		
		DefaultListModel<ListItem> model = new DefaultListModel<ListItem>();

		for(File f : files)
		{
			model.addElement(new ListItem(f));
		}
		
		_lstSubtitles.setModel(model);
		
	}
	
	
	
	
	
	

}

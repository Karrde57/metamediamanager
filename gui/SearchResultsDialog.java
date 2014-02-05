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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JDialog;

import com.t3.metamediamanager.*;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

public class SearchResultsDialog extends JDialog {
	private List<Entry<Searchable,ProviderResponse>> _responses;
	private SuggestionsFrame currentPanel = null;
	int currentIndex = 0;
	
	private List<String> _newNames = new ArrayList<String>();
	private List<Searchable> _SearchablesToSearchAgain = new ArrayList<Searchable>();
	
	private JLabel _lblInfo;
	
	private void nextSearchable()
	{
		if(currentIndex == _responses.size())
		{
			setVisible(false);
			return;
		}
		
		updateLabelInfo();
		
		if(currentPanel != null)
			getContentPane().remove(currentPanel);
		
		getContentPane().validate();
		
		
		
		
		currentPanel = new SuggestionsFrame(_responses.get(currentIndex).getKey().getName(), _responses.get(currentIndex).getValue().getSuggested());
		getContentPane().add(currentPanel, "cell 0 1 5 1,grow");
		
		getContentPane().validate();
		
		currentPanel.addNameSelectedListener(new NameSelectedListener() {
			@Override
			public void onNameSelected(NameSelectedEvent e) {
				String newName = e.getName();
				if(!newName.isEmpty())
				{
					_newNames.add(newName);
					_SearchablesToSearchAgain.add(_responses.get(currentIndex-1).getKey());
									}
				nextSearchable();
			}
		});
		
	 pack();
		
		currentIndex++;
		
		
	}
	
	private void updateLabelInfo()
	{
		_lblInfo.setText("Demande " + (currentIndex + 1) + " sur " + _responses.size());
	}
	
	public SearchResultsDialog (List<Entry<Searchable,ProviderResponse>> resp, String errors)
	{
		super();
		//setMinimumSize(new Dimension(500,500));
		setLocationRelativeTo(null);
		setModal(true);
		_responses=resp;
		getContentPane().setLayout(new MigLayout("", "[grow][][grow][][grow]", "[grow][][][grow]"));
		
		_lblInfo = new JLabel("New label");
		getContentPane().add(_lblInfo, "cell 0 0 5 1");
		
		nextSearchable();
		
		if(!errors.isEmpty())
		{
			JLabel lblErrors = new JLabel("Des erreurs sont arrivées :");
			getContentPane().add(lblErrors, "cell 0 2");
			
			
			
			JTextPane textPane = new JTextPane();
			textPane.setEditable(false);
			textPane.setText(errors);
			getContentPane().add(textPane, "cell 0 3 5 1,grow");
		}
		
		
		
	}
	
	public String[] getNewNames()
	{
		String[] array = new String[_newNames.size()];
		_newNames.toArray(array);
		return array;
	}
	
	public Searchable[] getSearchablesToSearchAgain()
	{
		Searchable[] array = new Searchable[_SearchablesToSearchAgain.size()];
		_SearchablesToSearchAgain.toArray(array);
		return array;
	}

}

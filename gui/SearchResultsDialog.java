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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JDialog;

import com.t3.metamediamanager.*;

import net.miginfocom.swing.MigLayout;

import javax.swing.JTextPane;

import javax.swing.JLabel;

/**
 * Window displayed at the end of a search. We ask the user if we must search again and which names to use
 * @author vincent
 *
 */
public class SearchResultsDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Entry<Searchable,ProviderResponse>> _responses;
	private SuggestionsFrame currentPanel = null;
	int currentIndex = 0;
	
	private List<Searchable> _SearchablesToSearchAgain = new ArrayList<Searchable>();
	
	private JLabel _lblInfo;
	
	/**
	 * Called when the user : double click on a item of the list, or "cancel"
	 */
	private void nextSearchable()
	{
		if(currentIndex == _responses.size()) //We are at the end
		{
			setVisible(false);
			return;
		}
		
		updateLabelInfo();
		
		if(currentPanel != null)
			getContentPane().remove(currentPanel);
		
		getContentPane().validate();
		
		currentIndex++;
		
		
		
		//We display a new list of suggestions
		currentPanel = new SuggestionsFrame(_responses.get(currentIndex-1).getKey().getName(), _responses.get(currentIndex-1).getValue().getSuggested());
		getContentPane().add(currentPanel, "cell 0 1 5 1,grow");
		
		getContentPane().validate();
		
		//When the user chooses a suggestion, we save it and go to the next media.
		currentPanel.addNameSelectedListener(new NameSelectedListener() {
			@Override
			public void onNameSelected(NameSelectedEvent e) {
				String newName = e.getName();
				if(!newName.isEmpty())
				{
					Searchable searchable = _responses.get(currentIndex-1).getKey();
					searchable.getInfo().put("title", newName);
					searchable.save();
					_SearchablesToSearchAgain.add(searchable);
				}
				nextSearchable();
			}
		});
		
	 pack();
		
		
		
		
	}
	
	private void updateLabelInfo()
	{
		_lblInfo.setText("Demande " + (currentIndex + 1) + " sur " + _responses.size());
	}
	
	/**
	 * Constructs the window and inits the list
	 * @param resp responses of the search
	 * @param errors
	 */
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

	
	public Searchable[] getSearchablesToSearchAgain()
	{
		Searchable[] array = new Searchable[_SearchablesToSearchAgain.size()];
		_SearchablesToSearchAgain.toArray(array);
		return array;
	}

}

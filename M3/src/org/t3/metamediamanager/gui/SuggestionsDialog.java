/*Copyright 2014  M3Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package org.t3.metamediamanager.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.t3.metamediamanager.Media;

public class SuggestionsDialog extends JDialog {
	
	private String _returnedName ="";
	
	public SuggestionsDialog(JFrame parent, String string, String[] suggestions)
	{
		super(parent);
		setLocationRelativeTo(null);
		setModal(true);
		
		setTitle("Nom du film");
		
		SuggestionsFrame sf = new SuggestionsFrame(string, suggestions);
		setContentPane(sf);
		
		sf.addNameSelectedListener(new NameSelectedListener(){
			@Override
			public void onNameSelected(NameSelectedEvent e) {
				SuggestionsDialog.this._returnedName = e.getName();
				SuggestionsDialog.this.setVisible(false);
			}	
		});
	}
	
	public String getReturnedName()
	{
		return _returnedName;
	}
}

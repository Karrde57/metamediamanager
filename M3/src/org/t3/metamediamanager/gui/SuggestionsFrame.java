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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.EventObject;


import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

class NameSelectedEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _name;
	public NameSelectedEvent(Object source, String name) {
		super(source);
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
}

interface NameSelectedListener extends EventListener {
	public void onNameSelected(NameSelectedEvent e);
}

/**
 * Displays the list of suggestions for a media.
 * It's used at the end of a search, when a media has not been found. We ask for a better name.
 * @author vincent
 *
 */
public class SuggestionsFrame extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String _returnedName = ""; //The final "new name" of the film, choosen by the user
	private String[] _suggestions;
	private JList<String> list;
	private JTextField _txtName;
	
	private void selectText(int index)
	{
		_returnedName = _suggestions[index];
		fireEvent();
	}
	
	private void fireEvent()
	{
		Object[] listeners = listenerList.getListenerList();
	    for (int i = 0; i < listeners.length; i = i+2) {
	      if (listeners[i] == NameSelectedListener.class) {
	        ((NameSelectedListener) listeners[i+1]).onNameSelected(new NameSelectedEvent(this, _returnedName));
	      }
	    }
	}
	
	public void addNameSelectedListener(NameSelectedListener l)
	{
		listenerList.add(NameSelectedListener.class, l);
	}
	public void removeNameSelectedListener(NameSelectedListener l)
	{
		listenerList.remove(NameSelectedListener.class, l);
	}
	
	/**
	 * Constructs the frame
	 * @param name of the media
	 * @param suggestions of the media
	 */
	public SuggestionsFrame(String name, String[] suggestions) {
		_suggestions = suggestions;
		setLayout(new MigLayout("", "[grow][grow][grow][][grow]", "[][][grow][][]"));
		
		JLabel lblNewLabel = new JLabel("Nous n'avons pas pu déterminer clairement " + name);
		add(lblNewLabel, "cell 0 0 5 1");
		
		if(suggestions != null)
		{
			list = new JList<String>(suggestions);
		} else {
			list = new JList<String>();
		}
		
		add(list, "cell 0 2 5 1,grow");
		
		//When the user click on a suggestion :
		// 1 clic : we copy it in the textfield
		// Double clic : it's okay, the final "new name" is used
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt) {
				JList<String> list = SuggestionsFrame.this.list;
				
				int index = list.locationToIndex(evt.getPoint());
		        
				_txtName.setText(_suggestions[index]);;
				
				if (evt.getClickCount() == 2) {
					SuggestionsFrame.this.selectText(index);
			        
			    }
		        
		    }
		});
		
		//The good name to use can also be chosen by pressing Enter.
		KeyListener kl = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
				{
					int index = list.getSelectedIndex();
			        SuggestionsFrame.this.selectText(index);
				}
					
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		

		
		JLabel lblNewLabel_1 = new JLabel("Ou directement le nom :");
		add(lblNewLabel_1, "cell 0 3,alignx trailing");
		
		_txtName = new JTextField();
		add(_txtName, "cell 1 3 4 1,growx");
		_txtName.setColumns(10);
		
		SwingUtilities.invokeLater( new Runnable() { 
				public void run() { 
				_txtName.requestFocus(); 
			    } 
		} );
		
		addKeyListener(kl);
		list.addKeyListener(kl);
		//If we press Enter while the text field is focused, we use it's text for the "new name" of the movie
		_txtName.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					SuggestionsFrame.this._returnedName = SuggestionsFrame.this._txtName.getText();
					fireEvent();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		
		JButton btnOk = new JButton("Valider");
		add(btnOk, "cell 1 4");
		
		JButton btnCancel = new JButton("Annuler");
		add(btnCancel, "cell 3 4");
		
		btnCancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				fireEvent();
			}
		});
		
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SuggestionsFrame.this._returnedName = SuggestionsFrame.this._txtName.getText();
				fireEvent();
			}
		});
		
		
	}
	
	public String getReturnedName()
	{
		return _returnedName;
	}
}

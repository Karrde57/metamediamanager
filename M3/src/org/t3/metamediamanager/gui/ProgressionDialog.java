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
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * Displayed by Searcher during a search. Dialog containing a progress bar
 * @author vincent
 *
 */
public class ProgressionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JProgressBar _progressBar;
	
	public ProgressionDialog()
	{
		setLocationRelativeTo(null);
		setModal(true);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("<html><center>Recherches en cours...</center></html>");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 24));
		getContentPane().add(lblNewLabel, BorderLayout.NORTH);
		
		_progressBar = new JProgressBar();
		_progressBar.setStringPainted(true);
		_progressBar.setValue(0);
		getContentPane().add(_progressBar, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	int confirm = JOptionPane.showOptionDialog(null,
                        "Voulez-vous vraiment annuler la recherche ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
		    	
		    	if (confirm != JOptionPane.YES_OPTION) {
		    		ProgressionDialog.this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                } else {
		    		ProgressionDialog.this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                }
		    }
		});
	}
	
	public void setProgression(Integer value)
	{
		_progressBar.setValue(value);
	}




}

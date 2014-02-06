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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;

import com.t3.metamediamanager.M3Config;
import com.t3.metamediamanager.MediaLibrary;

/**
 * Frame displayed when the user launches the app for the first time
 */
public class FirstRunFrame extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DirectoriesChooserPanel _chooser = new DirectoriesChooserPanel();
	
	public FirstRunFrame() {
		setModal(true);
		setMinimumSize(new Dimension(700,475));
		
		getContentPane().setLayout(new MigLayout("", "[grow][][grow]", "[][grow][]"));
		
		JLabel lblBienvenueDansMetamediamanager = new JLabel("<html>Bienvenue dans MetaMediaManager ! Vous lancez le programme pour la première fois. Configurez vos dossiers de films et séries :</html>");
		getContentPane().add(lblBienvenueDansMetamediamanager, "cell 0 0 3 1");
		
		getContentPane().add(_chooser, "cell 0 1 3 1,grow");
		
		JButton btnSave = new JButton("Continuer");
		getContentPane().add(btnSave, "cell 1 3 1 1");
		
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Vector<String> dirF = _chooser.getFilmsDirectories();
				Vector<String> dirS = _chooser.getSeriesDirectories();
				
				if(dirF.isEmpty() && dirS.isEmpty())
				{
					JOptionPane.showMessageDialog(FirstRunFrame.this,"Ajoutez au moins un dossier...", "Problème", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				M3Config.getInstance().setFilmsDirectories(dirF);
				M3Config.getInstance().setSeriesDirectories(dirS);
				M3Config.getInstance().setParam("firstrun", "false");
				M3Config.getInstance().save();
				
				MediaLibrary.getInstance().refreshDB();
				
				FirstRunFrame.this.setVisible(false);
			}
		});
		
		setPreferredSize(new Dimension(530,300));
	}

}

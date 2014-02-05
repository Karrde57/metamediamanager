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

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.MediaRenamer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.JTextPane;


public class Renamer extends JFrame
{
	private JTextField textField;
	private JTextField textField_1;
	public Renamer() {
		getContentPane().setLayout(new MigLayout("", "[23.00][162.00,grow][83.00][52.00][293.00]", "[30.00,center][30.00,top][30.00,top][30.00,top][30.00][30.00,grow,fill][30.00,center][30.00,bottom][30.00,bottom][30.00,bottom]"));
		
		JLabel lblFilm = new JLabel("FILM");
		getContentPane().add(lblFilm, "cell 1 0");
		
		JLabel lblOptionsDeRenommage = new JLabel("Options de renommage : ");
		getContentPane().add(lblOptionsDeRenommage, "cell 1 1");
		
		JLabel lblPourLesFilms = new JLabel("Pour les films et épisodes : ");
		getContentPane().add(lblPourLesFilms, "cell 4 1,aligny center");
		
		textField = new JTextField();
		getContentPane().add(textField, "cell 1 2,growx");
		textField.setColumns(10);
		
		JButton btnRenommer = new JButton("Renommer");
		btnRenommer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Media[] medias = Media.getAll();
				MediaRenamer.rename(medias, textField.getText(), "film");
			}
		}
		);
		
		JLabel lbltTitre = new JLabel("%t : titre");
		getContentPane().add(lbltTitre, "cell 4 2");
		getContentPane().add(btnRenommer, "cell 2 3");
		
		JLabel lbltoTitre = new JLabel("%o : titre original");
		getContentPane().add(lbltoTitre, "cell 4 3");
		
		JLabel lblaDate = new JLabel("%a : date ou année de sortie");
		getContentPane().add(lblaDate, "cell 4 4");
		
		JLabel lblEpisode = new JLabel("EPISODE");
		getContentPane().add(lblEpisode, "cell 1 6");
		
		JLabel lblOptionsDeRenommage_1 = new JLabel("Options de renommage :");
		getContentPane().add(lblOptionsDeRenommage_1, "cell 1 7");
		
		JLabel lblUniquementPourLes = new JLabel("Uniquement pour les épisodes : ");
		getContentPane().add(lblUniquementPourLes, "cell 4 7");
		
		textField_1 = new JTextField();
		getContentPane().add(textField_1, "cell 1 8,growx");
		textField_1.setColumns(10);
		
		JButton btnRenommer_1 = new JButton("Renommer");
		btnRenommer_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Media[] medias = Media.getAll();
				System.out.println(Arrays.toString(medias));
				MediaRenamer.rename(medias, textField_1.getText(), "episode");
			}
		});
		
		JLabel lblsNumro = new JLabel("%s : numéro de saison");
		getContentPane().add(lblsNumro, "cell 4 8");
		getContentPane().add(btnRenommer_1, "cell 2 9");
		
		JLabel lbleUmro = new JLabel("%e : numéro de l'épisode");
		getContentPane().add(lbleUmro, "cell 4 9");
	}


}

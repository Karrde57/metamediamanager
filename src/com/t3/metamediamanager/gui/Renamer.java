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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.MediaFilter;
import com.t3.metamediamanager.MediaRenamer;




/**
 * JFrame use to rename a films and series
 * @author jmey
 *
 */
public class Renamer extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Renamer() {
		getContentPane().setLayout(new BorderLayout(5, 5));
		
		JPanel filmEpisode = new JPanel(new BorderLayout(5,5));
		JPanel film = new JPanel(new BorderLayout(2,2));
		film.setBorder(new TitledBorder("<html> <b>Film </b> </html>"));
		JPanel episode = new JPanel(new BorderLayout(2,2));
		JPanel episode2 = new JPanel(new BorderLayout(2,2));
		episode.setBorder(new TitledBorder("<html> <b>Episode </b> </html>"));
		JLabel structfilm_label = new JLabel("Structure :");
		JLabel structep_label = new JLabel("Structure :");
		final JTextField film_textfield = new JTextField(20);
		final JTextField ep_textfield = new JTextField(20);
		JButton film_button = new JButton("Renommer");
		film_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MediaFilter mf = new MediaFilter(MediaFilter.Type.FILMS);
				Vector<Media> mediasv = Media.searchByName("", mf);
				Object[] mediaso = mediasv.toArray();
				Media[] medias = new Media[mediaso.length];
				for(int i=0;i<mediaso.length;i++)
				{
					medias[i] = (Media) mediaso[i];
				}
				MediaRenamer.rename(medias, film_textfield.getText(), "film");
				setVisible(false);
				dispose();
			}
		}
		);
		JButton ep_button = new JButton("Renommer");
		ep_button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MediaFilter mf = new MediaFilter(MediaFilter.Type.EPISODES);
				Vector<Media> mediasv = Media.searchByName("", mf);
				Object[] mediaso = mediasv.toArray();
				Media[] medias = new Media[mediaso.length];
				for(int i=0;i<mediaso.length;i++)
				{
					medias[i] = (Media) mediaso[i];
				}
				MediaRenamer.rename(medias, ep_textfield.getText(), "episode");
				setVisible(false);
				dispose();
			}
		}
		);
		JPanel explanations = new JPanel(new BorderLayout(5,5));
		JLabel explain_label = new JLabel("<html> <b> Mots-clés </b><br /><br />" +
				"%t = Titre<br />" +
				"%o = Titre Original<br /> " +
				"%a = Date de sortie<br /> " +
				"%s = Numéro de la saison <br />" + 
				"%e = Numéro de l'épisode <br /></html/>");
		explain_label.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		film.add(structfilm_label, BorderLayout.WEST);
		film.add(film_textfield, BorderLayout.CENTER);
		film.add(film_button,  BorderLayout.EAST);
		episode.add(structep_label, BorderLayout.WEST);
		episode.add(ep_textfield, BorderLayout.CENTER);
		episode.add(ep_button, BorderLayout.EAST);
		explanations.add(explain_label, BorderLayout.NORTH);
		episode2.add(episode, BorderLayout.NORTH);
		filmEpisode.add(film, BorderLayout.NORTH);
		filmEpisode.add(episode2, BorderLayout.CENTER);
		getContentPane().add(filmEpisode, BorderLayout.CENTER);
		getContentPane().add(explanations, BorderLayout.EAST);
		
		
	}
}

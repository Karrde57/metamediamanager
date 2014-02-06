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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import com.t3.metamediamanager.M3Config;
import com.t3.metamediamanager.MediaLibrary;
import com.t3.metamediamanager.ProviderManager;

/**
 * Frame-tabbed displayed when the user wants to edit option.
 * 4 tabs - general config : language, series regex... directly in this class
 *        - directories : the user can add a directory (series or movies). Display a "DirectoryChooserPanel"
 *        - priorities : the user can change priorities. Displays a PrioritiesPanel
 *        - provider configuration : FieldsConfigPanel
 * @author vincent
 *
 */
public class OptionsFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DirectoriesChooserPanel _dirChooser = new DirectoriesChooserPanel();
	private PrioritiesPanel _prioPanel = new PrioritiesPanel();
	private FieldsConfigPanel _fieldsConfigPanel = new FieldsConfigPanel();
	SPActivationPanel _saversActivationPanel = new SPActivationPanel();
	private JTextField _generalPatternTxt;
	private JTextField _generalLangTxt;
	private JTextField _generalVideoExtTxt;

	public OptionsFrame() {
		super("Options de M3");
		setMinimumSize(new Dimension(520,250));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		//START GENERAL TAB
		JPanel _generalPanel = new JPanel();
		tabbedPane.addTab("Général", null, _generalPanel, null);
		_generalPanel.setLayout(new MigLayout("", "[left][grow]", "[][][][]"));
		
		JLabel lblLangueDeRecherche = new JLabel("Langue de recherche");
		lblLangueDeRecherche.setHorizontalAlignment(SwingConstants.LEFT);
		_generalPanel.add(lblLangueDeRecherche, "cell 0 0,alignx trailing");
		
		_generalLangTxt = new JTextField();
		_generalPanel.add(_generalLangTxt, "cell 1 0,growx");
		_generalLangTxt.setColumns(10);
		
		JLabel lblPatternDtctionpisodes = new JLabel("Pattern détéction épisodes");
		_generalPanel.add(lblPatternDtctionpisodes, "cell 0 1,alignx trailing");
		
		_generalPatternTxt = new JTextField();
		_generalPanel.add(_generalPatternTxt, "cell 1 1,growx");
		_generalPatternTxt.setColumns(10);
		tabbedPane.addTab("Dossiers", null, _dirChooser, null);
		
		//We load general tab info
		_generalLangTxt.setText(M3Config.getInstance().getParam("language"));
		_generalPatternTxt.setText(M3Config.getInstance().getParam("seriesRegex"));
		
		JLabel lblNewLabel = new JLabel("Exensions vidéos :");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		_generalPanel.add(lblNewLabel, "cell 0 2,alignx trailing");
		
		_generalVideoExtTxt = new JTextField();
		_generalPanel.add(_generalVideoExtTxt, "cell 1 2,growx");
		_generalVideoExtTxt.setColumns(10);
		_generalVideoExtTxt.setText(M3Config.getInstance().getParam("videoExt"));
		
		//END GENERAL TAB
		
		_prioPanel.loadPriorities(ProviderManager.getInstance().getPriorities());
		tabbedPane.addTab("Priorités", null, _prioPanel, null);
		
		tabbedPane.addTab("Avancé", null, _fieldsConfigPanel, null);
		
		
		tabbedPane.addTab("Enregistreurs", null, _saversActivationPanel, null);
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNewButton = new JButton("Valider");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//We start by checking general tab values
				try {
		            Pattern.compile(_generalPatternTxt.getText());
		        } catch (PatternSyntaxException exception) {
		            //Regex not valid
		        	JOptionPane.showMessageDialog(OptionsFrame.this,"L'expression régulière pour la détection des épisodes de séries est invalide","Options : erreur", JOptionPane.ERROR_MESSAGE);
		        	return; //We don't continue saving
		        }
				
				M3Config.getInstance().setParam("language", _generalLangTxt.getText());
				M3Config.getInstance().setParam("seriesRegex", _generalPatternTxt.getText());
				M3Config.getInstance().setParam("videoExt", _generalVideoExtTxt.getText());
				M3Config.getInstance().save();
				
				
				//We save everything else
				M3Config.getInstance().setFilmsDirectories(_dirChooser.getFilmsDirectories());		
				M3Config.getInstance().setSeriesDirectories(_dirChooser.getSeriesDirectories());
				M3Config.getInstance().setEnabledSavers(_saversActivationPanel.getEnabledSavers());
				M3Config.getInstance().setEnabledProviders(_saversActivationPanel.getEnabledProviders());
				M3Config.getInstance().save();
				
				ProviderManager.getInstance().setPriorities(_prioPanel.getPriorities());
				ProviderManager.getInstance().save();
				
				_fieldsConfigPanel.save();
				
				MediaLibrary.getInstance().refreshDB();
				OptionsFrame.this.setVisible(false);
			}
		});
		panel_1.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Annuler");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OptionsFrame.this.setVisible(false);
			}
		});
		panel_1.add(btnNewButton_1);

		
		
	}
}

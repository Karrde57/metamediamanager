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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

import com.t3.metamediamanager.M3Config;
import com.t3.metamediamanager.MediaLibrary;
import com.t3.metamediamanager.ProviderManager;

import javax.swing.JTabbedPane;
import javax.swing.JPanel;

import java.awt.FlowLayout;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OptionsFrame extends JFrame {
	private DirectoriesChooserPanel _dirChooser = new DirectoriesChooserPanel();
	private PrioritiesPanel _prioPanel = new PrioritiesPanel();
	private FieldsConfigPanel _fieldsConfigPanel = new FieldsConfigPanel();

	public OptionsFrame() {
		super("Options de M3");
		setMinimumSize(new Dimension(520,250));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("Dossiers", null, _dirChooser, null);
		
		_prioPanel.loadPriorities(ProviderManager.getInstance().getPriorities());
		tabbedPane.addTab("Priorités", null, _prioPanel, null);
		
		tabbedPane.addTab("Fournisseurs", null, _fieldsConfigPanel, null);
		
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNewButton = new JButton("Valider");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				M3Config.getInstance().setFilmsDirectories(_dirChooser.getFilmsDirectories());		
				M3Config.getInstance().setSeriesDirectories(_dirChooser.getSeriesDirectories());	
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

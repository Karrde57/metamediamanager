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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import com.t3.metamediamanager.FieldsConfig;
import com.t3.metamediamanager.Provider;
import com.t3.metamediamanager.ProviderManager;

public class FieldsConfigPanel extends JPanel {
	
	
	private class MyModel extends DefaultTableModel
	{
		@Override
		public boolean isCellEditable(int row, int column) {
		   return (column == 1);
		}
		public boolean containsKey(String key)
		{
			int nbRows = getRowCount();
			for(int i=0; i<nbRows; i++)
			{
				if(getValueAt(i, 0).toString().toLowerCase().equals(key.toLowerCase()))
					return true;
			}
			return false;
		}
		
		public HashMap<String,String> getMap()
		{
			HashMap<String,String> res = new HashMap<String,String>();
			
			int nbRows = getRowCount();
			
			for(int i=0; i<nbRows; i++)
			{
				res.put(getValueAt(i, 0).toString(), getValueAt(i,1).toString());
			}
			
			return res;
		}
	}
	
	private JTable _fieldsTable = new JTable();
	
	private HashMap<String, MyModel> _models = new HashMap<String, MyModel>();
	
	private String _currentConfig;
	
	private JComboBox<String> cboxProviders;
	
	public FieldsConfigPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JLabel lblNewLabel = new JLabel("Provider à configurer :");
		panel.add(lblNewLabel);
		
		
		
		//Fill provider ComboBox
		//A single provider can have several config files. We load each provider et get its config file list
		Vector<Provider> providersList = ProviderManager.getInstance().getProviders();
		
		Vector<String> namesVec = new Vector<String>();
		for(Provider p : providersList)
		{
			for(String conf : p.getConfigFiles())
				namesVec.add(conf);
		}
		
		String[] namesTab = new String[namesVec.size()];
		namesVec.toArray(namesTab);
		
		//Finally fill combobox
		cboxProviders = new JComboBox<String>(namesTab);
		cboxProviders.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				FieldsConfigPanel.this.selectProvider(arg0.getItem().toString());
			}
		});
		
		if(namesTab.length > 0)
			selectProvider(namesTab[0]);
		
		panel.add(cboxProviders);
		
		JLayeredPane layeredPane = new JLayeredPane();
		add(layeredPane, BorderLayout.CENTER);
		layeredPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		layeredPane.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new MigLayout("", "[35px]", "[][][grow]"));
		
		JButton btnDelete = new JButton();
		btnDelete.setToolTipText("Supprimer ce champ");
		btnDelete.setIcon(new ImageIcon(FieldsConfig.class.getResource("/com/t3/metamediamanager/gui/icons/Trash.png")));

		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedRow = _fieldsTable.getSelectedRow();
				if(selectedRow != -1)
				{
		    	    if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Voulez-vous vraiment supprimer ce champ ?", "Options", JOptionPane.OK_CANCEL_OPTION))
		    	    {
		    	    	getCurrentModel().removeRow(selectedRow);
		    	    }

					
				}
			}
		});
		panel_1.add(btnDelete, "cell 0 1,grow");
		
		JButton btnNewField = new JButton();
		btnNewField.setToolTipText("Nouveau champ");
		btnNewField.setIcon(new ImageIcon(FieldsConfig.class.getResource("/com/t3/metamediamanager/gui/icons/Plus.png")));

		btnNewField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    String info = JOptionPane.showInputDialog(null, "Nom interne du champ :", "Options", JOptionPane.QUESTION_MESSAGE);
			    
			    if(info != null)
			    {
				    info = info.trim();
				    if(getCurrentModel().containsKey(info))
				    {
			    	    JOptionPane.showMessageDialog(null, "Vous ne pouvez pas utiliser deux fois le même nom interne.", "Options", JOptionPane.INFORMATION_MESSAGE);
			    	    return;
				    }
				    String[] data = new String[2];
				    data[0] = info;
				    data[1] = "balise";
				    getCurrentModel().addRow(data);
			    }
			    

			}
		});
		panel_1.add(btnNewField, "cell 0 0,grow");
		
		JScrollPane scrollPane = new JScrollPane();
		layeredPane.add(scrollPane, BorderLayout.CENTER);
		
		scrollPane.setViewportView(_fieldsTable);
		
		
	}
	
	
	public void selectProvider(String providerName)
	{
		_currentConfig = providerName;
		
		//If we already loaded this provider config
		if(_models.containsKey(providerName))
		{
			_fieldsTable.setModel(getCurrentModel());
			return;
		}
		
		
		
		FieldsConfig fc = new FieldsConfig(providerName);
		HashMap<String,String> fieldsAssoc = fc.getFieldsAssociation();
				
		_models.put(providerName, new MyModel());

		
		String[] colIdentifiers = {"Nom interne", "Balise utilisée"};
		
		getCurrentModel().setColumnIdentifiers(colIdentifiers);
		
		for(Entry<String,String> entry : fieldsAssoc.entrySet())
		{
			String[] data = new String[2];
			data[0] = entry.getKey();
			data[1] = entry.getValue();
			getCurrentModel().addRow(data);
		}
		
		_models.put(providerName, getCurrentModel());
		_fieldsTable.setModel(getCurrentModel());
		
	}
	
	
	private MyModel getCurrentModel()
	{
		return _models.get(_currentConfig);
	}
	
	public void save()
	{
		for(Entry<String,MyModel> modelEntry : _models.entrySet())
		{
			FieldsConfig fc = new FieldsConfig(modelEntry.getKey());
			fc.setFieldsAssociation(modelEntry.getValue().getMap());
			fc.save();
		}
	}
}

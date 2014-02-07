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
import java.awt.Component;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.t3.metamediamanager.M3Config;
import org.t3.metamediamanager.Provider;
import org.t3.metamediamanager.ProviderManager;
import org.t3.metamediamanager.Saver;
import org.t3.metamediamanager.SaverManager;

import net.miginfocom.swing.MigLayout;

import javax.swing.SwingConstants;

/**
 * This panel allows the user to select which savers and providers must be enabled
 * @author vincent
 *
 */
public class SPActivationPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel _saversPanel = new JPanel();
	JPanel _providersPanel = new JPanel();
	
	public SPActivationPanel() {
		setLayout(new MigLayout("", "[grow,left][150px:n:150px]", "[grow][grow]"));
		add(_saversPanel, BorderLayout.CENTER);
		_saversPanel.setLayout(new BoxLayout(_saversPanel, BoxLayout.Y_AXIS));
		
		JLabel lblInfoSaver = new JLabel("<html><h3>Enregistreurs activés :</h3></html>");
		lblInfoSaver.setVerticalAlignment(SwingConstants.TOP);
		lblInfoSaver.setHorizontalAlignment(SwingConstants.LEFT);
		_saversPanel.add(lblInfoSaver);
		
		add(_saversPanel);
		
		ImageUI imgDeco = new ImageUI(SPActivationPanel.class.getResource("/org/t3/metamediamanager/gui/icons/xbmcLogo.png"));
		add(imgDeco, "cell 1 0 1 2,grow");
		
		
		add(_providersPanel, "cell 0 1");
		_providersPanel.setLayout(new BoxLayout(_providersPanel, BoxLayout.Y_AXIS));
		
		JLabel lblInfoProvider = new JLabel("<html><h3>Fournisseurs activés :</h3></html>");
		_providersPanel.add(lblInfoProvider);
		
		
		load();
	}
	
	/**
	 * Loads enabled savers and providers from the XML configuration file
	 */
	private void load()
	{
		List<Saver> savers = SaverManager.getInstance().getSavers();
		Vector<String> enabledSaversNames = M3Config.getInstance().getEnabledSavers();
		for(Saver saver : savers)
		{
			JCheckBox cbox = new JCheckBox(saver.getName());
			_saversPanel.add(cbox);
			if(enabledSaversNames.contains(saver.getName()))
				cbox.setSelected(true);
		}
		
		
		List<Provider> providers = ProviderManager.getInstance().getProviders();
		Vector<String> enabledProvidersNames = M3Config.getInstance().getEnabledProviders();
		for(Provider p : providers)
		{
			JCheckBox cbox = new JCheckBox(p.getName());
			_providersPanel.add(cbox);
			if(enabledProvidersNames.contains(p.getName()))
				cbox.setSelected(true);
		}
	}
	
	/**
	 * Constructs a vector of enabled savers which can be saved by M3Config 
	 * @return enabled savers names
	 */
	public Vector<String> getEnabledSavers()
	{
		Vector<String> res = new Vector<String>();
		
		Component[] components = _saversPanel.getComponents();
		//For each checkbox in the panel
		for(Component c : components)
		{
			if(c instanceof JCheckBox)
			{
				JCheckBox cbox  = (JCheckBox) c;
				if(cbox.isSelected()) //checkbox is selected, we add to the vector
					res.add(cbox.getText());
			}
		}
		
		return res;
	}
	/**
	 * Constructs a vector of enabled savers which can be saved by M3Config 
	 * @return enabled providers names
	 */
	public Vector<String> getEnabledProviders()
	{
		Vector<String> res = new Vector<String>();
		
		Component[] components = _providersPanel.getComponents();
		//For each checkbox in the panel
		for(Component c : components)
		{
			if(c instanceof JCheckBox)
			{
				JCheckBox cbox  = (JCheckBox) c;
				if(cbox.isSelected()) //checkbox is selected, we add to the vector
					res.add(cbox.getText());
			}
		}
		
		return res;
	}

}

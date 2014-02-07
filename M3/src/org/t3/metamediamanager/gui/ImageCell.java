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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;


import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


/**
 * Cell displaying an image in a ImageGrid
 * @author vincent
 *
 */
public class ImageCell extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Create the panel.
	 */
	

	private File _file;
	
	private ImageUI btnJaquette;
	private JLabel label;
	private String _text;
	private boolean _isSelected = false;
	
	
	public void addActionListener(ActionListener a)
	{
		listenerList.add(ActionListener.class, a);
	}
	
	public void removeActionListener(ActionListener a)
	{
		listenerList.remove(ActionListener.class, a);
	}
	

	
	public ImageCell(File file, String name) {
		_file = file;
		_text = name;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
			    Object[] listeners = listenerList.getListenerList();
			    for (int i = 0; i < listeners.length; i++) {
			      if (listeners[i] == ActionListener.class) {
			        ((ActionListener) listeners[i+1]).actionPerformed(new ActionEvent(ImageCell.this, ActionEvent.ACTION_PERFORMED, ""));
			      }
			    }
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if(!ImageCell.this.isSelected())
					ImageCell.this.btnJaquette.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				if(!ImageCell.this.isSelected())
					ImageCell.this.btnJaquette.setBorder(BorderFactory.createEmptyBorder());
			}
		});
		
		setLayout(new BorderLayout());

		
		if(file != null && _file.exists())
			btnJaquette = new ImageUI(_file);
		else
			btnJaquette = new ImageUI(ClassLoader.getSystemResource("org/t3/metamediamanager/gui/unknown.png"));

		add(btnJaquette, BorderLayout.CENTER);



		
		label = new JLabel("<html><center>" + name + "</center></html>",SwingConstants.CENTER);
		
		add(label, BorderLayout.SOUTH);
		
		
		
		Font oldFont = label.getFont();
		label.setFont(new Font(oldFont.getFontName(), oldFont.getStyle(), 18));

	}
	

	
	public boolean isSelected()
	{
		return _isSelected;
	}
	
	@Override
	public void setPreferredSize(Dimension dim)
	{
		label.setPreferredSize(new Dimension(dim.width, 100));
		Dimension labelDim = label.getPreferredSize();
		btnJaquette.setPreferredSize(new Dimension(dim.width - labelDim.width, dim.height - labelDim.height));
	}
	
	public void zoom(int factor)
	{
		if(getPreferredSize().width <= 256)
		{
			setPreferredSize(new Dimension(getPreferredSize().width+2*factor,getPreferredSize().height+2*factor));
			btnJaquette.updateUI();
		}
	}
	
	public void unzoom(int factor)
	{
		if(getPreferredSize().width >= 95)
		{
			setPreferredSize(new Dimension(getPreferredSize().width-2*factor,getPreferredSize().height-2*factor));
			btnJaquette.updateUI();
		}
	}
	
	public void setSelected(boolean s)
	{
		if(!s)
		{
			btnJaquette.setBorder(BorderFactory.createEmptyBorder());
		}
		else
		{
			btnJaquette.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(1, 171, 240)));
		}
		
		_isSelected = s;
	}
	
	public File getImageFile()
	{
		return _file;
	}
	
	public String getText()
	{
		return _text;
	}
}

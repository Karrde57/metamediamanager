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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JTextField;
/**
 * Simple JTextField with an icon on the left
 * @author vincent
 *
 */


public class JIconTextField extends JTextField{
	 
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Icon icone;
	 
	 public JIconTextField(Icon i)
	 {
		 icone=i;
	 }

	 protected void paintComponent(final Graphics g) {
		 	
			super.paintComponent(g);
			this.icone.paintIcon(null, g,5,5);
		}

	 public Insets getInsets() {
			return new Insets(0, icone.getIconWidth() + 9, 0, 0);
	}
	 
	 @Override
	 public Dimension getMinimumSize()
	 {
		 Dimension min = super.getMinimumSize();
		 min.height = (min.height > icone.getIconHeight() + 8) ? min.height : icone.getIconHeight() + 8;
		 return new Dimension(min);
	 }


 }
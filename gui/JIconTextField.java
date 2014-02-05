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
/*public class JIconTextField extends JTextField{
 

	private static final long serialVersionUID = 1L;
	private Icon icon;
    private Insets dummyInsets;
 
    public JIconTextField(){
        super();
        this.icon = null;
 
       // Border border = UIManager.getBorder("TextField.border");
        JTextField dummy = new JTextField();
       // this.dummyInsets = border.getBorderInsets(dummy);
    }
 
    public void setIcon(Icon icon){
        this.icon = icon;
    }
 
    public Icon getIcon(){
        return this.icon;
    }
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
 
        /*int textX = 2;
 
        if(this.icon!=null){
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            int x = dummyInsets.left + 5;//this is our icon's x
            textX = x+iconWidth+2; //this is the x where text should start
            int y = (this.getHeight() - iconHeight)/2;
            icon.paintIcon(this, g, x, y);
        }
 
        setMargin(new Insets(2, textX, 2, 2));
 
    }
 
}*/

public class JIconTextField extends JTextField{
	 
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
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
import java.awt.Image;
import java.io.File;
import java.net.URL;

import javax.swing.JPanel;

/**
 * Swing component used to display an image
 * @author vincent
 *
 */
class ImageUI extends JPanel
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
Image image = null;
  public ImageUI(String src)
  {
    this(new File(src));
  }
  
  public ImageUI(File f)
  {
	  ImageLoader.getInstance().add(f, new ImageLoader.Callback() {
		@Override
		public void call(Image bi) {
			ImageUI.this.image = bi;
			ImageUI.this.updateUI();
		}
	  });
  }
  

public ImageUI(URL systemResource) {
    try
    {
      image = javax.imageio.ImageIO.read(systemResource);
    }
    catch (Exception e) { System.out.println(e.getMessage()); }
    
    //setPreferredSize(new Dimension(190,260));
}

public void paintComponent(Graphics g) {
    super.paintComponent(g);             
        
    if(image != null)
    {
	    // Scale it by width
	    int scaledWidth = (int)((image.getWidth(null) * getHeight()/image.getHeight(null)));
	
	    // If the image is not off the screen horizontally...
	    if (scaledWidth < getWidth()) {
	        // Center the left and right destination x coordinates.
	        int leftOffset = getWidth()/2 - scaledWidth/2;
	        int rightOffset = getWidth()/2 + scaledWidth/2;
	            
	        g.drawImage(image, 
	                leftOffset, 0, rightOffset, getHeight(), 
	                0, 0, image.getWidth(null), image.getHeight(null), 
	                null);
	    }
	
	    // Otherwise, the image width is too much, even scaled
	    // So we need to center it the other direction
	    else {
	        int scaledHeight = (image.getHeight(null)*getWidth())/image.getWidth(null);
	            
	        int topOffset = getHeight()/2 - scaledHeight/2;
	        int bottomOffset = getHeight()/2 + scaledHeight/2;
	            
	        g.drawImage(image,
	                0, topOffset, getWidth(), bottomOffset, 
	                0, 0, image.getWidth(null), image.getHeight(null), 
	                null);
	    }
    }
}


}
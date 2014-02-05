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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.jdom2.Document;
import org.jdom2.Element;

import com.t3.metamediamanager.DBManager;
import com.t3.metamediamanager.M3Config;



public class ImageLoader {
	
	public interface Callback
	{
		public void call(Image bi);
	}
		
	private static volatile ImageLoader instance = null;
	
	public final static ImageLoader getInstance() {
		if (ImageLoader.instance == null) {
			synchronized (ImageLoader.class) {
				if (ImageLoader.instance == null) {
					ImageLoader.instance = new ImageLoader();
				}
			}
		}
		return ImageLoader.instance;
	}
	
	public void add(final File file, final Callback c)
	{

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final Image img = javax.imageio.ImageIO.read(file);
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run() {
							c.call(img);
						}
					});
					
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		});
		
		thread.start();
	}
	


}

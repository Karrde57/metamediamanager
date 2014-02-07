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
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.t3.metamediamanager.Media;
import org.t3.metamediamanager.MediaInfo;



/**
 * Component used To display information in the general tab of the class "InfoPane"
 * @author Nicolas
 *
 */
public class SuperMigLayout extends JPanel{
	

	private static final long serialVersionUID = 1L;
	public JPanel _panel;
	public ImageUI _img;
	public int _nbCompo;
	public Component[] _tab = null;
	public Component[] _tabModif = null;
	public Boolean edit = false, _thumbnail = false;
	private MediaInfo mi;
	private Media m;
	private Font _fontBase;
	private Border _borderBase;
	
	/**
	 * Constructor of the SML
	 * @param nb
	 * 		Number informations in the SML
	 * @param t
	 * 		Title of the media
	 */
	public SuperMigLayout(int nb, String t)
	{
		_tab = new Component[nb+1];		//+1 pour le titre
		_tabModif = new Component[nb+1];
		
		_nbCompo = nb;
		String nbLigne = "";
		for (int i =0; i<nb; i++)
		{
			nbLigne += "[][][]";
		}
		setLayout(new MigLayout("", "[::30,fill][200,grow,fill]", "[][120::200,grow,fill]"+nbLigne));

		JLabel title = new JLabel(t);
		title.setName("title");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		_fontBase = title.getFont();
		_borderBase = title.getBorder();
		title.setFont(new Font("Tahoma", Font.BOLD, 16));
		this.add(title, "cell 0 0 2 1, alignx center");
		_tab[0] = title;										//ajout du titre dans le tab
		_tabModif[0]= new JTextField(title.getText());
	}
	
	/**
	 * Set a new Component in a line of the SML
	 * @param laCase
	 * 		Number of the line
	 * @param libelle
	 * 		"title" of the component
	 * @param infoC
	 * 		The component to place
	 */
	public void setComponent(int laCase, String libelle, Component infoC)
	{
		
		JLabel lbl = new JLabel (libelle+" :");	
		
		lbl.setFont(new Font("Tahoma", Font.BOLD, 11));
		String caseCompo = "cell 0 "+(laCase*3);
		this.add(lbl, caseCompo);
		
		String caseInfo = "cell 1 "+(laCase*3+1);	
		String textCompo = ((JLabel) infoC).getText();
		((JLabel) infoC).setText(textCompo);				//sert peut être à rien
		this.add(infoC,caseInfo);
		_tab[laCase] = infoC;
		
		if(infoC.getName() == "synopsis")			//rempli le deuxième tab
		{
			JTextArea synopsis = new JTextArea (textCompo);
			synopsis.setFont(_fontBase);
			synopsis.setBorder(_borderBase);
			synopsis.setName("synopsis");
			_tabModif[laCase] = synopsis;
		}
		else
		{
			_tabModif[laCase]= new JTextField (textCompo);
		}
	}
	
	/**
	 * Change the information of a line
	 * @param laCase
	 * 		the number of the line
	 * @param modif
	 * 		the new text of the component
	 */
	public void modifCompo (int laCase, String modif)			//modif d'un JLabel
	{
		((JLabel) _tab[laCase]).setText("<html>"+modif+"<html>");
		((JTextComponent) _tabModif[laCase]).setText(modif);
	}

	/**
	 * Switch every component to edit them
	 */
	public void SwitchAllCompos()
	{
			for(int i = 0; i<getLength(); i++)
			{
				SwitchCompo(i);
			}
			
			if (edit == true)
				edit = false;
			else
				edit = true;
	}
	
	/**
	 * Switch a component to edit it
	 * @param laCase
	 * 		number of the line
	 */
	public void SwitchCompo(int laCase)
	{
			String numCase = "";
			
			if (laCase == 0)
				numCase = "cell 0 0 2 1,alignx center";
			else
				numCase = "cell 1 "+(laCase*3+1);
			
			
			System.out.println("laCase :" + laCase +"   numCase : " + numCase);
			
			if (edit == false) //si on est en mode normal
			{
				Component enlever = _tab[laCase];
				String infoMettre ="";
				if (enlever.getName() == "filename")
				{
					infoMettre = m.getFilename();
				}
				else
					infoMettre = mi.get(enlever.getName());
				JTextComponent mettre;
				
				if(enlever.getName() == "synopsis")
				{
					mettre = new JTextArea (infoMettre);
				}
				else
					mettre = new JTextField (infoMettre);

				
				mettre.setName(enlever.getName());
				
				if(mettre instanceof JTextField)
					((JTextField) mettre).setColumns(20);
				else
				{
					((JTextArea) mettre).setColumns(65);
					((JTextArea) mettre).setLineWrap(true);
					((JTextArea) mettre).setWrapStyleWord(true);
					mettre.setFont(_fontBase);
					mettre.setBorder(_borderBase);
				}
				_tabModif[laCase] = mettre;
				this.remove(enlever);
				this.add(mettre, numCase);
			}
			else			//si on est en mode edit
			{					
				Component enlever = _tabModif[laCase];
				String textSave = ("<html>"+((JTextComponent) enlever).getText()+"</html>");
				JLabel mettre = (JLabel) _tab[laCase];
				mettre.setText(textSave);
				this.remove(enlever);
				this.add(mettre, numCase);
			}
			updateUI();
			
	}
	
	
	/**
	 * @return the tab of the modified components
	 */
	public Component[] getTab()
	{
	 return _tabModif;
	}
	
	/**
	 * 
	 * @return the length of the normal tab
	 */
	public int getLength ()				//renvoi le nombre d'élément d'un tab
	{
		int compteur = 0;
		for (Component c : _tab)
		{
			if(c != null)
			{
				compteur ++;
			}
		}
		System.out.println("nombre comp dans le tab :" + compteur);
		System.out.println("tab normal : "+_tab.length+ "tab modif" + _tabModif.length);
		return compteur;
	}
	
	 /**
	  * Return the length of a tab
	  * @param tab
	  * 	The tab you want the length
	  * @return
	  * 	The length of the tab
	  */
	public int getLength (Component[] tab)				//renvoi le nombre d'élément d'un tab
	{
		int compteur = 0;
		for (Component c : tab)
		{
			if(c != null)
			{
				compteur ++;
			}
		}
		return compteur;
	}
	
	/**
	 * set a media in the SML
	 * @param m
	 *     the media you want to set
	 */
	public void setMedia( Media m)
	{
		this.m = m;
		this.mi = m.getInfo();
	}
	
	public void setMedia( MediaInfo m)
	{
		this.mi = m;
	}
	
	/**
	 * display the first thumbnail of the current media or a defualt picture if no thumbnail
	 */
	public void setThumbnail()
	{
		//Image
		if(_thumbnail)
			this.remove(_img);
		String[] tabThumb = mi.getImages("img_backdrop");
		File pathThumb = new File(tabThumb[0]);			//prend arbitrairement le premier thumbnail
		if (pathThumb.exists())
		{
			_img = new ImageUI(tabThumb[0]);
			this.add(_img, "cell 0 1 2 1");
			_thumbnail = true;
		}
		else
			_img = new ImageUI(ClassLoader.getSystemResource("org/t3/metamediamanager/gui/UnknownThumb.png"));						//temp picture
			this.add(_img, "cell 0 1 2 1");
			_thumbnail = true;
	}
}








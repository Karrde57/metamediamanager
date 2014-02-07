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

import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.t3.metamediamanager.Actor;
import org.t3.metamediamanager.M3Config;


/**
 * Window opened when the user clicks on an actor
 * Displays the picture, and movies where the actor acts
 * @author vincent
 *
 */
public class ActorInfoFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel _lblActorFilms = new JLabel();
	
	public ActorInfoFrame(Actor a) {
		
		this.setLocationRelativeTo(null);
		
		//GENERATED WITH WINDOWBUILDER
		getContentPane().setLayout(new MigLayout("", "[150px:n:100px,grow][400px:n,grow]", "[150px:n][200px:n,grow]"));
		
		ImageUI panel = new ImageUI(M3Config.getInstance().getUserConfDirectory() + a.generateImageUrl());
		getContentPane().add(panel, "cell 0 0,grow");
		
		JLabel lblNewLabel = new JLabel("<html><h1>" + a.getName() + "</h1></html>");
		getContentPane().add(lblNewLabel, "cell 1 0");
		
		
		getContentPane().add(_lblActorFilms, "cell 0 1 2 1,alignx left,aligny top");
		
		List<Entry<String,String>> rolesAndMedianames = a.getRolesAndMediaNames();
		
		
		String html = "<html>Cet acteur joue dans les films :<ul>";
		for(Entry<String,String> entry : rolesAndMedianames)
		{
			html += "<li><i>" + entry.getKey() + "</i> dans le rôle de " + entry.getValue() + "</li>";
		}
		html += "</ul>";
		
		_lblActorFilms.setText(html);
	}

}

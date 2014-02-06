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
*/package com.t3.metamediamanager;

import java.util.Scanner;
import java.util.Vector;







public class MetaMediaManager {


	public static void main(String[] args) {
		DBManager.getInstance().connect("database.db");
		M3Config.getInstance().loadFile("config.xml");
		
		ProviderManager.getInstance().loadConfig("providers.xml");
		ProviderManager.getInstance().loadProviders();
		SaverManager.getInstance().loadSavers();
		
		Scanner s = new Scanner(System.in);
		
		int choix = 0, id;
		System.out.println("*********** MetaMediaManager pre alpha console version ***********\n");
		
		while(choix != 6)
		{
			System.out.println("1) Synchroniser le cache\n"
			         + "2) Recherche par nom dans le cache\n"
			         + "3) Recherche auto par id\n"
			         + "4) Export par id\n"
			         + "5) Afficher les infos connues d'un film\n"
			         + "6) Quitter\n"
			         + "\n"
			         + "Votre choix : ");

			
			choix = s.nextInt();
			
			switch(choix)
			{
			case 1:
				MediaLibrary.getInstance().refreshDB();
				System.out.println("Cache actualisé");
				break;
			case 2:
				System.out.println("Nom : ");
				String nom = s.next();
				
				Vector<Media> t = Media.searchByName(nom, null);
				if(t.size()==0)
					System.out.println("Aucun média trouvé.");
				for(Media media : t)
				{
					System.out.println(media.getName() + " qui est un " + media.getType() + " (ID = +" + media.getId() + ")");
				}
				break;
				
			case 3:
				System.out.println("ID à rechercher :");
				id = s.nextInt();
				
				Media media = Media.getById(id);
				
				System.out.println("Recherche en cours sur tous les providers pour "  + media.getName());
				
				ProviderRequest request = new ProviderRequest(ProviderRequest.Type.FILM, media.getName(), media.getFilename(), "en");


				ProviderResponse i = ProviderManager.getInstance().getInfo(new MediaInfo(), request);
				if(i.getType() != ProviderResponse.Type.FOUND)
				{
					System.out.println("Pouvez vous donner un nom simplifié pour " + media.getName()+"?");
					if(i.getType() == ProviderResponse.Type.SUGGESTED)
					{
						System.out.println("Suggestions :");
						for(String sug : i.getSuggested())
						{
							System.out.println(sug);
						}
					}
					
					s.nextLine();
					String name = s.nextLine();
					
					request = new ProviderRequest(request.getType(), name, request.getFilename(), request.getLanguage());

					System.out.println("vous avez demandé " + request.getName() + " "+name);
					i = ProviderManager.getInstance().getInfo(new MediaInfo(), request);
				}
				System.out.println(i.getResponse());
				if(i.getType() != ProviderResponse.Type.NOT_FOUND)
				{
					media.setInfo(i.getResponse());
					media.save();
				}
				

				break;
				
			case 4:
				System.out.println("ID à save :");
				id = s.nextInt();
				
				SaverManager.getInstance().save(Media.getById(id));
				
				break;
				
			case 5:
				System.out.println("ID à rechercher :");
				id = s.nextInt();
				Media m = Media.getById(id);
				System.out.println("Infos trouvées : ");
				System.out.println(m.getInfo());
				
				break;
				
			}
			
			
		}
		
		s.close();

	}
}

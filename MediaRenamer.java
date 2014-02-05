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
package com.t3.metamediamanager;

import java.io.File;

public class MediaRenamer {

	
	
	public static void rename(Media[] medialist, String format, String type) 
	{
		for(int i=0;i<medialist.length;i++)
		{
			System.out.println(medialist[i].getName());
		}
		for(int i=0;i<medialist.length;i++)
		{
			System.out.println("Renommage du film " + i);
				Media media = medialist[i];
			
			
			String newname = format;


			MediaInfo info = media.getInfo();
			
			
			if(type.equals("episode"))
			{
				while(newname.contains("%t") || newname.contains("%o") || newname.contains("%a") || newname.contains("%s") || newname.contains("%e"))
				{
					newname = replace(newname, "%t", info.get("title") );
					newname = replace(newname, "%o", info.get("originaltitle"));
					if(info.get("release") == null || info.get("release") != "")
					{
						newname = replace(newname, "%a", info.get("release"));
					}
					else
					{
						newname = replace(newname, "%a", info.get("year"));
					}
					newname = replace(newname, "%s", info.get("season"));
					newname = replace(newname, "%e", info.get("episode"));
				}
			
				media.renameMediaString(newname);
			}
			else if(type.equals("film"))
			{
				while(newname.contains("%t") || newname.contains("%o") || newname.contains("%a"))
				{
					newname = replace(newname, "%t", info.get("title"));
					newname = replace(newname, "%o", info.get("originaltitle"));
					if(info.get("release") == null || info.get("release") != "")
					{
					newname = replace(newname, "%a", info.get("release"));
					}
					else
					{
						newname = replace(newname, "%a", info.get("year"));
					}
					
				}
				System.out.println("Nouveau nom par mediarenamer : " + newname);
				media.renameMediaString(newname);
			}
			System.out.println("done");
		}
		
		
				
	}
    public static String replace(String originalText,
			 String subStringToFind, String subStringToReplaceWith) {
int s = 0;
int e = 0;

StringBuffer newText = new StringBuffer();

while ((e = originalText.indexOf(subStringToFind, s)) >= 0) {

   newText.append(originalText.substring(s, e));
   newText.append(subStringToReplaceWith);
   s = e + subStringToFind.length();

}

newText.append(originalText.substring(s));
return newText.toString();

} // end replace(String, String, String)
}

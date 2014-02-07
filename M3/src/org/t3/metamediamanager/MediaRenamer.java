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
*/package org.t3.metamediamanager;

import java.io.File;
import java.util.Vector;

/** 
 * This class is used to manipulate the name or the location of medias
 * @author jmey
 *
 */
public class MediaRenamer {

	/**
	 * this function create a directory for each film
	 */
	public static void folderEachFilm()
	{
		MediaFilter mf = new MediaFilter(MediaFilter.Type.FILMS);
		Vector<Media> mediasv = Media.searchByName("", mf);
		Object[] mediaso = mediasv.toArray();
		Media[] medialist = new Media[mediaso.length];
		for(int i=0;i<mediaso.length;i++)
		{
			medialist[i] = (Media) mediaso[i];
		}
		for(int i=0;i<medialist.length;i++)
		{
			String filename = medialist[i].getFilename().substring(medialist[i].getFilename().lastIndexOf(File.separator)+1, medialist[i].getFilename().lastIndexOf('.'));
			// -> name of the film
			System.out.println(filename);
			char[] forbiddenchar = {':', '*', '?' , '"', '<', '>', '|', '/', '\\'};
			for(int i2=0;i2<forbiddenchar.length;i2++)
			{
				for(int i3=0;i3<filename.length();i3++)
				{
					if(filename.charAt(i3) == forbiddenchar[i2])
						filename = filename.replace(filename.charAt(i3), ' ');
				}
			}
			String extension = medialist[i].getFilename().substring(medialist[i].getFilename().lastIndexOf('.'));
			File mediafile = new File(medialist[i].getFilename());

			File newfolder = new File(mediafile.getAbsolutePath().substring(0, medialist[i].getFilename().lastIndexOf(File.separator)+1) + File.separator + filename);
			newfolder.mkdir();
			File newmediafile = new File(newfolder, filename + extension);
			System.out.println(newmediafile.getAbsolutePath());
			mediafile.renameTo(newmediafile);
			medialist[i]._filename = newmediafile.getAbsolutePath();
			medialist[i].save();
		}
	}
	
	/**
	 * this function rename the media contains in the medialist with the format indicate
	 * @param medialist
	 * @param format
	 * @param type
	 */
	public static void rename(Media[] medialist, String format, String type) 
	{
		//System.out.println(Arrays.toString(medialist));
		for(int i=0;i<medialist.length;i++)
		{
			
			Media media = medialist[i];
			String newname = format;
			System.out.println("ancien nom =" + newname);
			if(media instanceof SeriesEpisode)
			{
				SeriesEpisode se = (SeriesEpisode) media;
				MediaInfo info = media.getInfo();
				while(newname.contains("%t") || newname.contains("%o") || newname.contains("%a") || newname.contains("%s") || newname.contains("%e"))
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
					newname = replace(newname, "%s", se.getSeasonNumber()+"");
					newname = replace(newname, "%e", se.getEpisodeNumber()+"");
					
				}
				System.out.println("Nouveau nom par mediarenamer : " + newname);
				media.renameMediaString(newname);
			}
			else if(media instanceof Film)
			{
				MediaInfo info = media.getInfo();
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
	
	/**
	 * replace a string by an other
	 * @param originalText
	 * @param subStringToFind
	 * @param subStringToReplaceWith
	 * @return
	 */
    private static String replace(String originalText,
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

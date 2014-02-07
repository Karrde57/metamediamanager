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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Some utility funtions
 */
public class Utility {
	/**
	 * Deletes a foler and its content
	 * @param folder
	 */
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
	
	/**
	 * Check if a file is in one of the entered directories
	 * @param f the file
	 * @param dirs folders array
	 * @return true if it's in one these dirs
	 */
	public static boolean isInADirectory(String f, List<String> dirs)
	{
		
		for(String dir : dirs)
		{
			if(f.startsWith(dir))
				return true;
		}
		return false;
	}
	

	
	/**
	 * Generate a more simple name (remove XVID, TRUEFRENCH etc)
	 * @param str
	 */
	public static String generateSimpleName(String str)
	{
		String strTitleAndYear, strFileName;
		
		strFileName = str.toLowerCase();
		
		strTitleAndYear = strFileName;

		String[] regexps = {"[ _\\,\\.\\(\\)\\[\\]\\-](ac3|dts|custom|dc|french|remastered|divx|divx5|dsr|dsrip|dutch|dvd|dvd5|dvd9|dvdrip|dvdscr|dvdscreener|screener|dvdivx|cam|fragment|fs|hdtv|hdrip|hdtvrip|internal|limited|multisubs|ntsc|ogg|ogm|pal|pdtv|proper|repack|rerip|retail|r3|r5|bd5|se|svcd|swedish|german|read.nfo|nfofix|unrated|extended|ws|telesync|ts|telecine|tc|brrip|bdrip|480p|480i|576p|576i|720p|720i|1080p|1080i|3d|hrhd|hrhdtv|hddvd|bluray|x264|h264|xvid|xvidvd|xxx|www.www|cd[1-9]|\\[.*\\])([ _\\,\\.\\(\\)\\[\\]\\-]|$)", "(\\[.*\\])"};

		String regCleanDate = "(.*[^ _\\,\\.\\(\\)\\[\\]\\-])[ _\\.\\(\\)\\[\\]\\-]+(19[0-9][0-9]|20[0-1][0-9])([ _\\,\\.\\(\\)\\[\\]\\-]|[^0-9]$)";
		
		Matcher matcher = Pattern.compile(regCleanDate).matcher(strTitleAndYear);
		
		if (matcher.find())
		    {
		      strTitleAndYear = matcher.group(1);
		    }
		
		if(strTitleAndYear.contains("."))
		  strTitleAndYear = strTitleAndYear.substring(0, strTitleAndYear.lastIndexOf('.')); //remove extension

		  for (int i = 0; i < regexps.length; i++)
		  {
			 matcher = Pattern.compile(regexps[i]).matcher(strTitleAndYear);

		    int j=0;
		    if (matcher.find() && (j=matcher.end()-1) > 0)
		      strTitleAndYear = strTitleAndYear.substring(0, j);
		  }
		  

		  // final cleanup - special characters used instead of spaces:
		  // all '_' tokens should be replaced by spaces
		  // if the file contains no spaces, all '.' tokens should be replaced by
		  // spaces - one possibility of a mistake here could be something like:
		  // "Dr..StrangeLove" - hopefully no one would have anything like this.

		    boolean initialDots = true;
		    boolean alreadyContainsSpace = strTitleAndYear.contains(" ");

		    StringBuilder sb = new StringBuilder(strTitleAndYear);
		    
		    for (int i = 0; i < (int)sb.length(); i++)
		    {
		      char c = sb.charAt(i);

		      if (c != '.')
		        initialDots = false;

		      if ((c == '_') || ((!alreadyContainsSpace) && !initialDots && (c == '.')))
		      {
		        sb.setCharAt(i, ' ');
		      }
		    }
		  

		  strTitleAndYear = sb.toString();
		  
		  return strTitleAndYear;
	}
}

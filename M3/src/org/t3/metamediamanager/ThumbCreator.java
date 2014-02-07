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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Class used to create thumbnails
 * @author jmey
 *
 */
public class ThumbCreator {
	String chemin;
	String osname;
	
	/**
	 * The constructor detects OS
	 */
	public ThumbCreator()
	{
		String osname = System.getProperty ("os.name").toLowerCase();
		if(osname.contains("windows"))
		{
			this.osname = "windows";
		}
		else if(osname.contains("ux") || osname.contains("ix") || osname.contains("aix")) //linux
		{
			this.osname = "linux";
		}
		else if(osname.contains("mac")) //mac
		{
			this.osname = "mac";
		}
	}
	
	/**
	 * create #nbthumb thumbnails for the media 
	 * @param mediafile
	 * @param nbthumb
	 * @return
	 * @throws ThumbException
	 */
	public String[] create(File mediafile, int nbthumb) throws ThumbException
	{
		String medianame = mediafile.getName();
		Runtime runtime = Runtime.getRuntime();
		if(osname.equals("windows"))
		{
			//File directoryjar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
			File directoryjar = new File(System.getProperty("user.dir"));
			String chemin = directoryjar.getAbsolutePath() + File.separator + "mtn";

				Path pathtemp;
				try {
					pathtemp = Files.createTempDirectory(medianame);
				} catch (Exception e) {
					throw(new ThumbException("Impossible de créer un fichier temporaire"));
				}
				String[] cmdarray = new String[] {chemin + "\\mtn.exe", mediafile.getAbsolutePath(), "-I", "-r " + nbthumb , "-c 1", "-O" + pathtemp.toString() };
				System.out.println(Arrays.toString(cmdarray));
				try {
					Process process = runtime.exec(cmdarray, null);
					process.getInputStream().close();
					process.getOutputStream().close();
					process.getErrorStream().close();
					process.waitFor(); 
				} catch (Exception e) {
					throw(new ThumbException("le processus mtn ne s'est pas effectué correctement"));
				}
				//Thread.sleep(1000);
				String[] imagesNames = new String[nbthumb];
				int i=0;
				for( File f : pathtemp.toFile().listFiles())
				{
					if(f.toString().contains(".jpg") && f.toString().contains("_000") )
					{
						imagesNames[i] = f.getAbsolutePath();
						i++;
					}
					
				}
				return imagesNames;

			
		}
		else if(osname.equals("linux"))
		{
			File directoryjar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
			String chemin = directoryjar.getParentFile().getAbsolutePath() + File.separator + "mtn";
				Path pathtemp = null;
				try {
					pathtemp = Files.createTempDirectory(medianame);
				} catch (Exception e) {
					throw new ThumbException("La création du fichier temporaire n'a pas fonctionné");
				}
				String[] cmdarray = new String[] {"/usr/local/bin/mtn", "-f", chemin + File.separator + "steelfish.ttf", mediafile.getAbsolutePath(), "-I", "-r " + nbthumb , "-c 1", "-O/"+pathtemp.toString()};
				try {
					Process process = runtime.exec(cmdarray, null);
					BufferedReader output = getOutput(process);
		            BufferedReader error = getError(process);
		            String ligne = "";
		            while ((ligne = output.readLine()) != null) {
		                System.out.println(ligne);
		            } 
		            while ((ligne = error.readLine()) != null) {
		                System.out.println(ligne);
		            }
		            process.getInputStream().close();
		            process.getOutputStream().close();
		            process.getErrorStream().close();
		            process.waitFor(); 
				} catch (Exception e)
				{
					throw new ThumbException("Le processus mtn n'a pas bien fonctionné ou n'est pas installé correctement");
				}
	            String[] imagesNames = new String[nbthumb];
				int i=0;
				for( File f : pathtemp.toFile().listFiles())
				{
					if(f.toString().contains(".jpg") && f.toString().contains("_000"))
					{
						imagesNames[i] = f.getAbsolutePath();
						i++;
					}
					
				}
				System.out.println(Arrays.toString(imagesNames));
				return imagesNames;
				
			
			

		}
		else if(osname.equals("mac"))
		{
			// TODO MetaMediaManager don't deal with evil !
			return new String[nbthumb];
		}
		else
		{
			return new String[nbthumb];
		}
		
	}
    private static BufferedReader getOutput(Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static BufferedReader getError(Process p) {
        return new BufferedReader(new InputStreamReader(p.getErrorStream()));
    }

}
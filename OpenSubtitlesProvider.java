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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class OpenSubtitlesProvider implements Provider {
	private static String USER_AGENT = "OS Test User Agent";
	private static String HOST = "http://api.opensubtitles.org/xml-rpc";
	private static int TIME_BEFORE_RELOGIN = 600000; //in ms
	
	private static String _token ="";
	private static Date _lastLogin = null;
	
	private FieldsConfig _config = new FieldsConfig(getName());
	
	private static String generateSimpleRequest(String methodName,String...params)
	{
		String res = "<methodCall><methodName>"+methodName+"</methodName><params>";
		for(String p : params)
		{
			String p2="";
			try {
				p2 = Integer.toString(Integer.parseInt(p));
			} catch(NumberFormatException e) {}

			if(p.equals(p2))
				res+="<param><double>"+p+"</double></param>";
			else
				res+="<param><string>"+p+"</string></param>";
		}
		res+="</params></methodCall>";
		return res;
	}
	
	private static String generateRequest(String methodName, String token, HashMap<String,String> params)
	{
		String res = "<methodCall><methodName>"+methodName+"</methodName><params><param><value><string>"+token+"</string></value></param><param><value><array><data><value><struct>";
		for(Entry<String,String> entry : params.entrySet())
		{
			res+="<member><name>"+entry.getKey()+"</name><value>";
			
			String p2="";
			try {
				p2 = Integer.toString(Integer.parseInt(entry.getValue()));
			} catch(NumberFormatException e) {}

			if(entry.getValue().equals(p2))
				res+="<double>"+entry.getValue()+"</double>";
			else
				res+="<string>"+entry.getValue()+"</string>";
			
			res+="</value></member>";
		}
		res+="</struct></value></data></array></value></param></params></methodCall>";
		return res;
	}
	
	private static void checkLogin()
	{
		Date currentTime = new Date();
		if(_lastLogin == null || currentTime.getTime() - _lastLogin.getTime() > TIME_BEFORE_RELOGIN)
		{
			
			Element elem = request(generateSimpleRequest("LogIn", "", "", "fr", USER_AGENT));
			try {
				Element struct = elem.getChild("params").getChild("param").getChild("value").getChild("struct");
				List<Element> members = struct.getChildren("member");
				for(Element e : members)
				{
					if(e.getChild("name").getText().equals("token"))
					{
						_token = e.getChild("value").getChild("string").getText();
					}
				}
			} catch(NullPointerException e)
			{
				
			}
			
			_lastLogin = currentTime;
		}
	}
		
	private static Element request(String request)
	{
		try {
			URL url = new URL(HOST); 
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
			
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "text/xml"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(request.getBytes().length));
			connection.setUseCaches (false);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
			wr.writeBytes(request);
			wr.flush();
			wr.close();
			
			String outputString = convertStreamToString(connection.getInputStream());
			
			System.out.println(outputString);

			 //On crée une instance de SAXBuilder
	        SAXBuilder sxb = new SAXBuilder();
	        Document doc;
	        Element elem = null;
	        try
	        {
	           //On crée un nouveau document JDOM avec en argument le fichier XML
	           //Le parsing est terminé ;)
	           doc = sxb.build(new StringReader(outputString));
	           
	           elem= doc.getRootElement();

	        }
	        catch(Exception e){
	        	System.out.println("Erreur lors de l'ouverture du fichier de configuration.");
	        }
			
			connection.disconnect();
			
			return elem;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	
	private void downloadAndExtractSubtitle(String url, File movieFile) throws ProviderException
	{
		URL urlPic;
		try {
			urlPic = new URL(url);
			InputStream fileIn = urlPic.openStream();  
			File srtFile = new File(movieFile.getAbsolutePath().substring(0, movieFile.getAbsolutePath().lastIndexOf('.'))+".srt");
			String srtPath = srtFile.getAbsolutePath();
			
			File zipFile = File.createTempFile("subtitles", ".zip");
			String zipPath = zipFile.getAbsolutePath();
			
			BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(zipPath));
			int b;  
			while ((b = fileIn.read()) != -1)  
			{
				fileOut.write(b);
			}
			fileOut.flush();  
			fileOut.close();  
			fileIn.close();  
			
			
			//Extract zip file
			//get the zip file content
	    	ZipInputStream zis = 
	    		new ZipInputStream(new FileInputStream(zipFile));
	    	//get the zipped file list entry
	    	ZipEntry ze = zis.getNextEntry();
	    	byte[] buffer = new byte[1024];
	    	System.out.println(zipFile.getAbsolutePath());
	    	while(ze!=null){
	    		if(ze.getName().endsWith(".srt"))
	    		{
	    			FileOutputStream fos = new FileOutputStream(srtFile);             
	    			 
	    			try {
                        final byte[] buf = new byte[8192];
                        int bytesRead;
                        while (-1 != (bytesRead = zis.read(buf)))
                            fos.write(buf, 0, bytesRead);
                    }
                    finally {
                        fos.close();
                    }
	     
	                fos.close();   
	                
	    		}
	    		ze = zis.getNextEntry();
	    	}
		} catch (IOException e) {
			throw new ProviderException("Erreur lors du download ou de l'extraction de fichier sous titre");
		}

	}
	
	@Override
	public String getName() {
		return "opensubtitles";
	}
	
	@Override
	public String[] getConfigFiles()
	{
		String[] tab = new String[1];
		tab[0] = getName();
		return tab;
	}

	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {
		if(r.getType() != ProviderRequest.Type.FILM)
			return new ProviderResponse(); //we only search info for films
		
		checkLogin();
		
		System.out.println("token : " + _token);
		
		HashMap<String,String> params = new HashMap<String,String>();
		
		if(!r.getImdbID().isEmpty())
			params.put("imdbid", r.getImdbID());
		else if(!r.getFilename().isEmpty())
		{
			File f = new File(r.getFilename());
			try {
				params.put("moviehash", OpenSubtitlesHasher.computeHash(f));
				params.put("moviebytesize", Long.toString(f.length()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		
		else if(r.getType() == ProviderRequest.Type.FILM && !r.getName().isEmpty())
			params.put("query", r.getName());
		else if(r.getType() == ProviderRequest.Type.EPISODE)
		{
			params.put("season", ""+r.getSeason());
			params.put("episode", ""+r.getEpisode());
		}
		Element res = request(generateRequest("SearchSubtitles", _token, params));
		
		try 
		{
			Element struct = res.getChild("params").getChild("param").getChild("value").getChild("struct").getChildren("member").get(1).getChild("value").getChild("array").getChild("data");
		
			List<Element> data = struct.getChildren("value");
			
			Vector<String> suggested = new Vector<String>();
			
			for(Element d : data)
			{
				Element struct2 = d.getChild("struct");
				List<Element> members = struct2.getChildren("member");
				
				String finalMovieName ="";
				String finaleImdbID;
				
				String foundLanguage="";
				String subtitleUrl="";
				
				for(Element m : members)
				{
					String pName = m.getChild("name").getText();
					if(pName.equals("MovieName"))
					{
						String movieName = m.getChild("value").getChild("string").getText();
						if(movieName.toLowerCase().trim().equals(r.getName()) || !r.getFilename().isEmpty())
							finalMovieName = movieName;
						else
							suggested.add(movieName);
					}
					
					if(pName.equals("ZipDownloadLink"))
					{
						subtitleUrl = m.getChild("value").getChild("string").getText();
					} else if(pName.equals("SubLanguageID"))
					{
						foundLanguage = m.getChild("value").getChildText("string");
						if(foundLanguage.equals("fre"))
							foundLanguage = "fr";
						else if(foundLanguage.equals("eng"))
							foundLanguage = "en";
					}
					
						
				}
				
				
				if(!finalMovieName.isEmpty() && foundLanguage.equals(r.getLanguage()))
				{
					if(r.getAdditions().contains(ProviderRequest.Additions.SUBTITLES))
						downloadAndExtractSubtitle(subtitleUrl, new File(r.getFilename()));
					MediaInfo mi = new MediaInfo();
					mi.put("title", finalMovieName);
					ProviderResponse result = new ProviderResponse(mi);
					return result;
				}

			}
			
			if(suggested.size() > 0)
			{
				String[] suggestedArray = new String[suggested.size()];
				suggested.toArray(suggestedArray);
				return new ProviderResponse(suggestedArray);
			}
		} catch(NullPointerException e)
		{
			throw new ProviderException("Erreur lors du parse de l'xml retourné par OpenSubtitles");
		}
			
		
		return new ProviderResponse();
	}

}

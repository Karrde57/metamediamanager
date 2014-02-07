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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/** Parse des fichiers du type :
 * <provider>
 * 		<field name="description" value="balise_utilisée_pour_la_description" />
 * 		<field ... />
 * </provider>
 */

public final class FieldsConfig {
 

    private File _configFile;
    
    private HashMap<String,String> _fieldsAssociation = new HashMap<String,String>();
    
    
    public FieldsConfig(String baseName) {
    	String filename = M3Config.getInstance().getUserConfDirectory() + baseName + ".xml";
    	
    	_configFile = new File(filename);
    	if(!_configFile.exists())
    		M3Config.getInstance().copyResourceToUserDir(baseName + ".xml", baseName + ".xml");
    	
    	Document document = new Document();
    	Element root;
        //On crée une instance de SAXBuilder
        SAXBuilder sxb = new SAXBuilder();
        try
        {
           //On crée un nouveau document JDOM avec en argument le fichier XML
           //Le parsing est terminé ;)
           document = sxb.build(_configFile);
        }
        catch(Exception e){
        	System.out.println("Erreur lors de l'ouverture du fichier de configuration de " + baseName);
        }

        //On initialise un nouvel élément racine avec l'élément racine du document.
        root = document.getRootElement();
        
        
        //Remplissage des paramètres de provider        	
        List<Element> listFields = root.getChildren("field");
        	
        Iterator<Element> i2 = listFields.iterator();
        while(i2.hasNext())
        {
        	Element currentField = i2.next();
        	_fieldsAssociation.put(currentField.getAttributeValue("name"), currentField.getAttributeValue("markup"));    
        }
    }
    
    public HashMap<String,String> getFieldsAssociation()
    {
    	return _fieldsAssociation;
    }
    
    public void setFieldsAssociation(HashMap<String,String> fa)
    {
    	_fieldsAssociation = fa;
    }
    
    public void save()
    {
    	Element root = new Element("fields");
    	Document doc = new Document(root);
    	
    	for(Entry<String,String> fieldEntry : _fieldsAssociation.entrySet())
    	{
    		Element elemField = new Element("field");
    		elemField.setAttribute("name", fieldEntry.getKey());
    		elemField.setAttribute("markup", fieldEntry.getValue());
    		root.addContent(elemField);
    	}
    	
    	//Save
	    XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
	    try {
			out.output(doc, new FileOutputStream(_configFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}
    

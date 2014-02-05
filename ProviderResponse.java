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

/**
 * 
 * @author vincent
 *
 */
public class ProviderResponse {
	public enum Type {FOUND, NOT_FOUND, SUGGESTED};
	
	private Type _type;
	private String[] _suggested = null;
	MediaInfo _response = null;
	
	public ProviderResponse(String[] suggested)
	{
		_suggested=suggested;
		_type=Type.SUGGESTED;
	}
	
	public ProviderResponse(MediaInfo mi)
	{
		_type=Type.FOUND;
		_response=mi;
	}
	
	public ProviderResponse()
	{
		_type=Type.NOT_FOUND;
	}
	
	public String[] getSuggested()
	{
		return _suggested;
	}
	
	public MediaInfo getResponse()
	{
		return _response;
	}
	
	public Type getType()
	{
		return _type;
	}
	
	public String toString()
	{
		String res ="";
		if(_type == Type.FOUND)
		{
			res += "Trouvé : \n" + _response;
		} else if(_type == Type.SUGGESTED)
		{
			res += "Suggestions : ";
			for(String s : _suggested)
			{
				res += s + ", ";
			}
			res += "\n";
		}
		else {
			res += "Non trouvé";
		}
		return res;
	}
}

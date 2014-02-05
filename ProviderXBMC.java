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

public class ProviderXBMC implements Provider {

	@Override
	public String getName() {
		return "XBMC";
	}
	
	@Override
	public String[] getConfigFiles()
	{
		String[] tab = new String[1];
		tab[0] = getName();
		return tab;
	}

	public MediaInfo getInfo(ProviderRequest request) throws ProviderException {
		MediaCenterDataXBMC xbmc = new MediaCenterDataXBMC();
		return xbmc.open(request.getFilename()) ;
	}

	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {
		if(r.getType() != ProviderRequest.Type.SERIES)
		{
			MediaInfo i = getInfo(r);
			if(i != null)
				return new ProviderResponse(i);
			else
				return new ProviderResponse();
		} else {
			MediaCenterDataXBMC xbmc = new MediaCenterDataXBMC();
			MediaInfo i = xbmc.openSeries(r.getFilename());

			if(i != null)
				return new ProviderResponse(i);
			else
				return new ProviderResponse();
		}
		
	}

}

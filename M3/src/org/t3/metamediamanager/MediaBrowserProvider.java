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

/**
 * MediaBrowserProvider is used to do the link between MediaCenterDataMediaBrowser and other classes  when mediabrowser is a provider
 * @author jmey
 */
public class MediaBrowserProvider implements Provider {

	@Override
	public String getName() {
		return "MediaBrowser";
	}
	
	@Override
	public String[] getConfigFiles()
	{
		String[] tab = new String[1];
		tab[0] = getName();
		return tab;
	}

	public MediaInfo getInfo(ProviderRequest request) throws ProviderException {
		MediaCenterDataMediaBrowser MediaBrowser = new MediaCenterDataMediaBrowser();
		return MediaBrowser.open(request.getFilename()) ;
	}

	@Override
	public ProviderResponse query(ProviderRequest r) throws ProviderException {
		if(r.getType() == ProviderRequest.Type.FILM)
		{
			MediaInfo i = getInfo(r);
			if(i != null)
				return new ProviderResponse(i);
			else
				return new ProviderResponse();
		} else if(r.getType() == ProviderRequest.Type.SERIES) {
			MediaCenterDataMediaBrowser MediaBrowser = new MediaCenterDataMediaBrowser();
			MediaInfo i = MediaBrowser.openSeries(r.getFilename());

			if(i != null)
				return new ProviderResponse(i);
			else
				return new ProviderResponse();
		} else
		{
			MediaCenterDataMediaBrowser MediaBrowser = new MediaCenterDataMediaBrowser();
			MediaInfo i = MediaBrowser.openEpisode(r.getFilename());

			if(i != null)
				return new ProviderResponse(i);
			else
				return new ProviderResponse();
		}
		
	}

}

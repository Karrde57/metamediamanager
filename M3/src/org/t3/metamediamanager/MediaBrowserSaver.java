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
public class MediaBrowserSaver implements Saver {
	
	MediaCenterDataMediaBrowser _mcdata = new MediaCenterDataMediaBrowser();

	@Override
	public void save(MediaInfo media, String filename) throws ProviderException {
		_mcdata.save(media, filename);
		
	}

	@Override
	public String getName() {
		return _mcdata.getName();
	}

	@Override
	public String[] getConfigFiles() {
		String[] f = new String[1];
		f[0] = getName();
		return f;
	}

	@Override
	public void saveEpisode(MediaInfo media, String filename)
			throws ProviderException {
		_mcdata.saveEpisode(media, filename);
	}

	@Override
	public void saveSeries(MediaInfo series, String directory)
			throws ProviderException {
		_mcdata.saveSeries(series, directory);
	}
	
}

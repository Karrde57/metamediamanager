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
*/package com.t3.metamediamanager;
/**
 * Abstract class used for local providers and savers : for example, it allows to open and save NFO XBMC files
 * @author jmey
 */
public abstract class MediaCenterData {
	/**
	 * use by the constructor of the FieldsConfig
	 * @return String name
	 */
	public abstract String getName();
	/**
	 * FOR A FILM
	 * open the .nfo or .jdom or .xml depending of the media center and read it.
	 * can also copy images to the cache and actor's images
	 * @param filename
	 * @return MediaInfo with the information of the film or episode
	 * @throws ProviderException
	 */
	public abstract MediaInfo open(String filename) throws ProviderException;
	/**
	 * FOR A FILM
	 * save the mediaInfo of the Media given in parameter into a .xml or .jdom or .xml depending
	 * of the media center.
	 * can also copy images in the cache into the folder of the film and actor's images
	 * @param info
	 * @throws ProviderException
	 */
	public abstract void save(MediaInfo info, String filename) throws ProviderException;
	/**
	 * FOR A SERIES
	 * save the mediaInfo of the Media given in parameter into a .xml or .jdom or .xml depending
	 * of the media center.
	 * can also copy images in the cache into the folder of the series and actor's images
	 * @param info
	 */
	public abstract void saveSeries(MediaInfo info, String directory);
	/**
	 * FOR A EPISODE
	 * save the mediaInfo of the Media given in parameter into a .xml or .jdom or .xml depending
	 * of the media center.
	 * can also copy images in the cache into the folder of the episode and actor's images
	 * @param info
	 */
	public abstract void saveEpisode(MediaInfo info, String filename);
	/**
	 * open the .xml or .jdom or.nfo (depending of the media center and copy informations into a MediaInfo
	 * can also copy images of the series into the cache.
	 * @param filename
	 * @throws ProviderException
	 */
	public abstract MediaInfo openSeries(String filename) throws ProviderException;
	
	/**
	 * FOR A FILM
	 * open the .nfo or .jdom or .xml depending of the media center and read it.
	 * can also copy images to the cache and actor's images
	 * @param filename
	 * @return MediaInfo with the information of the film or episode
	 * @throws ProviderException
	 */
	public abstract MediaInfo openEpisode(String filename) throws ProviderException;
}

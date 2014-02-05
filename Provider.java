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



public interface Provider {
	

	
	public String getName();
	
	/**
	 * Returns a ProviderResponse object.
	 * The MediaInfos object contains the informations about the movie.
	 * @param r
	 * 		A ProviderRequest object.
	 * @return	A ProviderResponse.
	 * @throws ProviderException
	 */
	public ProviderResponse query(ProviderRequest r) throws ProviderException;
	
	/**
	 * Returns a list of config files used by the provider (ex : omdb.xml)
	 * @return array of config file names
	 */
	public String[] getConfigFiles();
		
}

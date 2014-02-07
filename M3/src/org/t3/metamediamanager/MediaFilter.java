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
 * Filter used for local searches in databases
 */
public class MediaFilter
{
	/**
	 * Which media must be filtered
	 */
	public enum Type {ALL, FILMS, EPISODES, NONE};
	
	/**
	 * Only complete medias ?
	 *
	 */
	public enum Completion {COMPLETE, NOT_COMPLETE, ALL};
	public int season = -1; //should only used if episodes selection
	public MediaFilter(Type type){
		this.type = type;
	}
	public Type type = Type.ALL;
	public Completion completion = Completion.ALL;
}

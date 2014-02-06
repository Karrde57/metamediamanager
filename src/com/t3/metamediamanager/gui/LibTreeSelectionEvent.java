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
*/package com.t3.metamediamanager.gui;

import java.util.EventListener;
import java.util.EventObject;

import com.t3.metamediamanager.Media;
import com.t3.metamediamanager.Series;

/**
 * Event used by LibraryTree and MediaGrid to transmit what has been selected (a movie, a series, everything ...)
 * @author vincent
 *
 */
class LibTreeSelectionEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Type {MEDIA, SERIES, SEASON, ALL_FILMS, ALL_SERIES, EVERYTHING, NONE};
	private Type _type;
	
	public Series series = null;
	public int season = -1;
	public Media media = null;
	
	public LibTreeSelectionEvent(Object source, Type type) {
		super(source);
		_type = type;
	}
	
	public Type getType()
	{
		return _type;
	}
}

interface LibTreeSelectionListener extends EventListener
{
	public void selected(LibTreeSelectionEvent e);
}
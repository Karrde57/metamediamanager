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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

import com.t3.metamediamanager.Film;
import com.t3.metamediamanager.M3Config;
import com.t3.metamediamanager.ProviderManager;
import com.t3.metamediamanager.ProviderRequest;
import com.t3.metamediamanager.ProviderResponse;
import com.t3.metamediamanager.Searchable;
import com.t3.metamediamanager.Series;
import com.t3.metamediamanager.SeriesEpisode;

/**
 * Used to notify when a media has been changed during the search (information added)
 * @author vincent
 *
 */
class SearchableModifiedEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Searchable _Searchable;
	public SearchableModifiedEvent(Object source, Searchable Searchable) {
		super(source);
		_Searchable = Searchable;
	}
	
	public Searchable getSearchable()
	{
		return _Searchable;
	}
}

interface SearchableModifiedListener extends EventListener
{
	public void onSearchableModified(SearchableModifiedEvent e);
}

/**
 * Searcher display a progress bar, and start the search of movies or films.
 * It uses SwingWorker : the search is done inside a thread.
 * @author vincent
 *
 */
public class Searcher {
	protected EventListenerList listenerList = new EventListenerList();
	
	protected int _lastNotFoundSearchables = -1; //Number of not found searchables in the last search. Used to know if the current search was useful or not
	
	private class Worker extends SwingWorker<List<Entry<Searchable,ProviderResponse>>, Integer>
	{
		private Searchable[] _list;
		private ProgressionDialog _dialog;
		private EnumSet<ProviderRequest.Additions> _additions;
		public Worker(Searchable[] ml, ProgressionDialog pd,EnumSet<ProviderRequest.Additions> additions)
		{
			_list = ml;
			_dialog = pd;
			_additions = additions;
			//Init the progressbar, executed when its value change (ex : 50% -> 51%)
			addPropertyChangeListener(new PropertyChangeListener() {
				 
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						_dialog.setProgression((Integer) evt.getNewValue());
					}
				}
			});
		}

		/**
		 * Main function of Searcher. It launches the search for each searchable (movie, series, episode...) in the list.
		 * It updates the progress bar right after each media until 80%
		 * Then it saves the information
		 */
		@Override
		protected List<Entry<Searchable,ProviderResponse>> doInBackground() throws Exception {
			HashMap<Searchable,ProviderResponse> res = new HashMap<Searchable,ProviderResponse>(); //Searchable associated with the response of the providers
			int i = 0;
			for(Searchable Searchable : _list)
			{
				if(isCancelled())
					return null; //If the users closed the progress bar frame
				
				ProviderRequest request = null;
				if(Searchable instanceof Film)
					 request = new ProviderRequest(ProviderRequest.Type.FILM, Searchable.generateSimpleName(), Searchable.getFilename(), M3Config.getInstance().getParam("language"));
				else if(Searchable instanceof SeriesEpisode)
				{
					 SeriesEpisode episode = (SeriesEpisode) Searchable;
					 Series series = Series.loadById(episode.getSeriesId());
					 request = new ProviderRequest(ProviderRequest.Type.EPISODE, series.generateSimpleName(), Searchable.getFilename(), M3Config.getInstance().getParam("language"), episode.getSeasonNumber(), episode.getEpisodeNumber());
				} else if(Searchable instanceof Series)
				{
					Series s = (Series) Searchable;
					request = new ProviderRequest(ProviderRequest.Type.SERIES, s.generateSimpleName(), s.getDirectory(), M3Config.getInstance().getParam("language"));
		
				}
				
				request.setAdditions(_additions);
					
				ProviderResponse r = ProviderManager.getInstance().getInfo(Searchable.getInfo(), request);

				//Progress bar update
				res.put(Searchable, r);
				i++;
				setProgress(i * 80 / _list.length);
			}
			
			i = 0;
			//For each media, if information has been found, we save it
			List<Entry<Searchable,ProviderResponse>> SearchableToAsk = new ArrayList<Entry<Searchable,ProviderResponse>>();
			int size = res.size();
			for(Entry<Searchable, ProviderResponse> entry : res.entrySet())
			{
				if(isCancelled())
					return null; //If the user closed the window
				
				if(entry.getValue().getType() == ProviderResponse.Type.FOUND)
				{
					Searchable Searchable = entry.getKey();
					Searchable.setInfo(entry.getValue().getResponse());
					Searchable.save();
				} else {
					SearchableToAsk.add(entry);
				}	
				setProgress(i * 20 / size + 80);
				i++;
			}
			
			return SearchableToAsk;
		}
		
		@Override
		protected void done() {
			_dialog.setVisible(false);
			try {
				endSearch(get(), _additions);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} catch(CancellationException e)
			{
				//Nothing to do, the search has been cancelled
			}
			
		}
		
	}
	
	public Searcher()
	{
		
	}
	
	public void addSearchableModifiedListener(SearchableModifiedListener mml)
	{
		listenerList.add(SearchableModifiedListener.class, mml);
	}
	
	public void removeSearchableModifiedListener(SearchableModifiedListener mml)
	{
		listenerList.remove(SearchableModifiedListener.class, mml);
	}
	
	public void searchList(Searchable[] ml)
	{
		searchList(ml, EnumSet.noneOf(ProviderRequest.Additions.class));
	}
	
	/**
	 * Starts the search with additions
	 * @param ml
	 * @param additions
	 */
	public void searchList(Searchable[] ml,EnumSet<ProviderRequest.Additions> additions)
	{
		_lastNotFoundSearchables = -1;
		String[] names = new String[ml.length];
		for(int i=0; i<ml.length; i++)
			names[i] = ml[i].generateSimpleName();
		startSearch(ml, additions);
	}
	
	/**
	 * SearchList launches the SwingWorker
	 * @param ml
	 * @param namesToUse
	 * @param additions
	 */
	private void startSearch(Searchable[] ml,EnumSet<ProviderRequest.Additions> additions)
	{
		//If there are episodes, we must search for the series itself too.
		//We may want use other names for the medias : they are specified in namesToUse
		
	
		
		List<Integer> seriesIdToSearch = new ArrayList<Integer>();
		List<Integer> seriesIdToNotSearch = new ArrayList<Integer>();

		List<Searchable> finalList = new ArrayList<Searchable>();
		for(Searchable s : ml)
		{
			finalList.add(s);
		}

			
		
		//We add Series in searchable
		for(Searchable s : ml)
		{
			if(s instanceof SeriesEpisode)
			{
				SeriesEpisode episode = (SeriesEpisode) s;
				int seriesId = episode.getSeriesId();
				
				if(!seriesIdToSearch.contains(seriesId) && !seriesIdToNotSearch.contains(seriesId))
				{//If the series of the episode isn't already in the list
					Series series = Series.loadById(seriesId);
					
					seriesIdToSearch.add(seriesId); //We add it
					
					if(!finalList.contains(series))
					{
							finalList.add(series);
					}
					
				}
			}
		}
		//List -> Array
		Searchable[] finalTab = new Searchable[finalList.size()];
		finalList.toArray(finalTab);

		
		//We create the progress bar window and launch the worker
		
		ProgressionDialog pd = new ProgressionDialog();
		final Worker w = new Worker(finalTab, pd, additions);
		
		pd.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				w.cancel(false);
			}
		});
		
		
		w.execute();
		
		pd.pack();
		pd.setVisible(true);
	}
	
	/**
	 * Method called when the search is ended. It opens a new window (SearchResultDialog) if there are unfound media.
	 * @param SearchableToAsk
	 * @param additions
	 */
	private void endSearch(List<Entry<Searchable,ProviderResponse>> SearchableToAsk,EnumSet<ProviderRequest.Additions> additions)
	{
		
		if(SearchableToAsk == null || SearchableToAsk.size() == _lastNotFoundSearchables)
			return;
		
		if(SearchableToAsk.size() > 0)
		{
			List<Searchable> tosearch = new ArrayList<Searchable>(); //We will not ask the user about episodes.
			List<Entry<Searchable,ProviderResponse>> SearchableToAskNoEpisodes = new ArrayList<Entry<Searchable,ProviderResponse>>();
			for(Entry<Searchable,ProviderResponse> entry : SearchableToAsk)
			{
				if(entry.getKey() instanceof SeriesEpisode)
				{
					tosearch.add(entry.getKey());
				} else {
					SearchableToAskNoEpisodes.add(entry);
				}
			}
			
			if(SearchableToAskNoEpisodes.size() > 0)
			{
			
				//We open SearchResultDialog and asks the user if we must search again, and which names to use
				SearchResultsDialog d = new SearchResultsDialog(SearchableToAskNoEpisodes, ProviderManager.getInstance().getErrorLog());
				ProviderManager.getInstance().cleanErrorLog();
				d.pack();
				d.setVisible(true);
	
				tosearch.addAll(Arrays.asList(d.getSearchablesToSearchAgain()));
			}
			
			Searchable[] tosearchTab = new Searchable[tosearch.size()];
			tosearch.toArray(tosearchTab);
			
			//If there are movies to search again
			if(tosearchTab.length > 0)
			{
				_lastNotFoundSearchables = SearchableToAsk.size();
				startSearch(tosearchTab, additions);
			}
		}
		
		fireEvent(null);
	}
	
	/**
	 * Fire event when medias has been modified
	 * @param m
	 */
	private void fireEvent(Searchable m)
	{
		Object[] listeners = listenerList.getListenerList();
	    for (int j = 0; j < listeners.length; j+=2) {
	      if (listeners[j] == SearchableModifiedListener.class) {
	        ((SearchableModifiedListener) listeners[j+1]).onSearchableModified(new SearchableModifiedEvent(this, m));
	      }
	    }
	}
	
	public void search(Searchable s)
	{
		Searchable[] list = new Searchable[1];
		list[0] = s;
		searchList(list);
	}
	

	
}

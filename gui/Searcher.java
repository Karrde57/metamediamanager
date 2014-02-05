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
package com.t3.metamediamanager.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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

class SearchableModifiedEvent extends EventObject
{
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

public class Searcher {
	protected EventListenerList listenerList = new EventListenerList();
	
	private class Worker extends SwingWorker<List<Entry<Searchable,ProviderResponse>>, Integer>
	{
		private Searchable[] _list;
		private String[] _namesToUse;
		private ProgressionDialog _dialog;
		private EnumSet<ProviderRequest.Additions> _additions;
		public Worker(Searchable[] ml, String[] ntu, ProgressionDialog pd,EnumSet<ProviderRequest.Additions> additions)
		{
			_list = ml;
			_namesToUse = ntu;
			_dialog = pd;
			_additions = additions;
			addPropertyChangeListener(new PropertyChangeListener() {
				 
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						_dialog.setProgression((Integer) evt.getNewValue());
					}
				}
			});
		}

		@Override
		protected List<Entry<Searchable,ProviderResponse>> doInBackground() throws Exception {
			HashMap<Searchable,ProviderResponse> res = new HashMap<Searchable,ProviderResponse>();
			int i = 0;
			for(Searchable Searchable : _list)
			{
				if(isCancelled())
					return null;
				
				ProviderRequest request = null;
				if(Searchable instanceof Film)
					 request = new ProviderRequest(ProviderRequest.Type.FILM, _namesToUse[i], Searchable.getFilename(), M3Config.getInstance().getParam("language"));
				else if(Searchable instanceof SeriesEpisode)
				{
					 SeriesEpisode episode = (SeriesEpisode) Searchable;
					 Series series = episode.getSeries();
					 request = new ProviderRequest(ProviderRequest.Type.EPISODE, series.getName(), Searchable.getFilename(), M3Config.getInstance().getParam("language"), episode.getSeasonNumber(), episode.getEpisodeNumber());
				} else if(Searchable instanceof Series)
				{
					Series s = (Series) Searchable;
					request = new ProviderRequest(ProviderRequest.Type.SERIES, s.getName(), M3Config.getInstance().getParam("language"));
				}
				
				request.setAdditions(_additions);
					
				ProviderResponse r = ProviderManager.getInstance().getInfo(request);

				
				res.put(Searchable, r);
				i++;
				setProgress(i * 80 / _list.length);
			}
			
			i = 0;
			
			List<Entry<Searchable,ProviderResponse>> SearchableToAsk = new ArrayList<Entry<Searchable,ProviderResponse>>();
			int size = res.size();
			for(Entry<Searchable, ProviderResponse> entry : res.entrySet())
			{
				if(isCancelled())
					return null;
				
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
	
	public void searchList(Searchable[] ml,EnumSet<ProviderRequest.Additions> additions)
	{
		String[] names = new String[ml.length];
		for(int i=0; i<ml.length; i++)
			names[i] = ml[i].generateSimpleName();
		searchList(ml, names, additions);
	}
	
	private void searchList(Searchable[] ml, String[] namesToUse,EnumSet<ProviderRequest.Additions> additions)
	{
		ProgressionDialog pd = new ProgressionDialog();
		final Worker w = new Worker(ml, namesToUse, pd, additions);
		
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
	
	private void endSearch(List<Entry<Searchable,ProviderResponse>> SearchableToAsk,EnumSet<ProviderRequest.Additions> additions)
	{
		
		if(SearchableToAsk == null)
			return;
		
		if(SearchableToAsk.size() > 0)
		{
			SearchResultsDialog d = new SearchResultsDialog(SearchableToAsk, ProviderManager.getInstance().getErrorLog());
			ProviderManager.getInstance().cleanErrorLog();
			d.pack();
			d.setVisible(true);
			
			Searchable[] SearchablesToSearch = d.getSearchablesToSearchAgain();
			String[] names = d.getNewNames();
			
			if(names.length > 0)
			{
				searchList(SearchablesToSearch, names, additions);
			}
		}
		
		fireEvent(null);
	}
	
	private void fireEvent(Searchable m)
	{
		Object[] listeners = listenerList.getListenerList();
	    for (int j = 0; j < listeners.length; j+=2) {
	      if (listeners[j] == SearchableModifiedListener.class) {
	        ((SearchableModifiedListener) listeners[j+1]).onSearchableModified(new SearchableModifiedEvent(this, m));
	      }
	    }
	}
	
	public void search(Searchable Searchable)
	{
		ProviderRequest request = new ProviderRequest(ProviderRequest.Type.FILM, Searchable.generateSimpleName(), Searchable.getFilename(), M3Config.getInstance().getParam("language"));

		ProviderResponse i = ProviderManager.getInstance().getInfo(request);
		if(i.getType() != ProviderResponse.Type.FOUND)
		{
			SuggestionsDialog sf = new SuggestionsDialog(null, Searchable.getName(), i.getSuggested());
			sf.pack();
			sf.setVisible(true);
			
			String name = sf.getReturnedName();
			
			request = new ProviderRequest(request.getType(), name, request.getFilename(), request.getLanguage());

			i = ProviderManager.getInstance().getInfo(request);
		}
		
		if(i.getType() != ProviderResponse.Type.NOT_FOUND)
		{
			Searchable.setInfo(i.getResponse());
			Searchable.save();
			
		    fireEvent(Searchable);
		}
	}
	

	
}

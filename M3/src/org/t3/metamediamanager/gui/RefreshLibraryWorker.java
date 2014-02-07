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
*/package org.t3.metamediamanager.gui;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.t3.metamediamanager.MediaLibrary;

/**
 * Dialog containing a SwingWorker allowing to refresh the library (scan the filesystem) with a progress bar
 * @author vincent
 *
 */
public class RefreshLibraryWorker extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class Worker extends SwingWorker<Boolean, Integer>
	{
		
		@Override
		protected Boolean doInBackground() throws Exception {
			MediaLibrary.getInstance().refreshDB();
			return true;
		}
		
		@Override
		protected void done() {
			RefreshLibraryWorker.this.setVisible(false);
		}
	}
	
	public RefreshLibraryWorker()
	{
		this.setLocationRelativeTo(null);
		setModal(true);
		setTitle("Actualisation de la bibliothèque");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		this.getContentPane().add(progressBar);
		
		Worker w = new Worker();
		w.execute();
	}
}

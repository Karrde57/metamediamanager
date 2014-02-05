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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


class CellSelectedEvent extends EventObject
{

	private static final long serialVersionUID = 1L;
	private ImageCell _cell;
	public CellSelectedEvent(Object source, ImageCell cell) {
		super(source);
		_cell=cell;
	}
	
	public ImageCell getCell()
	{
		return _cell;
	}
}

interface CellSelectedListener extends EventListener
{
	public void imageChanged(CellSelectedEvent e);
}

/**
 * Grid used to display many images. The user can select the image.
 * @author vincent
 *
 */
public class ImageGrid extends JScrollPane {
	
	private Dimension _defaultCellDim = new Dimension(190,360);

	private static final long serialVersionUID = 1L;

	protected List<ImageCell> _list = new ArrayList<ImageCell>();
	
	JPanel container = new JPanel();	
	
	
	public ImageGrid()
	{
		super();
		
		setViewportView(container);
		container.setLayout(new AutoGridLayout());

		getVerticalScrollBar().setUnitIncrement(16);
		
		//Zoom feature
		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				
				if(arg0.isControlDown())
				{
					if(arg0.getWheelRotation()<0)
					{
						for(ImageCell item : _list)
						{
							item.zoom(arg0.getScrollAmount());
						}
					} else {
						for(ImageCell item : _list)
						{
							item.unzoom(arg0.getScrollAmount());
						}
					}
					
					arg0.consume();
					
				}
			}
			
		});
		
		
	}
	
	
	public void addCellSelectedListener(CellSelectedListener mcl)
	{
		listenerList.add(CellSelectedListener.class, mcl);
	}
	
	public void removeCellSelectedListener(CellSelectedListener mcl)
	{
		listenerList.remove(CellSelectedListener.class, mcl);
	}
	

	
	/**
	 * Adds a cell in the grid
	 * @param ic
	 */
	public void addCell(ImageCell ic)
	{
		ActionListener a = new ActionListener()
		{

			//When the user click on the cell, it became selected. We unselect every other cells.
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageCell mc = (ImageCell) e.getSource();
				
				for(ImageCell m : _list)
				{
					m.setSelected(false);
				}
				
				mc.setSelected(true);
				
				Object[] listeners = listenerList.getListenerList();
			    for (int i = 0; i < listeners.length; i = i+2) {
			      if (listeners[i] == CellSelectedListener.class) {
			        ((CellSelectedListener) listeners[i+1]).imageChanged(new CellSelectedEvent(this, mc));
			      }
			    }
				
				
			}
			
		};
		ic.setPreferredSize(_defaultCellDim);
		ic.addActionListener(a);
		_list.add(ic);
		container.add(ic);
	}
	
	
	public void clean()
	{
		_list.clear();
		container.removeAll();
	}
	
	public void setDefaultCellSize(Dimension d)
	{
		_defaultCellDim = d;
	}
	
	/**
	 * Returns the selected cell or null if nothing is selected
	 * @return selected cell or null
	 */
	public ImageCell getSelectedCell()
	{
		for(ImageCell cell : _list)
		{
			if(cell.isSelected())
				return cell;
		}
		return null;
	}
	
	
}

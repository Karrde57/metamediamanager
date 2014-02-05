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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JScrollPane;

import com.t3.metamediamanager.ProviderManager;
import javax.swing.ImageIcon;

public class PrioritiesPanel extends JPanel {
	private class PTreeModel extends DefaultTreeModel 
	{

		public PTreeModel() {
			super(new DefaultMutableTreeNode("root"));

		}
		
		public boolean addInfoType(String type)
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
			int nbChildren = root.getChildCount();
			for(int i=0; i<nbChildren; i++)
			{
				if(root.getChildAt(i).toString().equals((type)))
				{
					return false;
				}
			}
			root.add(new DefaultMutableTreeNode(type));
			return true;
		}
		
		public boolean addProvider(String name, String type)
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
		
			int nbChildren = root.getChildCount();
			for(int i=0; i<nbChildren; i++)
			{
				DefaultMutableTreeNode infoNode = (DefaultMutableTreeNode) root.getChildAt(i);
				
				if(infoNode.toString().equals((type)))
				{
					//Check is such provider is not already a child
					int nbChildrenInfo = infoNode.getChildCount();
					for(int j=0; j<nbChildrenInfo; j++)
					{
						if(infoNode.getChildAt(j).toString().equals(name))
							return false;
					}
					infoNode.add(new DefaultMutableTreeNode(name));
					return true;
				}
			}
			
			return false;
		}
		
		public void up(TreeNode n)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) n;
			if(node.isLeaf())
			{
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

				int index = parent.getIndex(node);
				System.out.println("ii"+index);
				if(index > 0)
				{
					parent.remove(node);
					parent.insert(node, index-1);
				}
			}
		}
		
		public void down(TreeNode n)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) n;
			if(node.isLeaf())
			{
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

				int index = parent.getIndex(node);
				System.out.println("ii"+index);
				if(index < parent.getChildCount()-1)
				{
					parent.remove(node);
					parent.insert(node, index+1);
				}
			}
		}
		
		/**
		 * Build a HashMap containing all information types and associated providers sorted by priority
		 * @return
		 */
		public HashMap<String, String[]> getPriorities()
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
			int nbInfoTypes = root.getChildCount();
			
			HashMap<String,String[]> res = new HashMap<String,String[]>();
			
			for(int i=0; i<nbInfoTypes; i++)
			{
				DefaultMutableTreeNode infoNode = (DefaultMutableTreeNode) root.getChildAt(i);
				int nbProviders = infoNode.getChildCount();
				
				String[] providerNames = new String[nbProviders];
				
				for(int j=0; j<nbProviders; j++)
				{
					providerNames[j] = infoNode.getChildAt(j).toString();
				}
				
				res.put(infoNode.toString(), providerNames);
			}
			
			return res;
		}
		
	}
	
	private PTreeModel _model = new PTreeModel();
	private JTree _tree = new JTree();
	
	public PrioritiesPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		panel.setLayout(new MigLayout("", "[][grow,center]", "[grow][][][grow][][]"));
		
		JButton btnDescendre = new JButton("");
		btnDescendre.setToolTipText("Descendre");
		btnDescendre.setIcon(new ImageIcon(PrioritiesPanel.class.getResource("/com/t3/metamediamanager/gui/icons/Arrow - Down.png")));
		panel.add(btnDescendre, "cell 0 2,alignx center");
		
		JButton btnNewButton = new JButton("");
		btnNewButton.setToolTipText("Monter");
		btnNewButton.setIcon(new ImageIcon(PrioritiesPanel.class.getResource("/com/t3/metamediamanager/gui/icons/Arrow - Up.png")));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(_tree.getSelectionPath()==null)
					return;
				DefaultMutableTreeNode selectedElement 
				   =(DefaultMutableTreeNode)_tree.getSelectionPath().getLastPathComponent();
				TreePath sp = _tree.getSelectionPath();
				_model.up(selectedElement);
				_model.reload();
				for (int i = 0; i < _tree.getRowCount(); i++) {
				    _tree.expandRow(i);
				}
				_tree.setSelectionPath(sp);
			}
		});
		
				panel.add(btnNewButton, "cell 0 1,alignx center");
				
				JButton btnNewButton_1 = new JButton("Info");
				btnNewButton_1.setToolTipText("Ajouter un nouveau type d'information");
				btnNewButton_1.setIcon(new ImageIcon(PrioritiesPanel.class.getResource("/com/t3/metamediamanager/gui/icons/Info2.png")));
				btnNewButton_1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
					    String info = JOptionPane.showInputDialog(null, "Champ interne de l'information :", "Priorités", JOptionPane.QUESTION_MESSAGE);
					    if(!info.isEmpty())
					    {
					    	if(_model.addInfoType(info))
					    		reloadTree();
					    	else
					    	    JOptionPane.showMessageDialog(null, "Ce type d'information est déjà renseigné.", "Priorités", JOptionPane.INFORMATION_MESSAGE);
					    }
					    
					}
				});
				panel.add(btnNewButton_1, "cell 0 4,alignx center");
				
				JButton btnNewButton_2 = new JButton("Fournisseur");
				btnNewButton_2.setToolTipText("Ajouter un nouveau fournisseur");
				btnNewButton_2.setIcon(new ImageIcon(PrioritiesPanel.class.getResource("/com/t3/metamediamanager/gui/icons/Download.png")));
				btnNewButton_2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TreePath sp = _tree.getSelectionPath();
						
						if(sp != null)
						{
							DefaultMutableTreeNode selectedElement 
							   =(DefaultMutableTreeNode)sp.getLastPathComponent();
							DefaultMutableTreeNode rootElement = (DefaultMutableTreeNode) _model.getRoot();
							String type = (selectedElement.getParent() == rootElement) ? selectedElement.toString() : selectedElement.getParent().toString();
							
							String[] plist = ProviderManager.getInstance().getProvidersNameList();
							String nom = (String)JOptionPane.showInputDialog(null, 
								      "Fournisseur à ajouter :",
								      "Priorités",
								      JOptionPane.QUESTION_MESSAGE,
								      null, plist, plist[0]
								      );
							
							if(nom != null)
							{
								_model.addProvider(nom, type);
								reloadTree();
							}
							
						}
						
						
					}
				});
				panel.add(btnNewButton_2, "cell 0 5,alignx center");
		btnDescendre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(_tree.getSelectionPath()==null)
					return;
				DefaultMutableTreeNode selectedElement 
				   =(DefaultMutableTreeNode)_tree.getSelectionPath().getLastPathComponent();
				TreePath sp = _tree.getSelectionPath();
				_model.down(selectedElement);
				reloadTree();
				_tree.setSelectionPath(sp);
			}
		});
		
		
		
		for (int i = 0; i < _tree.getRowCount(); i++) {
		    _tree.expandRow(i);
		}
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		
		_tree.setModel(_model);
		scrollPane.setViewportView(_tree);
		
		_tree.setRootVisible(false);
	}
	
	private void reloadTree()
	{
		_model.reload();
		_tree.setModel(_model);
		for (int i = 0; i < _tree.getRowCount(); i++) {
		    _tree.expandRow(i);
		}
	}
	
	
	public void loadPriorities(HashMap<String, String[]> priorities)
	{
		_model = new PTreeModel();
		for(Entry<String, String[]> entry : priorities.entrySet())
		{
			_model.addInfoType(entry.getKey());
			for(String providerName : entry.getValue())
			{
				_model.addProvider(providerName, entry.getKey());
			}
		}
		
		reloadTree();
	}
	
	public HashMap<String, String[]> getPriorities()
	{
		return _model.getPriorities();
	}
}

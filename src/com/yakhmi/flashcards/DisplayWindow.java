/*Copyright 2010 Sahil Yakhmi

This file is part of FlashY.

FlashY is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

FlashY is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with FlashY. If not, see http://www.gnu.org/licenses/.
*/

package com.yakhmi.flashcards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.AbstractTableModel;

import com.explodingpixels.macwidgets.*;
import com.yakhmi.swingutil.MacOptionPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

public class DisplayWindow extends JFrame
{
	private static final int COLUMN_COUNT = 2;

	private static final int WIDTH = 1000;
	private static final int HEIGHT = 800;
	private static final String ADD_CARD = "add";
	private static final String DELETE_CARD = "delete";
	private static final String LIST_VIEW = "list";
	private static final String LARGER_VIEW = "larger";
	private static final String QUIZ_VIEW = "quiz";
	private static final String ADD_LIST_MESSAGE = "Please enter a name for the new list of flash cards";
	private static final String ADD_CAT_MESSAGE = "Please enter a name for the new flash card category";
	private static final String REMOVE_CAT_MESSAGE = "Are you sure you want to delete the category: ";
	private static final String RENAME_CAT_MESSAGE = "Please enter the new name for the parent category of the selected list of flash cards";
	private static final String RENAME_LIST_MESSAGE = "Please enter the new name for the selected list of flash cards";
	private static final String LICENSE_DIALOG_TEXT = "This program is Free Software. It is distributed under the terms of the GNU GPL v.3";
	private static final String VIEW_ALL = "View All";
	private static final Color BACKGROUND = new Color(238, 238, 238);

	private Database db;
	private SourceList sourceList;
	private Map<SourceListItem, CardList> map = new HashMap<SourceListItem, CardList>();

	private JPanel main;
	private JPanel addPanel;
	private JScrollPane mainScroll;
	private JTable mainTable;
	private JTable largerTable;
	private FlashCardTableModel model;
	private JTextField question;
	private JTextField answer;
	private JButton addButton;
	private JButton deleteButton;
	private JLabel bottomLabel;
	private FlashCardViewerComponent cardViewer;
	private JMenuItem removeCat;
	private JMenuItem renameCat;
	private JMenuItem renameList;
	private JButton delCatButton;
	private JToggleButton listViewButton;
	private JToggleButton largerViewButton;
	private JToggleButton quizViewButton;
	private JToggleButton nullButton;

	public DisplayWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MacUtils.makeWindowLeopardStyle(this.getRootPane());
		setSize(WIDTH, HEIGHT);
		db = new Database();
		setupLayout();
		setupMenuBar();
		setTitle("FlashY");
		addWindowListener();
		setVisible(true);
		mainTable.getColumnModel().getColumn(0).setPreferredWidth(main.getWidth() / 2 - 7);
		mainTable.getColumnModel().getColumn(1).setPreferredWidth(main.getWidth() / 2 - 7);
		sourceChanged(null);
	}

	private void setupLayout()
	{
		setLayout(new BorderLayout());
		
		listViewButton = new JToggleButton(new ImageIcon(
                DUnifiedToolbar.class.getResource("/com/explodingpixels/macwidgets/icons/sourceViewNormal.png")));
		listViewButton.setSelectedIcon(new ImageIcon(
                DUnifiedToolbar.class.getResource("/com/explodingpixels/macwidgets/icons/sourceViewNormalSelected.png")));
		listViewButton.putClientProperty("JButton.buttonType", "segmentedTextured");
		listViewButton.putClientProperty("JButton.segmentPosition", "first");
		listViewButton.setFocusable(false);
		listViewButton.addActionListener(viewChanged);
		listViewButton.setActionCommand(LIST_VIEW);
        largerViewButton = new JToggleButton(new ImageIcon(DisplayWindow.class.getResource("/images/icons/largerSourceViewNormal.png")));
        largerViewButton.setSelectedIcon(new ImageIcon(DisplayWindow.class.getResource("/images/icons/largerSourceViewNormalSelected.png")));
        largerViewButton.putClientProperty("JButton.buttonType", "segmentedTextured");
        largerViewButton.putClientProperty("JButton.segmentPosition", "middle");
        largerViewButton.setFocusable(false);
        largerViewButton.addActionListener(viewChanged);
		largerViewButton.setActionCommand(LARGER_VIEW);
        quizViewButton = new JToggleButton(new ImageIcon(DisplayWindow.class.getResource("/images/icons/FullViewTemplate.png")));
        quizViewButton.putClientProperty("JButton.buttonType", "segmentedTextured");
        quizViewButton.putClientProperty("JButton.segmentPosition", "last");
        quizViewButton.setFocusable(false);
        quizViewButton.addActionListener(viewChanged);
		quizViewButton.setActionCommand(QUIZ_VIEW);
        nullButton = new JToggleButton();
        ButtonGroup group = new ButtonGroup();
        group.add(listViewButton);
        group.add(largerViewButton);
        group.add(quizViewButton);
        group.add(nullButton);
        LabeledComponentGroup viewButtons = new LabeledComponentGroup("View", listViewButton, largerViewButton, quizViewButton);
        JButton addCatButton = null;
		addCatButton = new JButton("Add Category", new ImageIcon(DisplayWindow.class.getResource("/images/icons/AddFolder.png")));
		addCatButton.addActionListener(addCatAction);
        delCatButton = new JButton("Remove Category", new ImageIcon(DisplayWindow.class.getResource("/images/icons/DeletePicture.png")));
        delCatButton.addActionListener(removeCatAction);
		UnifiedToolBar top = new UnifiedToolBar();
		top.installWindowDraggerOnWindow(this);
		top.addComponentToLeft(MacButtonFactory.makeUnifiedToolBarButton(addCatButton));
		top.addComponentToLeft(MacButtonFactory.makeUnifiedToolBarButton(delCatButton));
		top.addComponentToRight(viewButtons.getComponent());
		add(top.getComponent(), BorderLayout.NORTH);

		BottomBar bottom = new BottomBar(BottomBarSize.SMALL);
		bottomLabel = MacWidgetFactory.createEmphasizedLabel("");
		bottom.addComponentToCenter(bottomLabel);
		bottomLabel.setText(db.getNumCards() + " Cards");
		add(bottom.getComponent(), BorderLayout.SOUTH);

		main = new JPanel();
		main.setOpaque(true);
		main.setBackground(BACKGROUND);
		main.setLayout(new BorderLayout());

		JSplitPane hsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeSourceList(), main);
		hsplitPane.setDividerLocation(200);
		hsplitPane.setContinuousLayout(true);
		hsplitPane.setDividerSize(1);
		((BasicSplitPaneUI) hsplitPane.getUI()).getDivider().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xa5a5a5)));
		hsplitPane.setBorder(BorderFactory.createEmptyBorder());
		SourceListControlBar controlBar = new SourceListControlBar();
		sourceList.installSourceListControlBar(controlBar);
		controlBar.createAndAddButton(MacIcons.PLUS, addListAction());
		controlBar.createAndAddButton(MacIcons.MINUS, deleteListAction());
		controlBar.installDraggableWidgetOnSplitPane(hsplitPane);
		controlBar.getComponent();

		add(hsplitPane, BorderLayout.CENTER);

		addPanel = new JPanel();
		addPanel.setLayout(new GridLayout(1, 3));
		addPanel.setPreferredSize(new Dimension(0, 250));
		addPanel.setBackground(BACKGROUND);
		JPanel addPanelLeft = new JPanel();
		addPanelLeft.setOpaque(true);
		addPanelLeft.setBackground(BACKGROUND);
		deleteButton = new JButton("Delete Card");
		deleteButton.putClientProperty("JButton.buttonType", "textured");
		deleteButton.setEnabled(false);
		JPanel addPanelMiddle = new JPanel();
		addPanelMiddle.setOpaque(true);
		addPanelMiddle.setBackground(Color.WHITE);
		JPanel addPanelRight = new JPanel();
		addPanelRight.setOpaque(true);
		addPanelRight.setBackground(BACKGROUND);
		question = new JTextField(20);
		answer = new JTextField(20);
		addButton = new JButton("Add Card");
		addButton.putClientProperty("JButton.buttonType", "textured");
		addPanelLeft.add(deleteButton);
		addPanelRight.add(question);
		addPanelRight.add(answer);
		addPanelRight.add(addButton);
		addPanel.add(addPanelLeft);
		addPanel.add(addPanelMiddle);
		addPanel.add(addPanelRight);

		model = new FlashCardTableModel(new CardList()
		{
			public FlashCard getCard(int index) {return null;}
			public FlashCard[] getCards() {return new FlashCard[0];}
			public int numCards() {return 0;}
			public int addCard(String question, String answer) {return 0;}
		});

		answer.addActionListener(model);
		addButton.addActionListener(model);
		deleteButton.addActionListener(model);
		answer.setActionCommand(ADD_CARD);
		addButton.setActionCommand(ADD_CARD);
		deleteButton.setActionCommand(DELETE_CARD);

		mainTable = MacWidgetFactory.createITunesTable(model);
		mainTable.getSelectionModel().addListSelectionListener(model);
		largerTable = new JTable(model);
		largerTable.getSelectionModel().addListSelectionListener(model);
		mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mainScroll = IAppWidgetFactory.createScrollPane(mainTable);
		cardViewer = new FlashCardViewerComponent(model);
	}
	
	private void setupMenuBar()
	{
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMenu tools = new JMenu("Tools");
		JMenu help = new JMenu("Help");
		
		JMenuItem exit = new JMenuItem("Close");
		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DisplayWindow.this.dispose(); //rethink later (System.exit(0)?)
			}
		});
		file.add(exit);
		
		JMenuItem addCat = new JMenuItem("Add Category");
		addCat.addActionListener(addCatAction);
		removeCat = new JMenuItem("Remove Parent Category");
		removeCat.addActionListener(removeCatAction);
		removeCat.setEnabled(false);
		renameList = new JMenuItem("Rename List");
		renameList.addActionListener(renameListAction());
		renameList.setEnabled(false);
		renameCat = new JMenuItem("Rename Parent Category");
		renameCat.addActionListener(renameCatAction());
		renameCat.setEnabled(false);
		edit.add(addCat);
		edit.add(removeCat);
		edit.addSeparator();
		edit.add(renameCat);
		edit.add(renameList);
		
		JMenuItem license = new JMenuItem("License");
		license.addActionListener(showLicenseAction());
		help.add(license);
		
		bar.add(file);
		bar.add(edit);
		bar.add(tools);
		bar.add(help);
		setJMenuBar(bar);
	}

	private JComponent makeSourceList()
	{
		Category[] categories = db.getCategories();
		sourceList = new SourceList();
		SourceListModel model = sourceList.getModel();
		// SourceListModel model = new SourceListModel();

		for (Category c : categories)
		{
			SourceListCategory category = new SourceListCategory(c.getName());
			model.addCategory(category);
			SourceListItem item = new SourceListItem(VIEW_ALL);
			model.addItemToCategory(item, category);
			map.put(item, c);
			List[] lists = c.getLists();
			for (List l : lists)
			{
				item = new SourceListItem(l.getName());
				model.addItemToCategory(item, category);
				map.put(item, l);
			}
		}

		// sourceList = new SourceList(model);
		sourceList.addSourceListSelectionListener(new SourceListSelectionListener()
		{
			public void sourceListItemSelected(SourceListItem item)
			{
				sourceChanged(item);
			}
		});
		JComponent comp = sourceList.getComponent();
		return comp;
	}

	private void sourceChanged(SourceListItem item)
	{
		if (item == null)
		{
			removeCat.setEnabled(false);
			renameList.setEnabled(false);
			renameCat.setEnabled(false);
			nullButton.setSelected(true);
			listViewButton.setEnabled(false);
			largerViewButton.setEnabled(false);
			quizViewButton.setEnabled(false);
			delCatButton.setEnabled(false);
			main.removeAll();
			main.repaint();
			setVisible(true);
		}
		else if (item.getText().equals(VIEW_ALL))
		{
			addButton.setEnabled(false);
			removeCat.setEnabled(true);
			renameList.setEnabled(false);
			renameCat.setEnabled(true);
			listViewButton.setSelected(true);
			listViewButton.setEnabled(true);
			largerViewButton.setEnabled(true);
			quizViewButton.setEnabled(true);
			delCatButton.setEnabled(true);
			CardList l = map.get(item);
			model.cards = l;
			main.removeAll();
			main.add(mainScroll, BorderLayout.CENTER);
			main.add(addPanel, BorderLayout.SOUTH);
			mainTable.clearSelection();
			main.repaint();
			setVisible(true);
		}
		else
		{
			addButton.setEnabled(true);
			removeCat.setEnabled(true);
			renameList.setEnabled(true);
			renameCat.setEnabled(true);
			listViewButton.setSelected(true);
			listViewButton.setEnabled(true);
			largerViewButton.setEnabled(true);
			quizViewButton.setEnabled(true);
			delCatButton.setEnabled(true);
			CardList l = map.get(item);
			model.cards = l;
			main.removeAll();
			main.add(mainScroll, BorderLayout.CENTER);
			main.add(addPanel, BorderLayout.SOUTH);
			mainTable.clearSelection();
			main.repaint();
			setVisible(true);
		}
	}

	private void addWindowListener()
	{
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				db.dispose();
			}
		});
	}

	private ActionListener addListAction()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (sourceList.getSelectedItem() != null)
				{
					java.util.List<SourceListCategory> cats = sourceList.getModel().getCategories();
					SourceListCategory cat = null;
					for (int i = 0; i < cats.size(); i++)
					{
						if (cats.get(i).containsItem(sourceList.getSelectedItem()))
							cat = cats.get(i);
					}
					
					String listName = MacOptionPane.showInputDialog(DisplayWindow.this, ADD_LIST_MESSAGE);
					if (listName != null && !listName.equals(""))
					{
						SourceListItem item = new SourceListItem(listName);
						sourceList.getModel().addItemToCategory(item, cat);
						map.put(item, ((Category)map.get(cat.getItems().get(0))).addList(listName));
					}
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
		};
	}
	
	private ActionListener deleteListAction()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (sourceList.getSelectedItem() != null && !sourceList.getSelectedItem().getText().equals(VIEW_ALL))
				{
					java.util.List<SourceListCategory> cats = sourceList.getModel().getCategories();
					SourceListCategory cat = null;
					for (int i = 0; i < cats.size(); i++)
					{
						if (cats.get(i).containsItem(sourceList.getSelectedItem()))
							cat = cats.get(i);
					}
					
					((List)map.get(sourceList.getSelectedItem())).dispose();
					sourceList.getModel().removeItemFromCategory(sourceList.getSelectedItem(), cat);
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
		};
	}
	
	private ActionListener addCatAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String catName = MacOptionPane.showInputDialog(DisplayWindow.this, ADD_CAT_MESSAGE);
				if (catName != null && !catName.equals(""))
				{
					SourceListCategory category = new SourceListCategory(catName);
					Category c = db.addCategory(catName);
					sourceList.getModel().addCategory(category);
					SourceListItem viewAll = new SourceListItem(VIEW_ALL);
					map.put(viewAll, c);
					sourceList.getModel().addItemToCategory(viewAll, category);
				}
			}
		};
	
	private ActionListener removeCatAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (sourceList.getSelectedItem() != null)
				{
					java.util.List<SourceListCategory> cats = sourceList.getModel().getCategories();
					SourceListCategory cat = null;
					for (int i = 0; i < cats.size(); i++)
					{
						if (cats.get(i).containsItem(sourceList.getSelectedItem()))
							cat = cats.get(i);
					}
					
					int option = (Integer)MacOptionPane.showConfirmDialog(DisplayWindow.this, REMOVE_CAT_MESSAGE + cat.getText() + "?", JOptionPane.YES_NO_OPTION);
					if (option != JOptionPane.YES_OPTION)
						return;
					
					((Category)map.get(cat.getItems().get(0))).dispose();
					sourceList.getModel().removeCategory(cat);
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
		};
		
		private ActionListener viewChanged = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (sourceList.getSelectedItem() != null)
				{
					if (e.getActionCommand().equals(LIST_VIEW))
					{
						mainTable.setRowHeight(16);
//						mainScroll.setViewportView(mainTable);
						sourceChanged(sourceList.getSelectedItem());
					}
					else if (e.getActionCommand().equals(LARGER_VIEW))
					{
						mainTable.setRowHeight(150);
//						mainScroll.setViewportView(largerTable);
						sourceChanged(sourceList.getSelectedItem());
						largerViewButton.setSelected(true);
					}
					else
					{
						cardViewer.reset(mainTable.getSelectedRow());
						main.removeAll();
						main.add(cardViewer, BorderLayout.CENTER);
						DisplayWindow.this.setVisible(true);
					}
					repaint();
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
		};
	
	private ActionListener showLicenseAction()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MacOptionPane.showMessageDialog(DisplayWindow.this, LICENSE_DIALOG_TEXT);
			}
		};
	}
	
	private ActionListener renameCatAction()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (sourceList.getSelectedItem() != null)
				{
					java.util.List<SourceListCategory> cats = sourceList.getModel().getCategories();
					SourceListCategory cat = null;
					for (int i = 0; i < cats.size(); i++)
					{
						if (cats.get(i).containsItem(sourceList.getSelectedItem()))
							cat = cats.get(i);
					}
					
					String s = MacOptionPane.showInputDialog(DisplayWindow.this, RENAME_CAT_MESSAGE, cat.getText());
					if (s != null)
					{
						((Category)map.get(cat.getItems().get(0))).setName(s);
						cat.setText(s);
					}
				}
				else
					Toolkit.getDefaultToolkit().beep();
			}
		};
	}
	
	private ActionListener renameListAction()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String s = MacOptionPane.showInputDialog(DisplayWindow.this, RENAME_LIST_MESSAGE, sourceList.getSelectedItem().getText());
				if (s != null)
				{
					SourceListItem item = sourceList.getSelectedItem();
					((List)map.get(item)).setName(s);
					item.setText(s);
				}
			}
		};
	}

	private void updateBottomLabel()
	{
		bottomLabel.setText(db.getNumCards() + " Cards");
	}

	class FlashCardTableModel extends AbstractTableModel implements ActionListener, ListSelectionListener
	{
		private CardList cards;

		private FlashCardTableModel(CardList cards)
		{
			this.cards = cards;
		}

		public int getColumnCount()
		{
			return COLUMN_COUNT;
		}

		public Class getColumnClass()
		{
			return String.class;
		}

		public int getRowCount()
		{
			return cards.numCards();
		}

		public String getColumnName(int columnIndex)
		{
			if (columnIndex == 0)
				return "Question";
			return "Answer";
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (columnIndex == 0)
			{
				return cards.getCard(rowIndex).getQuestion();
			} else if (columnIndex == 1)
			{
				return cards.getCard(rowIndex).getAnswer();
			}
			return null;
		}

		public boolean isCellEditable(int row, int col)
		{
			return true;
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex)
		{
			if (columnIndex == 0)
			{
				cards.getCard(rowIndex).setQuestion(value.toString());
			} else if (columnIndex == 1)
			{
				cards.getCard(rowIndex).setAnswer(value.toString());
			}
		}

		public void rowAdded()
		{
			this.fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
		}

		public void rowRemoved(int row)
		{
			this.fireTableRowsDeleted(row, row);
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().equals(ADD_CARD) && addButton.isEnabled())
			{
				cards.addCard(question.getText(), answer.getText());
				question.setText("");
				answer.setText("");
				rowAdded();
				updateBottomLabel();
				question.requestFocus();
			} else if (e.getActionCommand().equals(DELETE_CARD))
			{
				int row = mainTable.getSelectedRow();
				cards.getCard(row).dispose();
				rowRemoved(row);
				updateBottomLabel();
			}
		}

		public void valueChanged(ListSelectionEvent e)
		{
			if (mainTable.getSelectedRow() == -1)
				deleteButton.setEnabled(false);
			else
				deleteButton.setEnabled(true);
		}
	}
}

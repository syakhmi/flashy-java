/*Copyright 2010 Sahil Yakhmi

This file is part of FlashY.

FlashY is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

FlashY is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with FlashY. If not, see http://www.gnu.org/licenses/.
*/

package com.yakhmi.swingutil;

import java.awt.Dialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class MacOptionPane
{
	private static final int REPAINT_DELAY = 50;
	
	public static void showMessageDialog(Frame parent, String message)
	{
		final JDialog dia = new JDialog(parent, "test", Dialog.ModalityType.DOCUMENT_MODAL);
		dia.getRootPane().putClientProperty("apple.awt.documentModalSheet", Boolean.TRUE);
		final JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.CLOSED_OPTION);
		pane.setWantsInput(false);
		pane.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();

				if (dia.isVisible() && (e.getSource() == pane) && (prop.equals(JOptionPane.VALUE_PROPERTY)))
				{
					// If you were going to check something
					// before closing the window, you'd do
					// it here.
					dia.dispose();
				}
			}
		});

		dia.setContentPane(pane);
		dia.pack();
		dia.setResizable(false);
		dia.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dia.setVisible(true);
		
		delayedRepaint(parent);
	}
	
	public static Object showConfirmDialog(Frame parent, String message, int optionType)
	{
		final JDialog dia = new JDialog(parent, "test", Dialog.ModalityType.DOCUMENT_MODAL);
		dia.getRootPane().putClientProperty("apple.awt.documentModalSheet", Boolean.TRUE);
		final JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, optionType);
		pane.setWantsInput(false);
		pane.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();

				if (dia.isVisible() && (e.getSource() == pane) && (prop.equals(JOptionPane.VALUE_PROPERTY)))
				{
					// If you were going to check something
					// before closing the window, you'd do
					// it here.
					dia.dispose();
				}
			}
		});

		dia.setContentPane(pane);
		dia.pack();
		dia.setResizable(false);
		dia.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dia.setVisible(true);
		
		delayedRepaint(parent);
		
		return pane.getValue();
	}
	
	public static String showInputDialog(Frame parent, String message)
	{
		return showInputDialog(parent, message, "");
	}
	
	public static String showInputDialog(Frame parent, String message, String oldValue)
	{
		final JDialog dia = new JDialog(parent, "test", Dialog.ModalityType.DOCUMENT_MODAL);
		dia.getRootPane().putClientProperty("apple.awt.documentModalSheet", Boolean.TRUE);
		final JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		pane.setWantsInput(true);
		pane.setInitialSelectionValue(oldValue);
		pane.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				String prop = e.getPropertyName();

				if (dia.isVisible() && (e.getSource() == pane) && (prop.equals(JOptionPane.VALUE_PROPERTY)))
				{
					// If you were going to check something
					// before closing the window, you'd do
					// it here.
					dia.dispose();
				}
			}
		});

		dia.setContentPane(pane);
		dia.pack();
		dia.setResizable(false);
		dia.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dia.setVisible(true);
		
		String value = (String)pane.getInputValue();
		delayedRepaint(parent);
		if (!value.equals("uninitializedValue"))
		{
			return value;
		}
		return null;
	}
	
	private static void delayedRepaint(final Frame frame)
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					Thread.sleep(REPAINT_DELAY);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				frame.repaint();
			}
		};
		thread.start();
	}
}

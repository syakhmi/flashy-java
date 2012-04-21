/*Copyright 2010 Sahil Yakhmi

This file is part of FlashY.

FlashY is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

FlashY is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with FlashY. If not, see http://www.gnu.org/licenses/.
*/

package com.yakhmi.flashcards;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JComponent;

import com.yakhmi.swingutil.MMouseAdaptor;
import com.yakhmi.swingutil.MMouseListener;
import com.yakhmi.util.Math2;

public class FlashCardViewerComponent extends JComponent
{
	private DisplayWindow.FlashCardTableModel tableModel;
	private int row;
	private int column = 0;
	private int[] order;
	
	private boolean overPrev;
	private boolean overNext;
	private boolean overFlip;
	
	private static final int NAV_BUTTON_WIDTH = 100;
	private static final int NAV_BUTTON_HEIGHT = 50;
	private static final int HOVER_DELAY = 100;
	private static final boolean SHUFFLE = true;
	
	public FlashCardViewerComponent(DisplayWindow.FlashCardTableModel tableModel)
	{
		setBackground(new Color(250, 250, 250));
		this.tableModel = tableModel;
		MMouseListener m = getClickListener();
		addMouseListener(m);
		addMouseMotionListener(m);
	}
	
	private Rectangle getNextButtonBounds()
	{
		return new Rectangle(getWidth() - 20 - NAV_BUTTON_WIDTH, getHeight() / 6 - NAV_BUTTON_HEIGHT / 2, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT);
	}
	
	private Rectangle getPreviousButtonBounds()
	{
		return new Rectangle(20, getHeight() / 6 - NAV_BUTTON_HEIGHT / 2, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT);
	}
	
	private Rectangle getFlipButtonBounds()
	{
		return new Rectangle(getWidth() / 2 - NAV_BUTTON_WIDTH / 2, getHeight() - 20 - NAV_BUTTON_HEIGHT, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT);
	}
	
	private MMouseListener getClickListener()
	{
		return new MMouseAdaptor()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (tableModel.getRowCount() == 0)
					return;
				if (getPreviousButtonBounds().contains(e.getPoint()) && row > 0)
				{
					row--;
					column = 0;
					repaint();
				}
				else if (getNextButtonBounds().contains(e.getPoint()) && tableModel.getRowCount() > row + 1)
				{
					row++;
					column = 0;
					repaint();
				}
				else if (getFlipButtonBounds().contains(e.getPoint()) && tableModel.getRowCount() > 0)
				{
					column = Math2.signum(column)* -1 + 1;
					repaint();
				}
			}
			
			public void mouseMoved(MouseEvent e)
			{
				if (tableModel.getRowCount() == 0)
					return;
				if (getPreviousButtonBounds().contains(e.getPoint()))
				{
					overPrev = true;
					repaint();
				}
				else if (getNextButtonBounds().contains(e.getPoint()))
				{
					overNext = true;
					repaint();
				}
				else if (getFlipButtonBounds().contains(e.getPoint()) && tableModel.getRowCount() > 0)
				{
					overFlip = true;
					repaint();
				}
				else
				{
					if (overNext == true || overPrev == true || overFlip == true)
					{
						Thread thread = new Thread()
						{
							public void run()
							{
								try
								{
									Thread.sleep(HOVER_DELAY);
									overPrev = false;
									overNext = false;
									overFlip = false;
									repaint();
								}
								catch (InterruptedException e)
								{
									overPrev = false;
									overNext = false;
									overFlip = false;
									repaint();
								}
							}
						};
						thread.start();
					}
				}
			}
		};
	}
	
	public void reset()
	{
		row = 0;
		order = new int[tableModel.getRowCount()];
		for (int i = 0; i < tableModel.getRowCount(); i++)
			order[i] = i;
		if (SHUFFLE)
			Collections.shuffle(Arrays.asList(order));
	}
	
	public void reset(int row)
	{
		if (row < 0)
			reset();
		else
		{
			this.row = row;
			order = new int[tableModel.getRowCount()];
			for (int i = 0; i < tableModel.getRowCount(); i++)
				order[i] = i;
			Collections.shuffle(Arrays.asList(order));
			
			for (int i = 0; i < tableModel.getRowCount(); i++)
				System.out.println(order[i]);
		}
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(Color.BLACK);
		if (tableModel.getRowCount() != 0)
		{
			g2.setFont(new Font("Tahoma", Font.PLAIN, 20));
			drawString("# " + (row + 1) + " of " + tableModel.getRowCount(), getWidth() / 2, 20, g2);
			g2.setFont(new Font("Tahoma", Font.PLAIN, 80));
			drawString((String)tableModel.getValueAt(order[row], column), getWidth() / 2, getHeight() / 2, g2);
		}
		g2.draw(getPreviousButtonBounds());
		if (overPrev)
			g2.fill(getPreviousButtonBounds());
		g2.draw(getNextButtonBounds());
		if (overNext)
			g2.fill(getNextButtonBounds());
		g2.draw(getFlipButtonBounds());
		if (overFlip)
			g2.fill(getFlipButtonBounds());
	}
	
	/**
	 * Paints a string about a center point
	 * @param s the string to draw
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param g2 the graphics context onto which to draw the string
	 */
	private void drawString(String s, double x, double y, Graphics2D g2)
	{
		//based upon a method described by explodingpixels.com blog
		//i have been using this method in java programs for years
		try
		{
			FontMetrics fontMetrics = g2.getFontMetrics();
			Rectangle bounds = fontMetrics.getStringBounds(s, g2).getBounds(); //get bounds based upon the current font
			Font font = g2.getFont();
			FontRenderContext renderContext = g2.getFontRenderContext();
			GlyphVector glyphVector = font.createGlyphVector(renderContext, s);
			Rectangle actualBounds = glyphVector.getVisualBounds().getBounds(); //adjust for differences in fonts

			double xToDraw = x - bounds.width/2; //calculate coordinates of baseline
			double yToDraw = y - actualBounds.height/2 - actualBounds.y;

			g2.drawString(s, (int)xToDraw, (int)yToDraw); //draw string at adjusted coordinates
		}
		catch (Exception e) //if something went wrong
		{
			g2.drawString(s, (int)x, (int)y); //draw the string at the original wrong, but close to correct, coordinates
		}
	}
}

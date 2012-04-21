/*Copyright 2010 Sahil Yakhmi

This file is part of FlashY.

FlashY is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

FlashY is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with FlashY. If not, see http://www.gnu.org/licenses/.
*/

package com.yakhmi.flashcards;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class List implements CardList
{
	private String name;
	private int id;

	private static PreparedStatement cardCount;
	private static PreparedStatement getCards;
	private static PreparedStatement setName;
	private static PreparedStatement addCard;
	private static PreparedStatement removeList;
	
	public List(int id, String name)
	{
		this.id = id;
		this.name = name;
	}

	private static synchronized int cardCount(int id) throws SQLException
	{
		if (cardCount == null)
			cardCount = Database.db.prepareStatement("SELECT COUNT(*) FROM Cards where listID=?");
		cardCount.setInt(1, id);
		ResultSet rs = cardCount.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		rs.close();
		return count;
	}

	private static synchronized ResultSet getCards(int id) throws SQLException
	{
		if (getCards == null)
			getCards = Database.db.prepareStatement("select * from Cards where listID=?");
		getCards.setInt(1, id);
		return getCards.executeQuery();
	}

	public FlashCard[] getCards()
	{
		FlashCard[] cards = null;
		try
		{
			int count = cardCount(id);
			ResultSet rs = getCards(id);
			cards = new FlashCard[count];
			int index = 0;
			while (rs.next())
			{
				int id = rs.getInt("id");
				cards[index] = new FlashCard(id);
				index++;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return cards;
	}
	
	public int numCards()
	{
		int count = 0;
		try
		{
			count = cardCount(id);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

		return count;
	}
	
	public FlashCard getCard(int index)
	{
		FlashCard card = null;
		try
		{
			ResultSet rs = getCards(id);
			for (int i = 0; i < index; i++)
				rs.next();
			rs.next();
			card = new FlashCard(rs.getInt("id"));
			rs.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return card;
	}
	
	public String getName()
	{
		return name;
	}
	
	private static synchronized void setName(int listID, String newName) throws SQLException
	{
		if (setName == null)
			setName = Database.db.prepareStatement("UPDATE Lists SET name=? WHERE id=?");
		setName.setString(1, newName);
		setName.setInt(2, listID);
		setName.executeUpdate();
	}
	
	public void setName(String newName)
	{
		try
		{
			setName(id, newName);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private static synchronized int addCard(int listID, String question, String answer) throws SQLException
	{
		if (addCard == null)
			addCard = Database.db.prepareStatement("INSERT INTO Cards(listID, question, answer) VALUES(?, ?, ?)");
		addCard.setInt(1, listID);
		addCard.setString(2, question);
		addCard.setString(3, answer);
		int id  = addCard.executeUpdate();
		return id;
	}
	
	public int addCard(String question, String answer)
	{
		try
		{
			addCard(id, question, answer);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	private static synchronized boolean removeList(int id) throws SQLException
	{
		if (removeList == null)
			removeList = Database.db.prepareStatement("DELETE FROM Lists WHERE id=?");
		removeList.setInt(1, id);
		return removeList.execute();
	}
	
	public void dispose()
	{
		try
		{
			removeList(id);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}

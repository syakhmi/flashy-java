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
import java.sql.Statement;


public class Category implements CardList
{
	private int id;
	private String name;
	private static PreparedStatement setName;
	private static PreparedStatement listCount;
	private static PreparedStatement getLists;
	private static PreparedStatement cardCount;
	private static PreparedStatement getCards;
	private static PreparedStatement addList;
	private static PreparedStatement lastKey;
	private static PreparedStatement removeCat;
	
	public Category(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	private static synchronized void setName(int listID, String newName) throws SQLException
	{
		if (setName == null)
			setName = Database.db.prepareStatement("UPDATE Categories SET name=? WHERE id=?");
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
	
	public int getId()
	{
		return id;
	}

	private static synchronized int listCount(int id) throws SQLException
	{
		if (listCount == null)
			listCount = Database.db.prepareStatement("SELECT COUNT(*) FROM Lists where categoryID=?");
		listCount.setInt(1, id);
		ResultSet rs = listCount.executeQuery();

		rs.next();
		int count = rs.getInt(1);
		rs.close();
		return count;
	}

	private static synchronized ResultSet getLists(int id) throws SQLException
	{
		if (getLists == null)
			getLists = Database.db.prepareStatement("select * from Lists where categoryID=?");
		getLists.setInt(1, id);
		return getLists.executeQuery();
	}
	
	public List[] getLists()
	{
		List[] lists = null;
		try
		{
			int count = listCount(id);
			ResultSet rs = getLists(id);
			lists = new List[count];
			int index = 0;
			while (rs.next())
			{
				String name = rs.getString("name");
				int id = rs.getInt("id");
				lists[index] = new List(id, name);
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

		return lists;
	}

	private static synchronized int cardCount(int id) throws SQLException
	{
		if (cardCount == null)
			cardCount = Database.db.prepareStatement("SELECT COUNT(*) FROM Cards, Lists, Categories where Cards.listID=Lists.id and Lists.categoryID=Categories.id and categoryID=?");
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
			getCards = Database.db.prepareStatement("SELECT Cards.id FROM Cards, Lists, Categories where Cards.listID=Lists.id and Lists.categoryID=Categories.id and categoryID=?");
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
	
	private static int lastInsertKey() throws SQLException
	{
		if (lastKey == null)
			lastKey = Database.db.prepareStatement("select last_insert_rowid()");
		ResultSet rs = lastKey.executeQuery();
		rs.next();
		int lastID = rs.getInt(1);
		rs.close();
		return lastID;
	}
	
	private static int addList(int id, String name) throws SQLException
	{
		if (addList == null)
			addList = Database.db.prepareStatement("INSERT into Lists(categoryID, name) VALUES(?, ?)");
		addList.setInt(1, id);
		addList.setString(2, name);
		addList.executeUpdate();
		return lastInsertKey();
	}
	
	public List addList(String name)
	{
		try
		{
			return new List(addList(id, name), name);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public int addCard(String question, String answer)
	{
		throw new UnsupportedOperationException();
	}
	
	private static synchronized boolean removeCat(int id) throws SQLException
	{
		if (removeCat == null)
			removeCat = Database.db.prepareStatement("DELETE FROM Categories WHERE id=?");
		removeCat.setInt(1, id);
		return removeCat.execute();
	}
	
	public void dispose()
	{
		try
		{
			removeCat(id);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}

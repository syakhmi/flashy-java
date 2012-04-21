/*Copyright 2010 Sahil Yakhmi

This file is part of FlashY.

FlashY is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

FlashY is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with FlashY. If not, see http://www.gnu.org/licenses/.
*/

package com.yakhmi.flashcards;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database
{
	public static Connection db;
	private static Statement stat;
	private static String PATH;
	
	private static PreparedStatement addCat;
	private static PreparedStatement lastKey;

	public Database()
	{
		try
		{
			if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1)
				PATH = System.getProperty("user.home") + "/Library/Application Support/FlashY";
			else if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
				PATH = System.getProperty("user.home") + "\\Application Data\\FlashY";
			else
				PATH = System.getProperty("user.home") + ".flashy";
			File f = new File(PATH);
			f.mkdir();
			f = new File(PATH + File.separatorChar + "FlashCards.db3");
			if (f.createNewFile())
				setupDatabase(f);
			else
			{
				try
				{
					Class.forName("org.sqlite.JDBC");
					db = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
				} catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void setupDatabase(File f)
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
			db = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
			stat = db.createStatement();
			Scanner sc = new Scanner(Database.class.getResourceAsStream("/FlashCards.sql")).useDelimiter("\\s*;\\s*");
			while (sc.hasNext())
			{
				String stmt = sc.next();
				if (stmt.equals("END"))
					continue;
				if (stmt.contains("OLD"))
					stmt += "; END;";
				stat.execute(stmt);
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dispose()
	{
		try
		{
			db.createStatement().execute("VACUUM");
			db.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}

	public Category[] getCategories()
	{
		Category[] categories = null;
		try
		{
			if (stat == null)
				stat = db.createStatement();
			ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM Categories");
			rs.next();
			int count = rs.getInt(1);
			rs.close();
			rs = stat.executeQuery("select * from Categories");
			categories = new Category[count];
			int index = 0;
			while (rs.next())
			{
				String name = rs.getString("name");
				int id = rs.getInt("id");
				categories[index] = new Category(id, name);
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

		return categories;
	}
	
	public int getNumCards()
	{
		try
		{
			if (stat == null)
				stat = db.createStatement();
			ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM Cards");
			rs.next();
			int count = rs.getInt(1);
			rs.close();
			return count;
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
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
	
	private static int addCategoryHelper(String name) throws SQLException
	{
		if (addCat == null)
			addCat = Database.db.prepareStatement("INSERT into Categories(name) VALUES(?)");
		addCat.setString(1, name);
		addCat.executeUpdate();
		return lastInsertKey();
	}
	
	public Category addCategory(String name)
	{
		try
		{
			return new Category(addCategoryHelper(name), name);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}

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


public class FlashCard
{
	private int id;
	private static PreparedStatement getQuestion;
	private static PreparedStatement getAnswer;
	private static PreparedStatement setQuestion;
	private static PreparedStatement setAnswer;
	private static PreparedStatement removeCard;
	
	
	public FlashCard(int id)
	{
		this.id = id;
	}

	private static synchronized ResultSet getQuestion(int id) throws SQLException
	{
		if (getQuestion == null)
			getQuestion = Database.db.prepareStatement("select question from Cards where id=?");
		getQuestion.setInt(1, id);
		return getQuestion.executeQuery();
	}

	public String getQuestion()
	{
		String question = null;
		try
		{
			
			ResultSet rs = getQuestion(id);
			rs.next();
			question = rs.getString("question");
			rs.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return question;
	}

	private static synchronized boolean setQuestion(int id, String question) throws SQLException
	{
		if (setQuestion == null)
			setQuestion = Database.db.prepareStatement("update Cards set question=? where id=?");
		setQuestion.setString(1, question);
		setQuestion.setInt(2, id);
		return setQuestion.execute();
	}

	public boolean setQuestion(String question)
	{
		boolean done = false;
		try
		{
			done = setQuestion(id, question);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return done;
	}

	private static synchronized ResultSet getAnswer(int id) throws SQLException
	{
		if (getAnswer == null)
			getAnswer = Database.db.prepareStatement("select answer from Cards where id=?");
		getAnswer.setInt(1, id);
		return getAnswer.executeQuery();
	}

	public String getAnswer()
	{
		String answer = null;
		try
		{
			ResultSet rs = getAnswer(id);
			rs.next();
			answer = rs.getString("answer");
			rs.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return answer;
	}

	private static synchronized boolean setAnswer(int id, String answer) throws SQLException
	{
		if (setAnswer == null)
			setAnswer = Database.db.prepareStatement("update Cards set answer=? where id=?");
		setAnswer.setString(1, answer);
		setAnswer.setInt(2, id);
		return setAnswer.execute();
	}

	public boolean setAnswer(String answer)
	{
		boolean done = false;
		try
		{
			done = setAnswer(id, answer);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return done;
	}
	
	private static synchronized boolean removeCard(int id) throws SQLException
	{
		if (removeCard == null)
			removeCard = Database.db.prepareStatement("DELETE FROM Cards WHERE id=?");
		removeCard.setInt(1, id);
		return removeCard.execute();
	}
	
	public void dispose()
	{
		try
		{
			removeCard(id);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
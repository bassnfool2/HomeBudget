package org.homebudget.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import org.homebudget.HomeBudgetController;

public class Budget {
	private static ArrayList<Budget> budgets = new ArrayList<Budget>();
	
	public static ArrayList<Budget> getBudgets() {
		return budgets;
	}

	public static Budget getBudget(int id) throws Exception {
		for ( Budget budget : budgets) {
			if ( budget.id == id ) return budget;
		}
		throw new Exception("Budget with id: "+id+" not found.");
	}

	int id = HomeBudgetController.NEW_ADD;
	Date date = Date.valueOf(LocalDate.now().plusMonths(1).withDayOfMonth(1));
	ArrayList<Payday> paydays = new ArrayList<Payday>();
	
	public ArrayList<Payday> getPaydays() throws Exception {
		if ( paydays.isEmpty()) {
			paydays = Payday.load(this);
		}
		return paydays;
	}

	public Budget() {
		super();
	}
	public Budget(int id, Date date) {
		this();
		this.id = id;
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getId() {
		return id;
	}

	public static void load() throws SQLException {
		budgets = new ArrayList<Budget>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().createStatement();
			rset = stmt.executeQuery("SELECT id, budgetDate\n"
					+ "FROM budget");
			while ( rset.next()) {
				int id = rset.getInt("id");
				Date date = rset.getDate("budgetDate");
				Budget budget = new Budget(id, date);
				budgets.add(budget);
			}
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
	}
	
	public int save() throws SQLException {
		if ( id == HomeBudgetController.NEW_ADD ) {
			return insert();
		}
		return update();
	}
	
	public int insert() throws SQLException {
		int i = 1;
		final int DATE = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("INSERT INTO budget\n"
					+ "(id, budgetDate)\n"
					+ "VALUES(?)");
			stmt.setDate(DATE, date);
			int updated = stmt.executeUpdate();
			id = Long.valueOf(HomeBudgetController.getDbConnection().createStatement().executeQuery("SELECT last_insert_rowid()").getLong(1)).intValue();
			return updated;
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
	}
	
	public int update() throws SQLException {
		int i = 1;
		final int DATE = i++;
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("UPDATE budget\n"
					+ "SET budgetDate=?\n"
					+ "WHERE id=?");
			stmt.setDate(DATE, date);
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
	
}

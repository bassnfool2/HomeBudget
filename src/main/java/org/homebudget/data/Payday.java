package org.homebudget.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import org.homebudget.HomeBudgetController;

public class Payday {
	int id = HomeBudgetController.NEW_ADD;
	Date date = Date.valueOf(LocalDate.now().plusMonths(1).withDayOfMonth(1));
	Income income = null;
	ArrayList<BudgetItem> budgetItems = new ArrayList<BudgetItem>();
	
	public Payday(int id, Date date, Income income) {
		this.id = id;
		this.date = date;
		this.income = income;
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

	public Income getIncome() {
		return income;
	}

	public ArrayList<BudgetItem> getBudgetItems() throws Exception {
		if ( budgetItems.isEmpty()) {
			budgetItems = BudgetItem.load(this);
		}
		return budgetItems;
	}

	public static ArrayList<Payday> load(Object parent) throws Exception {
		ArrayList<Payday> payDays = new ArrayList<Payday>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().createStatement();
			rset = stmt.executeQuery("SELECT id, payDate, income_id \n"
					+ "FROM payday order by payDate");
			while ( rset.next()) {
				int id = rset.getInt("id");
				Date date = rset.getDate("payDate");
				Income income = Income.getIncome(rset.getInt("income_id"));
				Payday payDay = new Payday(id, date, income);
				payDays.add(payDay);
			}
			return payDays;
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
		final int PAYDATE = i++;
		final int DATE = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("INSERT INTO payDay\n"
					+ "(payDate, income_id)\n"
					+ "VALUES(?,?)");
			stmt.setDate(DATE, date);
			stmt.setInt(i, DATE);
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
		final int INCOME_ID = i++;
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("UPDATE payDay\n"
					+ "SET payDate=?, income_id = ?\n"
					+ "WHERE id=?");
			stmt.setDate(DATE, date);
			stmt.setInt(INCOME_ID, income.getId());
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}

	
}

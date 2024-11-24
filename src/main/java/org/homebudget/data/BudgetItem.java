package org.homebudget.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import org.homebudget.HomeBudgetController;

public class BudgetItem {
	int id = HomeBudgetController.NEW_ADD;
	Payday payday = null;
	Payee payee = null;
	float amount = 0;
	boolean payed = false;
	Date payDate = null;

	public BudgetItem(int id, Payday payday, Payee payee, float amount, boolean payed, Date payDate) {
		super();
		this.id = id;
		this.payday = payday;
		this.payee = payee;
		this.amount = amount;
		this.payed = payed;
		this.payDate = payDate;
	}


	public Payday getPayday() {
		return payday;
	}

	public void setPayday(Payday payday) {
		this.payday = payday;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public boolean isPayed() {
		return payed;
	}

	public void setPayed(boolean payed) {
		this.payed = payed;
	}

	public Date getPayDate() {
		return payDate;
	}

	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}

	public int getId() {
		return id;
	}

	public Payee getPayee() {
		return payee;
	}

	public static ArrayList<BudgetItem> load(Payday payday) throws Exception {
		ArrayList<BudgetItem> budgetItems = new ArrayList<BudgetItem>();
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("SELECT id, payday_id, payee_id, amount, payed, payDate\n"
					+ "FROM budgetItem where payday_id = ?");
			stmt.setInt(1, payday.getId());
			rset = stmt.executeQuery();
			while ( rset.next()) {
				int id = rset.getInt("id");
				Payee payee = Payee.getPayee(rset.getInt("payee_id"));
				float amount = rset.getFloat("amount");
				boolean payed = rset.getBoolean("payed");
				Date payDate = rset.getDate("payDate");
				BudgetItem budgetItem = new BudgetItem(id, payday, payee, amount, payed, payDate);
				budgetItems.add(budgetItem);
			}
			return budgetItems;
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
		final int PAYDAY_ID = i++;
		final int PAYEE_ID = i++;
		final int AMOUNT = i++;
		final int PAYED = i++;
		final int PAYDATE = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("INSERT INTO budgetItem\n"
					+ "(payday_id, payee_id, amount, payed, payDate)\n"
					+ "VALUES(?, ?, ?, ?, ?)");
			stmt.setInt(PAYDAY_ID, payday.getId());
			stmt.setInt(PAYEE_ID, payee.getId());
			stmt.setFloat(AMOUNT, amount);
			stmt.setBoolean(PAYED, payed);
			stmt.setDate(PAYDATE, payDate);
			int updated = stmt.executeUpdate();
			id = Long.valueOf(HomeBudgetController.getDbConnection().createStatement().executeQuery("SELECT last_insert_rowid()").getLong(1)).intValue();
			return updated;
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
	}
	
	public int update() throws SQLException {
		int i = 1;
		final int PAYDAY_ID = i++;
		final int PAYEE_ID = i++;
		final int AMOUNT = i++;
		final int PAYED = i++;
		final int PAYDATE = i++;
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("UPDATE budgetItem\n"
					+ "SET payday_id=?, payee_id=?, amount=?, payed=?, payDate=?\n"
					+ "WHERE id=?");
			stmt.setInt(PAYDAY_ID, payday.getId());
			stmt.setInt(PAYEE_ID, payee.getId());
			stmt.setFloat(AMOUNT, amount);
			stmt.setBoolean(PAYED, payed);
			stmt.setDate(PAYDATE, payDate);
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
	
}

package org.homebudget.data;

/*
 * Copyright (C) 2024 Gerry Hobbs
 * bassnfool2@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.homebudget.HomeBudgetController;
import org.homebudget.db.DbUtils;

public class BudgetItem {
	int id = HomeBudgetController.NEW_ADD;
	Payday payday = null;
	Payee payee = null;
	double amount = 0;
	boolean payed = false;
	Date payDate = null;
	boolean prevPaid = false;

	public BudgetItem(int id, Payday payday, Payee payee, double amount, boolean payed, Date payDate) {
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public boolean isPayed() {
		return payed;
	}

	public void setPayed(boolean payed) {
		this.payed = payed;
		if ( payed && !prevPaid && this.getPayee().getBalance() != 0) {
			this.getPayee().setBalance(this.getPayee().getBalance() - amount); 
		}
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
					+ "FROM \"budgetItem\" where payday_id = ?");
			stmt.setInt(1, payday.getId());
			rset = stmt.executeQuery();
			while ( rset.next()) {
				int id = rset.getInt("id");
				Payee payee = Payee.getPayee(rset.getInt("payee_id"));
				double amount = rset.getDouble("amount");
				boolean payed = rset.getBoolean("payed");
				Date payDate = rset.getDate("payDate");
				BudgetItem budgetItem = new BudgetItem(id, payday, payee, amount, payed, payDate);
				budgetItem.prevPaid = payed;
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
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("INSERT INTO \"budgetItem\"\n"
					+ "(payday_id, payee_id, amount, payed, payDate)\n"
					+ "VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(PAYDAY_ID, payday.getId());
			stmt.setInt(PAYEE_ID, payee.getId());
			stmt.setDouble(AMOUNT, amount);
			stmt.setBoolean(PAYED, payed);
			stmt.setDate(PAYDATE, payDate);
			int updated = stmt.executeUpdate();
			id = DbUtils.getLastGeneratedId(stmt);
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
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("UPDATE \"budgetItem\"\n"
					+ "SET payday_id=?, payee_id=?, amount=?, payed=?, payDate=?\n"
					+ "WHERE id=?");
			stmt.setInt(PAYDAY_ID, payday.getId());
			stmt.setInt(PAYEE_ID, payee.getId());
			stmt.setDouble(AMOUNT, amount);
			stmt.setBoolean(PAYED, payed);
			stmt.setDate(PAYDATE, payDate);
			stmt.setInt(ID, id);
			int updated = stmt.executeUpdate();
			if ( updated == 0 ) 
				throw new SQLException("No budget item updated!");
			return updated;
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
	
}

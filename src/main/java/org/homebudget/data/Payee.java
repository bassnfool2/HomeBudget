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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.homebudget.HomeBudgetController;
import org.homebudget.PayeeAddedListener;
import org.homebudget.db.DbUtils;

public class Payee {
	private static ArrayList<Payee> payees = null;
	
	public static ArrayList<Payee> getPayees() {
		return payees;
	}

	public static Payee getPayee(int id) throws Exception {
		for ( Payee payee : payees) {
			if ( payee.id == id ) return payee;
		}
		throw new Exception("Payee with id: "+id+" not found.");
	}

	static List<PayeeAddedListener> payeeAddedListeners = new ArrayList<PayeeAddedListener>();
	
	public static void addPayeeAddedListener(PayeeAddedListener listener) {
		payeeAddedListeners.add(listener);
	}
	
	private static void add(Payee payee) {
		payees.add(payee);
		for ( PayeeAddedListener listener : payeeAddedListeners) {
			listener.newPayeeAdded(payee);
		}
	}

	final static int DEFAULT_DAY_DUE = 1;
	final static double DEFAULT_AMOUNT_DUE = 0;
	final static double DEFAULT_BALANCE = 0;
	int id = HomeBudgetController.NEW_ADD;
	String name = "";
	String url = "";
	String username = "";
	String password = "";
	PayonEnum dueOn = PayonEnum.fromInt(DEFAULT_DAY_DUE);
	double defaultPaymentAmount = Payee.DEFAULT_AMOUNT_DUE;
	double balance = Payee.DEFAULT_BALANCE;
	FundSource paywithFundSource = null;
	
	public Payee() {
		super();
	}
	public Payee(int id, String name, String url, String username, String password, PayonEnum dueOn, double defaultPaymentAmount, double balance, FundSource fundSource) {
		this();
		this.id = id;
		this.name = name;
		this.url = url;
		this.username = username;
		this.password = password;
		this.dueOn = dueOn;
		this.defaultPaymentAmount = defaultPaymentAmount;
		this.balance = balance;
		this.paywithFundSource = fundSource;
	}

	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public FundSource getPaywithFundSource() {
		return paywithFundSource;
	}

	public void setPaywithFundSource(FundSource paywithFundSource) {
		this.paywithFundSource = paywithFundSource;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public PayonEnum getDueOn() {
		return dueOn;
	}
	public void setDueOn(PayonEnum dueOn) {
		this.dueOn = dueOn;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public Double getDefaultPaymentAmount() {
		return defaultPaymentAmount;
	}

	public void setDefaultPaymentAmount(double defaultPaymentAmount) {
		this.defaultPaymentAmount = defaultPaymentAmount;
	}

	public String toString() {
		return name;
	}
	public static void load() throws Exception {
		payees = new ArrayList<Payee>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().createStatement();
			rset = stmt.executeQuery("select id, name, url, username, password, due_on, budgetedPayment, balance, income_id from \"payee\" order by name");
			while ( rset.next()) {
				int id = rset.getInt("id");
				String name = rset.getString("name");
				String url = rset.getString("url");
				String username = rset.getString("username");
				String password = rset.getString("password");
				Integer dueOn = rset.getInt("due_on");
				Double budgetedPayment = rset.getDouble("budgetedPayment");
				Double balance = rset.getDouble("balance");
				FundSource paywithFundSource = rset.getInt("income_id") == 0 ? null : FundSource.getFundSource(rset.getInt("income_id"));
				Payee payee = new Payee(id, name, url, username, password, PayonEnum.fromInt(dueOn), budgetedPayment, balance, paywithFundSource);
				payees.add(payee);
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
		final int NAME = i++;
		final int URL = i++;
		final int USERNAME = i++;
		final int PASSWORD = i++;
		final int DUE_ON = i++;
		final int BUDGETED_PAYMENT = i++;
		final int BALANCE = i++;
		final int INCOME_ID = i++;
		PreparedStatement stmt = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("insert into \"payee\" (name, url, username, password, due_on, budgetedPayment, balance, income_id) values (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(NAME, name);
			stmt.setString(URL, url);
			stmt.setString(USERNAME, username);
			stmt.setString(PASSWORD, password);
			stmt.setInt(DUE_ON, dueOn.getValue());
			stmt.setDouble(BUDGETED_PAYMENT, defaultPaymentAmount);
			stmt.setDouble(BALANCE, balance);
			stmt.setInt(INCOME_ID, paywithFundSource.getId());
			int updated = stmt.executeUpdate();
			id = DbUtils.getLastGeneratedId(stmt);
			Payee.add(this);
			return updated;
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
	}
	
	public int update() throws SQLException {
		int i = 1;
		final int NAME = i++;
		final int URL = i++;
		final int USERNAME = i++;
		final int PASSWORD = i++;
		final int DUE_ON = i++;
		final int BUDGETED_PAYMENT = i++;
		final int BALANCE = i++;
		final int INCOME_ID = i++;
		final int ID = i++;
		PreparedStatement stmt = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("update \"payee\" set name = ?, url = ?, username=?, password=?, due_on=?, budgetedPayment=?, balance=?, income_id=? where id = ?");
			stmt.setString(NAME, name);
			stmt.setString(URL, url);
			stmt.setString(USERNAME, username);
			stmt.setString(PASSWORD, password);
			stmt.setInt(DUE_ON, dueOn.getValue());
			stmt.setDouble(BUDGETED_PAYMENT, defaultPaymentAmount);
			stmt.setDouble(BALANCE, balance);
			stmt.setInt(INCOME_ID, paywithFundSource.getId());
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
}

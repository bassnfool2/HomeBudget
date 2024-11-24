package org.homebudget.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.homebudget.HomeBudgetController;

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

	final static int DEFAULT_DAY_DUE = 1;
	final static float DEFAULT_AMOUNT_DUE = 0;
	final static float DEFAULT_BALANCE = 0;
	int id = HomeBudgetController.NEW_ADD;
	String name = "";
	String url = "";
	String username = "";
	String password = "";
	int dueOn = Payee.DEFAULT_DAY_DUE;
	float defaultPaymentAmount = Payee.DEFAULT_AMOUNT_DUE;
	float balance = Payee.DEFAULT_BALANCE;
	
	public Payee() {
		super();
	}
	public Payee(int id, String name, String url, String username, String password, int dueOn, float defaultPaymentAmount, float balance) {
		this();
		this.id = id;
		this.name = name;
		this.url = url;
		this.username = username;
		this.password = password;
		this.dueOn = dueOn;
		this.defaultPaymentAmount = defaultPaymentAmount;
		this.balance = balance;
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
	public Integer getDueOn() {
		return dueOn;
	}
	public void setDueOn(int dueOn) {
		this.dueOn = dueOn;
	}
	public Float getBalance() {
		return balance;
	}
	public void setBalance(float balance) {
		this.balance = balance;
	}
	
	public Float getDefaultPaymentAmount() {
		return defaultPaymentAmount;
	}

	public void setDefaultPaymentAmount(float defaultPaymentAmount) {
		this.defaultPaymentAmount = defaultPaymentAmount;
	}

	public static void load() throws SQLException {
		payees = new ArrayList<Payee>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().createStatement();
			rset = stmt.executeQuery("select id, name, url, username, password, due_on, budgetedPayment, balance from payee order by name");
			while ( rset.next()) {
				int id = rset.getInt("id");
				String name = rset.getString("name");
				String url = rset.getString("url");
				String username = rset.getString("username");
				String password = rset.getString("password");
				Integer dueOn = rset.getInt("due_on");
				Float budgetedPayment = rset.getFloat("budgetedPayment");
				Float balance = rset.getFloat("balance");
				Payee payee = new Payee(id, name, url, username, password, dueOn, budgetedPayment, balance);
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
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("insert into payee (name, url, username, password, due_on, budgetedPayment, balance) values (?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(NAME, name);
			stmt.setString(URL, url);
			stmt.setString(USERNAME, username);
			stmt.setString(PASSWORD, password);
			stmt.setInt(DUE_ON, dueOn);
			stmt.setFloat(BUDGETED_PAYMENT, defaultPaymentAmount);
			stmt.setFloat(BALANCE, balance);
			int updated = stmt.executeUpdate();
			id = Long.valueOf(HomeBudgetController.getDbConnection().createStatement().executeQuery("SELECT last_insert_rowid()").getLong(1)).intValue();
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
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("update payee set name = ?, url = ?, username=?, password=?, due_on=?, budgetedPayment=?, balance=? where id = ?");
			stmt.setString(NAME, name);
			stmt.setString(URL, url);
			stmt.setString(USERNAME, username);
			stmt.setString(PASSWORD, password);
			stmt.setInt(DUE_ON, dueOn);
			stmt.setFloat(BUDGETED_PAYMENT, defaultPaymentAmount);
			stmt.setFloat(BALANCE, balance);
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
	
}

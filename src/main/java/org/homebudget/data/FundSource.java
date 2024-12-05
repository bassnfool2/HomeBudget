package org.homebudget.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.homebudget.HomeBudgetController;
import org.homebudget.IncomeAddedListener;
import org.homebudget.PayeeAddedListener;
import org.homebudget.db.DbUtils;

public class FundSource {
	private static ArrayList<FundSource> incomes = null;
	
	public static ArrayList<FundSource> getFundSources() {
		return incomes;
	}

	public static FundSource getFundSource(int id) throws Exception {
		for ( FundSource income : incomes) {
			if ( income.id == id ) return income;
		}
		throw new Exception("Income with id: "+id+" not found.");
	}

	static List<IncomeAddedListener> incomeAddedListeners = new ArrayList<IncomeAddedListener>();
	
	public static void addFunAddedListener(IncomeAddedListener listener) {
		incomeAddedListeners.add(listener);
	}
	
	private static void add(FundSource fundSource) {
		incomes.add(fundSource);
		for ( IncomeAddedListener listener : incomeAddedListeners) {
			listener.newFundingSourceAdded(fundSource);
		}
	}

	final static double DEFAULT_AMOUNT = 0;
	int id = HomeBudgetController.NEW_ADD;
	String name = "";
	PayFrequency payFrequency = PayFrequency.MONTHLY;
	Date firstPayDate = new Date(Calendar.getInstance().getTimeInMillis());
	double defaultPayAmount = FundSource.DEFAULT_AMOUNT;
	
	public FundSource() {
		super();
	}
	public FundSource(int id, String name, PayFrequency payFrequency, double defaultPayAmount, Date payDate) {
		this();
		this.id = id;
		this.name = name;
		this.defaultPayAmount = defaultPayAmount;
		this.payFrequency = payFrequency;
		setFirstPayDate(payDate);
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
	public PayFrequency getPayFrequency() {
		return payFrequency;
	}
	public void setPayFrequency(PayFrequency payFrequency) {
		this.payFrequency = payFrequency;
	}

	public Date getFirstPayDate() {
		return firstPayDate;
	}
	public void setFirstPayDate(Date nextPayDate) {
		this.firstPayDate = nextPayDate == null ? new Date(Calendar.getInstance().getTimeInMillis()) : nextPayDate;
	}
	public Double getBudgetedPay() {
		return defaultPayAmount;
	}
	public void setDefaultPayAmount(Double defaultPayAmount) {
		this.defaultPayAmount = defaultPayAmount;
	}
	public static void setIncomes(ArrayList<FundSource> incomes) {
		incomes = incomes;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public static void load() throws SQLException {
		incomes = new ArrayList<FundSource>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().createStatement();
			rset = stmt.executeQuery("SELECT id, name, budgetedPay, payFrequency, nextPayDate\n"
					+ "FROM \"income\"");
			while ( rset.next()) {
				int id = rset.getInt("id");
				String name = rset.getString("name");
				Double budgetedPay = rset.getDouble("budgetedPay");
				String payFrequencyS = rset.getString("payFrequency");
				Date nextPayDate = rset.getDate("nextPayDate");
				FundSource Income = new FundSource(id, name, PayFrequency.valueOf(payFrequencyS), budgetedPay, nextPayDate);
				incomes.add(Income);
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
		final int BUDGETED_PAY = i++;
		final int PAY_FREQUENCY = i++;
		final int NEXT_PAY_DATE = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("INSERT INTO \"income\"\n"
					+ "(name, budgetedPay, payFrequency, nextPayDate)\n"
					+ "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(NAME, name);
			stmt.setDouble(BUDGETED_PAY, getBudgetedPay());
			stmt.setString(PAY_FREQUENCY, getPayFrequency().name());
			stmt.setDate(NEXT_PAY_DATE, firstPayDate);
			int inserted = stmt.executeUpdate();
			id = DbUtils.getLastGeneratedId(stmt);
			FundSource.add(this);
			return inserted;
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
	}
	
	public int update() throws SQLException {
		int i = 1;
		final int NAME = i++;
		final int BUDGETED_PAY = i++;
		final int PAY_FREQUENCY = i++;
		final int NEXT_PAY_DATE = i++;
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("UPDATE \"income\"\n"
					+ "SET name=?, budgetedPay=?, payFrequency=?, nextPayDate=?\n"
					+ "WHERE id=?");
			stmt.setString(NAME, name);
			stmt.setDouble(BUDGETED_PAY, getBudgetedPay());
			stmt.setString(PAY_FREQUENCY, getPayFrequency().name());
			stmt.setDate(NEXT_PAY_DATE, firstPayDate);
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
	
	public enum PayFrequency {
	    EVERY_WEEK {
	        @Override
	        public String toString() {
	            return "Every week";
	        }
	    },
	    EVERY_TWO_WEEKS {
	        @Override
	        public String toString() {
	            return "Every two weeks";
	        }
	    },
	    MONTHLY {
	        @Override
	        public String toString() {
	            return "Monthly";
	        }
	    },
	    TWICE_MONTHLY {
	        @Override
	        public String toString() {
	            return "Twice Monthly";
	        }
	    };

	    public abstract String toString();
	}
	
	public String toString() {
		return name;
	}
}

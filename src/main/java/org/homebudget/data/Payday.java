package org.homebudget.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import org.homebudget.HomeBudgetController;
import org.homebudget.data.FundSource.PayFrequency;

public class Payday {
	int id = HomeBudgetController.NEW_ADD;
	Date date = Date.valueOf(LocalDate.now().plusMonths(1).withDayOfMonth(1));
	FundSource income = null;
	double amount = 0;
	Budget budget = null;
	ArrayList<BudgetItem> budgetItems = new ArrayList<BudgetItem>();
	
	public Payday(int id, Budget budget, Date date, FundSource income, double amount) {
		this.id = id;
		this.date = date;
		this.income = income;
		this.amount = id == HomeBudgetController.NEW_ADD ? income.defaultPayAmount : amount; 
		this.budget = budget;
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

	public FundSource getIncome() {
		return income;
	}

	public ArrayList<BudgetItem> getBudgetItems() throws Exception {
		if ( budgetItems.isEmpty()) {
			budgetItems = BudgetItem.load(this);
		}
		return budgetItems;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public static ArrayList<Payday> load(Budget budget) throws Exception {
		Date endDate = Date.valueOf(budget.getDate().toLocalDate().plusMonths(1));
		ArrayList<Payday> payDays = new ArrayList<Payday>();
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("SELECT id, payDate, income_id, amount \n"
					+ "FROM payday where payDate >= ? and payDate < ? order by payDate");
			stmt.setDate(1, budget.getDate());
			stmt.setDate(2, endDate);
			rset = stmt.executeQuery();
			while ( rset.next()) {
				int id = rset.getInt("id");
				Date date = rset.getDate("payDate");
				FundSource income = FundSource.getFundSource(rset.getInt("income_id"));
				double amount = rset.getDouble("amount");
				Payday payDay = new Payday(id, budget, date, income, amount);
				payDays.add(payDay);
			}
			return payDays;
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
	}
	
	public int save() throws Exception {
		for ( BudgetItem budgetItem : getBudgetItems()) {
			budgetItem.save();
		}
		if ( id == HomeBudgetController.NEW_ADD ) {
			return insert();
		}
		return update();
	}
	
	public int insert() throws SQLException {
		int i = 1;
		final int PAYDATE = i++;
		final int INCOME_ID = i++;
		final int BUDGET_ID = i++;
		final int AMOUNT = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("INSERT INTO payDay\n"
					+ "(payDate, income_id, budget_id, amount)\n"
					+ "VALUES(?, ?, ?, ?)");
			stmt.setDate(PAYDATE, date);
			stmt.setInt(INCOME_ID, income.getId());
			stmt.setInt(BUDGET_ID, budget.getId());
			stmt.setDouble(AMOUNT, amount);
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
		final int AMOUNT = i++;
		final int ID = i++;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = HomeBudgetController.getDbConnection().prepareStatement("UPDATE payDay\n"
					+ "SET payDate=?, income_id = ?, amount = ?\n"
					+ "WHERE id=?");
			stmt.setDate(DATE, date);
			stmt.setInt(INCOME_ID, income.getId());
			stmt.setDouble(AMOUNT, amount);
			stmt.setInt(ID, id);
			return stmt.executeUpdate();
			
		} finally {
			if ( stmt != null ) try { stmt.close();} catch (Exception e) {};
		}
		
	}
	
	public Payday getNextPayday() throws Exception {
		Date paydate = null;
		Payday payday = null;
		switch (income.payFrequency) {
		case EVERY_TWO_WEEKS:
			paydate = Date.valueOf(date.toLocalDate().plusWeeks(2));
			break;
		case MONTHLY:
			paydate = Date.valueOf(date.toLocalDate().plusMonths(1));
			break;
		case TWICE_MONTHLY:
		default:
			throw new Exception("not yet implemented!");			
		}
		payday = new Payday(HomeBudgetController.NEW_ADD, null, paydate, income, income.getBudgetedPay());
		return payday;
	}

	public BudgetItem getBudgetItem(Payee payee) throws Exception {
		for ( BudgetItem budgetItem : getBudgetItems() ) {
			if ( budgetItem.getPayee() == payee ) {
				return budgetItem;
			}
		}
		throw new Exception("Payee not found!");
	}

	public void addBudgetItem(BudgetItem budgetItem) {
		budgetItems.add(budgetItem);
	}
	
}

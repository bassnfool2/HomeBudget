package org.homebudget.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;

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
					+ "FROM budget order by budgetDate desc");
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
					+ "(budgetDate)\n"
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
	
	public static Budget createNextBudget(Date date) throws Exception {
		Budget budget = null;
		budget = new Budget(HomeBudgetController.NEW_ADD, date);
		budget.save();
		for ( FundSource fundSource : FundSource.getFundSources()) {
			switch (fundSource.getPayFrequency()) {
			case EVERY_TWO_WEEKS: 
				getNextFundDrop2WeekInterval(fundSource, budget);
				break;
			case MONTHLY:
				getNextFundDropMonthlyInterval(fundSource, budget);
				break;
			case TWICE_MONTHLY:
				getNextFundDropTwiceMonthlyInterval(fundSource, budget);
				break;
			}
		}
		for ( Payee payee : Payee.getPayees()) {
			LocalDate payDueDate = budget.getDate().toLocalDate().plusDays(payee.getDueOn()-1);
			LocalDate nextPayDueDate = budget.getDate().toLocalDate().plusMonths(1).plusDays(payee.getDueOn()-1);
			for ( int i = budget.getPaydays().size()-1; i>= 0; i--) {
				Payday payday = budget.getPaydays().get(i);
				if ( payday.getIncome().equals(payee.getPaywithFundSource())) {
					if ( payday.getDate().before(Date.valueOf(payDueDate)) || payday.getDate().equals(Date.valueOf(payDueDate))) {
						BudgetItem budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, payday, payee, payee.getDefaultPaymentAmount(), false, null);
						budgetItem.save();
						break;
					}

				}
			}
			for ( int i = budget.getPaydays().size()-1; i>= 0; i--) {
				Payday payday = budget.getPaydays().get(i);
				if ( payday.getIncome().equals(payee.getPaywithFundSource())) {
					if ( payday.getNextPayday().getDate().after(Date.valueOf(nextPayDueDate))) {
						BudgetItem budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, payday, payee, payee.getDefaultPaymentAmount(), false, null);
						budgetItem.save();
						break;
					}
				}

			}
		}
		addPaymentsDueNextMonthWhichNeedToBePaidThisMonth(budget);

		return budget;
	}
	
	private static void addPaymentsDueNextMonthWhichNeedToBePaidThisMonth(Budget currentBudget) throws Exception {
		Budget nextBudget = new Budget(HomeBudgetController.NEW_ADD, Date.valueOf(currentBudget.getDate().toLocalDate().plusMonths(1)));
		for ( Payee payee : Payee.getPayees()) {
			LocalDate payDueDate = nextBudget.getDate().toLocalDate().plusDays(payee.getDueOn()-1);
			for ( int i = nextBudget.getPaydays().size()-1; i>= 0; i--) {
				Payday payday = nextBudget.getPaydays().get(i);
				if ( payday.getIncome().equals(payee.getPaywithFundSource())) {
					if ( payday.getDate().after(Date.valueOf(payDueDate))) {
						BudgetItem budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, payday, payee, payee.getDefaultPaymentAmount(), false, null);
						budgetItem.save();
						break;
					} else {
						break;
					}
				}
			}
		}
	}

	private static void getNextFundDropTwiceMonthlyInterval(FundSource fundSource, Budget budget) throws SQLException {
	}

	private static void getNextFundDropMonthlyInterval(FundSource fundSource, Budget budget) throws SQLException {
		while ( fundSource.getNextPayDate().before(budget.getDate()))  {
			fundSource.setNextPayDate(Date.valueOf(fundSource.getNextPayDate().toLocalDate().plusWeeks(2)));
		}
		Payday payday = new Payday(HomeBudgetController.NEW_ADD, budget, fundSource.getNextPayDate(), fundSource);
		payday.save();		
	}

	private static void getNextFundDrop2WeekInterval(FundSource fundSource, Budget budget) throws SQLException {
		while ( fundSource.getNextPayDate().before(budget.getDate()))  {
			fundSource.setNextPayDate(Date.valueOf(fundSource.getNextPayDate().toLocalDate().plusWeeks(2)));
		}
		while ( fundSource.getNextPayDate().before(Date.valueOf(budget.getDate().toLocalDate().plusMonths(1))))  {
			Payday payday = new Payday(HomeBudgetController.NEW_ADD, budget, fundSource.getNextPayDate(), fundSource);
			payday.save();
			fundSource.setNextPayDate(Date.valueOf(fundSource.getNextPayDate().toLocalDate().plusWeeks(2)));
		}
	}
	
	public static LocalDate getStartOfNextMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
    }
	
	public static Budget getBudgetByDate(Date targetDate) {
		for ( Budget budget : getBudgets() ) {
			if (( targetDate.equals(budget.date) || targetDate.after(budget.date)) && (targetDate.before(Date.valueOf(budget.date.toLocalDate().plusMonths(1))))) {
				return budget;
			} 
		}
		return null;
	}
}

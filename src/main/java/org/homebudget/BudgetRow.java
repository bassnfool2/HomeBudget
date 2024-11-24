package org.homebudget;

import java.util.ArrayList;

import org.homebudget.data.BudgetItem;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;

public class BudgetRow {
	Payee payee = null;
	
	BudgetItem[] budgetItems = new BudgetItem[6];
	
	public BudgetRow (Payee payee, ArrayList<Payday> paydays) {
		this.payee = payee;
		for ( int i = 0; i<budgetItems.length; i++) {
			if ( i < paydays.size() ) {
				budgetItems[i] = new BudgetItem(HomeBudgetController.NEW_ADD, paydays.get(i), payee, 0, false, null);
			} else {
				budgetItems[i] = new BudgetItem(HomeBudgetController.NEW_ADD, null, payee, 0, false, null);
			}
		}
	}
	public Payee grabPayee() {
		return payee;
	}
	public String getPayee() {
		return payee.getName();
	}
	public String getBudgetItem0() {
		return Float.toString(budgetItems[0].getAmount());
	}
	public String getBudgetItem1() {
		return Float.toString(budgetItems[1].getAmount());
	}
	public String getBudgetItem2() {
		return Float.toString(budgetItems[2].getAmount());
	}
	public String getBudgetItem3() {
		return Float.toString(budgetItems[3].getAmount());
	}
	public String getBudgetItem4() {
		return Float.toString(budgetItems[5].getAmount());
	}
	public String getBudgetItem5() {
		return Float.toString(budgetItems[5].getAmount());
	}
	public BudgetItem[] grabBudgetItems() {
		return budgetItems;
	}
	
	public BudgetItem grabBudgetItemAt(int index) {
		return budgetItems[index];
	}

	public void replaceBudgetItemAt(int index, BudgetItem budgetItem) {
		budgetItem.setAmount(150);
		budgetItems[index] = budgetItem;
	}
}

package org.homebudget;

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
		return Double.toString(budgetItems[0].getAmount());
	}
	public String getBudgetItem1() {
		return Double.toString(budgetItems[1].getAmount());
	}
	public String getBudgetItem2() {
		return Double.toString(budgetItems[2].getAmount());
	}
	public String getBudgetItem3() {
		return Double.toString(budgetItems[3].getAmount());
	}
	public String getBudgetItem4() {
		return Double.toString(budgetItems[5].getAmount());
	}
	public String getBudgetItem5() {
		return Double.toString(budgetItems[5].getAmount());
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

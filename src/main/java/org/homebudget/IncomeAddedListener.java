package org.homebudget;

import org.homebudget.data.FundSource;

public interface IncomeAddedListener {
	public void newFundingSourceAdded(FundSource fundSource);
}

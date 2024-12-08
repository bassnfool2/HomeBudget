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


import java.io.IOException;
import java.net.URL;
import java.sql.Date;

import org.homebudget.data.Budget;
import org.homebudget.data.FundSource;
import org.homebudget.data.Payee;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class WelcomeController  extends VBox  {
	HomeBudgetController homeBudgetController = null;
	public WelcomeController(HomeBudgetController homeBudgetController) {
		this();
		this.homeBudgetController = homeBudgetController;
	}
	
	public WelcomeController() {
		URL fxmlLocation = WelcomeController.class.getResource("Welcome.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		fxmlLoader.setClassLoader(getClass().getClassLoader());

		try {
			fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public void createBudget() {
		if ( FundSource.getFundSources().isEmpty()) {
			System.out.println("No funding sources");
		} else if ( Payee.getPayees().isEmpty()) {
			System.out.println("No Payees");			
		} else {
			try {
				Budget budget = Budget.createNextBudget(Date.valueOf(Budget.getStartOfNextMonth()));
				homeBudgetController.setCurrentBudget(budget);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
}

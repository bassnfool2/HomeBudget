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


import java.awt.Button;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.homebudget.data.Budget;
import org.homebudget.data.BudgetItem;
import org.homebudget.data.FundSource;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;
import org.homebudget.data.PayonEnum;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
			// TODO Auto-generated catch block
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

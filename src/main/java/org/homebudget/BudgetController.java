package org.homebudget;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.homebudget.data.Budget;
import org.homebudget.data.BudgetItem;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class BudgetController  extends VBox  {
    @FXML private GridPane grid;
	Budget budget = null;
	HashMap<Payee, Integer> payeeToRow = new HashMap<Payee, Integer>();
	HashMap<Payday, Integer> paydayToColumn = new HashMap<Payday, Integer>();
	
	public BudgetController() {
		URL fxmlLocation = HomeBudgetController.class.getResource("Budget.fxml");
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
	
	public void setBudget(Budget budget) throws Exception {
		this.budget = budget;
		int paydayCount = budget.getPaydays().size();
//		this.setWidth(paydayCount*150);
		grid.setMaxWidth((paydayCount+1)*150);
//		grid.setPrefWidth(paydayCount*150);
//		grid.setMinWidth(paydayCount*150);
		System.out.println("Budget date:"+budget.getDate());
		int payeeindex = 0;
		TextField[][] gridTextFields = new TextField[paydayCount+1][Payee.getPayees().size()];
		for ( Payee payee : Payee.getPayees()) {
			payeeToRow.put(payee, payeeindex);
			Label label = new Label(payee.getName());
			grid.add(label, 0, payeeindex);
			for ( int paydayCounter = 1; paydayCounter <= paydayCount; paydayCounter++) {
				TextField textField = new TextField("");
				textField.setMinWidth(150);
				gridTextFields[paydayCounter][payeeindex] = textField;
				grid.add(textField, paydayCounter, payeeindex);				
			}
			payeeindex++;
		}
		int paydayindex = 1;
		for ( Payday payday : budget.getPaydays()) {
			paydayToColumn.put(payday, paydayindex);
			for ( BudgetItem budgetItem : payday.getBudgetItems()) {
				TextField textField = gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())];
				textField.setUserData(budgetItem);
				textField.setText(budgetItem.getAmount().toString());
			}
			paydayindex++;
		}
	}
	
	public void saveBudget() {
		System.out.println("Save budget pressed!");
	}

	public void previousBudget() {
		System.out.println("Previous budget pressed!");
	}

	public void nextBudget() {
		System.out.println("Next budget pressed!");
	}
}

package org.homebudget;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.homebudget.data.Budget;
import org.homebudget.data.BudgetItem;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BudgetController  extends VBox  {
    @FXML private GridPane grid;
    @FXML private HBox gridHeaderHBox;
    @FXML private Label budgetHeadLabel;
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
		budgetHeadLabel.setText(budget.getDate().toLocalDate().toString());
		initGridHeaderHBox(budget);
		int paydayCount = budget.getPaydays().size();
//		this.setWidth(paydayCount*150);
		grid.setMaxWidth(((paydayCount+1)*150)+20);
		grid.setPrefWidth(((paydayCount+1)*150)+20);
//		grid.setPrefWidth(paydayCount*150);
//		grid.setMinWidth(paydayCount*150);
		System.out.println("Budget date:"+budget.getDate());
		int payeeindex = 0;
		TextField[][] gridTextFields = new TextField[paydayCount+1][Payee.getPayees().size()];
		for ( Payee payee : Payee.getPayees()) {
			payeeToRow.put(payee, payeeindex);
			Label label = new Label(payee.getName());
			label.setPadding(new Insets(5, 5, 5, 5));
			grid.add(label, 0, payeeindex);
			for ( int paydayCounter = 1; paydayCounter <= paydayCount; paydayCounter++) {
				TextField textField = new TextField("");
				textField.setMinWidth(150);
				textField.setMaxWidth(150);
				textField.setPrefWidth(150);
				BudgetItem budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, 0, false, null );
				textField.setUserData(budgetItem);
				gridTextFields[paydayCounter][payeeindex] = textField;
				grid.add(textField, paydayCounter, payeeindex);				
			}
			payeeindex++;
		}
		int paydayindex = 1;
		for ( Payday payday : budget.getPaydays()) {
			gridHeaderHBox.getChildren().add(getPaydayHeader(payday));
			paydayToColumn.put(payday, paydayindex);
			for ( BudgetItem budgetItem : payday.getBudgetItems()) {
				TextField textField = gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())];
				textField.setUserData(budgetItem);
				textField.setText(budgetItem.getAmount() == 0 ? "" : budgetItem.getAmount().toString());
			}
			paydayindex++;
		}
	}
	
	private void initGridHeaderHBox(Budget budget2) {
		Node payeesLabel = gridHeaderHBox.getChildren().get(0);
		gridHeaderHBox.getChildren().clear();
		gridHeaderHBox.getChildren().add(payeesLabel);
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
	
	public VBox getPaydayHeader(Payday payday) {
		VBox vbox = new VBox();
		Label payeeNameLabel = new Label();
		payeeNameLabel.setPrefWidth(150);
		payeeNameLabel.setMaxWidth(150);
		payeeNameLabel.setText(payday.getIncome().getName());
		
		Label paydayDateLabel = new Label();
		paydayDateLabel.setPrefWidth(150);
		paydayDateLabel.setMaxWidth(150);
		paydayDateLabel.setText(payday.getDate().toLocaleString());
		
		TextField textField = new TextField();
		textField.setPrefWidth(150);
		textField.setMaxWidth(150);
		textField.setText(payday.getIncome().getBudgetedPay().toString());
		
		vbox.getChildren().add(payeeNameLabel);
		vbox.getChildren().add(paydayDateLabel);
		vbox.getChildren().add(textField);
		
		return vbox;
	}
}

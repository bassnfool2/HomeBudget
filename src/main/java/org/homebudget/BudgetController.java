package org.homebudget;

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
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;

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

public class BudgetController  extends VBox  {
    @FXML private GridPane grid;
    @FXML private HBox gridHeaderHBox;
    @FXML private Label budgetHeadLabel;
	Budget budget = null;
	HashMap<Payee, Integer> payeeToRow = new HashMap<Payee, Integer>();
	HashMap<Payday, Integer> paydayToColumn = new HashMap<Payday, Integer>();
	
	TextField[][] gridTextFields = null;
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
		grid.setMaxWidth(((paydayCount+1)*150)+59);
		grid.setPrefWidth(((paydayCount+1)*150)+59);
//		grid.setPrefWidth(paydayCount*150);
		grid.setMinWidth(((paydayCount+1)*150)+59);
		System.out.println("Budget date:"+budget.getDate());
		int payeeindex = 0;
		grid.getChildren().clear();
		gridTextFields = new TextField[paydayCount+1][Payee.getPayees().size()];
		for ( Payee payee : Payee.getPayees()) {
			payeeToRow.put(payee, payeeindex);
			Label label = new Label(payee.getName()+" ( Due: "+formatDay(payee.getDueOn())+" )");
			//label.setPadding(new Insets(5, 5, 5, 5));
			label.setMinWidth(180);
			label.setMaxWidth(180);
			label.setPrefWidth(180);
			grid.add(label, 0, payeeindex);
			for ( int paydayCounter = 1; paydayCounter <= paydayCount; paydayCounter++) {
				TextField textField = new TextField("");
				textField.setMinWidth(150);
				textField.setMaxWidth(150);
				textField.setPrefWidth(150);
//				ContextMenu contextMenu = new ContextMenu();
//				MenuItem menuItem = new MenuItem("Menu Item");
//				contextMenu.getItems().add(menuItem);
//				textField.setContextMenu(contextMenu);
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
		grid.setGridLinesVisible(true);
	}
	
	private String formatDay(Integer dueOn) {
		switch (dueOn) {
		case 1:
		case 21:
		case 31:
			return Integer.toString(dueOn)+"st";
		case 2:
		case 22:
			return Integer.toString(dueOn)+"nd";
		case 3:
		case 23:
			return Integer.toString(dueOn)+"rd";
		default:
			return Integer.toString(dueOn)+"th";
		}
	}

	private void initGridHeaderHBox(Budget budget2) {
		Node payeesLabel = gridHeaderHBox.getChildren().get(0);
		gridHeaderHBox.getChildren().clear();
		gridHeaderHBox.getChildren().add(payeesLabel);
	}

	public void saveBudget() {
		try {
			if ( gridTextFields != null ) {
				for ( Payday payday : budget.getPaydays()) { payday.save();};
				for ( int paydayIndex = 1; paydayIndex<=budget.getPaydays().size(); paydayIndex++ ) {
					for ( int payeeIndex = 0; payeeIndex < Payee.getPayees().size(); payeeIndex++ ) {
						BudgetItem budgetItem = ((BudgetItem)gridTextFields[paydayIndex][payeeIndex].getUserData());
						String amountText = gridTextFields[paydayIndex][payeeIndex].getText();
						float newamount =  amountText == "" ? 0 : Float.parseFloat(amountText);
						budgetItem.setAmount(newamount);
						((BudgetItem)gridTextFields[paydayIndex][payeeIndex].getUserData()).save();
					}
				}
			}
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

	public void previousBudget() {
		Budget nextBudget = budget.getBudgetByDate(Date.valueOf(budget.getDate().toLocalDate().minusMonths(1)));
		if ( nextBudget == null ) {
			System.out.println("No previous budget");
			return;
		}
		try {
			setBudget(nextBudget);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void nextBudget() {
		Budget nextBudget = budget.getBudgetByDate(Date.valueOf(budget.getDate().toLocalDate().plusMonths(1)));
		if ( nextBudget == null ) {
			try {
				nextBudget = Budget.createNextBudget(Date.valueOf(budget.getDate().toLocalDate().plusMonths(1)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			setBudget(nextBudget);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

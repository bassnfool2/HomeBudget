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

public class BudgetController  extends VBox  {
    @FXML private GridPane grid;
    @FXML private HBox gridHeaderHBox;
    @FXML private HBox budgetPaydayTotalsHBox;
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
		initBudgetPaydayTotalsHBox();
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
			Label label = new Label(payee.getName()+" ( Due: "+payee.getDueOn().getAsString()+" )");
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
				ContextMenu contextMenu = new ContextMenu();
				contextMenu.setUserData(textField);
				MenuItem menuItem = new MenuItem("Mark Paid");
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
    				@Override
    				public void handle(ActionEvent event) {
    					ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
    					TextField textField = (TextField)contextMenu.getUserData();
        				((BudgetItem)textField.getUserData()).setPayed(true);
        				textField.setStyle("-fx-control-inner-background: #008000;");
    				}
				});
				contextMenu.getItems().add(menuItem);

				MenuItem notPaidMenuItem = new MenuItem("Mark Not Paid");
				notPaidMenuItem.setOnAction(new EventHandler<ActionEvent>() {
    				@Override
    				public void handle(ActionEvent event) {
    					ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
    					TextField textField = (TextField)contextMenu.getUserData();
        				((BudgetItem)textField.getUserData()).setPayed(false);
        				textField.setStyle(null);
    				}
				});
				contextMenu.getItems().add(notPaidMenuItem);

				textField.setContextMenu(contextMenu);
				BudgetItem budgetItem = null;
				try {
					budgetItem = budget.getPaydays().get(paydayCounter-1).getBudgetItem(payee);
					textField.setText(budgetItem.getAmount() == 0 ? "" : budgetItem.getAmount().toString());
					if ( budgetItem.isPayed()) {
						textField.setStyle("-fx-control-inner-background: #008000;");
					}
				} catch ( Exception e) {
					budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, 0, false, null );
					budget.getPaydays().get(paydayCounter-1).addBudgetItem(budgetItem);
				}
				textField.setUserData(budgetItem);
				textField.textProperty().addListener(new ChangeListener<String>() {
				    @Override
				    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				    	TextField focusedTextField = (TextField) getScene().focusOwnerProperty().get();
				    	try {
							budgetItemTextFieldChanged(focusedTextField, oldValue, newValue);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }

				});
				gridTextFields[paydayCounter][payeeindex] = textField;
				grid.add(textField, paydayCounter, payeeindex);				
			}
			payeeindex++;
		}
		int paydayindex = 1;
		for ( Payday payday : budget.getPaydays()) {
			gridHeaderHBox.getChildren().add(getPaydayHeader(payday));
			budgetPaydayTotalsHBox.getChildren().add(getPaydayFooter(payday));
			paydayToColumn.put(payday, paydayindex);
//			for ( BudgetItem budgetItem : payday.getBudgetItems()) {
//				TextField textField = gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())];
//				textField.setUserData(budgetItem);
//				textField.setText(budgetItem.getAmount() == 0 ? "" : budgetItem.getAmount().toString());
//			}
			float[] paydayTotals = computePaydayTotals(payday);
			paydayindex++;
		}
		grid.setGridLinesVisible(true);
	}
	
	public void budgetItemTextFieldChanged(TextField textField, String oldValue, String newValue) throws Exception {
    	BudgetItem budgetItem = ((BudgetItem)textField.getUserData());
    	budgetItem.setAmount(newValue.isBlank() ? 0 :  Float.parseFloat(newValue));
        VBox vbox = (VBox)budgetPaydayTotalsHBox.getChildren().get(paydayToColumn.get(budgetItem.getPayday()));
        float[] totals = computePaydayTotals(budgetItem.getPayday());
		((TextField)vbox.getChildren().get(0)).setText(Float.toString(totals[PAYDAY_TOTAL_OUT_INDEX]));
		((TextField)vbox.getChildren().get(1)).setText(Float.toString(totals[PAYDAY_TOTAL_LEFT_INDEX]));		
	}

	private void initGridHeaderHBox(Budget budget2) {
		Node payeesLabel = gridHeaderHBox.getChildren().get(0);
		gridHeaderHBox.getChildren().clear();
		gridHeaderHBox.getChildren().add(payeesLabel);
	}

	private void initBudgetPaydayTotalsHBox() {
		Node payeesTotalsLabels = budgetPaydayTotalsHBox.getChildren().get(0);
		budgetPaydayTotalsHBox.getChildren().clear();
		budgetPaydayTotalsHBox.getChildren().add(payeesTotalsLabels);
	}

	public void saveBudget() {
		try {
			if ( gridTextFields != null ) {
				for ( Payday payday : budget.getPaydays()) { payday.save();};
//				for ( int paydayIndex = 1; paydayIndex<=budget.getPaydays().size(); paydayIndex++ ) {
//					for ( int payeeIndex = 0; payeeIndex < Payee.getPayees().size(); payeeIndex++ ) {
//						BudgetItem budgetItem = ((BudgetItem)gridTextFields[paydayIndex][payeeIndex].getUserData());
//						String amountText = gridTextFields[paydayIndex][payeeIndex].getText();
//						float newamount =  amountText == "" ? 0 : Float.parseFloat(amountText);
//						budgetItem.setAmount(newamount);
//						((BudgetItem)gridTextFields[paydayIndex][payeeIndex].getUserData()).save();
//					}
//				}
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
		textField.setText(payday.getAmount().toString());
		
		vbox.getChildren().add(payeeNameLabel);
		vbox.getChildren().add(paydayDateLabel);
		vbox.getChildren().add(textField);
		
		return vbox;
	}
	
	private Node getPaydayFooter(Payday payday) throws Exception {
		float[] totals = computePaydayTotals(payday);
		VBox vbox = new VBox();
		TextField paydayTotalOutTextField = new TextField();
		paydayTotalOutTextField.setEditable(false);
		paydayTotalOutTextField.setPrefWidth(150);
		paydayTotalOutTextField.setMaxWidth(150);
		paydayTotalOutTextField.setText(Float.toString(totals[PAYDAY_TOTAL_OUT_INDEX]));
		
		TextField paydayTotalLeftTextField = new TextField();
		paydayTotalLeftTextField.setEditable(false);
		paydayTotalLeftTextField.setPrefWidth(150);
		paydayTotalLeftTextField.setMaxWidth(150);
		paydayTotalLeftTextField.setText(Float.toString(totals[PAYDAY_TOTAL_LEFT_INDEX]));

		vbox.getChildren().add(paydayTotalOutTextField);
		vbox.getChildren().add(paydayTotalLeftTextField);
		return vbox;
	}


	
	final static int PAYDAY_TOTAL_OUT_INDEX = 0; 
	final static int PAYDAY_TOTAL_LEFT_INDEX = 1; 
	private float[] computePaydayTotals(Payday payday) throws Exception {
		float[] totals = new float[2];
		float outTotal = 0;
		float leftTotal = payday.getAmount();
		for (BudgetItem budgetItem : payday.getBudgetItems()) {
			outTotal = outTotal + budgetItem.getAmount();
			leftTotal = leftTotal - budgetItem.getAmount();
		}
		totals[PAYDAY_TOTAL_OUT_INDEX] = outTotal;
		totals[PAYDAY_TOTAL_LEFT_INDEX] = leftTotal;
		return totals;
	}
}

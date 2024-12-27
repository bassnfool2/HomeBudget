package org.homebudget;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;

import org.homebudget.data.Budget;
import org.homebudget.data.BudgetItem;
import org.homebudget.data.FundSource;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;
import org.homebudget.data.PayonEnum;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BudgetController  extends VBox implements PayeeAddedListener, IncomeAddedListener {
    @FXML private GridPane grid;
    @FXML private HBox gridHeaderHBox;
    @FXML private HBox budgetPaydayTotalsHBox;
    @FXML private Label budgetHeadLabel;
    @FXML private Button nextButton;
    @FXML private Button previousButton;
    @FXML private Button saveBudgetButton;
	Budget budget = null;
	HashMap<Payee, Integer> payeeToRow = new HashMap<Payee, Integer>();
	HashMap<Payday, Integer> paydayToColumn = new HashMap<Payday, Integer>();
	SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
	BudgetItem currentBudgetItem = null;
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
			e.printStackTrace();
			return;
		}
		if ( nextButton != null ) {
			nextButton.setGraphic(new ImageView(new Image(BudgetController.class.getResourceAsStream("go-next-symbolic.png"))));
			nextButton.setTooltip(new Tooltip("Move to next month"));
		}
		if ( previousButton != null ) {
			previousButton.setGraphic(new ImageView(new Image(BudgetController.class.getResourceAsStream("go-previous-symbolic.png"))));
			previousButton.setTooltip(new Tooltip("Move to previous month"));
		}
		if ( saveBudgetButton != null ) {
			saveBudgetButton.setGraphic(new ImageView(new Image(HomeBudgetController.class.getResourceAsStream("object-select-symbolic.png"))));
			saveBudgetButton.setTooltip(new Tooltip("Save Budget"));
		}
		FundSource.addFunAddedListener(this);
	}
	
	public void setBudget(Budget budget) throws Exception {
		this.budget = budget;
		for ( FundSource fundSource : FundSource.getFundSources() ) {
			boolean found = false;
			for ( Payday payday : budget.getPaydays()) {
				if ( payday.getIncome().equals(fundSource)) {
					found = true;
					break;
				} 
			}
			if ( found ) continue;
			Budget.getFundDrop(fundSource, budget);
		}
		budgetHeadLabel.setText(budget == null ? "" : budget.getDate().toLocalDate().toString());
		initGridHeaderHBox(budget);
		initBudgetPaydayTotalsHBox();
		int paydayCount = budget == null ? 0 : budget.getPaydays().size();
		grid.setMaxWidth(((paydayCount+1)*150)+180);
		grid.setPrefWidth(((paydayCount+1)*150)+180);
		grid.setMinWidth(((paydayCount+1)*150)+180);
		int payeeindex = 0;
		grid.getChildren().clear();
		gridTextFields = new TextField[paydayCount+1][Payee.getPayees().size()];
		for ( Payee payee : Payee.getPayees()) {
			payeeToRow.put(payee, payeeindex);
			boolean payeeAddtoAPayday = false;
			Label label = new Label(payee.getName()+" ( Due: "+payee.getDefaultPaymentAmount()+" on: "+payee.getDueOn().getAsString()+" )");
			//label.setPadding(new Insets(5, 5, 5, 5));
			label.setMinWidth(300);
			label.setMaxWidth(300);
			label.setPrefWidth(300);
			grid.add(label, 0, payeeindex);
			for ( int paydayCounter = 1; paydayCounter <= paydayCount; paydayCounter++) {
				TextField textField = new TextField("");
				//textField.setTextFormatter(getCurrencyTextFormatter());
				textField.setMinWidth(150);
				textField.setMaxWidth(150);
				textField.setPrefWidth(150);
				textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				    if (newValue) {
				    	currentBudgetItem = (BudgetItem)textField.getUserData();
				    } 
				});

				addContextMenu(textField);
				BudgetItem budgetItem = null;
				try {
					budgetItem = budget.getPaydays().get(paydayCounter-1).getBudgetItem(payee);
					if ( budgetItem.isPayed()) {
						textField.setStyle("-fx-control-inner-background: #008000;");
					}
				} catch ( Exception e) {
					if ( payee.getPaywithFundSource().equals(budget.getPaydays().get(paydayCounter-1).getIncome())) {
						LocalDate payDueDate = budget.getDate().toLocalDate().plusDays(payee.getDueOn().getValue()-1);
						if ( payee.getDueOn().equals(PayonEnum.ON_SELECTED_PAYDAY)) {
							budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, payee.getDefaultPaymentAmount(), false, null);
						} else if (( budget.getPaydays().get(paydayCounter-1).getDate().before(Date.valueOf(payDueDate)) || budget.getPaydays().get(paydayCounter-1).getDate().equals(Date.valueOf(payDueDate))) && !payeeAddtoAPayday) {
							budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, payee.getDefaultPaymentAmount(), false, null);
							payeeAddtoAPayday = true;
						} else {
							budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, 0, false, null);
						}
					} else {
						budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, 0, false, null);
					}
					budgetItem.save();
					budget.getPaydays().get(paydayCounter-1).addBudgetItem(budgetItem);
				}
				textField.setText(budgetItem.getAmount() == 0 ? "" : Double.valueOf(budgetItem.getAmount()).toString());
				textField.setUserData(budgetItem);
				textField.textProperty().addListener(new ChangeListener<String>() {
				    @Override
				    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				    	TextField focusedTextField = (TextField) getScene().focusOwnerProperty().get();
				    	try {
							budgetItemTextFieldChanged(focusedTextField, oldValue, newValue);
						} catch (Exception e) {
							HomeBudgetController.showErrorDialog("Unhandled error:\n"+e.getMessage());
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
		if ( budget == null ) return; 
		for ( Payday payday : budget.getPaydays()) {
			gridHeaderHBox.getChildren().add(getPaydayHeader(payday));
			budgetPaydayTotalsHBox.getChildren().add(getPaydayFooter(payday));
			paydayToColumn.put(payday, paydayindex);
			paydayindex++;
		}
		grid.setGridLinesVisible(true);
	}
	
	private void addContextMenu(TextField textField) {
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setUserData(textField);
		MenuItem payonlineMenuItem = new MenuItem("Pay Online");
		payonlineMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();
				payOnline(((BudgetItem)textField.getUserData()));
			}
		});
		contextMenu.getItems().add(payonlineMenuItem);

		MenuItem copyUsernameMenuItem = new MenuItem("Copy Username");
		copyUsernameMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();
				copyUserName(((BudgetItem)textField.getUserData()));

			}
		});
		contextMenu.getItems().add(copyUsernameMenuItem);

		MenuItem copyPasswordMenuItem = new MenuItem("Copy Password");
		copyPasswordMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();
				copyPassword(((BudgetItem)textField.getUserData()));
			}
		});
		contextMenu.getItems().add(copyPasswordMenuItem);

		MenuItem menuItem = new MenuItem("Mark Paid");
		menuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();
				markPaid((BudgetItem)textField.getUserData());
			}
		});
		contextMenu.getItems().add(menuItem);

		MenuItem notPaidMenuItem = new MenuItem("Mark Not Paid");
		notPaidMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();				
				try {
					((BudgetItem)textField.getUserData()).setPayed(false);
					textField.setStyle(null);
				} catch (SQLException e) {
					HomeBudgetController.showErrorDialog("Unable to mark budget item as not paid. Error:\n"+e.getMessage());
				}
			}
		});
		contextMenu.getItems().add(notPaidMenuItem);
		textField.setContextMenu(contextMenu);

	}

	protected void copyUserName(BudgetItem budgetItem) {
		String username = budgetItem.getPayee().getUsername();

		javafx.application.Platform.runLater(new BudgetClipboard(username)); 
    	Platform.runLater(() -> gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())].requestFocus());
	}

	protected void payOnline(BudgetItem budgetItem) {
		String url = budgetItem.getPayee().getUrl();
		Runtime runtime = Runtime.getRuntime();
		try {
			String[] args = {"xdg-open", url};
			runtime.exec(args); // for Unix/Linux
			// runtime.exec("rundll32 url.dll,FileProtocolHandler " + url); // for Windows
		} catch (IOException e) {
			HomeBudgetController.showErrorDialog("Unable to pay online... Error:\n"+e.getMessage());
			e.printStackTrace();
		}    					
    	Platform.runLater(() -> gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())].requestFocus());
	}

	public void budgetItemTextFieldChanged(TextField textField, String oldValue, String newValue) throws Exception {
    	BudgetItem budgetItem = ((BudgetItem)textField.getUserData());
    	budgetItem.setAmount(newValue.isBlank() ? 0 :  Double.parseDouble(newValue));
        VBox vbox = (VBox)budgetPaydayTotalsHBox.getChildren().get(paydayToColumn.get(budgetItem.getPayday()));
        double[] totals = computePaydayTotals(budgetItem.getPayday());
		((TextField)vbox.getChildren().get(0)).setText(Double.toString(((double)((int)totals[PAYDAY_TOTAL_OUT_INDEX]*100))/100));
		((TextField)vbox.getChildren().get(1)).setText(Double.toString(((double)((int)totals[PAYDAY_TOTAL_LEFT_INDEX]*100))/100));		
	}

	public void paydayTextFieldChanged(TextField textField, String oldValue, String newValue) throws Exception {
		Payday payday = ((Payday)textField.getUserData());
    	payday.setAmount(newValue.isBlank() ? 0 :  Double.parseDouble(newValue));
        VBox vbox = (VBox)budgetPaydayTotalsHBox.getChildren().get(paydayToColumn.get(payday));
        double[] totals = computePaydayTotals(payday);
		((TextField)vbox.getChildren().get(0)).setText(Double.toString(((double)((int)totals[PAYDAY_TOTAL_OUT_INDEX]*100))/100));
		((TextField)vbox.getChildren().get(1)).setText(Double.toString(((double)((int)totals[PAYDAY_TOTAL_LEFT_INDEX]*100))/100));		
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
			}
		} catch ( Exception e) {
			HomeBudgetController.showErrorDialog("Error saving budget... Error:\n"+e.getMessage());
			e.printStackTrace();
		}
	}

	public void previousBudget() {
		Budget nextBudget = Budget.getBudgetByDate(Date.valueOf(budget.getDate().toLocalDate().minusMonths(1)));
		if ( nextBudget == null ) {
			System.out.println("No previous budget");
			return;
		}
		try {
			setBudget(nextBudget);
			currentBudgetItem = null;
		} catch (Exception e) {
			HomeBudgetController.showErrorDialog("Error moving to previous budget... Error:\n"+e.getMessage());
			e.printStackTrace();
		}
	}

	public void nextBudget() {
		Budget nextBudget = Budget.getBudgetByDate(Date.valueOf(budget.getDate().toLocalDate().plusMonths(1)));
		if ( nextBudget == null ) {
			try {
				nextBudget = Budget.createNextBudget(Date.valueOf(budget.getDate().toLocalDate().plusMonths(1)));
			} catch (Exception e) {
				HomeBudgetController.showErrorDialog("Error creating next budget... Error:\n"+e.getMessage());
				e.printStackTrace();
			}
		}
		try {
			setBudget(nextBudget);
			currentBudgetItem = null;
		} catch (Exception e) {
			HomeBudgetController.showErrorDialog("Error moving to next budget... Error:\n"+e.getMessage());
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
		paydayDateLabel.setText(dateFormatter.format(payday.getDate()));
		
		TextField textField = new TextField();
		textField.setPrefWidth(150);
		textField.setMaxWidth(150);
		textField.setText(payday.getAmount().toString());
		textField.setUserData(payday);
		textField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		    	TextField focusedTextField = (TextField) getScene().focusOwnerProperty().get();
		    	try {
					paydayTextFieldChanged(focusedTextField, oldValue, newValue);
				} catch (Exception e) {
					HomeBudgetController.showErrorDialog("Error handling payday text field change... Error:\n"+e.getMessage());
					e.printStackTrace();
				}
		    }

		});
		
		vbox.getChildren().add(payeeNameLabel);
		vbox.getChildren().add(paydayDateLabel);
		vbox.getChildren().add(textField);
		
		return vbox;
	}
	
	private Node getPaydayFooter(Payday payday) throws Exception {
		double[] totals = computePaydayTotals(payday);
		VBox vbox = new VBox();
		TextField paydayTotalOutTextField = new TextField();
		paydayTotalOutTextField.setEditable(false);
		paydayTotalOutTextField.setPrefWidth(150);
		paydayTotalOutTextField.setMaxWidth(150);
		paydayTotalOutTextField.setText(Double.toString(((double)((int)totals[PAYDAY_TOTAL_OUT_INDEX]*100))/100));
		
		TextField paydayTotalLeftTextField = new TextField();
		paydayTotalLeftTextField.setEditable(false);
		paydayTotalLeftTextField.setPrefWidth(150);
		paydayTotalLeftTextField.setMaxWidth(150);
		paydayTotalLeftTextField.setText(Double.toString(((double)((int)totals[PAYDAY_TOTAL_LEFT_INDEX]*100))/100));

		vbox.getChildren().add(paydayTotalOutTextField);
		vbox.getChildren().add(paydayTotalLeftTextField);
		return vbox;
	}


	
	final static int PAYDAY_TOTAL_OUT_INDEX = 0; 
	final static int PAYDAY_TOTAL_LEFT_INDEX = 1; 
	private double[] computePaydayTotals(Payday payday) throws Exception {
		double[] totals = new double[2];
		double outTotal = 0;
		double leftTotal = payday.getAmount();
		for (BudgetItem budgetItem : payday.getBudgetItems()) {
			outTotal = outTotal + budgetItem.getAmount();
			leftTotal = leftTotal - budgetItem.getAmount();
		}
		totals[PAYDAY_TOTAL_OUT_INDEX] = outTotal;
		totals[PAYDAY_TOTAL_LEFT_INDEX] = leftTotal;
		return totals;
	}
	
	private class BudgetClipboard implements Runnable {
		String pasteContent = null;
		

		public BudgetClipboard(String pasteContent) {
			super();
			this.pasteContent = pasteContent;
		}


		@Override
		public void run() {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent content = new ClipboardContent();
			content.putString(pasteContent);
			clipboard.setContent(content);
		}
	}

	@Override
	public void newPayeeAdded(Payee payee) {
		try {
			setBudget(budget);
		} catch (Exception e) {
			HomeBudgetController.showErrorDialog("Error reloading budget after new payee added... Error:\n"+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void newFundingSourceAdded(FundSource fundSource) {
		try {
			setBudget(budget);
		} catch (Exception e) {
			HomeBudgetController.showErrorDialog("Error reloading budget after new fund source added... Error:\n"+e.getMessage());
			e.printStackTrace();
		}
	}	

	public BudgetItem getCurrentBudgetItem() {
		return currentBudgetItem;
	}

	public void copyPassword(BudgetItem budgetItem) {
		String password = budgetItem.getPayee().getPassword();

		javafx.application.Platform.runLater(new BudgetClipboard(password)); 
    	Platform.runLater(() -> gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())].requestFocus());
	}

	public void markPaid(BudgetItem budgetItem) {
		try {
			budgetItem.setPayed(true);
			budgetItem.save();
		} catch (SQLException e) {
			HomeBudgetController.showErrorDialog("Unable to mark budget item as paid. Error:\n"+e.getMessage());
		}
		gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())].setStyle("-fx-control-inner-background: #008000;");; 
    	Platform.runLater(() -> gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())].requestFocus());
    	
	}
}

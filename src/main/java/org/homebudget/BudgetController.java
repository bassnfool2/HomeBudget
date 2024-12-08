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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.UnaryOperator;

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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

public class BudgetController  extends VBox implements PayeeAddedListener, IncomeAddedListener {
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
//		this.setWidth(paydayCount*150);
		grid.setMaxWidth(((paydayCount+1)*150)+180);
		grid.setPrefWidth(((paydayCount+1)*150)+180);
//		grid.setPrefWidth(paydayCount*150);
		grid.setMinWidth(((paydayCount+1)*150)+180);
		//System.out.println("Budget date:"+budget.getDate());
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
					//budgetItem = new BudgetItem(HomeBudgetController.NEW_ADD, budget.getPaydays().get(paydayCounter-1), payee, 0, false, null );
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
		if ( budget == null ) return; 
		for ( Payday payday : budget.getPaydays()) {
			gridHeaderHBox.getChildren().add(getPaydayHeader(payday));
			budgetPaydayTotalsHBox.getChildren().add(getPaydayFooter(payday));
			paydayToColumn.put(payday, paydayindex);
//			for ( BudgetItem budgetItem : payday.getBudgetItems()) {
//				TextField textField = gridTextFields[paydayToColumn.get(budgetItem.getPayday())][payeeToRow.get(budgetItem.getPayee())];
//				textField.setUserData(budgetItem);
//				textField.setText(budgetItem.getAmount() == 0 ? "" : budgetItem.getAmount().toString());
//			}
			double[] paydayTotals = computePaydayTotals(payday);
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
				String url = ((BudgetItem)textField.getUserData()).getPayee().getUrl();
				Runtime runtime = Runtime.getRuntime();
				try {
					runtime.exec("xdg-open " + url); // for Unix/Linux
					// runtime.exec("rundll32 url.dll,FileProtocolHandler " + url); // for Windows
				} catch (IOException e) {
					e.printStackTrace();
				}    					
			}
		});
		contextMenu.getItems().add(payonlineMenuItem);

		MenuItem copyUsernameMenuItem = new MenuItem("Copy Username");
		copyUsernameMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();
				String username = ((BudgetItem)textField.getUserData()).getPayee().getUsername();

				javafx.application.Platform.runLater(new BudgetClipboard(username)); 

			}
		});
		contextMenu.getItems().add(copyUsernameMenuItem);

		MenuItem copyPasswordMenuItem = new MenuItem("Copy Password");
		copyPasswordMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ContextMenu contextMenu = ((MenuItem)event.getSource()).getParentPopup();
				TextField textField = (TextField)contextMenu.getUserData();
				String password = ((BudgetItem)textField.getUserData()).getPayee().getPassword();

				javafx.application.Platform.runLater(new BudgetClipboard(password)); 

			}
		});
		contextMenu.getItems().add(copyPasswordMenuItem);

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

	}

	public void budgetItemTextFieldChanged(TextField textField, String oldValue, String newValue) throws Exception {
    	BudgetItem budgetItem = ((BudgetItem)textField.getUserData());
    	budgetItem.setAmount(newValue.isBlank() ? 0 :  Double.parseDouble(newValue));
        VBox vbox = (VBox)budgetPaydayTotalsHBox.getChildren().get(paydayToColumn.get(budgetItem.getPayday()));
        double[] totals = computePaydayTotals(budgetItem.getPayday());
		((TextField)vbox.getChildren().get(0)).setText(Double.toString(totals[PAYDAY_TOTAL_OUT_INDEX]));
		((TextField)vbox.getChildren().get(1)).setText(Double.toString(totals[PAYDAY_TOTAL_LEFT_INDEX]));		
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
//						double newamount =  amountText == "" ? 0 : Double.parseDouble(amountText);
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
		double[] totals = computePaydayTotals(payday);
		VBox vbox = new VBox();
		TextField paydayTotalOutTextField = new TextField();
		paydayTotalOutTextField.setEditable(false);
		paydayTotalOutTextField.setPrefWidth(150);
		paydayTotalOutTextField.setMaxWidth(150);
		paydayTotalOutTextField.setText(Double.toString(totals[PAYDAY_TOTAL_OUT_INDEX]));
		
		TextField paydayTotalLeftTextField = new TextField();
		paydayTotalLeftTextField.setEditable(false);
		paydayTotalLeftTextField.setPrefWidth(150);
		paydayTotalLeftTextField.setMaxWidth(150);
		paydayTotalLeftTextField.setText(Double.toString(totals[PAYDAY_TOTAL_LEFT_INDEX]));

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void newFundingSourceAdded(FundSource fundSource) {
		try {
			setBudget(budget);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TextFormatter<Double> getCurrencyTextFormatter() {
	    UnaryOperator<Change> Double_Filter = change -> {
	        String Demo_Text = change.getControlNewText();
	        if (Demo_Text.matches("-?([1-9][0-9]*)?")) {
	          return change;
	        } else if ("-".equals(change.getText())) {
	          if (change.getControlText().startsWith("-")) {
	            change.setText("");
	            change.setRange(0, 1);
	            change.setCaretPosition(change.getCaretPosition() - 2);
	            change.setAnchor(change.getAnchor() - 2);
	            return change;
	          } else {
	            change.setRange(0, 0);
	            return change;
	          }
	        }
	        return null;
	      };
	      StringConverter<Double> String_Converter = new DoubleStringConverter() {
		      @Override
		      public Double fromString(String s) {
		        if (s.isEmpty())
		          return 0.0;
		        return super.fromString(s);
		      }
		    };

		  return new TextFormatter<Double>(String_Converter, 0.0, Double_Filter);
		
	}
}

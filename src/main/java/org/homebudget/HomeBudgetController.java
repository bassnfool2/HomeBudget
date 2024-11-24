package org.homebudget;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.homebudget.data.Budget;
import org.homebudget.data.BudgetItem;
import org.homebudget.data.Income;
import org.homebudget.data.Income.PayFrequency;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HomeBudgetController extends VBox  {
	public static final int NEW_ADD = -1;
	public Payee currentPayee = new Payee();
	public Income currentIncome = new Income();
    private static Connection conn = null;
    @FXML private TextField payeeNameTextField;
    @FXML private TextField payeeUsernameTextField;
    @FXML private PasswordField payeePasswordField;
    @FXML private TextField payeeUrlTextField;
    @FXML private TextField payeeDueOnTextField;
    @FXML private TextField payeeBudgetedPaymentTextField;
    @FXML private TextField payeeBalanceTextField;

    @FXML private TableView<Payee> payeeTableView;
    @FXML private TableColumn<Payee, String> payeeNameTableColumn;
    @FXML private TableColumn<Payee, String> payeeUrlTableColumn;
    @FXML private TableColumn<Payee, Integer> payeeDueDayTableColumn;
    @FXML private TableColumn<Payee, Float> payeeDefaultPaymentTableColumn;
    @FXML private TableColumn<Payee, Float> payeeBalanceTableColumn;
    
    @FXML private TextField incomeNameTextField;
    @FXML private TextField incomeBudgetedPaymentAmountTextField;
    @FXML private ComboBox<PayFrequency> incomePayfrequencyComboBox;
    @FXML private DatePicker incomeStartingDateDatePicker;
    
    @FXML private TableView<Income> incomeTableView;
    @FXML private TableColumn<Income, String> incomeNameTableColumn;
    @FXML private TableColumn<Income, String> incomePayFrequencyTableColumn;
    @FXML private TableColumn<Income, Float> incomeBudgetedAmountTableColumn;

    @FXML private TableView<BudgetRow> budgetTableView;
    @FXML private TableColumn<BudgetRow, String> budgetPayeeTableColumn;
    @FXML private TableColumn<BudgetRow, String> budgetPayday1TableColmn;
    @FXML private TableColumn<BudgetRow, String> budgetPayday2TableColmn;
    @FXML private TableColumn<BudgetRow, String> budgetPayday3TableColmn;
    @FXML private TableColumn<BudgetRow, String> budgetPayday4TableColmn;
    @FXML private TableColumn<BudgetRow, String> budgetPayday5TableColmn;
    @FXML private TableColumn<BudgetRow, String> budgetPayday6TableColmn;
    
    HashMap<Payee, Integer> payeeToRow = new HashMap<Payee, Integer>();
    HashMap<Payday, Integer> paydayToColumn = new HashMap<Payday, Integer>();
    ArrayList<BudgetRow> budgetRows = new ArrayList<BudgetRow>();
    public HomeBudgetController() {
		URL fxmlLocation = HomeBudgetController.class.getResource("HomeBudget.fxml");
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
		try {
			budgetPayeeTableColumn.setCellValueFactory(new PropertyValueFactory<>("payee"));
			budgetPayday1TableColmn.setCellValueFactory(new PropertyValueFactory<>("budgetItem0"));
			budgetPayday1TableColmn.setCellFactory(TextFieldTableCell.forTableColumn());
			//budgetPayday1TableColmn.setCellFactory(EditableTableCell::new);
			budgetPayday2TableColmn.setCellValueFactory(new PropertyValueFactory<>("budgetItem1"));
			budgetPayday2TableColmn.setCellFactory(TextFieldTableCell.forTableColumn());
			budgetPayday3TableColmn.setCellValueFactory(new PropertyValueFactory<>("budgetItem2"));
			budgetPayday3TableColmn.setCellFactory(TextFieldTableCell.forTableColumn());
			budgetPayday4TableColmn.setCellValueFactory(new PropertyValueFactory<>("budgetItem3"));
			budgetPayday4TableColmn.setCellFactory(TextFieldTableCell.forTableColumn());
			budgetPayday5TableColmn.setCellValueFactory(new PropertyValueFactory<>("budgetItem4"));
			budgetPayday5TableColmn.setCellFactory(TextFieldTableCell.forTableColumn());
			budgetPayday6TableColmn.setCellValueFactory(new PropertyValueFactory<>("budgetItem5"));
			budgetPayday6TableColmn.setCellFactory(TextFieldTableCell.forTableColumn());
			initDb("/home/ghobbs/Documents/zbudget.db");
			incomePayfrequencyComboBox.getItems().setAll(PayFrequency.values());
			loadPayees();
			loadIncome();
			loadBudgets();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void loadBudgets() throws Exception {
		Budget.load();
/*
column.setCellFactory(TextFieldTableCell.forTableColumn());
 */
		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setPrefHeight(30);
		ColumnConstraints columnConstraints = new ColumnConstraints();
		columnConstraints.setPrefWidth(152);
		budgetRows = new ArrayList<BudgetRow>();
		// Add the row constraints to the grid pane
		for ( Budget budget : Budget.getBudgets()) {
			System.out.println("Budget date:"+budget.getDate());
			int payeeindex = 0;
			for ( Payee payee : Payee.getPayees()) {
				payeeToRow.put(payee, payeeindex);
				BudgetRow budgetRow = new BudgetRow(payee, budget.getPaydays());
				budgetRows.add(budgetRow);
				payeeindex++;
			}
			int paydayindex = 0;
			for ( Payday payday : budget.getPaydays()) {
				paydayToColumn.put(payday, paydayindex);
				for ( BudgetItem budgetItem : payday.getBudgetItems()) {
					budgetRows.get(payeeToRow.get(budgetItem.getPayee())).replaceBudgetItemAt(paydayindex, budgetItem);
				}
				paydayindex++;
			}
		}
		ObservableList<BudgetRow> budgetData = FXCollections.observableList(budgetRows);
		budgetTableView.setItems(budgetData);
		budgetTableView.refresh();
		
	}

	private void loadIncome() throws SQLException {
		incomeNameTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));
		incomePayFrequencyTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getPayFrequency().toString()));
		incomeBudgetedAmountTableColumn.setCellValueFactory(p -> new SimpleFloatProperty(p.getValue().getBudgetedPay()).asObject());
		Income.load();
		for ( Income income : Income.getIncomes()) {
			incomeTableView.getItems().add(income);
			System.out.println("Income name:"+income.getName());
		}
		incomeTableView.refresh();
	}

	private void loadPayees() throws SQLException {
		payeeNameTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));
		payeeUrlTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getUrl()));
		payeeDueDayTableColumn.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getDueOn()).asObject());
		payeeDefaultPaymentTableColumn.setCellValueFactory(p -> new SimpleFloatProperty(p.getValue().getDefaultPaymentAmount()).asObject());
		payeeBalanceTableColumn.setCellValueFactory(p -> new SimpleFloatProperty(p.getValue().getBalance()).asObject());
		Payee.load();
		for ( Payee payee : Payee.getPayees()) {
			payeeTableView.getItems().add(payee);
			System.out.println("Payee name:"+payee.getName());
		}
		payeeTableView.refresh();
	}

	private void initDb(String dbPath) throws SQLException {
		try {
		    Class.forName("org.sqlite.JDBC");
		    String url = "jdbc:sqlite:"+dbPath;
		    HomeBudgetController.conn = DriverManager.getConnection(url);
		} catch (ClassNotFoundException e) {
		    System.err.println("Could not init JDBC driver - driver not found");
		    e.printStackTrace();
		}		
	}
	
	public static Connection getDbConnection() {
		return conn;
	}

	public void openFile() {
		System.out.println("open file");
	}

	public void saveFile() {
		System.out.println("save file");
	}

	public void newFile() {
		System.out.println("new file");
	}

	public void quit() {
		System.exit(0);
	}

	public void newBudget() {
		System.out.println("new budget");
	}
	
	public void newPayee() {
		setCurrentPayee(new Payee());
	}

	public void savePayee() {
		boolean newAdd = ( currentPayee.getId() == HomeBudgetController.NEW_ADD);
		currentPayee.setName(payeeNameTextField.getText());
		currentPayee.setUsername(payeeUsernameTextField.getText());
		currentPayee.setPassword(payeePasswordField.getText());
		currentPayee.setUrl(payeeUrlTextField.getText());
		currentPayee.setDueOn(Integer.parseInt(payeeDueOnTextField.getText()));
		currentPayee.setBalance(Float.parseFloat(payeeBalanceTextField.getText()));
		currentPayee.setDefaultPaymentAmount(Float.parseFloat(payeeBudgetedPaymentTextField.getText()));
		try {
			int result = currentPayee.save();
			if (result != 1 ) {
				throw new SQLException("No payee changed!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if ( newAdd ) {
			payeeTableView.getItems().add(currentPayee);
		}
		payeeTableView.refresh();
	}
	
	public void newIncomeSource() {
		setCurrentIncome(new Income());
	}
	
	public void saveIncome() {
		boolean newAdd = ( currentIncome.getId() == HomeBudgetController.NEW_ADD);
		currentIncome.setName(incomeNameTextField.getText());
		currentIncome.setPayFrequency(incomePayfrequencyComboBox.getSelectionModel().getSelectedItem());
		currentIncome.setDefaultPayAmount(Float.parseFloat(incomeBudgetedPaymentAmountTextField.getText()));
		currentIncome.setNextPayDate(Date.valueOf(incomeStartingDateDatePicker.getValue()));
		try {
			int result = currentIncome.save();
			if (result != 1 ) {
				throw new SQLException("No Income changed!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if ( newAdd ) {
			incomeTableView.getItems().add(currentIncome);
		}
		incomeTableView.refresh();
	}
	
	public void payeeSelected(MouseEvent event) {
		int rowIndex = payeeTableView.getSelectionModel().getSelectedIndex();
        //int columnIndex = payeeTableView.getColumns().indexOf(((TableView<Payee>)event.getSource()).getClickedColumn());

        // Handle the click event based on the row and column indices
        if (event.getClickCount() == 2) { // double click
            // Handle double click on specific row and column
        } else {
        	setCurrentPayee(payeeTableView.getSelectionModel().getSelectedItem());
        }	
    }
	
	public void incomeSelected(MouseEvent event) {
		int rowIndex = incomeTableView.getSelectionModel().getSelectedIndex();
        //int columnIndex = payeeTableView.getColumns().indexOf(((TableView<Payee>)event.getSource()).getClickedColumn());

        // Handle the click event based on the row and column indices
        if (event.getClickCount() == 2) { // double click
            // Handle double click on specific row and column
        } else {
        	setCurrentIncome(incomeTableView.getSelectionModel().getSelectedItem());
        }	
    }
	
	private void setCurrentIncome(Income selectedItem) {
		this.currentIncome = selectedItem;
		incomeNameTextField.setText(currentIncome.getName());
		incomePayfrequencyComboBox.getSelectionModel().select(currentIncome.getPayFrequency());
		incomeBudgetedPaymentAmountTextField.setText(currentIncome.getBudgetedPay().toString());
		incomeStartingDateDatePicker.setValue(currentIncome.getNextPayDate().toLocalDate());
	}

	private void setCurrentPayee(Payee payee) {
		this.currentPayee = payee;
    	payeeNameTextField.setText(currentPayee.getName());
    	payeeUsernameTextField.setText(currentPayee.getUsername());
    	payeePasswordField.setText(currentPayee.getPassword());
    	payeeUrlTextField.setText(currentPayee.getUrl());
    	payeeDueOnTextField.setText(currentPayee.getDueOn().toString());
    	payeeBudgetedPaymentTextField.setText(currentPayee.getDefaultPaymentAmount().toString());
    	payeeBalanceTextField.setText(currentPayee.getBalance().toString());
	}
	
	public void previousBudget() {
		System.out.println("Previous budget clicked!");
	}

	public void nextBudget() {
		System.out.println("Next budget clicked!");
	}

	public void saveBudget() throws Exception {
		for ( BudgetRow budgetRow : budgetRows ) {
			for ( BudgetItem budgetItem : budgetRow.grabBudgetItems()) {
				if ( budgetItem.getAmount() != 0 && budgetItem.getId() == HomeBudgetController.NEW_ADD && budgetItem.getPayday() != null ) {
					budgetItem.save();
				} else if ( budgetItem.getPayday() != null ) {
					budgetItem.save();
				}
			}
			System.out.println(budgetRow.grabPayee().getName()+" ");
		}
		loadBudgets();
	}

	public class EditableTableCell extends TableCell<String, String> {
	    private TextField textField;

	    public EditableTableCell() {
	        textField = new TextField();
	        textField.setOnAction(event -> commitEdit(textField.getText()));
	    }

	    @Override
	    public void startEdit() {
	        super.startEdit();
	        textField.setText(getItem());
	        setGraphic(textField);
	    }

	    @Override
	    public void cancelEdit() {
	        super.cancelEdit();
	        setGraphic(null);
	    }
	}
}

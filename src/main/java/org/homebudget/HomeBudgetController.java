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


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;

import org.homebudget.data.Budget;
import org.homebudget.data.FundSource;
import org.homebudget.data.FundSource.PayFrequency;
import org.homebudget.data.Payday;
import org.homebudget.data.Payee;
import org.homebudget.db.DbUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;

public class HomeBudgetController extends VBox  {
	Scene myScene = null;

	public Scene getMyScene() {
		return myScene;
	}

	public void setMyScene(Scene scene) {
		this.myScene = scene;
	}

	public static String homeBudgetDb = null;
	public static String getHomeBudgetDb() {
		return homeBudgetDb;
	}

	public static void setHomeBudgetDb(String inHomeBudgetDb) {
		homeBudgetDb = inHomeBudgetDb;
	}

	public static String dbType = null;
	public String password = null;
	public static final int NEW_ADD = -1;
	public Payee currentPayee = new Payee();
	public FundSource currentIncome = new FundSource();
	public Budget currentBudget = null;
    private static Connection conn = null;
    @FXML private TextField payeeNameTextField;
    @FXML private TextField payeeUsernameTextField;
    @FXML private PasswordField payeePasswordField;
    @FXML private TextField payeeUrlTextField;
    @FXML private TextField payeeBudgetedPaymentTextField;
    @FXML private TextField payeeBalanceTextField;
    @FXML private ComboBox<FundSource> payeeIncomeComboBox;
    @FXML private ComboBox<String> fileComboBox;
    @FXML private PayonSelectController payonSelectController;
    
    @FXML private TableView<Payee> payeeTableView;
    @FXML private TableColumn<Payee, String> payeeNameTableColumn;
    @FXML private TableColumn<Payee, String> payeeUrlTableColumn;
    @FXML private TableColumn<Payee, String> payeeDueDayTableColumn;
    @FXML private TableColumn<Payee, Double> payeeDefaultPaymentTableColumn;
    @FXML private TableColumn<Payee, Double> payeeBalanceTableColumn;
    
    @FXML private TextField incomeNameTextField;
    @FXML private TextField incomeBudgetedPaymentAmountTextField;
    @FXML private ComboBox<PayFrequency> incomePayfrequencyComboBox;
    @FXML private DatePicker incomeStartingDateDatePicker;
    
    @FXML private TableView<FundSource> incomeTableView;
    @FXML private TableColumn<FundSource, String> incomeNameTableColumn;
    @FXML private TableColumn<FundSource, String> incomePayFrequencyTableColumn;
    @FXML private TableColumn<FundSource, Double> incomeBudgetedAmountTableColumn;
    @FXML private Tab budgetTab;
    @FXML private VBox passwordVBox;
    @FXML private TabPane tabbedPane;
    @FXML private PasswordField passwordTextField;
    @FXML private Menu openRecentMenu;
    public boolean isNewFile = false;

    
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
	}
    
    public void unlockDb() {
		try {
			initDb(homeBudgetDb,"derby", passwordTextField.getText());
			passwordVBox.setVisible(false);
			tabbedPane.setVisible(true);
			incomePayfrequencyComboBox.getItems().setAll(PayFrequency.values());
			loadIncome();
			payeeIncomeComboBox.getItems().setAll(FundSource.getFundSources());
			loadPayees();
			loadBudgets();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }

	protected void initOpenRecent() {
		if ( Settings.config == null || Settings.config.getProperty("recent.files") == null || Settings.config.getProperty("recent.files").trim().equals("")) return;
		System.out.println(Settings.config.getProperty("recent.files"));
		for ( String filePathS : Settings.config.getProperty("recent.files").split(",")) {
			MenuItem menuItem = new MenuItem(filePathS);
			menuItem.setOnAction((ActionEvent event) -> {
	            System.out.println("open recent selected...");
	            closeDB();
	            fileComboBox.getSelectionModel().select(((MenuItem)event.getSource()).getText());	    
	        });
			openRecentMenu.getItems().add(menuItem);
		}
	}

	private void loadBudgets() throws Exception {
		Budget.load();
		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setPrefHeight(30);
		ColumnConstraints columnConstraints = new ColumnConstraints();
		columnConstraints.setPrefWidth(152);
		// Add the row constraints to the grid pane
		Budget budget = null;
		if ( Budget.getBudgets().size() == 0) {
			//budget = Budget.createNextBudget(Date.valueOf(Budget.getStartOfNextMonth()));
		} else {
			budget = Budget.getBudgetByDate(Date.valueOf(LocalDate.now()));
			if ( budget == null ) {
				budget = Budget.getBudgets().get(Budget.getBudgets().size()-1); 
			}
		}
		currentBudget = budget;
		if ( budget == null ) {
			WelcomeController welcomeController = new WelcomeController(this);
			budgetTab.setContent(welcomeController);
		} else {
			BudgetController budgetController = new BudgetController();
			budgetController.setBudget(budget);
			budgetTab.setContent(budgetController);
		}
		
	}

	public Budget getCurrentBudget() {
		return currentBudget;
	}

	public void setCurrentBudget(Budget budget) {
		try {
			if ( this.currentBudget == null) {
				BudgetController budgetController = new BudgetController();
				budgetController.setBudget(budget);
				budgetTab.setContent(budgetController);			
			}
			this.currentBudget = budget;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadIncome() throws SQLException {
		incomeNameTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));
		incomePayFrequencyTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getPayFrequency().toString()));
		incomeBudgetedAmountTableColumn.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().getBudgetedPay()).asObject());
		FundSource.load();
		for ( FundSource income : FundSource.getFundSources()) {
			incomeTableView.getItems().add(income);
			System.out.println("Income name:"+income.getName());
		}
		incomeTableView.refresh();
	}

	private void loadPayees() throws Exception {
		payeeNameTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));
		payeeUrlTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getUrl()));
		payeeDueDayTableColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDueOn().getAsString()));
		payeeDefaultPaymentTableColumn.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().getDefaultPaymentAmount()).asObject());
		payeeBalanceTableColumn.setCellValueFactory(p -> new SimpleDoubleProperty(p.getValue().getBalance()).asObject());
		Payee.load();
		for ( Payee payee : Payee.getPayees()) {
			payeeTableView.getItems().add(payee);
			System.out.println("Payee name:"+payee.getName());
		}
		payeeTableView.refresh();
	}

	private void initDb(String dbPath, String type, String password) throws SQLException, IOException, URISyntaxException {
		if ( isNewFile ) {
			type = "derby";
		}
		switch ( type) {
		case "sqlite" :
			try {
				Class.forName("org.sqlite.JDBC");
				String url = "jdbc:sqlite:"+dbPath;
				HomeBudgetController.conn = DriverManager.getConnection(url);
			} catch (ClassNotFoundException e) {
				System.err.println("Could not init JDBC driver - driver not found");
				e.printStackTrace();
			}

			break;
		case "derby" :
			try {
				Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
				String url = "jdbc:derby:"+dbPath+File.separator+"hb.db"+";dataEncryption=true;bootPassword="+password;
				if ( isNewFile) url = url.concat(";create=true");
				HomeBudgetController.conn = DriverManager.getConnection(url);
				if (isNewFile) {
					DbUtils.initNewDB();
				}
			} catch (ClassNotFoundException e) {
				System.err.println("Could not init JDBC driver - driver not found");
				e.printStackTrace();
			}
		}
	}	

	public static Connection getDbConnection() {
		return conn;
	}

	public void openFile() {
		closeDB();
		DirectoryChooser fileChooser = new DirectoryChooser();
		//File selectedFile = fileChooser.showOpenDialog((Stage)this.getScene().getWindow());
		File selectedFile = fileChooser.showDialog(this.getScene().getWindow());
		if ( selectedFile.exists()) {
			if ( selectedFile.isDirectory()) {
				if ( selectedFile.list().length == 0) {
					this.setIsNewFile(true);
				}
				HomeBudgetController.setHomeBudgetDb(selectedFile.getAbsolutePath());
				if ( !fileComboBox.getItems().contains(selectedFile.getAbsolutePath())) {
					fileComboBox.getItems().add(selectedFile.getAbsolutePath());
				}
					
				fileComboBox.getSelectionModel().select(selectedFile.getAbsolutePath());
			}
		}
	}

	public void saveFile() {
		if ( budgetTab.getContent() instanceof BudgetController) {
			((BudgetController)budgetTab.getContent()).saveBudget();
		}
	}

	public void newFile() {
		openFile();
	}

	public void quit() {
		try {
			Settings.saveProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		currentPayee.setDueOn(payonSelectController.getSelectedPayon());
		currentPayee.setBalance(payeeBalanceTextField.getText().equals("") ? 0 :
			Double.parseDouble(payeeBalanceTextField.getText()));
		currentPayee.setDefaultPaymentAmount(payeeBudgetedPaymentTextField.getText().equals("") ? 0 : 
			Double.parseDouble(payeeBudgetedPaymentTextField.getText()));
		currentPayee.setPaywithFundSource(payeeIncomeComboBox.getSelectionModel().getSelectedItem());
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
		setCurrentIncome(new FundSource());
	}
	
	public void saveIncome() {
		boolean newAdd = ( currentIncome.getId() == HomeBudgetController.NEW_ADD);
		currentIncome.setName(incomeNameTextField.getText());
		currentIncome.setPayFrequency(incomePayfrequencyComboBox.getSelectionModel().getSelectedItem());
		currentIncome.setDefaultPayAmount(Double.parseDouble(incomeBudgetedPaymentAmountTextField.getText()));
		currentIncome.setFirstPayDate(Date.valueOf(incomeStartingDateDatePicker.getValue()));
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
			payeeIncomeComboBox.getItems().clear();
			payeeIncomeComboBox.getItems().addAll(FundSource.getFundSources());
		}
		incomeTableView.refresh();
	}
	
	public void payeeSelected(MouseEvent event) {
        // Handle the click event based on the row and column indices
        if (event.getClickCount() == 2) { // double click
            // Handle double click on specific row and column
        } else {
        	setCurrentPayee(payeeTableView.getSelectionModel().getSelectedItem());
        }	
    }
	
	public void incomeSelected(MouseEvent event) {
        // Handle the click event based on the row and column indices
        if (event.getClickCount() == 2) { // double click
            // Handle double click on specific row and column
        } else {
        	setCurrentIncome(incomeTableView.getSelectionModel().getSelectedItem());
        }	
    }
	
	private void setCurrentIncome(FundSource selectedItem) {
		this.currentIncome = selectedItem;
		incomeNameTextField.setText(currentIncome.getName());
		incomePayfrequencyComboBox.getSelectionModel().select(currentIncome.getPayFrequency());
		incomeBudgetedPaymentAmountTextField.setText(currentIncome.getBudgetedPay().toString());
		incomeStartingDateDatePicker.setValue(currentIncome.getFirstPayDate().toLocalDate());
	}

	private void setCurrentPayee(Payee payee) {
		this.currentPayee = payee;
    	payeeNameTextField.setText(currentPayee.getName());
    	payeeUsernameTextField.setText(currentPayee.getUsername());
    	payeePasswordField.setText(currentPayee.getPassword());
    	payeeUrlTextField.setText(currentPayee.getUrl());
    	payonSelectController.setSelectedPayon(currentPayee.getDueOn());
    	payeeBudgetedPaymentTextField.setText(currentPayee.getDefaultPaymentAmount().toString());
    	payeeBalanceTextField.setText(currentPayee.getBalance().toString());
    	payeeIncomeComboBox.getSelectionModel().select(currentPayee.getPaywithFundSource());
	}
	
	public void previousBudget() {
		System.out.println("Previous budget clicked!");
	}

	public void nextBudget() {
		System.out.println("Next budget clicked!");
	}

	public void saveBudget() throws Exception {
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

	public static void setDBType(String string) {
		dbType = string;
	}

	public void setIsNewFile(boolean b) {
		this.isNewFile = b;
	}
	
	public void fileSelected() {
		HomeBudgetController.setHomeBudgetDb(fileComboBox.getSelectionModel().getSelectedItem());
	}

	public void setRecentFiles(String[] recentFiles) {
		for ( String fileName : recentFiles ) fileComboBox.getItems().add(fileName);
		fileComboBox.getSelectionModel().select(0);
	}
	
	public void closeDB() {
		Budget.getBudgets().clear();
		Payee.getPayees().clear();
		FundSource.getFundSources().clear();
		try {
			getDbConnection().close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		passwordVBox.setVisible(true);
		tabbedPane.setVisible(false);
	}
}

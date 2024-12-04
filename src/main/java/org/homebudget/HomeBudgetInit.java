/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.homebudget;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class HomeBudgetInit extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			HomeBudgetController homeBankController = new HomeBudgetController();
			
			Scene homeBudgetScene = new Scene(homeBankController);
			primaryStage.setScene(homeBudgetScene);
			primaryStage.setMaximized(true);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HomeBudgetController.setDBType(args[0]);
		launch(args);
	}
}

module JavaFx {
	requires javafx.controls;
	
	opens org.homebudget to javafx.graphics, javafx.fxml;
}

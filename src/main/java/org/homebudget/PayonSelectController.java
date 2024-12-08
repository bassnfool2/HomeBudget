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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import org.homebudget.data.Budget;
import org.homebudget.data.BudgetItem;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PayonSelectController  extends VBox  {
    @FXML private GridPane grid;
    @FXML private TextField selectedDueOnLabel;
    PayonEnum selectedPayon = null;

	public PayonSelectController() {
		URL fxmlLocation = HomeBudgetController.class.getResource("PayonSelect.fxml");
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
    

    public PayonEnum getSelectedPayon() {
		return selectedPayon;
	}

	public void setSelectedPayon(PayonEnum selectedPayon) {
		this.selectedPayon = selectedPayon;
    	selectedDueOnLabel.setText(selectedPayon.getAsString());
	}


	public void toggleSelection() {
    	grid.setVisible(!grid.isVisible());
    }

    public void eachPaycheckSelected() {
    	setSelectedPayon(PayonEnum.ON_SELECTED_PAYDAY);
    	toggleSelection();
    }
    
    public void on1Selected() {
    	setSelectedPayon(PayonEnum.FIRST);
    	toggleSelection();
    }
    
    public void on2Selected() {
    	setSelectedPayon(PayonEnum.SECOND);
    	toggleSelection();
    }
    
    public void on3Selected() {
    	setSelectedPayon(PayonEnum.THIRD);
    	toggleSelection();
    }
    
    public void on4Selected() {
    	setSelectedPayon(PayonEnum.FOURTH);
    	toggleSelection();
    }
    
    public void on5Selected() {
    	setSelectedPayon(PayonEnum.FIFTH);
    	toggleSelection();
    }
    
    public void on6Selected() {
    	setSelectedPayon(PayonEnum.SIXTH);
    	toggleSelection();
    }
    
    public void on7Selected() {
    	setSelectedPayon(PayonEnum.SEVENTH);
    	toggleSelection();
    }
    
    public void on8Selected() {
    	setSelectedPayon(PayonEnum.EIGHTH);
    	toggleSelection();
    }
    
    public void on9Selected() {
    	setSelectedPayon(PayonEnum.NINETH);
    	toggleSelection();
    }
    
    public void on10Selected() {
    	setSelectedPayon(PayonEnum.TENTH);
    	toggleSelection();
    }
    
    public void on11Selected() {
    	setSelectedPayon(PayonEnum.ELEVENTH);
    	toggleSelection();
    }
    
    public void on12Selected() {
    	setSelectedPayon(PayonEnum.TWELFTH);
    	toggleSelection();
    }
    
    public void on13Selected() {
    	setSelectedPayon(PayonEnum.THIRTEENTH);
    	toggleSelection();
    }
    
    public void on14Selected() {
    	setSelectedPayon(PayonEnum.FOURTEENTH);
    	toggleSelection();
    }
    
    public void on15Selected() {
    	setSelectedPayon(PayonEnum.FIFTTEENTH);
    	toggleSelection();
    }
    
    public void on16Selected() {
    	setSelectedPayon(PayonEnum.SIXTEENGTH);
    	toggleSelection();
    }
    
    public void on17Selected() {
    	setSelectedPayon(PayonEnum.SEVENTEENGTH);
    	toggleSelection();
    }
    
    public void on18Selected() {
    	setSelectedPayon(PayonEnum.EIGHTTEENGTH);
    	toggleSelection();
    }
    
    public void on19Selected() {
    	setSelectedPayon(PayonEnum.NINETEENTH);
    	toggleSelection();
    }
    
    public void on20Selected() {
    	setSelectedPayon(PayonEnum.TWENTIETH);
    	toggleSelection();
    }
    
    public void on21Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_FIRST);
    	toggleSelection();
    }
    
    public void on22Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_SENCOND);
    	toggleSelection();
    }
    
    public void on23Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_THIRD);
    	toggleSelection();
    }
    
    public void on24Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_FOURTH);
    	toggleSelection();
    }
    
    public void on25Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_FIFTH);
    	toggleSelection();
    }
    
    public void on26Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_SIXTH);
    	toggleSelection();
    }
    
    public void on27Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_SEVENTH);
    	toggleSelection();
    }
    
    public void on28Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_EIGHTH);
    	toggleSelection();
    }
    
    public void on29Selected() {
    	setSelectedPayon(PayonEnum.TWENTY_NINETH);
    	toggleSelection();
    }
    
    public void on30Selected() {
    	setSelectedPayon(PayonEnum.THIRTIETH);
    	toggleSelection();
    }
}

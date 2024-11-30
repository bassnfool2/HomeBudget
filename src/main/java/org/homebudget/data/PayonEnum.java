package org.homebudget.data;

import java.util.HashMap;
import java.util.Map;

public enum PayonEnum {
	ON_SELECTED_PAYDAY(0),
    FIRST(1),
    SECOND(2),
    THIRD(3),
	FOURTH(4),
	FIFTH(5),
	SIXTH(6),
	SEVENTH(7),
	EIGHTH(8),
	NINETH(9),
	TENTH(10),
	ELEVENTH(11),
	TWELFTH(12),
	THIRTEENTH(13),
	FOURTEENTH(14),
	FIFTTEENTH(15),
	SIXTEENGTH(16),
	SEVENTEENGTH(17),
	EIGHTTEENGTH(18),
	NINETEENTH(19),
	TWENTIETH(20),
	TWENTY_FIRST(21),
	TWENTY_SENCOND(22),
	TWENTY_THIRD(23),
	TWENTY_FOURTH(24),
	TWENTY_FIFTH(25),
	TWENTY_SIXTH(26),
	TWENTY_SEVENTH(27),
	TWENTY_EIGHTH(28),
	TWENTY_NINETH(29),
	THIRTIETH(27),
	;
	
    private int value;

    public int getValue() {
		return value;
	}

	private PayonEnum(int value) {
        this.value = value;
    }

    public static PayonEnum[] valuesArray = PayonEnum.values();

    public static PayonEnum fromInt(int value) {
        return valuesArray[value];
    }
    
    public String getAsString() {
   		switch (value) {
		case 0:
			return "on each payday of income source";
		case 1:
		case 21:
		case 31:
			return Integer.toString(value)+"st";
		case 2:
		case 22:
			return Integer.toString(value)+"nd";
		case 3:
		case 23:
			return Integer.toString(value)+"rd";
		default:
			return Integer.toString(value)+"th";
		}
	}


}


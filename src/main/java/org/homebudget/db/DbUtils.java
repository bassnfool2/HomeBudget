package org.homebudget.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.homebudget.HomeBudgetController;

public class DbUtils {
	
	public static int getLastGeneratedId(PreparedStatement stmt) throws SQLException {
		int id = -1;
		switch (HomeBudgetController.dbType) {
		case "derby" :
			ResultSet rset = stmt.getGeneratedKeys();
			rset.next();
			id = rset.getInt(1);
//			id = Long.valueOf(HomeBudgetController.getDbConnection().createStatement().executeQuery("select IDENTITY_VAL_LOCAL()").getLong(1)).intValue();
			return id;
		case "sqlite" :
			id = Long.valueOf(HomeBudgetController.getDbConnection().createStatement().executeQuery("SELECT last_insert_rowid()").getLong(1)).intValue();
			return id;
		default:
			throw new SQLException("Unknown database type!");
		}
	}

}

package org.homebudget.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

	public static void initNewDB() throws IOException, URISyntaxException, SQLException {
		System.out.println(DbUtils.class.getResource("derby.sql").toURI());
		Statement statement = HomeBudgetController.getDbConnection().createStatement();
		
		List<String> sqlScript = Files.readAllLines(
			    Paths.get(DbUtils.class.getResource("derby.sql").toURI()), Charset.defaultCharset());

		StringBuilder commandBuilder = new StringBuilder("");
		for ( String line : sqlScript) {
			if ( line.startsWith("--") || line.trim().isEmpty()) continue;
			commandBuilder.append(line.trim());
			commandBuilder.append("\n");
			if ( line.trim().endsWith(";")) {
				if ( !commandBuilder.toString().toUpperCase().startsWith("DROP")) {
					commandBuilder.setLength(commandBuilder.indexOf(";"));
					System.out.println(commandBuilder.toString());
					try {
						statement.execute(commandBuilder.toString());
					} catch ( SQLException e ) {
						e.printStackTrace();
					}
				}
				commandBuilder.setLength(0);
			}
		}
		System.out.println(commandBuilder);
	}

}

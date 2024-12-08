package org.homebudget.db;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		
		List<String> sqlScript = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(DbUtils.class.getResourceAsStream("derby.sql")));

		String inLine = null;
		while ((inLine = reader.readLine()) != null ) {
			sqlScript.add(inLine);
		}
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

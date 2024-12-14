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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings {
	static String OS = null;
	static Properties config = null;
	static String workingDirectory ="";
	public static void loadProperties() throws IOException {
		String OS = (System.getProperty("os.name")).toUpperCase();
		if (OS.contains("WIN")) {
		    workingDirectory = System.getenv("AppData")+File.pathSeparator+"HomeBudget"+File.pathSeparator;
		} else if (OS.toUpperCase().contains("LINUX")) {
		    workingDirectory = System.getProperty("user.home")+"/.local/share/HomeBudget/";
		}
		System.out.println(workingDirectory);
		Files.createDirectories(Paths.get(workingDirectory));
		File propertiesFile = new File(workingDirectory+"application.properties");
		if ( !propertiesFile.createNewFile()) {
			config = new Properties();
			config.load(new FileInputStream(propertiesFile));
		}
	}

	public static void saveProperties() throws IOException {
		String currentFile = HomeBudgetController.getHomeBudgetDb();
		StringBuilder newRecentFiles = new StringBuilder(currentFile);
		if (!( Settings.config.getProperty("recent.files") == null || Settings.config.getProperty("recent.files").trim().equals(""))) {
			String[] recentFiles = config.getProperty("recent.files").split(",");
			int counter = 0;
			for ( String filePath : recentFiles) {
				if ( filePath.equals(currentFile)) {
					continue;
				}
				newRecentFiles.append(",");
				newRecentFiles.append(filePath);
				counter++;
				if ( counter > 4) {
					break;
				}
			}
		}
		config.setProperty("recent.files",newRecentFiles.toString());
		config.store(new FileOutputStream(new File(workingDirectory+"application.properties")), "");
	}
}

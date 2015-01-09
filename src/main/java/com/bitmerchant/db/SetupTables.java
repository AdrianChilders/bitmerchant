package com.bitmerchant.db;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.bitcoinj.core.Coin;
import org.javalite.http.Delete;
import org.omg.CORBA.portable.Delegate;

import spark.utils.IOUtils;

import com.bitmerchant.tools.DataSources;
import com.google.common.io.Files;

public class SetupTables {
	public static final Boolean DELETE = true;
	
	public static void main( String args[] )
	{
		Connection c = null;
		Statement stmt = null;
		
		try {
			if (DELETE == true) {
				new File(DataSources.DB_FILE).delete();
				System.out.println("DB deleted");
			}
			
			Class.forName("org.sqlite.JDBC");
			if (!new File(DataSources.DB_FILE).exists()) {
				c = DriverManager.getConnection("jdbc:sqlite:" + DataSources.DB_FILE);
				System.out.println("Opened database successfully");

				stmt = c.createStatement();
				String sql =Files.toString(new File(DataSources.SQL_FILE), Charset.defaultCharset());
				stmt.executeUpdate(sql);
				stmt.close();
				c.close();
				
				System.out.println("Table created successfully");
			} else {
				System.out.println("DB already exists");
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}

	}
}

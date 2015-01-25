package com.bitmerchant.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitmerchant.db.Tables.ButtonStyle;
import com.bitmerchant.db.Tables.ButtonType;
import com.bitmerchant.db.Tables.Currency;
import com.bitmerchant.db.Tables.MerchantInfo;
import com.bitmerchant.db.Tables.OrderStatus;
import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;

public class InitializeTables {
	public static Boolean DELETE;
	public static Boolean FIRST_FILL = true;
	static final Logger log = LoggerFactory.getLogger(InitializeTables.class);
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		createTables();

		if (FIRST_FILL) {
			fillTables();
		}
	}
	
	public static void init(Boolean delete) {
		
		log.info("Using database located at : " + DataSources.DB_FILE());
		DELETE = delete;
		
		createTables();

		if (FIRST_FILL) {
			fillTables();
		}
	}



	public static void createTables() {
		Connection c = null;

		
		try {
			if (DELETE == true) {
				new File(DataSources.DB_FILE()).delete();
				log.info("DB deleted");
			}

			Class.forName("org.sqlite.JDBC");
			if (!new File(DataSources.DB_FILE()).exists()) {
				c = DriverManager.getConnection("jdbc:sqlite:" + DataSources.DB_FILE());
				log.info("Opened database successfully");

				Tools.runSQLFile(c, new File(DataSources.SQL_FILE));
				Tools.runSQLFile(c, new File(DataSources.SQL_VIEWS_FILE));

				c.close();

				log.info("Table created successfully");
			} else {
				log.info("DB already exists");
				FIRST_FILL = false;
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}


	public static void fillTables() {
		Tools.dbInit();

		log.info("Filling tables...");

		setupCurrencies();
		setupButtonStyles();
		setupButtonTypes();
		setupOrderStatuses();
		setupMerchantInfo();

		Tools.dbClose();
		log.info("Filled Tables succesfully");
	}



	private static void setupCurrencies() {

		for (Entry<String, String> e : TableConstants.CURRENCY_MAP.entrySet()) {
			// Unicode still not working
			Currency.createIt("iso", e.getKey(), "desc", e.getValue(), "unicode" , TableConstants.CURRENCY_UNICODES.get(e.getKey()));
		}
	}

	private static void setupButtonStyles() {
		for (String style: TableConstants.BUTTON_STYLES) {
			ButtonStyle.createIt("style", style);
		}
	}

	private static void setupOrderStatuses() {
		for (String status : TableConstants.ORDER_STATUSES) {
			OrderStatus.createIt("status", status);
		}
	}

	private static void setupButtonTypes() {
		for (String type: TableConstants.BUTTON_TYPES) {
			ButtonType.createIt("type", type);
		}
	}

	private static void setupMerchantInfo() {
		Integer currencyId = TableConstants.CURRENCY_LIST().indexOf("USD") + 1;
		MerchantInfo.createIt("currency_id", currencyId);

	}


}

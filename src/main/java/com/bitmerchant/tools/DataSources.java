package com.bitmerchant.tools;


public class DataSources {

	public static String APP_NAME = "bitmerchant-wallet";
	
	public static final Integer SPARK_WEB_PORT = 4567;
	
	// The path to the bitmerchant dir
	public static final String HOME_DIR = System.getProperty( "user.home" ) + "/.bitmerchant-wallet/";
	
	public static final String CODE_DIR = System.getProperty("user.dir");
	
	public static final String DB_FILE = HOME_DIR + APP_NAME + ".db";
	
	public static final String SQL_FILE = CODE_DIR + "/src/main/resources/bitmerchant-wallet.sql";
	
	
	
	
	
}

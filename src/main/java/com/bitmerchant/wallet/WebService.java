package com.bitmerchant.wallet;

import static spark.Spark.get;
import static spark.SparkBase.setPort;
import static spark.SparkBase.externalStaticFileLocation;
import static spark.SparkBase.staticFileLocation;
import com.bitmerchant.Tools;


public class WebService {
	public static void main(String[] args) {
		setPort(4567);
		
		staticFileLocation("/web"); // Static files
//		externalStaticFileLocation("/home/tyler/git/bitmerchant-wallet/src/main/resources/web"); // Static files


		
		get("/hello", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return "hi from the bitmerchant wallet web service";
		});
		
	}
}

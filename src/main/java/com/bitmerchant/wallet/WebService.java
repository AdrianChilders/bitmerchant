package com.bitmerchant.wallet;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.util.Map;
import java.util.NoSuchElementException;

import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;


public class WebService {
	public static void start() {
		setPort(DataSources.SPARK_WEB_PORT);
		
		staticFileLocation("/web"); // Static files
		

		get("/hello", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return "hi from the bitmerchant wallet web service";
		});
		get("/garp", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			res.redirect("/html/main2.html");
			return null;
		});
		
		get("/status_progress", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
//			return lw.controller.getStatusProgress();
			return LocalWallet.instance.controller.getStatusProgress();
		
		});
		
		get("/status_text", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getStatusText();
		});
		
		get("/recieve_address", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getAddressText();
		});
		
		get("/balance", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getBalanceText();
		});
		
		get("/wallet_words", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getWalletWords();
		});
		
		get("/wallet_creation_date", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getWalletCreationDateStr();
		});
		
		get("/wallet_is_encrypted", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getWalletIsEncrypted();
		});
		get("/wallet_is_locked", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getWalletIsLocked();
		});
		
		post("/set_wallet_password", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
				String password = Tools.createMapFromAjaxPost(req.body()).get("password");
				String message = LocalWallet.instance.controller.setWalletPassword(password);
				return message;
		});
		
		post("/remove_wallet_password", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
				String password = Tools.createMapFromAjaxPost(req.body()).get("password");
				String message = LocalWallet.instance.controller.removeWalletPassword(password);

				return message;
		});
		
		post("/unlock_wallet", (req, res) -> {
			try {
			Tools.allowResponseHeaders(req, res);
				String password = Tools.createMapFromAjaxPost(req.body()).get("password");
				String message = LocalWallet.instance.controller.unlockWallet(password);
				
				return message;
				
			} catch (NoSuchElementException e) {
					res.status(666);
					return e.getMessage();
				}
			
		});
		
		post("/restore_wallet", (req, res) -> {
			try {
			Tools.allowResponseHeaders(req, res);
				
				Map<String, String> formItems = Tools.createMapFromAjaxPost(req.body());
				
				String walletWords = formItems.get("wallet_words");
				String dateStr = formItems.get("wallet_creation_date");
				
				String message = LocalWallet.instance.controller.restoreWallet(walletWords, dateStr);
				
				return message;
				
			} catch (NoSuchElementException e) {
					res.status(666);
					return e.getMessage();
				}
			
		});
		
		post("/send_money", (req, res) -> {
			try {
			Tools.allowResponseHeaders(req, res);
				
				Map<String, String> formItems = Tools.createMapFromAjaxPost(req.body());
				
				String amount = formItems.get("sendAmount");
				String toAddress = formItems.get("address");
				
				String message = LocalWallet.instance.controller.sendMoney(amount, toAddress);
				
				return message;
				
			} catch (NoSuchElementException e) {
					res.status(666);
					return e.getMessage();
				}
			
		});
		
		get("/send_status", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getSendStatus();
		});
	
		
		
	}
}

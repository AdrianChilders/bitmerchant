package com.bitmerchant.wallet;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.bitcoinj.crypto.MnemonicException;

import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;


public class WebService {
	
	// How long to keep the cookies
	public static final Integer COOKIE_EXPIRE_SECONDS = Tools.cookieExpiration(1440);
	
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
		
		get("/receive_address", (req, res) -> {
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
			try {
			Tools.allowResponseHeaders(req, res);
				String password = Tools.createMapFromAjaxPost(req.body()).get("password");
				String message = LocalWallet.instance.controller.removeWalletPassword(password);

				return message;
				
			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
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
		
		
		post("/send_money_encrypted", (req, res) -> {
			try {
			Tools.allowResponseHeaders(req, res);
				
				Map<String, String> formItems = Tools.createMapFromAjaxPost(req.body());
				
				String amount = formItems.get("sendAmount");
				String toAddress = formItems.get("address");
				String password = formItems.get("password");
				
				LocalWallet.instance.controller.unlockWallet(password);
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
		
		get("/get_transactions", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.instance.controller.getTransactionsJSON();
		});
		
		get("/newest_received_tx", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			// use a cookie, not a return
//			res.cookie("newestReceivedTransaction", LocalWallet.instance.controller.getNewestReceivedTransaction(),
//					COOKIE_EXPIRE_SECONDS, true);
			res.cookie("newestReceivedTransaction", LocalWallet.instance.controller.getNewestReceivedTransactionHash(),
					COOKIE_EXPIRE_SECONDS);
			return LocalWallet.instance.controller.getNewestReceivedTransaction();
		});
		
	
		
		
	}
}

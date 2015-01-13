package com.bitmerchant.wallet;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;
import static spark.SparkBase.externalStaticFileLocation;




import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.NoSuchElementException;




import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.bitcoinj.crypto.MnemonicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import com.bitmerchant.db.Actions;
import com.bitmerchant.tools.Connections;
import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;import com.google.protobuf.ByteString;


/*
 * TODO not all of these should be exposed to the web. Really only the paymentrequest
 */
public class WebService {

	// How long to keep the cookies
	public static final Integer COOKIE_EXPIRE_SECONDS = Tools.cookieExpiration(1440);
	static final Logger log = LoggerFactory.getLogger(WebService.class);

	public static void start() {
		setPort(DataSources.SPARK_WEB_PORT);

		staticFileLocation("/web"); // Static files
		externalStaticFileLocation(DataSources.HOME_DIR);
//		externalStaticFileLocation(DataSources.CODE_DIR);




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
			return LocalWallet.INSTANCE.controller.getStatusProgress();

		});

		get("/status_text", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getStatusText();
		});

		get("/receive_address", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getAddressText();
		});

		get("/balance", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getBalanceText();
		});

		get("/wallet_words", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletWords();
		});

		get("/wallet_creation_date", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletCreationDateStr();
		});

		get("/wallet_is_encrypted", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletIsEncrypted();
		});
		get("/wallet_is_locked", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletIsLocked();
		});

		post("/set_wallet_password", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			String password = Tools.createMapFromAjaxPost(req.body()).get("password");
			String message = LocalWallet.INSTANCE.controller.setWalletPassword(password);
			return message;
		});

		post("/remove_wallet_password", (req, res) -> {
			try {
				Tools.allowResponseHeaders(req, res);
				String password = Tools.createMapFromAjaxPost(req.body()).get("password");
				String message = LocalWallet.INSTANCE.controller.removeWalletPassword(password);

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
				String message = LocalWallet.INSTANCE.controller.unlockWallet(password);

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

				String message = LocalWallet.INSTANCE.controller.restoreWallet(walletWords, dateStr);

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

				String message = LocalWallet.INSTANCE.controller.sendMoney(amount, toAddress);

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

				LocalWallet.INSTANCE.controller.unlockWallet(password);
				String message = LocalWallet.INSTANCE.controller.sendMoney(amount, toAddress);

				return message;

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/send_status", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getSendStatus();
		});

		get("/get_transactions", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getTransactionsJSON();
		});

		get("/newest_received_tx", (req, res) -> {
			Tools.allowResponseHeaders(req, res);
			// use a cookie, not a return
			//			res.cookie("newestReceivedTransaction", LocalWallet.instance.controller.getNewestReceivedTransaction(),
			//					COOKIE_EXPIRE_SECONDS, true);
			res.cookie("newestReceivedTransaction", LocalWallet.INSTANCE.controller.getNewestReceivedTransactionHash(),
					COOKIE_EXPIRE_SECONDS);
			return LocalWallet.INSTANCE.controller.getNewestReceivedTransaction();
		});

		get("/payment_request/:order", "application/octet-stream", (req, res) -> {
			res.type("application/octet-stream");
			
			Tools.allowResponseHeaders(req, res);
			Tools.dbInit();
			
			
			Integer orderNum = Integer.valueOf(req.params(":order"));
			PaymentRequest pr = PaymentTools.createPaymentRequestFromOrder(orderNum);

			Tools.dbClose();
			log.info("paymentreq : " + pr.toString());
			OutputStream os;
			try {
				File btcRequestFolder = new File(DataSources.HOME_DIR + 
						"/payment_requests/" );
				btcRequestFolder.mkdirs();
				
				File btcFile = new File(btcRequestFolder.getAbsolutePath().concat("/" + orderNum + ".bitcoinpaymentrequest"));
				
				
				os = new FileOutputStream(btcFile);
			
				pr.writeTo(os);
				os.flush();
				os.close();
				
				res.redirect("/payment_requests/" + orderNum + ".bitcoinpaymentrequest");
				
				return "";
			} catch (Exception e) {
				// TODO Auto-generated catch block
			
				e.printStackTrace();
				return null;
			}
		});

		//		post("/create_button", (req, res) -> {
		//			Tools.allowResponseHeaders(req, res);
		//			return Actions.createButton(req.body());
		//			
		//		});


	}
}

package com.bitmerchant.wallet;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.externalStaticFileLocation;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;





import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NoSuchElementException;





import javax.servlet.http.HttpServletResponse;


import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;


/*
 * TODO not all of these should be exposed to the web. Really only the paymentrequest
 */
public class WebService {

	// How long to keep the cookies
	public static final Integer COOKIE_EXPIRE_SECONDS = Tools.cookieExpiration(1440);
	static final Logger log = LoggerFactory.getLogger(WebService.class);
	
	public static final String PROTOCOL_CONTENT_TYPE = "application/octet-stream";
	public static final String PAYMENTREQUEST_CONTENT_TYPE = "application/bitcoin-paymentrequest";

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

	
		get("/create_order/:order",PAYMENTREQUEST_CONTENT_TYPE , (req, res) -> {
			res.type(PAYMENTREQUEST_CONTENT_TYPE);

			Tools.allowResponseHeaders(req, res);
			Tools.dbInit();

			Integer orderNum = Integer.valueOf(req.params(":order"));
			PaymentRequest pr = PaymentTools.createPaymentRequestFromOrder(orderNum);

			Tools.dbClose();
			log.info("paymentreq : " + pr.toString());
			

			String orderPath = PaymentTools.writePaymentRequestToFile(orderNum, pr);

			res.redirect("/payment_requests" + orderPath);

			return "can not touch this code . jpg";

		});
		
		get("/orders_alt/:order",PAYMENTREQUEST_CONTENT_TYPE , (req, res) -> {
			res.type(PAYMENTREQUEST_CONTENT_TYPE);
			res.header("Content-Transfer-Encoding", "binary");
			
			Integer orderNum = Integer.valueOf(req.params(":order"));
			String orderPath = DataSources.HOME_DIR + 
					"/orders/order_" + orderNum + ".bitcoinpaymentrequest";
//			res.redirect("/payment_requests" + orderPath);
			
			// try to return the bytes of the file
//			Files.readAllBytes(new File(orderPath));
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(orderPath));			
				
				HttpServletResponse raw = res.raw();
				
				PaymentRequest pr = PaymentRequest.parseFrom(bytes);
				
				pr.writeTo(raw.getOutputStream());
				
				raw.getOutputStream().flush();
				raw.getOutputStream().close();
				
				
				return res.raw();
				
				
				
				
//				return new File(orderPath);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return "can not touch this code . jpg";
		});
		//		post("/create_button", (req, res) -> {
		//			Tools.allowResponseHeaders(req, res);
		//			return Actions.createButton(req.body());
		//			
		//		});


	}


}

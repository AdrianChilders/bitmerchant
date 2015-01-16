package com.bitmerchant.webservice;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.externalStaticFileLocation;
import static spark.SparkBase.setPort;
import static spark.SparkBase.staticFileLocation;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

import org.bitcoin.protocols.payments.Protos.Payment;
import org.bitcoin.protocols.payments.Protos.PaymentACK;
import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitmerchant.db.Actions;
import com.bitmerchant.db.Actions.OrderActions;
import com.bitmerchant.db.Actions.PaymentActions;
import com.bitmerchant.db.Tables.Button;
import com.bitmerchant.db.Tables.Order;
import com.bitmerchant.db.Tables.OrderView;
import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;
import com.bitmerchant.wallet.LocalWallet;


/*
 * TODO not all of these should be exposed to the web. Really only the paymentrequest
 * 
 */

public class WebService {

	// How long to keep the cookies
	public static final Integer COOKIE_EXPIRE_SECONDS = Tools.cookieExpiration(1440);
	static final Logger log = LoggerFactory.getLogger(WebService.class);

	public static final String PROTOCOL_CONTENT_TYPE = "application/octet-stream";
	public static final String PAYMENTREQUEST_CONTENT_TYPE = "application/bitcoin-paymentrequest";
	public static final String PAYMENT_CONTENT_TYPE = "application/bitcoin-payment";
	public static final String PAYMENT_ACK_CONTENT_TYPE = "application/bitcoin-paymentack";

	public static final String JSON_CONTENT_TYPE = "application/json";




	public static void start() {
		setPort(DataSources.SPARK_WEB_PORT) ;

		staticFileLocation("/web"); // Static files
		externalStaticFileLocation(DataSources.HOME_DIR);
		//		externalStaticFileLocation(DataSources.CODE_DIR);

		
		get("/hello", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return "hi from the bitmerchant wallet web service";
		});
		
		get("/garp", (req, res) -> {

			Tools.allowOnlyLocalHeaders(req, res);
			res.redirect("/html/main2.html");
			return null;
		});

		get("/status_progress", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			//			return lw.controller.getStatusProgress();
			return LocalWallet.INSTANCE.controller.getStatusProgress();

		});

		get("/status_text", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getStatusText();
		});

		get("/receive_address", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getAddressText();
		});

		get("/balance", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getBalanceText();
		});

		get("/wallet_words", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletWords();
		});

		get("/wallet_creation_date", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletCreationDateStr();
		});

		get("/wallet_is_encrypted", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletIsEncrypted();
		});
		get("/wallet_is_locked", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getWalletIsLocked();
		});

		post("/set_wallet_password", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			String password = Tools.createMapFromAjaxPost(req.body()).get("password");
			String message = LocalWallet.INSTANCE.controller.setWalletPassword(password);
			return message;
		});

		post("/remove_wallet_password", (req, res) -> {
			try {
				Tools.allowOnlyLocalHeaders(req, res);
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
				Tools.allowOnlyLocalHeaders(req, res);
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
				Tools.allowOnlyLocalHeaders(req, res);

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
				Tools.allowOnlyLocalHeaders(req, res);

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
				Tools.allowOnlyLocalHeaders(req, res);

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
			Tools.allowOnlyLocalHeaders(req, res);
			return LocalWallet.INSTANCE.controller.getSendStatus();
		});

		get("/get_transactions", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			Tools.dbInit();
			String txs = LocalWallet.INSTANCE.controller.getTransactionsJSON();
			Tools.dbClose();
			return txs;

		});

		get("/newest_received_tx", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			// use a cookie, not a return
			//			res.cookie("newestReceivedTransaction", LocalWallet.instance.controller.getNewestReceivedTransaction(),
			//					COOKIE_EXPIRE_SECONDS, true);
			res.cookie("newestReceivedTransaction", LocalWallet.INSTANCE.controller.getNewestReceivedTransactionHash(),
					COOKIE_EXPIRE_SECONDS);
			return LocalWallet.INSTANCE.controller.getNewestReceivedTransaction();
		});

		post("/create_button",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);
			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				// Creates the button and the button from the req body
				Button b = Actions.ButtonActions.createButton(req.body());

				String json = Actions.ButtonActions.showButton(Integer.valueOf(b.getId().toString()));
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/buttons/:button",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				// Creates the button and the button from the req body
				Integer buttonNum = Integer.valueOf(req.params(":button"));

				String json = Actions.ButtonActions.showButton(buttonNum);
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/buttons/:button/create_order",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				Integer buttonNum = Integer.valueOf(req.params(":button"));

				Order o = Actions.OrderActions.createOrderFromButton(buttonNum);


				String json = Actions.OrderActions.showOrder(Integer.valueOf(o.getId().toString()));
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/buttons",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				String json = Actions.ButtonActions.listButtons();
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});


		post("/create_order",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);
			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				// Creates the order and the button from the req body
				Order o = Actions.OrderActions.createOrder(req.body());

				String json = Actions.OrderActions.showOrder(Integer.valueOf(o.getId().toString()));
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/orders/:order",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				// Creates the order and the button from the req body
				Integer orderNum = Integer.valueOf(req.params(":order"));

				String json = Actions.OrderActions.showOrder(orderNum);
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/orders",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				String json = Actions.OrderActions.listOrders();
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});


		get("/payment_request/:order",PAYMENTREQUEST_CONTENT_TYPE , (req, res) -> {
			res.type(PAYMENTREQUEST_CONTENT_TYPE);
			res.header("Content-Transfer-Encoding", "binary");
			try {
				Integer orderNum = Integer.valueOf(req.params(":order"));

				Tools.dbInit();

				// Fetch the order
				Order o = Order.findById(orderNum);

				// Builds a payment request from the order row
				PaymentRequest pr = OrderActions.createPaymentRequestFromOrder(Integer.valueOf(o.getId().toString()));

				Tools.dbClose();

				HttpServletResponse raw = res.raw();
				pr.writeTo(raw.getOutputStream());
				raw.getOutputStream().flush();
				raw.getOutputStream().close();


				return res.raw();
			} catch (NoSuchElementException  | IOException e) {
				res.status(666);
				return e.getMessage();
			}
		});

		post("/create_payment/:order", PAYMENT_ACK_CONTENT_TYPE , (req, res) -> {
			try {
				res.type(PAYMENT_ACK_CONTENT_TYPE);
				res.header("Content-Transfer-Encoding", "binary");


				Tools.dbInit();

				Integer orderNum = Integer.valueOf(req.params(":order"));

				Payment payment = Payment.parseFrom(req.raw().getInputStream());

				PaymentActions.savePaymentToRow(payment, orderNum);

				PaymentACK pa = PaymentActions.createPaymentAck(payment);

				HttpServletResponse raw = res.raw();
				pa.writeTo(raw.getOutputStream());
				raw.getOutputStream().flush();
				raw.getOutputStream().close();

				Tools.dbClose();

				log.info("payment = " + payment);

				return res.raw();
			} catch (NoSuchElementException  | IOException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/refund/:order", JSON_CONTENT_TYPE , (req, res) -> {
			try {

				Tools.dbInit();
				Integer orderNum = Integer.valueOf(req.params(":order"));

				OrderView ov = OrderView.findById(orderNum);

				// Get the native currency
				String nativeCurrencyIso = ov.getString("native_currency_iso");
				String amount = ov.getString("total_native");



				String message = PaymentActions.sendRefund(orderNum, amount, nativeCurrencyIso);

				Tools.dbClose();

				return message;

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/refund/:order/:amount/:currency", JSON_CONTENT_TYPE , (req, res) -> {
			try {

				Tools.dbInit();

				Integer orderNum = Integer.valueOf(req.params(":order"));
				String amount =req.params(":amount");
				String nativeCurrencyIso = req.params(":currency");


				String message = PaymentActions.sendRefund(orderNum, amount, nativeCurrencyIso);

				Tools.dbClose();

				return message;

			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/currencies", JSON_CONTENT_TYPE , (req, res) -> {
			try {
				res.type(JSON_CONTENT_TYPE);

				Tools.dbInit();
				String json = Actions.listCurrencies();
				Tools.dbClose();

				return json;
			} catch (NoSuchElementException e) {
				res.status(666);
				return e.getMessage();
			}
		});


		//		post("/create_button", (req, res) -> {
		//			Tools.allowResponseHeaders(req, res);
		//			return Actions.createButton(req.body());
		//			
		//		});


	}


}

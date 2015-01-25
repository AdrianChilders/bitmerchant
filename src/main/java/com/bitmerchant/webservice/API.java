package com.bitmerchant.webservice;

import static spark.Spark.get;
import static spark.Spark.post;

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
import com.bitmerchant.tools.Tools;


public class API {
	
	static final Logger log = LoggerFactory.getLogger(API.class);
	
	public static final String PROTOCOL_CONTENT_TYPE = "application/octet-stream";
	public static final String PAYMENTREQUEST_CONTENT_TYPE = "application/bitcoin-paymentrequest";
	public static final String PAYMENT_CONTENT_TYPE = "application/bitcoin-payment";
	public static final String PAYMENT_ACK_CONTENT_TYPE = "application/bitcoin-paymentack";
	public static final String JSON_CONTENT_TYPE = "application/json";
	
	public static void setup() {

		post("/api/create_button",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);
			try {
				
				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				// Creates the button and the button from the req body
				Button b = Actions.ButtonActions.createButton(Tools.jsonToNode(req.body()));

				String json = Actions.ButtonActions.showButton(Integer.valueOf(b.getId().toString()));
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/api/buttons/:button",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				
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
		
		get("/api/buttons/:button/orders",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				// Creates the button and the button from the req body
				Integer buttonNum = Integer.valueOf(req.params(":button"));
				
				String json = OrderActions.listOrdersForButton(buttonNum);
				
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/api/buttons/:button/create_order",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

//				Tools.allowOnlyLocalHeaders(req, res);
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
		
		/**
		 * This is done when there is a variable price selection
		 */
		post("/api/buttons/:button/create_order/:custom_native_price",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

//				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				Integer buttonNum = Integer.valueOf(req.params(":button"));
				String customPrice = req.params(":custom_native_price");

				Order o = Actions.OrderActions.createOrderFromButton(buttonNum, customPrice);
				

				String json = Actions.OrderActions.showOrder(Integer.valueOf(o.getId().toString()));
				Tools.dbClose();

				return json; 

			} catch (NoSuchElementException  | ArithmeticException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/api/buttons",JSON_CONTENT_TYPE , (req, res) -> {
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


		post("/api/create_order",JSON_CONTENT_TYPE , (req, res) -> {
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
		
		post("/api/generate_button" , (req, res) -> {
		
			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				Map<String, String> postMap = Tools.createMapFromAjaxPost(req.body());
				// Creates the button and the button from the post map
				Button b = Actions.ButtonActions.createButton(Tools.createNodeFromPost("button", postMap));				
				
				String buttonCode = Actions.ButtonActions.generateButtonCode(b, postMap.get("frameType"));
				Tools.dbClose();
				
				return buttonCode; 

			} catch (NoSuchElementException  e) {
				res.status(666);
				return e.getMessage();
			}

		});

		get("/api/orders/:order",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

//				Tools.allowOnlyLocalHeaders(req, res);
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

		get("/api/orders",JSON_CONTENT_TYPE , (req, res) -> {
			res.type(JSON_CONTENT_TYPE);

			try {

				Tools.allowOnlyLocalHeaders(req, res);
				Tools.dbInit();

				String json = Actions.OrderActions.listAllOrders();
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

		post("/api/create_payment/:order", PAYMENT_ACK_CONTENT_TYPE , (req, res) -> {
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

				log.info("BIP70 payment created = " + payment);

				return res.raw();
			} catch (NoSuchElementException  | IOException e) {
				res.status(666);
				return e.getMessage();
			}

		});

		post("/api/refund/:order", JSON_CONTENT_TYPE , (req, res) -> {
			try {

				Tools.allowOnlyLocalHeaders(req, res);
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

		post("/api/refund/:order/:amount/:currency", JSON_CONTENT_TYPE , (req, res) -> {
			try {

				Tools.allowOnlyLocalHeaders(req, res);
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

		get("/api/currencies", JSON_CONTENT_TYPE , (req, res) -> {
			try {
				Tools.allowOnlyLocalHeaders(req, res);
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


	}
}

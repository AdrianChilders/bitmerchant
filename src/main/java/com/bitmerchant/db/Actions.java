package com.bitmerchant.db;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bitcoin.protocols.payments.Protos.Output;
import org.bitcoin.protocols.payments.Protos.Payment;
import org.bitcoin.protocols.payments.Protos.PaymentACK;
import org.bitcoin.protocols.payments.Protos.PaymentDetails;
import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.javalite.activejdbc.Model;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitmerchant.db.Tables.Button;
import com.bitmerchant.db.Tables.ButtonStyle;
import com.bitmerchant.db.Tables.ButtonType;
import com.bitmerchant.db.Tables.ButtonView;
import com.bitmerchant.db.Tables.Currency;
import com.bitmerchant.db.Tables.Order;
import com.bitmerchant.db.Tables.OrderStatus;
import com.bitmerchant.db.Tables.OrderView;
import com.bitmerchant.db.Tables.Refund;
import com.bitmerchant.tools.Connections;
import com.bitmerchant.tools.CurrencyConverter;
import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;
import com.bitmerchant.wallet.LocalWallet;
import com.google.protobuf.ByteString;

public class Actions {

	static final Logger log = LoggerFactory.getLogger(Actions.class);



	public static class ButtonActions {
		public static Button createButtonObj(JsonNode root) {

			JsonNode n = root.get("button");

			// Create the DB record for the button
			// Get the button style, type, and currency id

			// Only name, price_string, and price_iso are required

			ButtonStyle style = (n.get("style") != null) ? ButtonStyle.findFirst("style=?", n.get("style").asText()) : null;

			ButtonType type = (n.get("type") != null) ? ButtonType.findFirst("type=?", n.get("type").asText())  : null;

			Currency currency = (n.get("price_currency_iso") != null) ? Currency.findFirst("iso=?", n.get("price_currency_iso").asText()) :null;

			Button b = new Button();

			// required
			b.set("name", n.get("name").asText());
			b.set("total_native", n.get("price_string").asText());
			b.set("native_currency_id", currency.getId().toString());


			// optionals
			if (type != null) 
				b.set("type_id", type.getId().toString());
			if (style != null)
				b.set("style_id", style.getId().toString());
			if (n.get("network") != null)
				b.set("network",  n.get("network").asText());
			if (n.get("text") != null)
				b.set("text",  n.get("text").asText());
			if (n.get("description") != null)
				b.set("description", n.get("description").asText());
			if (n.get("callback_url") != null)
				b.set("callback_url", n.get("callback_url").asText());
			if  (n.get("variable_price") != null) 
				b.set("variable_price", n.get("variable_price").asText());
			if (n.get("price_select") != null)
				b.set("price_select", n.get("price_select").asText());
			for (int i=1;  i<=5; i++) {
				if (n.get("price_" + i) != null)
					b.set("price_" + i,  n.get("price_" + i).asText());
			}


			return b;
		}

		public static String showButton(Integer id) {
			ButtonView b = ButtonView.findById(id);

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode a = mapper.createObjectNode();


			JsonNode n = Tools.jsonToNode(b.toJson(false));
			a.put("button", n);
			try {
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(a);
			} catch (IOException e) {

				e.printStackTrace();
				return null;
			}
		}

		public static String listButtons() {
			List<ButtonView> btns = ButtonView.findAll();

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode a = mapper.createObjectNode();

			ArrayNode an = a.putArray("buttons");

			for (ButtonView ov : btns) {
				ObjectNode b = mapper.createObjectNode();
				b.put("button", Tools.jsonToNode(ov.toJson(false)));
				an.add(b);
			}

			try {
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(a);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}



		/**
		 * 
		 *A Sample Request json <br>
		 *{
 "button" : {
    "name": "shoe",
    "type": "buy_now",
    "style": "custom_large",
    "network": "test",
    "text": "Buy with USD/BTC",
    "price_string": "1.23",
    "price_currency_iso": "USD",
    "callback_url": "http://www.example.com/my_custom_button_callback",
    "description": "Sample description"
  }

}
		 * @param jsonReq
		 * @return 
		 */
		public static Button createButton(JsonNode root) {

			// Create the button object, save it to the db
			Button b = createButtonObj(root);
			b.saveIt();

			return b;

		}

		public static String generateButtonCode(Button b, String type) {
			String code = null;
			String buttonId = b.getId().toString();
			if (type.equals("iframe")) {
				code = "&lt;iframe name=&quot;" + buttonId + "&quot; src=&quot;http://96.28.13.51:4567/html/payment_iframe.html&quot; style=&quot;width: 460px; height: 350px; border: none; box-shadow: 0 1px 3px rgba(0,0,0,0.25); &quot; allowtransparency=&quot;true&quot; frameborder=&quot;0&quot; white-space=&quot;nowrap&quot;&gt;&lt;/iframe&gt;";
			} else if (type.equals("button")) {
				code =  "&lt;a name=&quot;" + buttonId + "&quot; class=&quot;bitmerchant-button ui-button ui-widget ui-corner-all ui-state-default ui-button-text-only&quot; href=&quot;http://96.28.13.51:4567/html/payment_iframe.html&quot; data-title=&quot;Purchase&quot; data-width=&quot;460&quot; data-height=&quot;350&quot;&gt;&lt;script src=&quot;http://96.28.13.51:4567/html/payment_button.js&quot; type=&quot;text/javascript&quot;&gt;&lt;/script&gt;&lt;span class=&quot;ui-button-text&quot;&gt; Pay with Bitcoin &lt;/span&gt;&lt;/a&gt;";	
			}

			return code;

		}




	}




	public static class OrderActions {

		/**
		 * This is a shortcut of 
		 * @param root
		 * @return
		 */
		public static Order createOrderObj(Integer buttonId, String customPrice) {

			Button b = Button.findById(buttonId);

			Currency currency = Currency.findById(b.getInteger("native_currency_id"));
			String currencyIso = currency.getString("iso");

			Order o = new Order();

			o.set("status_id", OrderStatus.findFirst("status=?", "new").getId().toString());

			// TODO For now, set the memo to the button name
			o.set("memo", b.getString("nameadv"));

			// Set the currency to the most recent currency
			CurrencyConverter cc = CurrencyConverter.INSTANCE;
			long satoshis;
			if (customPrice == null) {
				satoshis = cc.convertToSatoshisCurrent(currencyIso, b.getBigDecimal("total_native"));
			} else {
				satoshis = cc.convertToSatoshisCurrent(currencyIso, new BigDecimal(customPrice));
			}


			o.set("total_satoshis", satoshis);

			o.set("expire_time", System.currentTimeMillis() + 600000);
			o.set("button_id", Integer.valueOf(b.getId().toString()));

			o.set("merchant_data", "DickTowel.com");





			// needs to be left for later until the file is created
			//			String paymentURL = DataSources.WEB_SERVICE_URL + "/payment_request/" + 
			//					o.getId().toString() + ".bitcoinpaymentrequest";
			//			o.set("payment_url", paymentURL);

			// log.info("params = " + LocalWallet.params.getId());


			return o;
		}

		/**
		 * A combination of create button and create order
		 * @param root
		 * @return
		 */
		public static Order createOrderObj(JsonNode root) {
			Button b = ButtonActions.createButtonObj(root);

			if (b.getId() ==  null) {
				b.saveIt();
			}

			Order o = createOrderObj(Integer.valueOf(b.getId().toString()), null);

			return o;


		}

		public static String showOrder(Integer id) {

			OrderView ov = OrderView.findById(id);

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode a = mapper.createObjectNode();


			JsonNode n = Tools.jsonToNode(ov.toJson(false));
			a.put("order", n);

			try {
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(a);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}


		}

		public static String listOrders(List<OrderView> ovs) {

			ObjectMapper mapper = new ObjectMapper();
			ObjectNode a = mapper.createObjectNode();

			ArrayNode an = a.putArray("orders");

			for (OrderView ov : ovs) {
				ObjectNode b = mapper.createObjectNode();
				b.put("order", Tools.jsonToNode(ov.toJson(false)));
				an.add(b);
			}

			try {
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(a);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		public static String listAllOrders() {
			List<OrderView> ovs = OrderView.findAll();

			return listOrders(ovs);
		}
		public static String listOrdersForButton(Integer buttonId) {
			List<OrderView> ovs = OrderView.find("button_id=?", buttonId);
			return listOrders(ovs);
		}

		public static Order createOrder(String jsonReq) {

			// Parse the JSON into a tree
			JsonNode root = Tools.jsonToNode(jsonReq);

			// Create the button and  object, save it to the db
			Order o = createOrderObj(root);
			o.saveIt();

			// Add the payment_url from the order id(can only be done after its saved to get the id)
			// and the receive address
			o = updateExtraInfo(o);

			return o;

		}

		public static Order createOrderFromButton(Integer buttonId, String customPrice) {


			Order o = createOrderObj(buttonId, customPrice);
			o.saveIt();

			// Add the payment_url from the order id(can only be done after its saved to get the id)
			// and the receive address
			o = updateExtraInfo(o);


			return o;

		}
		
		public static Order createOrderFromButton(Integer buttonId) {
			return createOrderFromButton(buttonId, null);
		}

		public static Order updateExtraInfo(Order o) {
			String id = o.getId().toString();


			String paymentURL = DataSources.WEB_SERVICE_URL + "api/create_payment/" + id;
			o.set("payment_url", paymentURL);

			Address receiveAddr = bitcoin.wallet().freshReceiveAddress();
			o.set("receive_address", receiveAddr.toString());
			BigDecimal btcAmount = BigDecimal.valueOf(o.getLong("total_satoshis"), 8);

			String paymentRequestURL = "bitcoin:" + receiveAddr.toString() + "?" + 
					"r=" + DataSources.WEB_SERVICE_URL + "payment_request/" + id + "&" + 
					"amount=" + btcAmount;
			o.set("payment_request_url", paymentRequestURL);

			// Set the network on the button
			String network = (bitcoin.params().getId().equals("org.bitcoin.test")) ? "test" : "main";
			Button b = Button.findById(o.getInteger("button_id"));
			b.set("network", network);


			b.saveIt();
			o.saveIt();

			return o;
		}


		public static PaymentRequest createPaymentRequestFromOrder(Integer orderNum) {
			Order o = Order.findById(orderNum);
			return createPaymentRequestFromOrder(o);
		}

		public static PaymentRequest createPaymentRequestFromOrder(Order o) {

			// Remember, need to make some updates to the order row too

			Output.Builder ob = Output.newBuilder();

			Coin amount = Coin.valueOf(o.getLong("total_satoshis"));
			ob.setAmount(amount.value);


			Transaction tx = new Transaction(bitcoin.params());		

			try {
				Address receiveAddr = new Address(bitcoin.params(), o.getString("receive_address"));

				TransactionOutput outputToMe = new TransactionOutput(bitcoin.params(), tx, amount, receiveAddr);
				ob.setScript(ByteString.copyFrom(outputToMe.getScriptBytes()));
			} catch (AddressFormatException e) {
				e.printStackTrace();
			}

			PaymentDetails.Builder pdB = PaymentDetails.newBuilder();

			String network = (bitcoin.params().getId().equals("org.bitcoin.test")) ? "test" : "main";
			pdB.setNetwork(network);

			pdB.setTime(o.getLong("created_at")/1000L);


			pdB.setExpires(o.getLong("expire_time")/1000L);

			// TODO can't do this due to SSL
			pdB.setPaymentUrl(o.getString("payment_url"));


			pdB.addOutputs(ob);

			if (o.getString("memo") != null) 
				pdB.setMemo(o.getString("memo"));

			if (o.getString("merchant_data") != null) 
				pdB.setMerchantData(ByteString.copyFromUtf8(o.getString("merchant_data")));

			PaymentDetails pd = pdB.build();

			PaymentRequest pr = PaymentRequest.newBuilder()
					.setPaymentDetailsVersion(1)
					.setPkiType("none")
					.setSerializedPaymentDetails(pd.toByteString())
					.build();

			return pr;

		}



		/**
		 *  @deprecated No need to write to a file anymore, just build the request from the order row
		 */
		@Deprecated 
		public static String writePaymentRequestToFile(Integer orderNum,
				PaymentRequest pr) {
			OutputStream os;
			File btcRequestFolder = new File(DataSources.HOME_DIR + 
					"/orders/" );
			btcRequestFolder.mkdirs();

			String orderPath = "/order_" + orderNum + ".bitcoinpaymentrequest";
			File btcFile = new File(btcRequestFolder.getAbsolutePath().concat(orderPath));
			try {
				os = new FileOutputStream(btcFile);

				pr.writeTo(os);
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return orderPath;
		}

		public static void updateOrderFromTransactionReceived(Transaction tx) {
			// associate the tx outputs with the order receive_addresses 
			// Get addresses

			Connections.INSTANCE.open();


			for (TransactionOutput txo : tx.getOutputs()) {
				String txReceive = Tools.getTransactionOutputAddress(txo);

				Order o = Order.findFirst("receive_address=?", txReceive);

				if (o != null) {
					// Found it! now update the row
					log.info("Associating order #" + o.getId() + " with tx " +  tx.getHashAsString());
					o.set("transaction_hash", tx.getHashAsString());
					System.out.println("tx value = " + tx.getValue(bitcoin.wallet()));
					System.out.println("order value = " + o.getInteger("total_satoshis"));

					o.saveIt();
				}
			}



		}


		// TODO finish some updates to the order row

		//	   private PaymentRequest newSimplePaymentRequest(NetworkParameters params,
		//			   Coin amount, String receiveAddress, DateTime time, @Nullable DateTime expireTime, 
		//			   String paymentUrl, @Nullable String memo, @Nullable String merchantData) {
		//	        Output.Builder outputBuilder = Output.newBuilder()
		//	                .setAmount(coin.value)
		//	                .setScript(ByteString.copyFrom(outputToMe.getScriptBytes()));
		//	        PaymentDetails paymentDetails = PaymentDetails.newBuilder()
		//	                .setNetwork(netID)
		//	                .setTime(time)
		//	                .setExpires(expireTime)
		//	                .setPaymentUrl(simplePaymentUrl)
		//	                .addOutputs(outputBuilder)
		//	                .setMemo(paymentRequestMemo)
		//	                .setMerchantData(merchantData)
		//	                .build();
		//	        return PaymentRequest.newBuilder()
		//	                .setPaymentDetailsVersion(1)
		//	                .setPkiType("none")
		//	                .setSerializedPaymentDetails(paymentDetails.toByteString())
		//	                .build();
		//	    }

	}

	public static class PaymentActions {

		public static void savePaymentToRow(Payment p, Integer orderNum) {
			com.bitmerchant.db.Tables.Payment pRow = new com.bitmerchant.db.Tables.Payment();

			pRow.set("order_id", orderNum);

			if (p.getMerchantData() != null) 
				pRow.set("merchant_data", p.getMerchantData().toByteArray());

			if (p.getMemo() != null) 
				pRow.set("memo", p.getMemo());

			pRow.saveIt();


			// Get the pRow id for downstream
			Integer pRowId = Integer.valueOf(pRow.getId().toString());

			// Loop over the transactions
			Coin totalAmount = Coin.ZERO;
			for (int i = 0; i < p.getTransactionsCount(); i++) {
				byte[] tBytes = p.getTransactions(i).toByteArray();

				// Do this to get the value
				Transaction tx = new Transaction(bitcoin.params(), tBytes);

				Coin amount = tx.getValue(bitcoin.wallet());
				totalAmount = totalAmount.add(amount);

				com.bitmerchant.db.Tables.Transaction.createIt(
						"payment_id", pRowId,
						"index_", i,
						"satoshis", amount.getValue(),
						"bytes", tBytes);

			}

			// update the total satoshis from the payment
			long satoshisReceived = totalAmount.getValue();
			pRow.set("satoshis_received", satoshisReceived);
			pRow.saveIt();

			// TODO set the status on the order from the payment
			Order oRow = Order.findById(orderNum);
			long orderSatoshis = oRow.getLong("total_satoshis");

			if (satoshisReceived > orderSatoshis) {
				oRow.set("status_id", TableConstants.ORDER_STATUSES.indexOf("overpaid")+1);
			} else if (satoshisReceived < orderSatoshis) {
				oRow.set("status_id", TableConstants.ORDER_STATUSES.indexOf("underpaid")+1);
			} else {
				oRow.set("status_id", TableConstants.ORDER_STATUSES.indexOf("completed")+1);
			}

			oRow.saveIt();


			// Loop over the refund data
			for (int i = 0; i < p.getRefundToCount(); i++) {
				log.info("refund info = " + p.getRefundToList());

				log.info("2 = " + p.getRefundTo(0));
				Output o = p.getRefundTo(i);
				long amount = o.getAmount();
				byte[] sBytes = o.getScript().toByteArray();
				com.bitmerchant.db.Tables.Refund.createIt(
						"payment_id", pRowId,
						"index_", i,
						"amount", amount, // This isn't really used, since the refund amount is determined later
						"script_bytes", sBytes);
			}



		}

		public static PaymentACK createPaymentAck(Payment payment) {
			PaymentACK.Builder paB = PaymentACK.newBuilder();
			paB.setPayment(payment);
			paB.setMemo("Payment received.");

			PaymentACK pa = paB.build();

			return pa;
		}

		/**
		 * Sends a specific refund to the first refund address.
		 * @param orderNum
		 * @param amount
		 * @return
		 */
		public static String sendRefund(Integer orderNum, Coin amount) {

			// First, get the first payment row from the orderNum
			com.bitmerchant.db.Tables.Payment pRow = 
					com.bitmerchant.db.Tables.Payment.findFirst("order_id=?", orderNum);

			Integer pId = Integer.valueOf(pRow.getId().toString());


			Refund firstRefundRow = Refund.findFirst("payment_id=?", pId);
			Script s = new Script(firstRefundRow.getBytes("script_bytes"));

			Transaction tx = new Transaction(bitcoin.params());
			tx.addOutput(amount, s);

			// Send the refund tx
			String message = LocalWallet.INSTANCE.controller.sendRefund(tx);

			// Change the order status to refunded
			Order oRow = Order.findById(orderNum);
			oRow.set("status_id", TableConstants.ORDER_STATUSES.indexOf("refunded")+1);
			oRow.saveIt();

			return message;

		}

		public static String sendRefund(Integer orderNum, String amount, String nativeCurrencyIso) {

			CurrencyConverter cc = CurrencyConverter.INSTANCE;
			long satoshis = cc.convertToSatoshisCurrent(nativeCurrencyIso, 
					new BigDecimal(amount.replaceAll(",", "")));

			Coin amountC = Coin.valueOf(satoshis);

			return sendRefund(orderNum, amountC);

		}

		@Deprecated 
		public static String sendRefund(Integer orderNum) {

			// First, get the payment row from the orderNum
			com.bitmerchant.db.Tables.Payment pRow = 
					com.bitmerchant.db.Tables.Payment.findFirst("order_id=?", orderNum);

			Integer pId = Integer.valueOf(pRow.getId().toString());

			// loop over the refund rows
			Transaction tx = new Transaction(bitcoin.params());
			List<Refund> refundRows = Refund.find("payment_id=?", pId);
			for (Refund cRefund : refundRows) {
				Coin amount = Coin.valueOf(cRefund.getLong("amount"));
				Script s = new Script(cRefund.getBytes("script_bytes"));
				tx.addOutput(amount, s);
			}

			// Send the refund tx
			String message = LocalWallet.INSTANCE.controller.sendRefund(tx);

			return message;



		}


	}

	public static String listCurrencies() {
		List<Currency> currencies = Currency.findAll();

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode a = mapper.createObjectNode();

		ArrayNode an = a.putArray("currencies");

		for (Currency cCurr : currencies) {
			ObjectNode b = mapper.createObjectNode();
			b.put("currency", Tools.jsonToNode(cCurr.toJson(false, "desc", "iso", "unicode")));
			an.add(b);
		}

		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(a);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}



}


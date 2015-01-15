package com.bitmerchant.db;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

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
import org.bitcoinj.kits.WalletAppKit;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
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
			b.set("name", n.get("name").asText());
			b.set("total_native", n.get("price_string").asText());
			b.set("native_currency_id", currency.getId().toString());

			
			
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
		public static Button createButton(String jsonReq) {

			// Parse the JSON into a tree
			JsonNode root = Tools.jsonToNode(jsonReq);

			// Create the button object, save it to the db
			Button b = createButtonObj(root);
			b.saveIt();

			return b;

		}

	}


	public static class OrderActions {

		/**
		 * This is a shortcut of 
		 * @param root
		 * @return
		 */
		public static Order createOrderObj(Integer buttonId) {

			 Button b = Button.findById(buttonId);
		
			Currency currency = Currency.findById(b.getInteger("native_currency_id"));
			String currencyIso = currency.getString("iso");

			Order o = new Order();

			o.set("status_id", OrderStatus.findFirst("status=?", "new").getId().toString());

			// TODO For now, set the memo to the button description
			o.set("memo", b.getString("description"));

			// Set the currency to the most recent currency
			long satoshis;
			if (!currencyIso.equals("BTC")) {
				CurrencyConverter cc = CurrencyConverter.INSTANCE;
				Money amountM = Money.of(CurrencyUnit.of(currencyIso), 
						b.getBigDecimal("total_native"));

				// Convert to BTC using the currency converter
				Money amountBTC = cc.convertMoneyForToday(CurrencyUnit.of("BTC"), amountM);

				// Convert to satoshis
				satoshis = amountBTC.getAmount().multiply(BigDecimal.valueOf(1E8)).longValue();

			} else {
				satoshis = b.getBigDecimal("total_native").multiply(BigDecimal.valueOf(1E8)).longValue();
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

			Order o = createOrderObj(Integer.valueOf(b.getId().toString()));
			
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
		
		public static String listOrders() {
			List<OrderView> ovs = OrderView.findAll();
			
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
		
		public static Order createOrderFromButton(Integer buttonId) {
			
			
			Order o = createOrderObj(buttonId);
			o.saveIt();
			
			// Add the payment_url from the order id(can only be done after its saved to get the id)
			// and the receive address
			o = updateExtraInfo(o);
		

			return o;
			
			
			
		}
		
		public static Order updateExtraInfo(Order o) {
			String id = o.getId().toString();
			String paymentRequestURL = DataSources.WEB_SERVICE_URL + "payment_request/" + id;
			o.set("payment_request_url", paymentRequestURL);
			
			String paymentURL = DataSources.WEB_SERVICE_URL + "create_payment/" + id;
			o.set("payment_url", paymentURL);
			
			
			
			Address receiveAddr = bitcoin.wallet().freshReceiveAddress();
			o.set("receive_address", receiveAddr.toString());
			
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

		/**
		 *  @deprecated No need to do this anymore
		 */
		@Deprecated 
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
			for (int i = 0; i < p.getTransactionsCount(); i++) {
				byte[] tBytes = p.getTransactions(i).toByteArray();
				com.bitmerchant.db.Tables.Transaction.createIt(
						"payment_id", pRowId,
						"index", i,
						"bytes", tBytes);
			}
			
			// Loop over the refund data
			for (int i = 0; i < p.getRefundToCount(); i++) {
				
				Output o = p.getRefundTo(i);
				long amount = o.getAmount();
				byte[] sBytes = o.getScript().toByteArray();
				com.bitmerchant.db.Tables.Refund.createIt(
						"payment_id", pRowId,
						"index", i,
						"amount", amount,
						"script_bytes", sBytes);
			}
			
			
			
		}

		public static PaymentACK createPaymentAck(Payment payment) {
			PaymentACK.Builder paB = PaymentACK.newBuilder();
			paB.setPayment(payment);
			paB.setMemo("I need less week and more weekend");

			PaymentACK pa = paB.build();
			
			return pa;
		}
		
		
	}

}


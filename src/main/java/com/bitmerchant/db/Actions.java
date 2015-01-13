package com.bitmerchant.db;

import java.io.IOException;
import java.math.BigDecimal;

import org.bitcoinj.core.Coin;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
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
			return a.toString();
		}

		/**
		 * 
		 *A Sample Request json <br>
		 *{
 "button" : {
    "name": "shoe",
    "type": "buy_now",
    "style": "custom_large",
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
		public static String createButton(String jsonReq) {

			// Parse the JSON into a tree
			JsonNode root = Tools.jsonToNode(jsonReq);

			// Create the button object, save it to the db
			Button b = createButtonObj(root);
			b.saveIt();

			// Fetch the view back for the user
			String json = showButton(Integer.valueOf(b.getId().toString()));

			return json;

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
		
		public static String createOrder(String jsonReq) {

			// Parse the JSON into a tree
			JsonNode root = Tools.jsonToNode(jsonReq);

			// Create the button and  object, save it to the db
			Order o = createOrderObj(root);
			o.saveIt();
			
			// Fetch the view back for the user
			String json = showOrder(Integer.valueOf(o.getId().toString()));

			return json;

		}






		//			o.set(namesAndValues)

	}

}


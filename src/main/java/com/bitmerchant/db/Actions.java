package com.bitmerchant.db;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.bitmerchant.db.Tables.Button;
import com.bitmerchant.db.Tables.ButtonStyle;
import com.bitmerchant.db.Tables.ButtonType;
import com.bitmerchant.db.Tables.ButtonView;
import com.bitmerchant.db.Tables.Currency;
import com.bitmerchant.db.Tables.Order;
import com.bitmerchant.tools.Tools;

public class Actions {

	//	public static void createPaymentRequest() {
	//		PaymentProtocol.createPaymentRequest(params, outputs, memo, paymentUrl, merchantData)
	//
	//	}


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
			if (n.get("price_1") != null)
				b.set("price_1",  n.get("price_1").asText());
			if (n.get("price_2") != null)
				b.set("price_2",  n.get("price_2").asText());
			if (n.get("price_3") != null)
				b.set("price_3",  n.get("price_3").asText());
			if (n.get("price_4") != null)
				b.set("price_4",  n.get("price_4").asText());
			if (n.get("price_5") != null)
				b.set("price_5",  n.get("price_5").asText());

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

		//				public static Order createOrderObj(JsonNode root) {
		//					
		//				
		//				}

	}
}

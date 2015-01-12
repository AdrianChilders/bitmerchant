package com.bitmerchant.merchant;

import junit.framework.TestCase;

import com.bitmerchant.db.Actions.ButtonActions;
import com.bitmerchant.db.Tables.Button;
import com.bitmerchant.tools.Connections;
import com.bitmerchant.tools.Tools;
public class ActionsTest extends TestCase {

	public void setUp() {
		Connections.INSTANCE.open();
	}
	
	public void testCreateButtonObj() {

		
		
		String jsonReq ="{\n \"button\" : {\n    \"name\": \"shoe\",\n    \"type\": \"buy_now\",\n       \"text\": \"Buy with USD/BTC\",\n    \"price_string\": \"1.23\",\n    \"price_currency_iso\": \"USD\",\n    \"callback_url\": \"http://www.example.com/my_custom_button_callback\",\n    \"description\": \"Sample description\"\n  }\n   \n}";
		
		Button b = ButtonActions.createButtonObj(Tools.jsonToNode(jsonReq));
//		b.saveIt();
		

		System.out.println(b.toJson(true));
		
		assertEquals(b.getString("text"), "Buy with USD/BTC");

	}
	
	public void estShowButton() {
		System.out.println(ButtonActions.showButton(1));

	}
	

}

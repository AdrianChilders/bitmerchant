package com.bitmerchant.db;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;
import junit.framework.TestCase;

import org.junit.Before;

import com.bitmerchant.db.Actions.ButtonActions;
import com.bitmerchant.db.Actions.OrderActions;
import com.bitmerchant.db.Tables.Button;
import com.bitmerchant.db.Tables.Order;
import com.bitmerchant.tools.Connections;
import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;
import com.bitmerchant.wallet.LocalWallet;
public class ActionsTest extends TestCase {


	 @Before
	public void setUp() {
		Connections.INSTANCE.open();
		LocalWallet.INSTANCE.init();
		bitcoin.awaitRunning();
		
		// Create the first button
		Button b = Button.findById(1);
		
		if (b == null) {
			ButtonActions.createButton(DataSources.BUTTON_JSON_REQ);
		}
		
		// Create the first order
		Order o = Order.findById(1);
		
		if (o == null) {

			OrderActions.createOrder(DataSources.BUTTON_JSON_REQ);
		}
	}
	
	 
	public void testCreateButtonObj() {

		Button b = ButtonActions.createButtonObj(Tools.jsonToNode(DataSources.BUTTON_JSON_REQ));
		

		System.out.println(b.toJson(true));
		
		assertEquals(b.getString("text"), "Buy with USD/BTC");

	}
	
	public void testShowButton() {
		System.out.println(ButtonActions.showButton(1));

	}
	
	public void testCreateOrderObj() {
		Order o = OrderActions.createOrderObj(1);
		
		System.out.println(o.toJson(true));
	}
	
	public void testShowOrderObj() {
		System.out.println(OrderActions.showOrder(1));
	}
	
	

}

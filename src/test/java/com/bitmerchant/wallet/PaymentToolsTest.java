package com.bitmerchant.wallet;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import junit.framework.TestCase;
import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.bitcoinj.kits.WalletAppKit;
import org.junit.Before;

import com.bitmerchant.db.Actions.OrderActions;
import com.bitmerchant.db.Tables.Order;
import com.bitmerchant.tools.Connections;
import com.bitmerchant.tools.DataSources;

public class PaymentToolsTest extends TestCase {
	
	@Before
	public void setUp() {
		Connections.INSTANCE.open();
		LocalWallet.INSTANCE.init();
		
		bitcoin.awaitRunning();
		
		System.out.println(bitcoin.wallet());

		
		// Create the first order
		Order o = Order.findById(1);
		
		if (o == null) {
			OrderActions.createOrder(DataSources.BUTTON_JSON_REQ);
		}
		
		
	}
	
	
	public void testCreatePaymentRequestFromOrder() {

		Order o = Order.findById(1);
	
		
		PaymentRequest pr = OrderActions.createPaymentRequestFromOrder(o);
		
		System.out.println(pr);
	}
}

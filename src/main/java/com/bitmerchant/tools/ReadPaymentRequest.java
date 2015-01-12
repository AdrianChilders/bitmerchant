package com.bitmerchant.tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.bitcoin.protocols.payments.Protos.Output;
import org.bitcoin.protocols.payments.Protos.Payment;
import org.bitcoin.protocols.payments.Protos.PaymentDetails;
import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.bitcoinj.protocols.payments.PaymentSession;
import org.bitcoinj.tools.PaymentProtocolTool;


public class ReadPaymentRequest {
	
	public static void main3(String[] args) {
		
	}
	
	public static void main2(String[] args) {

				String[] asdf = new String[5];
				asdf[0] = "bitcoin:myTYR6DG7V3EQvwHLDYbnP4BuwVTKHwg1h?"
						+ "r=https%3A%2F%2Fbitcoincore.org%2F%7Egavin%2Ff.php"
						+ "%3Fh%3D56f25b7d2103000312496610d0e23dd0&amount=2";
				
				PaymentProtocolTool.main(asdf);
			
	}
	public static void main(String[] args) throws IOException {
		FileInputStream fis = new FileInputStream(new File(
				"/home/tyler/git/bitmerchant-wallet/src/main/resources/r1420812277.bitcoinpaymentrequest"));
		PaymentRequest pr = PaymentRequest.parseFrom(fis);
		System.out.println(pr);
//		Tools.GSON.toJson(pr);
		
		
		PaymentDetails pd = PaymentDetails.parseFrom(pr.getSerializedPaymentDetails());
//		Output o = Output.parseFrom(fis);
//		System.out.println(o);
		
//		pd.get
		System.out.println(pd);
		

		
		
		
	}

			
	
}

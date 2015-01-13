package com.bitmerchant.tools;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bitcoin.protocols.payments.Protos.PaymentDetails;
import org.bitcoin.protocols.payments.Protos.PaymentRequest;
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
		
		Connections.INSTANCE.open();
//		LocalWallet.INSTANCE.init();
//		bitcoin.awaitRunning();
//		PaymentRequest prDerp = PaymentTools.createPaymentRequestFromOrder(1);
		
//		byte[] bytes = prDerp.toByteArray();
		
		InputStream fis = new FileInputStream(new File(
//				"/home/tyler/git/bitmerchant-wallet/src/main/resources/1"));
				"/home/tyler/git/bitmerchant-wallet/test"));
		
//		byte[] bytes = Files.readAllBytes(Paths.get("/home/tyler/git/bitmerchant-wallet/src/main/resources/1"));
		
		ByteBuffer bytes = Tools.getAsByteArray("http://localhost:4567/payment_request/1");
		

		PaymentRequest pr = PaymentRequest.parseFrom(bytes.array());
		
		System.out.println(pr);
//		Tools.GSON.toJson(pr);
		
	
		
		PaymentDetails pd = PaymentDetails.parseFrom(pr.getSerializedPaymentDetails());
//		Output o = Output.parseFrom(fis);
//		System.out.println(o);
		
//		pd.get
		
//		System.out.println(pd);
		
		//
		
		FileInputStream fis1 = new FileInputStream(new File(
				"/home/tyler/git/bitmerchant-wallet/test"));
		PaymentRequest pr1 = PaymentRequest.parseFrom(fis1);
		System.out.println(pr1);
		
		// ---
		
		ByteBuffer b = Tools.getAsByteArray("http://localhost:4567/payment_request/1");
		
//		ByteString a = ByteString.copyFrom(Tools.httpGet(, "UTF-8");
//		System.out.println(a.toByteArray());
		
		PaymentRequest pr2 = PaymentRequest.parseFrom(new ByteArrayInputStream(b.array()));
		
		System.out.println(pr2);

		
		
		
	}

			
	
}

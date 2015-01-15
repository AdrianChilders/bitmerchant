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
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.tools.PaymentProtocolTool;


public class ReadPaymentRequest {
	
	public static void main(String[] args) throws IOException {
		
		
//		ByteBuffer b = Tools.getAsByteArray("http://localhost:4567/payment_request/1");
		ByteBuffer b = Tools.getAsByteArray("https://bitcoincore.org/~gavin/f.php?h=9d0cd000620747205f567901b985f749");
		
//		ByteString a = ByteString.copyFrom(Tools.httpGet(, "UTF-8");
//		System.out.println(a.toByteArray());
		
		PaymentRequest pr = PaymentRequest.parseFrom(b.array());
		
		System.out.println("pr file = " + pr);
		
		PaymentDetails pd = PaymentDetails.parseFrom(pr.getSerializedPaymentDetails());
		System.out.println(pd);
//		System.out.println(pd.getOutputsList().get(0).);
//		Transaction tx = new Transaction();
		
		
	}
	
	public static void main2(String[] args) {

				String[] asdf = new String[5];
//				asdf[0] = "bitcoin:mg7siucRD8SQPq264kYed5nY6D8qABdx6K?r=https%3A%2F%2Fbitcoincore.org%2F%7Egavin%2Ff.php%3Fh%3Dbfa0fb0a2dc8985d84b2fe8c8140c894&amount=1";
				String[] orderReq = new String[]{"bitcoin:mg7siucRD8SQPq264kYed5nY6D8qABdx6K?r=http%3A%2F%2F96.28.13.51%3A4567%2Forders_alt%2F1"};
				String[] orderReq2 = new String[]{"bitcoin:mg7siucRD8SQPq264kYed5nY6D8qABdx6K?r=http%3A%2F%2F96.28.13.51%3A4567%2Forders%2Forder_1.bitcoinpaymentrequest"};
				String[] orderReq3 = new String[]{"bitcoin:n2KoQQ45AsXwukKqJJCuw358PfAakgXU8J?r=http%3A%2F%2F96.28.13.51%3A4567%2Fpayment_request%2F2"};
				
				PaymentProtocolTool.main(orderReq3);
				
				
			
	}
	

	public static void main3(String[] args) throws IOException {
		
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
		
		ByteBuffer b = Tools.getAsByteArray("http://localhost:4567/payment_request/2");
		
//		ByteString a = ByteString.copyFrom(Tools.httpGet(, "UTF-8");
//		System.out.println(a.toByteArray());
		
		PaymentRequest pr2 = PaymentRequest.parseFrom(b.array());
		
		System.out.println("pr file = " + pr2);

		
		
		
	}

			
	
}

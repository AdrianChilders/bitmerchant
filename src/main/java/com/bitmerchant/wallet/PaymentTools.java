package com.bitmerchant.wallet;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bitcoin.protocols.payments.Protos.Output;
import org.bitcoin.protocols.payments.Protos.PaymentDetails;
import org.bitcoin.protocols.payments.Protos.PaymentRequest;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.kits.WalletAppKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitmerchant.db.Tables.Order;
import com.bitmerchant.db.Tables.OrderView;
import com.bitmerchant.tools.Connections;
import com.bitmerchant.tools.DataSources;
import com.google.protobuf.ByteString;

public class PaymentTools {
	
	

	static final Logger log = LoggerFactory.getLogger(PaymentTools.class);

	public static PaymentRequest createPaymentRequestFromOrder(Integer orderNum) {
		Order o = Order.findById(orderNum);
		return createPaymentRequestFromOrder(o);
	}
	
	public static PaymentRequest createPaymentRequestFromOrder(Order o) {

		// Remember, need to make some updates to the order row too
		
		Output.Builder ob = Output.newBuilder();

		Coin amount = Coin.valueOf(o.getLong("total_satoshis"));
		ob.setAmount(amount.value);

		Address receiveAddr = bitcoin.wallet().freshReceiveAddress();
		o.set("receive_address", receiveAddr.toString());
		Transaction tx = new Transaction(bitcoin.params());		
		TransactionOutput outputToMe = new TransactionOutput(bitcoin.params(), tx, amount, receiveAddr);
		ob.setScript(ByteString.copyFrom(outputToMe.getScriptBytes()));

		PaymentDetails.Builder pdB = PaymentDetails.newBuilder();

		String network = (bitcoin.params().getId().equals("org.bitcoin.test")) ? "test" : "main";
		log.info("params = " + network);
		o.set("network", network);
		pdB.setNetwork(network);

		pdB.setTime(o.getLong("created_at")/1000L);


//		pdB.setExpires(o.getLong("expire_time")/1000L);

		// Generate the payment URL
		String paymentURL = DataSources.WEB_SERVICE_URL + "payment_request/" + o.getId().toString();
//		pdB.setPaymentUrl(paymentURL);
//		o.set("payment_url", paymentURL);

		pdB.addOutputs(ob);

//		if (o.getString("memo") != null) 
//			pdB.setMemo(o.getString("memo"));

//		if (o.getString("merchant_data") != null) 
//			pdB.setMerchantData(ByteString.copyFromUtf8(o.getString("merchant_data")));
		
		PaymentDetails pd = pdB.build();
		
	    PaymentRequest pr = PaymentRequest.newBuilder()
                .setPaymentDetailsVersion(1)
                .setPkiType("none")
                .setSerializedPaymentDetails(pd.toByteString())
                .build();
	    
	    
	    o.saveIt();
	    
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

package com.bitmerchant.tools;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.BtcFormat;
import org.bitcoinj.utils.MonetaryFormat;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitmerchant.wallet.LocalWallet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Request;
import spark.Response;

public class Tools {
	public static final Gson GSON = new Gson();
	public static final Gson GSON2 = new GsonBuilder().setPrettyPrinting().create();
	static final Logger log = LoggerFactory.getLogger(Tools.class);
	public static final DateTimeFormatter DTF2 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");


	public static void allowResponseHeaders(Request req, Response res) {
		String origin = req.headers("Origin");
		res.header("Access-Control-Allow-Credentials", "true");
		//		System.out.println("origin = " + origin);
		//		if (DataSources.ALLOW_ACCESS_ADDRESSES.contains(req.headers("Origin"))) {
		//			res.header("Access-Control-Allow-Origin", origin);
		//		}
		res.header("Access-Control-Allow-Origin", origin);

	}

	public static final Map<String, String> createMapFromAjaxPost(String reqBody) {
		//				log.info(reqBody);
		Map<String, String> postMap = new HashMap<String, String>();
		String[] split = reqBody.split("&");
		for (int i = 0; i < split.length; i++) {
			String[] keyValue = split[i].split("=");
			try {
				postMap.put(URLDecoder.decode(keyValue[0], "UTF-8"),URLDecoder.decode(keyValue[1], "UTF-8"));
			} catch (UnsupportedEncodingException |ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				throw new NoSuchElementException(e.getMessage());
			}
		}

		log.info(GSON2.toJson(postMap));

		return postMap;

	}


	public static List<Map<String, String>> convertTransactionsToLOM(List<Transaction> transactions) {
		List<Map<String, String>> lom = new ArrayList<Map<String,String>>();

		for (Transaction cT : transactions) {
			Map<String, String> tMap = convertTransactionToMap(cT);
			lom.add(tMap);

		}

		return lom;
	}

	public static Map<String, String>convertTransactionToMap(Transaction tx) {
		Map<String, String> map = new LinkedHashMap<String, String>();

		Coin value = tx.getValue(bitcoin.wallet());
		Coin fee = tx.getFee();

		if (value.isPositive()) {
			map.put("message", "You received payment for an order");
			//			address = tx.getOutput(0).getAddressFromP2PKHScript(LocalWallet.params);
			//			address = tx.getOutput(0).getScriptPubKey().getFromAddress(LocalWallet.params);

			// TODO grab order number from SQL
			map.put("order", "asdf");


		} else if (value.isNegative()) {
			map.put("message", "You sent bitcoin to an external account");
			Address address = tx.getOutput(0).getAddressFromP2PKHScript(LocalWallet.params);
			//			address = tx.getOutput(0).getScriptPubKey().getFromAddress(LocalWallet.params);
			map.put("address", address.toString());

		}

		String dateStr = adjustUpdateTime(tx.getUpdateTime().getTime());

		//		String date = Tools.DTF2.print(dtStr);
		map.put("date", dateStr);

		if (fee != null) {

			map.put("fee", "<span class=\"text-muted\">-" + mBtcFormat(fee) + "</span>");


			// Subtract the fee from the net amount(in negatives)
			Coin amountBeforeFee = value.add(fee);
			map.put("amount", "<span class=\"text-danger\">" + mBtcFormat(amountBeforeFee) + "</span>");
		} 
		// If there was no fee, then you received btc
		else {
			map.put("amount", "<span class=\"text-success\"> +" + mBtcFormat(value) + "</span>");
		}


		String status = tx.getConfidence().getConfidenceType().name();

		// For now, if the value transferred is greater than 1 BTC, require 6 confirmations.
		// Otherwise, require only for it to be in the building state
		if (status.equals("BUILDING")) {
			int depth = tx.getConfidence().getDepthInBlocks();

			if (value.isGreaterThan(Coin.COIN)) {
				if (depth >=6) {
					status = "COMPLETED";
				} else {
					status = "PENDING";
				}
			} else {
				status = "COMPLETED";
			}
		} 

		map.put("status", status);
		map.put("depth",String.valueOf(tx.getConfidence().getDepthInBlocks()));

		return map;
	}
	
	public static String adjustUpdateTime(long time) {
		
		DateTimeZone tz = DateTimeZone.getDefault();
		Long instant = DateTime.now().getMillis();
		long offsetInMilliseconds = tz.getOffset(instant);
		Integer hours = (int) TimeUnit.MILLISECONDS.toHours( offsetInMilliseconds);
		DateTime dt = new DateTime(time).minusHours(hours);

		return dt.toString(DTF2);
	}

	public static String convertLOMtoJson(List<Map<String, String>> lom) {
		return Tools.GSON.toJson(lom);
	}

	public static String btcFormat(Coin c) {
		return MonetaryFormat.BTC.noCode().format(c).toString() + " BTC";
	}

	public static String mBtcFormat(Coin c) {
		return MonetaryFormat.MBTC.noCode().format(c).toString() + " mBTC";
	}


	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(String urlString) {
		try {
			URL url = new URL(urlString);
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

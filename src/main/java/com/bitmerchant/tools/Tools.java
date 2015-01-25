package com.bitmerchant.tools;

import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.utils.MonetaryFormat;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;

import com.bitmerchant.db.Actions.OrderActions;
import com.bitmerchant.db.Tables.OrderView;
import com.bitmerchant.wallet.LocalWallet;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tools {
	public static final Gson GSON = new Gson();
	public static final Gson GSON2 = new GsonBuilder().setPrettyPrinting().create();
	static final Logger log = LoggerFactory.getLogger(Tools.class);

	public static final DateTimeFormatter DTF2 = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").
			withZone(DateTimeZone.UTC);
	public static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").
			withZone(DateTimeZone.UTC);


	public static void allowOnlyLocalHeaders(Request req, Response res) {

		String origin = req.headers("Origin");
		String origin2 = req.headers("origin");
		String host = req.headers("Host");
		
	
		log.info("request host: " + host);
		log.info("request origin: " + origin);
		log.info("request origin2: " + origin2);


		//		System.out.println("origin = " + origin);
		//		if (DataSources.ALLOW_ACCESS_ADDRESSES.contains(req.headers("Origin"))) {
		//			res.header("Access-Control-Allow-Origin", origin);
		//		}
		for (String header : req.headers()) {
			log.info("request header | " + header + " : " + req.headers(header));
		}
		log.info("request ip = " + req.ip());
		log.info("request pathInfo = " + req.pathInfo());
		log.info("request host = " + req.host());
		log.info("request url = " + req.url());
	
		
//				res.header("Access-Control-Allow-Origin", "http://mozilla.com");
//		res.header("Access-Control-Allow-Origin", "null");
//		res.header("Access-Control-Allow-Origin", "*");
//		res.header("Access-Control-Allow-Credentials", "true");

		if (!req.ip().equals("127.0.0.1")) {
			throw new NoSuchElementException("Not a local ip, can't access");
		}
	}

	public static final Map<String, String> createMapFromAjaxPost(String reqBody) {
		log.info(reqBody);
		Map<String, String> postMap = new HashMap<String, String>();
		String[] split = reqBody.split("&");
		for (int i = 0; i < split.length; i++) {
			String[] keyValue = split[i].split("=");
			try {
				if (keyValue.length > 1) {
					postMap.put(URLDecoder.decode(keyValue[0], "UTF-8"),URLDecoder.decode(keyValue[1], "UTF-8"));
				}
			} catch (UnsupportedEncodingException |ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				throw new NoSuchElementException(e.getMessage());
			}
		}

		log.info(GSON2.toJson(postMap));

		return postMap;

	}

	public static final ObjectNode createNodeFromPost(String name, Map<String, String> postMap) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode bn = mapper.createObjectNode();
		ObjectNode n = mapper.createObjectNode();

		for (Entry<String, String> e : postMap.entrySet()) {
			n.put(e.getKey(), e.getValue());
		}

		bn.put(name, n);

		return bn;
	}


	public static List<Map<String, String>> convertTransactionsToLOM(List<Transaction> transactions) {
		List<Map<String, String>> lom = new ArrayList<Map<String,String>>();

		// Get all the orders at once
		List<OrderView> ovs = OrderView.findAll();

		// create a map from order hash to order number
		Map<String, OrderView> orderHashToOrderMap = orderHashToOrderMap(ovs);

		for (Transaction cT : transactions) {
			String txHash = cT.getHashAsString();			
			OrderActions.updateOrderFromTransactionReceived(cT);
			OrderView ov = (orderHashToOrderMap.get(txHash) != null) ? orderHashToOrderMap.get(txHash) : null;
			Map<String, String> tMap = convertTransactionToMap(cT, ov);
			lom.add(tMap);

		}

		return lom;
	}

	public static Map<String, OrderView> orderHashToOrderMap(List<OrderView> ovs) {
		Map<String, OrderView> orderHashToIdMap = new HashMap<String, OrderView>();
		for (OrderView ov : ovs) {
			String hash = ov.getString("transaction_hash");
			orderHashToIdMap.put(hash,  ov);
		}

		return orderHashToIdMap;
	}

	public static Map<String, String>convertTransactionToMap(Transaction tx, 
			OrderView ov) {
		Map<String, String> map = new LinkedHashMap<String, String>();

		Coin value = tx.getValue(bitcoin.wallet());
		Coin fee = tx.getFee();

		map.put("transaction_hash", tx.getHashAsString());


		String receiveMessage;
		if (ov != null) {
			receiveMessage = "You received payment for a ";
			map.put("button_name", ov.getString("button_name"));
			map.put("order", ov.getId().toString());

		} else {
			receiveMessage = "You received money directly";
		}

		String moneySent = "You sent bitcoin to an external account" + 
				"";

		if (value.isPositive()) {
			map.put("message", receiveMessage);
			//			address = tx.getOutput(0).getAddressFromP2PKHScript(LocalWallet.params);
			//			address = tx.getOutput(0).getScriptPubKey().getFromAddress(LocalWallet.params);

			map.put("amount", "<span class=\"text-success\"> +" + mBtcFormat(value) + "</span>");

		} else if (value.isNegative()) {
			map.put("message", moneySent);
			Address address = tx.getOutput(0).getAddressFromP2PKHScript(LocalWallet.params);
			//			address = tx.getOutput(0).getScriptPubKey().getFromAddress(LocalWallet.params);
			map.put("address", address.toString());

			if (fee != null) {

				map.put("fee", "-" + mBtcFormat(fee));


				// Subtract the fee from the net amount(in negatives)
				Coin amountBeforeFee = value.add(fee);
				map.put("amount", "<span class=\"text-danger\">" + mBtcFormat(amountBeforeFee) + "</span>");
			} 

		}

		String dateStr = adjustUpdateTime(tx.getUpdateTime().getTime());

		//		String date = Tools.DTF2.print(dtStr);
		map.put("date", dateStr);
		map.put("longDate", String.valueOf(tx.getUpdateTime().getTime()));



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
		DateTime dt = new DateTime(time);//.minusHours(hours);
		String dateStr = dt.toString(DTF2);
		return dateStr;
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

	public static void pollAndOpenStartPage() {
		// TODO poll some of the url's every .5 seconds, and load the page when they come back with a result
		int i = 500;
		int cTime = 0;
		while (cTime < 30000) {
			try {
				try {
					String webServiceStartedURL = "http://localhost:4567/status_progress";

					HttpURLConnection connection = null;
					URL url = new URL(webServiceStartedURL);
					connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(5000);//specify the timeout and catch the IOexception
					connection.connect();
					Thread.sleep(2*i);
					Tools.openWebpage("http://localhost:4567/wallet");
					cTime = 30000;
				} catch (IOException e) {
					log.info("Could not connect to local webservice, retrying in 500ms up to 30 seconds");
					cTime += i;

					Thread.sleep(i);

				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public static String getTransactionOutputAddress(TransactionOutput txo) {
		try {
			log.info("txo here " + txo + "\n" + LocalWallet.params + "\n");
			//		log.info(txo.getAddressFromP2SH( LocalWallet.params).toString());

			log.info(txo.getAddressFromP2PKHScript(LocalWallet.params).toString());
			return txo.getAddressFromP2PKHScript(LocalWallet.params).toString();
		} catch (NullPointerException e) { 
			e.printStackTrace();
			log.info("Probably no tx out here yet");
			return null;
		}

	}

	public static String getTransactionInputAddress(TransactionInput txi) {
		log.info("txi here " + txi + "\n" + LocalWallet.params + "\n");
		return getTransactionOutputAddress(txi.getConnectedOutput());
	}

	public static String getTransactionInfo(Transaction tx) {
		List<TransactionOutput> txos = tx.getOutputs();
		List<TransactionInput> txis = tx.getInputs();

		StringBuilder s = new StringBuilder();
		s.append("Inputs: \n");
		for (TransactionInput txi : txis) {
			s.append(getTransactionInputAddress(txi) + "\n");
		}

		s.append("Outputs: \n");
		for (TransactionOutput txo : txos) {
			s.append(getTransactionOutputAddress(txo) + "\n");
		}

		s.append("Hash: " + tx.getHashAsString() + "\n");





		return s.toString();
	}

	public static class TransactionJSON {
		String hash;
		String amount;

		public TransactionJSON(Transaction tx) {
			if (tx != null) {
				hash = tx.getHashAsString();
				amount = mBtcFormat(tx.getValue(bitcoin.wallet()));
			} else {
				hash = "none yet";
				amount = "none yet";
			}
		}

		public String json() {
			return GSON.toJson(this);
		}

		public String getHash() {
			return hash;
		}

		public String getAmount() {
			return amount;
		}


	}

	public static Integer cookieExpiration(Integer minutes) {
		return minutes*60;
	}

	public static final String httpGet(String url) {
		String res = "";
		try {
			URL externalURL = new URL(url);

			URLConnection yc = externalURL.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
				res+="\n" + inputLine;
			in.close();

			return res;
		} catch(IOException e) {}
		return res;
	}

	public static ByteBuffer getAsByteArray(String urlStr) throws IOException {
		URL url = new URL(urlStr);

		URLConnection connection = url.openConnection();
		// Since you get a URLConnection, use it to get the InputStream
		InputStream in = connection.getInputStream();
		// Now that the InputStream is open, get the content length
		int contentLength = connection.getContentLength();

		// To avoid having to resize the array over and over and over as
		// bytes are written to the array, provide an accurate estimate of
		// the ultimate size of the byte array
		ByteArrayOutputStream tmpOut;
		if (contentLength != -1) {
			tmpOut = new ByteArrayOutputStream(contentLength);
		} else {
			tmpOut = new ByteArrayOutputStream(16384); // Pick some appropriate size
		}

		byte[] buf = new byte[512];
		while (true) {
			int len = in.read(buf);
			if (len == -1) {
				break;
			}
			tmpOut.write(buf, 0, len);
		}
		in.close();
		tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive

		byte[] array = tmpOut.toByteArray();

		//Lines below used to test if file is corrupt
		//FileOutputStream fos = new FileOutputStream("C:\\abc.pdf");
		//fos.write(array);
		//fos.close();

		return ByteBuffer.wrap(array);
	}

	public static List<Map<String, String>> ListOfMapsPOJO(String json) {

		ObjectMapper mapper = new ObjectMapper();

		try {

			List<Map<String,String>> myObjects = mapper.readValue(json, 
					new TypeReference<ArrayList<LinkedHashMap<String,String>>>(){});

			return myObjects;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, String> mapPOJO(String json) {

		ObjectMapper mapper = new ObjectMapper();

		try {

			Map<String,String> myObjects = mapper.readValue(json, 
					new TypeReference<LinkedHashMap<String,String>>(){});

			return myObjects;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Object> mapPOJO2(String json) {

		ObjectMapper mapper = new ObjectMapper();

		try {

			Map<String,Object> myObjects = mapper.readValue(json, 
					new TypeReference<LinkedHashMap<String,Object>>(){});

			return myObjects;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JsonNode jsonToNode(String json) {

		ObjectMapper mapper = new ObjectMapper();

		try {

			JsonNode root = mapper.readTree(json);

			return root;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void runSQLFile(Connection c,File sqlFile) throws SQLException, IOException {
		Statement stmt = null;
		stmt = c.createStatement();
		String sql =Files.toString(sqlFile, Charset.defaultCharset());
		stmt.executeUpdate(sql);
		stmt.close();
	}

	public static final void dbInit() {
		try {
			Base.open("org.sqlite.JDBC", "jdbc:sqlite:" + DataSources.DB_FILE(), "root", "p@ssw0rd");
		} catch (DBException e) {
			dbClose();
			dbInit();
		}

	}

	public static final void dbClose() {
		Base.close();
	}

	public static void restartApplication() throws URISyntaxException, IOException
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(Tools.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if(!currentJar.getName().endsWith(".jar"))
			return;

		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}

	public static void addExternalWebServiceVarToTools() {
		String externalSparkLine = "var externalSparkService ='" + DataSources.WEB_SERVICE_URL + "';";
		
		String internalSparkLine = LocalWallet.INSTANCE.controller.getIsSSLEncrypted() ? 
				"var sparkService = 'https://localhost:4567/';" :
					"var sparkService = 'http://localhost:4567/';";
		try {


			List<String> lines = java.nio.file.Files.readAllLines(Paths.get(DataSources.TOOLS_JS));
		
			lines.set(0,  internalSparkLine);
			lines.set(1, externalSparkLine);
			
			
			java.nio.file.Files.write(Paths.get(DataSources.TOOLS_JS), lines);
			Files.touch(new File(DataSources.TOOLS_JS));
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}




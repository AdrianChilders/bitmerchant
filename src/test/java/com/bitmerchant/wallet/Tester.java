package com.bitmerchant.wallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;

import com.bitmerchant.tools.Tools;

public class Tester {
	

	
	
	public static void main(String[] args) throws IOException {
		String buttonsReq = "[{\n    \"name\": \"test\",\n    \"type\": \"buy_now\",\n "
				+ "   \"subscription\": false,\n    \"price_string\": \"1.23\",\n    \"price_currency_iso\": \"USD\",\n    \"custom\""
				+ ": \"Order123\",\n    \"callback_url\": \"http://www.example.com/my_custom_button_callback\",\n    \"descr"
				+ "iption\": \"Sample description\",\n    \"style\": \"custom_large\",\n    \"include_email\": true\n  }]";

		List<Map<String, String>> lom = Tools.ListOfMapsPOJO(buttonsReq);
		System.out.println(lom);
		String buttonReq = "{\n    \"name\": \"test\",\n    \"type\": \"buy_now\",\n "
				+ "   \"subscription\": false,\n    \"price_string\": \"1.23\",\n    \"price_currency_iso\": \"USD\",\n    \"custom\""
				+ ": \"Order123\",\n    \"callback_url\": \"http://www.example.com/my_custom_button_callback\",\n    \"descr"
				+ "iption\": \"Sample description\",\n    \"style\": \"custom_large\",\n    \"include_email\": true\n  }";
		
		Map<String, String> map = Tools.mapPOJO(buttonReq);
		System.out.println(map);
		
		String weirdReq = "{\n  \"orders\": [\n    {\n      \"order\": {\n        \"id\": \"A7C52JQT\",\n        \"created_at\": \"2013-03-11T22:04:37-07:00\",\n        \"status\": \"completed\",\n        \"total_btc\": {\n          \"cents\": 100000000,\n          \"currency_iso\": \"BTC\"\n        },\n        \"total_native\": {\n          \"cents\": 3000,\n          \"currency_iso\": \"USD\"\n        },\n        \"custom\": \"\",\n        \"receive_address\": \"mgrmKftH5CeuFBU3THLWuTNKaZoCGJU5jQ\",\n        \"button\": {\n          \"type\": \"buy_now\",\n          \"name\": \"Order #1234\",\n          \"description\": \"order description\",\n          \"id\": \"eec6d08e9e215195a471eae432a49fc7\"\n        },\n        \"transaction\": {\n          \"id\": \"513eb768f12a9cf27400000b\",\n          \"hash\": \"4cc5eec20cd692f3cdb7fc264a0e1d78b9a7e3d7b862dec1e39cf7e37ababc14\",\n          \"confirmations\": 0\n        }\n      }\n    }\n  ],\n  \"total_count\": 1,\n  \"num_pages\": 1,\n  \"current_page\": 1\n}";
		
		JsonNode root = Tools.jsonToNode(weirdReq);
		System.out.println(root);
		JsonNode val = root.get("orders").get(0).get("order").get("created_at");
		System.out.println(val);
		


		
//		System.out.println(ip.trim());
		
		
		
		
	}
	
	
}

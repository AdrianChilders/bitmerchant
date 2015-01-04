package com.bitmerchant.tools;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spark.Request;
import spark.Response;

public class Tools {
	public static final Gson GSON2 = new GsonBuilder().setPrettyPrinting().create();
	static final Logger log = LoggerFactory.getLogger(Tools.class);
	
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

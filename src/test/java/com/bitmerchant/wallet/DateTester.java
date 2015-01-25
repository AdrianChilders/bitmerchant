package com.bitmerchant.wallet;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTester {
	public static void main(String[] args) {
		long millis1= new Date().getTime();
		System.out.println(millis1);

		long millis2 = new DateTime().getMillis();
		System.out.println(millis2);

		DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		System.out.println(new DateTime().toString(DTF));
	}
}

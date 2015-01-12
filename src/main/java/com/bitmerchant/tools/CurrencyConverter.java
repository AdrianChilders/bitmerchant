package com.bitmerchant.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.Coin;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CurrencyConverter {

	static final Logger log = LoggerFactory.getLogger(CurrencyConverter.class);

	public static String bitcoinAverageHistoricalCurrQuery(String ISO) {
		return "https://api.bitcoinaverage.com/history/" + ISO + "/per_day_all_time_history.csv";
	}
	public static String bitcoinCurrentCurrQuery(String ISO) {
		return "https://api.bitcoinaverage.com/ticker/global/" + ISO + "/last" ;
	}

	public static final CurrencyUnit BTC = CurrencyUnit.registerCurrency("BTC", 016, 8, Arrays.asList(""));

	// The map of toCurrency, and the given currency service
	private final LoadingCache<CurrencyUnit, Map<DateTime, Double>> btcRatesCache = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(
					new CacheLoader<CurrencyUnit, Map<DateTime, Double>>() {
						public Map<DateTime, Double> load(CurrencyUnit cu) {

							// Put historical rates
							String historyRes = Tools.httpGet(bitcoinAverageHistoricalCurrQuery(cu.getCurrencyCode()));
							Map<DateTime, Double> rates = btcSpotRatesFromBtcAverageResponse(historyRes);

							// Grab the most recent rate for today, and add it
							String currentRes = Tools.httpGet(bitcoinCurrentCurrQuery(cu.getCurrencyCode()));
							Entry<DateTime, Double> recentRate = getMostRecentConversionRateForToday(currentRes);

							// This is the coinbase version
							//								String currentRes = Tools.httpGet(bitcoinCoinbaseCurrentCurrQuery(ISO));
							//								Entry<DateTime, Double> recentRate = getMostRecentConversionRateForTodayCoinbase(currentRes);

							rates.put(recentRate.getKey(), recentRate.getValue());
							log.info("recent rate put = " + recentRate.getValue());

							System.out.println("Recaching BTC -> " + cu.getCurrencyCode());
							return rates;
						}
					});

	public static void main(String[] args) throws InterruptedException {
		Coin c = Coin.COIN;

		Money m = Money.of(BTC, 5.12);

		Money m1 = Money.parse("BTC 6.1823");

		System.out.println(m1);

		System.out.println(m);
		
		CurrencyConverter cc = new CurrencyConverter();
		
		Money convertedMoney = cc.convertMoneyForToday(CurrencyUnit.USD, m);
		
		System.out.println(convertedMoney);
		Thread.sleep(6000);
		convertedMoney = cc.convertMoneyForToday(CurrencyUnit.USD, m);
		System.out.println(convertedMoney);
		
	}


	public LoadingCache<CurrencyUnit, Map<DateTime, Double>> getBtcRatesCache() {
		return btcRatesCache;
	}

	public static final Map<DateTime, Double> btcSpotRatesFromBtcAverageResponse(String res) {
		Map<DateTime, Double> rates = new LinkedHashMap<DateTime, Double>();
		//		System.out.println(res);

		String cvsSplit = ",";
		String lines[] = res.split("\\r?\\n");

		for (int i = 1; i < lines.length; i++) {
			// Starting at line #2, put the two values into a map
			String cLine[] = lines[i].split(cvsSplit);

			try {
				rates.put(Tools.DTF.parseDateTime(cLine[0]), Double.parseDouble(cLine[3]));
			} catch (IllegalArgumentException e) {
				// TODO it finds a bunch of these for some reason

			}

		}

		//		System.out.println(Tools.GSON2.toJson(rates));


		return rates;

	}

	public static final Map.Entry<DateTime, Double> getMostRecentConversionRateForToday(String res) {



		// had a weird error where the date in the csv file looked like this: 2014-07-
		DateTime time = new DateTime();
		DateTime startOfToday = getStartOfDay(time);

		// Goto the last one
		Double value = Double.parseDouble(res);



		// Normalize time to today
		//		LocalDate today = time.toLocalDate();
		//		DateTime startOfToday = today.toDateTimeAtStartOfDay(time.getZone());


		Map.Entry<DateTime, Double> entry = 
				new AbstractMap.SimpleEntry<DateTime, Double>(startOfToday, value);
		return entry;
	}

	public static DateTime getStartOfDay(DateTime dt) {
		LocalDate localDate = dt.toLocalDate();
		DateTime startOfDay = localDate.toDateTimeAtStartOfDay(dt.getZone());

		return startOfDay;
	}

	public Map<DateTime, Money> convertSeriesToCurrency(CurrencyUnit cu, 
			Map<DateTime, Money> btcSeries) {

		Map<DateTime, Money> convertedSeries = new LinkedHashMap<DateTime, Money>();

		try {


			Map<DateTime, Double> rates = getBtcRatesCache().get(cu);

			// Convert the history first
			for (Entry<DateTime, Money> e : btcSeries.entrySet()) {
				DateTime dayStart = getStartOfDay(e.getKey());

				Money unconverted = e.getValue();
				Double conversionRate = rates.get(dayStart);
				Money converted = unconverted.convertedTo(cu, BigDecimal.valueOf(conversionRate), 
						RoundingMode.FLOOR);

				convertedSeries.put(dayStart, converted);
			}


			// Replace todays with the most recent spot rate
			DateTime now = new DateTime();
			DateTime startOfToday = getStartOfDay(now);
			Double todayRate = rates.get(startOfToday);
			Money unconverted = btcSeries.get(startOfToday);
			Money converted = unconverted.convertedTo(cu, BigDecimal.valueOf(todayRate), 
					RoundingMode.FLOOR);

			convertedSeries.put(startOfToday, converted);


		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return convertedSeries;
	}

	public Money convertMoney(CurrencyUnit cu, DateTime dt, Money unconverted) {
		Money converted = null;
		try {
			DateTime startOfDay = getStartOfDay(dt);
			Double rate;

			rate = getBtcRatesCache().get(cu).get(startOfDay);

			converted = unconverted.convertedTo(cu, BigDecimal.valueOf(rate), 
					RoundingMode.FLOOR);


		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return converted;
	}
	
	public Money convertMoneyForToday(CurrencyUnit cu, Money unconverted) {
		DateTime now = new DateTime();
		return convertMoney(cu, now, unconverted);
	}



}

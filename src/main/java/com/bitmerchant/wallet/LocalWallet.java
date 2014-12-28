package com.bitmerchant.wallet;

import java.io.File;

import javax.annotation.Nullable;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitmerchant.tools.DataSources;
import com.bitmerchant.tools.Tools;


/**
 * TODO list:
 * - Implement a local wallet using bitcoinj
 * - Wallet should be able to:
 * - Generate addresses
 * - Send and receive bitcoin without having to specify addresses
 * - Create an HTML5 GUI with which to view/send/receive moneys
 * - Do a wizard dialog in bootstrap on first run to set up the wallet, set a password,
 * 	 and then set up your merchant properties
 * - Use base 36 for order numbers, or customer presented number values
 * - 
 */
public class LocalWallet {

	static final Logger log = LoggerFactory.getLogger(LocalWallet.class);
	public static String APP_NAME = "bitmerchant-wallet";

	public static NetworkParameters params = TestNet3Params.get();
	public static WalletAppKit bitcoin;
	public Controller controller;

	public void init() {

		setupDirectories();
		
		
		setupWalletKit(null);


		bitcoin.startAsync();


	}

	public void setupDirectories() {
		log.info("Setting up dirs");
		new File(DataSources.DIR).mkdirs();
	}

	public void setupWalletKit(@Nullable DeterministicSeed seed) {
		controller = new Controller();
		// If seed is non-null it means we are restoring from backup.
		bitcoin = new WalletAppKit(params, new File(DataSources.DIR), APP_NAME) {
			@Override
			protected void onSetupCompleted() {
				// Don't make the user wait for confirmations for now, as the intention is they're sending it
				// their own money!
				bitcoin.wallet().allowSpendingUnconfirmedTransactions();
				if (params != RegTestParams.get())
					bitcoin.peerGroup().setMaxConnections(11);
				bitcoin.peerGroup().setBloomFilterFalsePositiveRate(0.00001);
				controller.onBitcoinSetup();
				//                Platform.runLater(controller::onBitcoinSetup);
			}
		};
		// Now configure and start the appkit. This will take a second or two - we could show a temporary splash screen
		// or progress widget to keep the user engaged whilst we initialise, but we don't.
		if (params == RegTestParams.get()) {
			bitcoin.connectToLocalHost();   // You should run a regtest mode bitcoind locally.
		} else if (params == TestNet3Params.get()) {
			// As an example!
			bitcoin.useTor();
			// bitcoin.setDiscovery(new HttpDiscovery(params, URI.create("http://localhost:8080/peers"), ECKey.fromPublicOnly(BaseEncoding.base16().decode("02cba68cfd0679d10b186288b75a59f9132b1b3e222f6332717cb8c4eb2040f940".toUpperCase()))));
		}

		// The progress bar stuff
		
		bitcoin.setDownloadListener(controller.progressBarUpdater())
		.setBlockingStartup(false)
		.setUserAgent(APP_NAME, "1.0");

		if (seed != null)
			bitcoin.restoreWalletFromSeed(seed);
		

	}


	public void stop() throws Exception {
		bitcoin.stopAsync();
		bitcoin.awaitTerminated();
		// Forcibly terminate the JVM because Orchid likes to spew non-daemon threads everywhere.
		Runtime.getRuntime().exit(0);
	}

	public static void main( String[] args ) {

		// Start the wallet
		LocalWallet lw = new LocalWallet();
		lw.init();
		
		
		
		// Start the web service
		WebService.start(lw);
		
		
		Tools.openWebpage("http://localhost:4567/garp");
		
	}
}

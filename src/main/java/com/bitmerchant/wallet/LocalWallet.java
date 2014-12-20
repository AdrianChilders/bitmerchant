package com.bitmerchant.wallet;

import java.io.File;

import javafx.application.Platform;

import javax.annotation.Nullable;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;


/**
 * TODO list:
 * - Implement a local wallet using bitcoinj
 * - Wallet should be able to:
 * - Generate addresses
 * - Send and receive bitcoin without having to specify addresses
 * - Create an HTML5 GUI with which to view/send/receive moneys
 *
 */
public class LocalWallet {
	
    public static String APP_NAME = "bitmerchant-wallet";

    public static NetworkParameters params = TestNet3Params.get();
    public static WalletAppKit bitcoin;
    public static LocalWallet instance;
    
    public void init() {
    	 setupWalletKit(null);
    	 
    	 bitcoin.startAsync();
    	 
    	 
    }
    
    public void setupWalletKit(@Nullable DeterministicSeed seed) {
        // If seed is non-null it means we are restoring from backup.
        bitcoin = new WalletAppKit(params, new File("."), APP_NAME) {
            @Override
            protected void onSetupCompleted() {
                // Don't make the user wait for confirmations for now, as the intention is they're sending it
                // their own money!
                bitcoin.wallet().allowSpendingUnconfirmedTransactions();
                if (params != RegTestParams.get())
                    bitcoin.peerGroup().setMaxConnections(11);
                bitcoin.peerGroup().setBloomFilterFalsePositiveRate(0.00001);
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
        
        // TODO The progress bar stuff
//        bitcoin.setDownloadListener(controller.progressBarUpdater())
//               .setBlockingStartup(false)
//               .setUserAgent(APP_NAME, "1.0");
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
    	
        System.out.println( "Initializing local wallet..." );
        
        LocalWallet lw = new LocalWallet();
        
        lw.init();
        
        
        
    }
}

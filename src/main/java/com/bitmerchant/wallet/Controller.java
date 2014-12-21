package com.bitmerchant.wallet;


import static com.bitmerchant.wallet.LocalWallet.bitcoin;

import java.util.Date;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DownloadListener;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
public class Controller {

	static final Logger log = LoggerFactory.getLogger(Controller.class);
	
	public String statusText; 
	public Double statusProgress;
	private ProgressBarUpdater syncProgressUpdater = new ProgressBarUpdater();
	private Address address = null;
	private Coin balance = Coin.ZERO;




	public void onBitcoinSetup() {
		setWallet(bitcoin.wallet());
	
		TorClient torClient = bitcoin.peerGroup().getTorClient();
		if (torClient != null) {
			
			String torMsg = "Initialising Tor...";

			torClient.addInitializationListener(new TorInitializationListener() {
				@Override
				public void initializationProgress(String message, int percent) {

					statusText = torMsg + ": " + message;
					statusProgress = percent / 100.0;

				}

				@Override
				public void initializationCompleted() {

					statusProgress = 0.05d;
					statusText = "Synchronising with the Bitcoin network...";

				}
			});
		} else {
			statusText = "Synchronising with the Bitcoin network...";
		}
		
		
	
	
	}
	
	private class ProgressBarUpdater extends DownloadListener {
		@Override
		protected void progress(double pct, int blocksLeft, Date date) {
			super.progress(pct, blocksLeft, date);
			
			statusProgress = pct / 100.0;
			log.info("Progress = " + statusProgress);
		}

		@Override
		protected void doneDownload() {
			super.doneDownload();
			statusProgress = 1.0;
			statusText = "Sync Complete";
			
		}
	}


	public void setWallet(Wallet wallet) {
		wallet.addEventListener(new AbstractWalletEventListener() {
			@Override
			public void onWalletChanged(Wallet wallet) {
				super.onWalletChanged(wallet);
				update(wallet);
			}
		});
		update(wallet);
	}

	private void update(Wallet wallet) {
		balance = wallet.getBalance();
		address = wallet.currentReceiveAddress();
	}
	
	public DownloadListener getDownloadListener() { return syncProgressUpdater; }
	
    public DownloadListener progressBarUpdater() {
        return getDownloadListener();
    }
    
	public String getBalanceText() {
		return MonetaryFormat.BTC.noCode().format(getBalance()).toString();
	}

	public String getAddressText() {
		return getAddress().toString();
	}

	public String getStatusText() {
		return statusText;
	}
	public ProgressBarUpdater getSyncProgressUpdater() {
		return syncProgressUpdater;
	}
	


	

	public Address getAddress() {
		return address;
	}

	public Coin getBalance() {
		return balance;
	}

	
	public Double getStatusProgress() {
		return statusProgress;
	}

}

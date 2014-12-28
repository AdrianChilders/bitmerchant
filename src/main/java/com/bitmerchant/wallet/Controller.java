package com.bitmerchant.wallet;


import static com.bitmerchant.wallet.LocalWallet.bitcoin;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DownloadListener;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import com.google.common.base.Joiner;
import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
public class Controller {

	static final Logger log = LoggerFactory.getLogger(Controller.class);
	
	public String statusText; 
	public Double statusProgress;
	private ProgressBarUpdater syncProgressUpdater = new ProgressBarUpdater();
	private Address address = null;
	private Coin balance = Coin.ZERO;
	
	private KeyParameter aesKey;
    private String walletCreationDateStr;
    private String walletWords;
    private String passwordBtnText;




	public void onBitcoinSetup() {
		

		
		
		setWallet(bitcoin.wallet());
		
		// Set the wallet words
		initializeWalletSettings(null);
	
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
	private void initializeWalletSettings(@Nullable KeyParameter aesKey) {

        DeterministicSeed seed = bitcoin.wallet().getKeyChainSeed();
        if (aesKey == null) {
            if (seed.isEncrypted()) {
                log.info("Wallet is encrypted, requesting password first.");
                // Delay execution of this until after we've finished initialising this screen.
                
                return;
            }
        } else {
            this.aesKey = aesKey;
            seed = seed.decrypt(checkNotNull(bitcoin.wallet().getKeyCrypter()), "", aesKey);
            // Now we can display the wallet seed as appropriate.
            passwordBtnText = "Remove password";
        }

        // Set the date picker to show the birthday of this wallet.
        Instant creationTime = Instant.ofEpochSecond(seed.getCreationTimeSeconds());
        LocalDate origDate = creationTime.atZone(ZoneId.systemDefault()).toLocalDate();
        walletCreationDateStr = origDate.toString();
       

        // Set the mnemonic seed words.
        final List<String> mnemonicCode = seed.getMnemonicCode();
        checkNotNull(mnemonicCode);    // Already checked for encryption.
        String origWords = Joiner.on(" ").join(mnemonicCode);
        walletWords = origWords;

    
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
	public String getWalletCreationDateStr() {
		return walletCreationDateStr;
	}
	public String getWalletWords() {
		return walletWords;
	}
	public String getPasswordBtnText() {
		return passwordBtnText;
	}

}

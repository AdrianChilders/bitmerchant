package com.bitmerchant.wallet;


import static com.bitmerchant.wallet.LocalWallet.bitcoin;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DownloadProgressTracker;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.utils.MonetaryFormat;
import org.bitcoinj.wallet.DeterministicSeed;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import com.bitmerchant.db.Actions.OrderActions;
import com.bitmerchant.db.Tables.MerchantInfoView;
import com.bitmerchant.tools.CurrencyConverter;
import com.bitmerchant.tools.Tools;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.protobuf.ByteString;
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
	private Boolean walletIsEncrypted = false;
	private Boolean walletIsLocked = false;
	private Boolean isSSLEncrypted = false;


	private Transaction newestReceivedTransaction;

	public static final String TAG = Controller.class.getName() + ".target-time";

	private Wallet.SendResult sendResult;
	private String sendStatus;
	



	public void onBitcoinSetup() {

		setWallet(bitcoin.wallet());

		// Set the wallet words
		initializeWalletSettings(null);

		TorClient torClient = bitcoin.peerGroup().getTorClient();
		if (torClient != null) {

			String torMsg = "Initialising Tor...";
			statusProgress = 0.05d;
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

		// for receiving money
		bitcoin.wallet().addEventListener(new AbstractWalletEventListener() {
			@Override
			public void onCoinsReceived(Wallet wallet, Transaction tx,
					Coin prevBalance, Coin newBalance) {

				log.info("u received coins");
				newestReceivedTransaction = tx;
				log.info(Tools.getTransactionInfo(tx));

				// TODO for now, just associate the send addresses with the orders
				// Since the payment_url unfortunately requires SSL
				OrderActions.updateOrderFromTransactionReceived(tx);




			}
		});

	}

	private class ProgressBarUpdater extends DownloadProgressTracker {
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
				walletIsEncrypted = true;
				walletIsLocked = true;
				// Delay execution of this until after we've finished initialising this screen.
				return;
			}
		} else {
			this.aesKey = aesKey;
			walletIsLocked = false;
			seed = seed.decrypt(checkNotNull(bitcoin.wallet().getKeyCrypter()), "", aesKey);
			// Now we can display the wallet seed as appropriate.
			passwordBtnText = "Remove password";
		}

		// Set the date picker to show the birthday of this wallet.
		Instant creationTime = Instant.ofEpochSecond(seed.getCreationTimeSeconds());
		LocalDate origDate = creationTime.atZone(ZoneId.systemDefault()).toLocalDate();

		// For some reason, its needing to add another day here
		origDate = origDate.plusDays(1);
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

	public String setWalletPassword(String password) {

		// Figure out how fast this computer can scrypt. We do it on the UI thread because the delay should be small
		// and so we don't really care about blocking here.
		IdealPasswordParameters params = new IdealPasswordParameters(password);
		KeyCrypterScrypt scrypt = new KeyCrypterScrypt(params.realIterations);

		// Write the target time to the wallet so we can make the progress bar work when entering the password.
		setTargetTime(params.realTargetTime);

		// Deriving the actual key runs on a background thread.
		KeyDerivationTasks tasks = new KeyDerivationTasks(scrypt, password, params.realTargetTime);


		// The actual encryption part doesn't take very long as most private keys are derived on demand.
		bitcoin.wallet().encrypt(scrypt, tasks.getAesKey());
		walletIsEncrypted = true;
		//                informationalAlert("Wallet encrypted",
		//                        "You can remove the password at any time from the settings screen.");
		String message = "Wallet encrypted. You can remove the password at any time from the settings screen ";

		return message;
	}

	public String removeWalletPassword(String password) {

		final KeyCrypterScrypt keyCrypter = (KeyCrypterScrypt) bitcoin.wallet().getKeyCrypter();
		checkNotNull(keyCrypter);   // We should never arrive at this GUI if the wallet isn't actually encrypted.
		KeyDerivationTasks tasks = new KeyDerivationTasks(keyCrypter, password, getTargetTime());
		try {
			bitcoin.wallet().decrypt(tasks.getAesKey());
		} catch(KeyCrypterException e) {
			throw new NoSuchElementException("Incorrect password");
		}
		walletIsEncrypted = false;

		// re-init the wallet
		initializeWalletSettings(null);

		walletIsLocked = false;

		String message = "Wallet decrypted. A password will no longer be required to send money or edit settings.";
		return message;
	}

	public String unlockWallet(String password) {
		final KeyCrypterScrypt keyCrypter = (KeyCrypterScrypt) bitcoin.wallet().getKeyCrypter();
		checkNotNull(keyCrypter);   // We should never arrive at this GUI if the wallet isn't actually encrypted.
		KeyDerivationTasks tasks = new KeyDerivationTasks(keyCrypter, password, getTargetTime());

		if (bitcoin.wallet().checkAESKey(tasks.getAesKey())) {
			this.aesKey = tasks.getAesKey();

			// re-init the wallet
			initializeWalletSettings(aesKey);

			String message = "Wallet Unlocked.";

			return message;
		} else {
			throw new NoSuchElementException("Incorrect password");
		}



	}

	public String restoreWallet(String walletWords, String dateStr) {

		// Don't allow a restore unless this wallet is presently empty. We don't want to end up with two wallets, too
		// much complexity, even though WalletAppKit will keep the current one as a backup file in case of disaster.
		// TODO notify user if there is a balance in their current wallet

		// check to see if wallet words are okay
		try {
			MnemonicCode codec = new MnemonicCode();

			codec.check(Splitter.on(' ').splitToList(walletWords));
		} catch (MnemonicException | IOException e) {
			throw new NoSuchElementException("Incorrect wallet words");
		}




		if (aesKey != null) {
			// This is weak. We should encrypt the new seed here.
			return ("Wallet is encrypted. " + 
					"After restore, the wallet will no longer be encrypted and you must set a new password.");
		}

		log.info("Attempting wallet restore using seed '{}' from date {}", walletWords, dateStr);




		//        long birthday = datePicker.getValue().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		long birthday = LocalDate.parse(dateStr).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		log.info("bday again = " + LocalDate.parse(dateStr));

		DeterministicSeed seed = new DeterministicSeed(Splitter.on(' ').splitToList(walletWords), null, "", birthday);

		// Shut down bitcoinj and restart it with the new seed.
		bitcoin.addListener(new Service.Listener() {
			@Override
			public void terminated(Service.State from) {
				LocalWallet.INSTANCE.setupWalletKit(seed);
				bitcoin.startAsync();
			}
		},MoreExecutors.sameThreadExecutor());

		bitcoin.stopAsync();

		return ("Wallet restore in progress. " + 
				"Your wallet will now be resynced from the Bitcoin network. This can take a long time for old wallets.");
	}

	public String sendMoney(String amountStr, String addressStr) {
		Coin amount = Coin.parseCoin(amountStr);
		Address destination;
		try {
			destination = new Address(LocalWallet.params, addressStr);

			Wallet.SendRequest req = Wallet.SendRequest.to(destination, amount);

			String message = sendWalletRequest(req);

			return message;

		} catch (AddressFormatException e) {
			// Cannot happen because we already validated it when the text field changed.
			throw new NoSuchElementException("Invalid bitcoin address");

		}


	}

	public String sendWalletRequest(Wallet.SendRequest req) {
		// TODO Address exception cannot happen as we validated it beforehand.
		try {

			if (walletIsLocked) {
				return "Wallet is locked, cannot send money.";
			}

			req.aesKey = aesKey;

			sendResult = bitcoin.wallet().sendCoins(req);
			Futures.addCallback(sendResult.broadcastComplete, new FutureCallback<Transaction>() {
				@Override
				public void onSuccess(Transaction result) {
					sendStatus = "Success";
					log.info("Sending money was a success");
				}

				@Override
				public void onFailure(Throwable t) {
					throw new NoSuchElementException("Failed");
				}
			});
			sendResult.tx.getConfidence().addEventListener((tx, reason) -> {
				if (reason == TransactionConfidence.Listener.ChangeReason.SEEN_PEERS)
					updateTitleForBroadcast();
			});

			updateTitleForBroadcast();
		} catch (InsufficientMoneyException e) {
			throw new NoSuchElementException("Could not empty the wallet. " + 
					"You may have too little money left in the wallet to make a transaction.");
			//            overlayUI.done();
		} catch (ECKey.KeyIsEncryptedException e) {
			//            askForPasswordAndRetry();
		} 

		return "Sending money...";
	}

	public String sendRefund(Transaction tx) {

		Wallet.SendRequest req = Wallet.SendRequest.forTx(tx);
		String message = sendWalletRequest(req);

		return message;
	}

	private void updateTitleForBroadcast() {
		if (sendStatus != null && !sendStatus.equals("Success")) {
			final int peers = sendResult.tx.getConfidence().numBroadcastPeers();
			sendStatus = "Broadcasting ... seen by " + peers + " peers";
		}
	}

	private static class IdealPasswordParameters {
		public final int realIterations;
		public final Duration realTargetTime;

		public IdealPasswordParameters(String password) {
			final int targetTimeMsec = 2000;

			int iterations = 16384;
			KeyCrypterScrypt scrypt = new KeyCrypterScrypt(iterations);
			long now = System.currentTimeMillis();
			scrypt.deriveKey(password);
			long time = System.currentTimeMillis() - now;
			log.info("Initial iterations took {} msec", time);

			// N can only be a power of two, so we keep shifting both iterations and doubling time taken
			// until we are in sorta the right general area.
			while (time < targetTimeMsec) {
				iterations <<= 1;
				time *= 2;
			}

			realIterations = iterations;
			// Fudge it by +10% to ensure our progress meter is always a bit behind the real encryption. Plus
			// without this it seems the real scrypting always takes a bit longer than we estimated for some reason.
			realTargetTime = Duration.ofMillis((long) (time * 1.1));
		}
	}





	public String getTransactionsJSON() {

		
		List<Transaction> transactions = bitcoin.wallet().getTransactionsByTime();

		return Tools.convertLOMtoJson(Tools.convertTransactionsToLOM(transactions));
	}

	public String getNewestReceivedTransaction() {
		//		log.info("newest transaction = " + Tools.GSON.toJson(newestReceivedTransaction));
		return new Tools.TransactionJSON(newestReceivedTransaction).json();
	}

	public String getNewestReceivedTransactionHash() {
		return new Tools.TransactionJSON(newestReceivedTransaction).getHash();
	}



	// Reads target time or throws if not set yet (should never happen).
	public static Duration getTargetTime() throws IllegalArgumentException {
		return Duration.ofMillis(Longs.fromByteArray(bitcoin.wallet().getTag(TAG).toByteArray()));
	}

	// Writes the given time to the wallet as a tag so we can find it again in this class.
	public static void setTargetTime(Duration targetTime) {
		ByteString bytes = ByteString.copyFrom(Longs.toByteArray(targetTime.toMillis()));
		bitcoin.wallet().setTag(TAG, bytes);
	}

	public DownloadProgressTracker getDownloadProgressTracker() { return syncProgressUpdater; }

	public DownloadProgressTracker progressBarUpdater() {
		return getDownloadProgressTracker();
	}

	public String getBalanceText() {
		return MonetaryFormat.BTC.noCode().format(getBalance()).toString();
	}
	
	public String getNativeBalance() {

		String toCurrIso = MerchantInfoView.findById(1).getString("native_currency_iso");
		long satoshis = getBalance().value;
		
		Money amount = CurrencyConverter.INSTANCE.convertFromSatoshisCurrent(toCurrIso, satoshis);
		
		return amount.getAmount().toPlainString();
		
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
	public Boolean getWalletIsEncrypted() {
		return walletIsEncrypted;
	}
	public String getWalletWords() {
		return walletWords;
	}
	public String getPasswordBtnText() {
		return passwordBtnText;
	}
	public Boolean getWalletIsLocked() {
		return walletIsLocked;
	}
	public String getSendStatus() {
		return sendStatus;
	}
	
	public Boolean getIsSSLEncrypted() {
		return isSSLEncrypted;
	}
	public void setIsSSLEncrypted(Boolean isSSLEncrypted) {
		this.isSSLEncrypted = isSSLEncrypted;
	}

}

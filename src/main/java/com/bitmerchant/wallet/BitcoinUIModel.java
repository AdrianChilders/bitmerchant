package com.bitmerchant.wallet;



import org.bitcoinj.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * A class that exposes relevant bitcoin stuff
 */
public class BitcoinUIModel {
	
	static final Logger log = LoggerFactory.getLogger(BitcoinUIModel.class);
	
	private Address address = null;
	private Coin balance = Coin.ZERO;



	public BitcoinUIModel() {
	}

	public BitcoinUIModel(Wallet wallet) {
		setWallet(wallet);
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

	

	public Address getAddress() {
		return address;
	}

	public Coin getBalance() {
		return balance;
	}




}

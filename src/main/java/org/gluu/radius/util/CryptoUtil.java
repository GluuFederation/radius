package org.gluu.radius.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import org.gluu.radius.GluuRadiusException;



public class CryptoUtil  {
	
	//we're using 3DES-ECB with PKCS#5 padding
	private static final Integer TRIPLE_DES_BLOCK_SIZE = 8; // in bytes
	private static final Integer TRIPLE_DES_KEY_SIZE = 24; // in bytes
	private static final String  CHARSET_ENCODING = "UTF-8";
	private static final String  TRIPLE_DES_ENCRYPTION_ALGORITHM =  "DESede";
	private static final String  TRIPLE_DES_CIPHER_TRANSFORMATION = "DESede/ECB/PKCS5Padding";


	public static final SecretKeySpec generateTripleDesSecretKeySpec(String encryptionkey) {

		if(encryptionkey == null)
			throw new GluuRadiusException("encryption key cannot be empty");

		try {
			SecretKeySpec keyspec = new SecretKeySpec(encryptionkey.getBytes(CHARSET_ENCODING),
				TRIPLE_DES_ENCRYPTION_ALGORITHM);
			return keyspec;
		}catch(UnsupportedEncodingException e) {
			throw new GluuRadiusException("encryption key unsupported encoding",e);
		}
	}

	public static final String encryptPassword(String plainpassword,String encryptionkey) {

		if(plainpassword == null)
			throw new GluuRadiusException("Invalid password specified");

		try {

			SecretKeySpec keyspec = generateTripleDesSecretKeySpec(encryptionkey);
			Cipher cipher = Cipher.getInstance(TRIPLE_DES_CIPHER_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE,keyspec);
			byte [] encrypted =  cipher.doFinal(plainpassword.getBytes(CHARSET_ENCODING));
			return Base64.encodeBase64String(encrypted);
		}catch(NoSuchAlgorithmException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}catch(NoSuchPaddingException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}catch(InvalidKeyException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}catch(UnsupportedEncodingException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}catch(IllegalBlockSizeException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}catch(BadPaddingException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}
	}


	public static final String decryptPassword(String encryptedpassword,String decryptionkey) {

		if(encryptedpassword == null)
			throw new GluuRadiusException("Invalid encrypted password specified");
		
		try {
			byte [] encpassword = Base64.decodeBase64(encryptedpassword);
			SecretKeySpec keyspec = generateTripleDesSecretKeySpec(decryptionkey);
			Cipher cipher = Cipher.getInstance(TRIPLE_DES_CIPHER_TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE,keyspec);
			byte [] decrypted = cipher.doFinal(encpassword);
			return new String(decrypted,CHARSET_ENCODING);
		}catch(IndexOutOfBoundsException e)  {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(ArrayStoreException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(NullPointerException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(NoSuchAlgorithmException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(NoSuchPaddingException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(InvalidKeyException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(IllegalBlockSizeException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(UnsupportedEncodingException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(BadPaddingException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}
	}
}
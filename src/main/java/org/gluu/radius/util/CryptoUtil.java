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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import org.gluu.radius.GluuRadiusException;



public class CryptoUtil  {
	
	//we're using AES-CBC with PKCS#5 padding 
	private static final Integer IV_SIZE = 16; // in bytes
	private static final Integer BLOCK_SIZE = 16; // in bytes
	private static final Integer KEY_SIZE = 16; // in bytes
	private static final String  DIGEST_ALGORITHM = "SHA-256"; // SHA-256 message digest algorithm
	private static final String  CHARSET_ENCODING = "UTF-8";
	private static final String  ENCRYPTION_ALGORITHM = "AES";
	private static final String  CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

	private static final SecretKeySpec generateSecretKey(String password) {

		try {
			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
			md.update(password.getBytes(CHARSET_ENCODING));
			byte [] keybytes = new byte[KEY_SIZE];
			System.arraycopy(md.digest(),0,keybytes,0,keybytes.length);
			SecretKeySpec ret = new SecretKeySpec(keybytes,ENCRYPTION_ALGORITHM);
			return ret;
		}catch(IndexOutOfBoundsException ie) {
			throw new GluuRadiusException("An unexpected error occured during an encryption operation",ie);
		}catch(ArrayStoreException ae) {
			throw new GluuRadiusException("An unexpected error occured during an encryption operation",ae);
		}catch(NullPointerException npe) {
			throw new GluuRadiusException("An unexpected error occured during an encryption operation",npe);
		}catch(NoSuchAlgorithmException nse) {
			throw new GluuRadiusException("An unexpected error occured during an encryption operation",nse);
		}catch(UnsupportedEncodingException ue) {
			throw new GluuRadiusException("An unexpected error occured during an encryption operation",ue);
		}
	}

	public static final String encryptLdapPassword(String plainpassword) {

		byte [] init_vector = new byte[IV_SIZE];
		SecureRandom srand = new SecureRandom();
		srand.nextBytes(init_vector);
		IvParameterSpec ivpspec = new IvParameterSpec(init_vector);
		SecretKeySpec keyspec = generateSecretKey(plainpassword);
		try {

			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE,keyspec,ivpspec);
			byte [] encrypted =  cipher.doFinal(plainpassword.getBytes(CHARSET_ENCODING));
			byte [] payloadbuffer = new byte [IV_SIZE+KEY_SIZE+encrypted.length];
			int payloadpos = 0;
			//copy init vector 
			System.arraycopy(init_vector,0,payloadbuffer,payloadpos,IV_SIZE);
			payloadpos+=IV_SIZE;
			//copy encryption key
			System.arraycopy(keyspec.getEncoded(),0,payloadbuffer,payloadpos,KEY_SIZE);
			payloadpos+=KEY_SIZE;
			//copy encrypted password
			System.arraycopy(encrypted,0,payloadbuffer,payloadpos,payloadbuffer.length);
			return Base64.encodeBase64String(payloadbuffer);
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
		}catch(InvalidAlgorithmParameterException e) {
			throw new GluuRadiusException("ldap password encryption failed",e);
		}
	}


	public static final String decryptLdapPassword(String encryptedpassword) {

		try {

			byte [] payload = Base64.decodeBase64(encryptedpassword);
			int pos = 0;
			
			byte [] init_vector = new byte [IV_SIZE];
			System.arraycopy(payload,pos,init_vector,0,IV_SIZE);
			pos+=IV_SIZE;

			
			byte [] keydata = new byte[KEY_SIZE];
			System.arraycopy(payload,pos,keydata,0,KEY_SIZE);
			pos+=KEY_SIZE;

			byte [] encpwd_data = new byte[payload.length - IV_SIZE - KEY_SIZE];
			System.arraycopy(payload,pos,encpwd_data,0,payload.length - IV_SIZE - KEY_SIZE);

			IvParameterSpec ivpspec = new IvParameterSpec(init_vector);
			SecretKeySpec  keyspec = new SecretKeySpec(keydata,ENCRYPTION_ALGORITHM);

			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE,keyspec,ivpspec);

			byte [] decrypted = cipher.doFinal(encpwd_data);
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
		}catch(InvalidAlgorithmParameterException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}catch(BadPaddingException e) {
			throw new GluuRadiusException("ldap password decryption failed",e);
		}
	}
}
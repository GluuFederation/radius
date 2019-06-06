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
import org.gluu.radius.exception.GluuRadiusException;

public class EncDecUtil {

    // Encryption/Decryption used here is 
    // 3DES-ECB withc PKCS#5 padding 

    private static final String CHARSET_ENCODING = "UTF-8";
    private static final Integer BLOCK_SIZE = 8; // in bytes
    private static final Integer KEY_SIZE = 24; // in bytes
    private static final String  ALGORITHM_NAME = "DESede";
    private static final String  CIPHER_TRANSFORMATION = "DESede/ECB/PKCS5Padding";

    private static final SecretKeySpec generateTripleDesSecretKeySpec(String encryptionkey) {

        if(encryptionkey == null)
            throw new GluuRadiusException("null encryption key ");
        
        try {
            byte [] keybytes = encryptionkey.getBytes(CHARSET_ENCODING);
            return new SecretKeySpec(keybytes,ALGORITHM_NAME);
        }catch(UnsupportedEncodingException e) {
            throw new GluuRadiusException("unsupported encoding for encryption key",e);
        }
    }

    public static final String encode(String data,String salt) {

        if(data == null )
            throw new GluuRadiusException("Plaintext data cannot be null");
        
        if(salt == null)
            throw new GluuRadiusException("encryption key cannot be null");
        
        try {
            SecretKeySpec keyspec = generateTripleDesSecretKeySpec(salt);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE,keyspec);
            byte [] encrypted = cipher.doFinal(data.getBytes(CHARSET_ENCODING));
            return Base64.encodeBase64String(encrypted);
        }catch(NoSuchAlgorithmException e) {
            throw new GluuRadiusException("data encoding failed",e);
        }catch(NoSuchPaddingException e) {
            throw new GluuRadiusException("data encoding failed",e);
        }catch(InvalidKeyException e) {
            throw new GluuRadiusException("data encoding failed",e);
        }catch(UnsupportedEncodingException e) {
            throw new GluuRadiusException("data encoding failed",e);
        }catch(IllegalBlockSizeException e) {
            throw new GluuRadiusException("data encoding failed",e);
        }catch(BadPaddingException e) {
            throw new GluuRadiusException("data encoding failed",e);
        }
    }

    public static final String decode(String data,String salt) {

        if(data == null)
            throw new GluuRadiusException("data cannot be null");
        
        if(salt == null)
            throw new GluuRadiusException("data cannot be null");

        try {
            byte [] binarydata = Base64.decodeBase64(data);
            SecretKeySpec keyspec = generateTripleDesSecretKeySpec(salt);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE,keyspec);
            byte [] decoded = cipher.doFinal(binarydata);
            return new String(decoded,CHARSET_ENCODING);
        }catch(IndexOutOfBoundsException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(ArrayStoreException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(NoSuchAlgorithmException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(NoSuchPaddingException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(InvalidKeyException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(IllegalBlockSizeException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(UnsupportedEncodingException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }catch(BadPaddingException e) {
            throw new GluuRadiusException("data decoding failed",e);
        }
    }
}
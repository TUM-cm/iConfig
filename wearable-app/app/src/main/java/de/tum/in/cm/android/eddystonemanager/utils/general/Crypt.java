package de.tum.in.cm.android.eddystonemanager.utils.general;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {

  private static final String TAG = Crypt.class.getSimpleName();
  private static final String KEY = "TVVgoAJNLa2sWFXeDOoSP/0GKQSER7bfTJ4RfTdxYqo=";
  private static final String cipherTransformation = "AES/CBC/PKCS5Padding";
  private static final String aesEncryptionAlgorithm = "AES";
  private static final int AES_BLOCK_SIZE = 16;
  private SecretKey secretKey;
  private Cipher cipher;
  private static Crypt instance = null;

  public Crypt() {
    try {
      byte[] data = Base64.decode(KEY, Base64.DEFAULT);
      this.secretKey = new SecretKeySpec(data, aesEncryptionAlgorithm);
      this.cipher = Cipher.getInstance(cipherTransformation);
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG, "create crypt", e);
    } catch (NoSuchPaddingException e) {
      Log.e(TAG, "create crypt", e);
    }
  }

  public static Crypt getInstance() {
    if(instance == null){
      instance = new Crypt();
    }
    return instance;
  }

  public String encrypt(final String data) {
    if (data != null && data.length() > 0) {
      byte[] encryptedData = encrypt(data.getBytes());
      return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    } else {
      return data;
    }
  }

  public String decrypt(final String data) {
    if (data != null && data.length() > 0) {
      byte[] decodedData = Base64.decode(data, 0);
      byte[] decryptedBytes = decrypt(decodedData);
      return new String(decryptedBytes);
    } else {
      return data;
    }
  }

  private byte[] encrypt(byte[] mes){
    try {
      getCipher().init(Cipher.ENCRYPT_MODE, getSecretKey());
      byte[] ivBytes = cipher.getIV();
      byte[] destination = new byte[ivBytes.length + mes.length];
      System.arraycopy(ivBytes, 0, destination, 0, ivBytes.length);
      System.arraycopy(mes, 0, destination, ivBytes.length, mes.length);
      return cipher.doFinal(destination);
    } catch (IllegalBlockSizeException e) {
      Log.e(TAG, "encrypt", e);
    } catch (BadPaddingException e) {
      Log.e(TAG, "encrypt", e);
    } catch (InvalidKeyException e) {
      Log.e(TAG, "encrypt", e);
    }
    return null;
  }

  private byte[] decrypt(byte[] bytes) {
    try {
      byte[] ivB = Arrays.copyOfRange(bytes, 0, AES_BLOCK_SIZE);
      byte[] codB = Arrays.copyOfRange(bytes, AES_BLOCK_SIZE, bytes.length);
      AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivB);
      getCipher().init(Cipher.DECRYPT_MODE, getSecretKey(), ivSpec);
      return getCipher().doFinal(codB);
    } catch (IllegalBlockSizeException e) {
      Log.e(TAG, "decrypt", e);
    } catch (BadPaddingException e) {
      Log.e(TAG, "decrypt", e);
    } catch (InvalidKeyException e) {
      Log.e(TAG, "decrypt", e);
    } catch (InvalidAlgorithmParameterException e) {
      Log.e(TAG, "decrypt", e);
    }
    return null;
  }

  private SecretKey getSecretKey() {
    return this.secretKey;
  }

  private Cipher getCipher() {
    return this.cipher;
  }

}

import java.io.*;
import java.util.function.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

//import java.security.spec.*;
//import javax.crypto.spec.*;
//import javax.crypto.*;

/* Password:
 *     User-entered byte array or string to derive a proper length cipherkey that looks random.
 * Cipher key:
 *     The symmetric (secret) key suitable, sized/padded properly for the encryption ciphers
 *     and is to be used along with the init vector.
 * Initialization vector (aka. nonce!):
 *     A non-secret piece of random data that is distinct for every single encryption and used along with the cipher key.
 */


public class Crypto {
	
	/** My modification of existing classes. Produces encrypted data with  */
	public static final SymmetricCipher aes256gcm = new AES256GCMImpl();
	public static final Hasher sha256 = new SHA256Hasher();
	
	public static interface SymmetricCipher extends Cipher {
		public byte[] encryptWithPassword(byte[] data_plain, byte[] passwordAnyLength);
		public byte[] encrypt(byte[] data_plain, byte[] cipherkey);
		public byte[] encrypt(byte[] data_plain, byte[] initVector, byte[] cipherkey);
		
		public byte[] decryptWithPassword(byte[] data_plain, byte[] passwordAnyLength);
		public byte[] decrypt(byte[] data_cipher_noIV, byte[] initVector, byte[] key);
		public byte[] decrypt(byte[] data_cipher_withIV, byte[] key);
	}
	public static interface AsymmetricCipher extends Cipher {
		public byte[] derivePublic(byte[] privateKey);
		public byte[] encrypt(byte[] publicKey, byte[] data_plain);
		public byte[] decrypt(byte[] privateKey, byte[] data_cipher);
	}
	public static interface Cipher {}
	public static interface Hasher { // Digest
		public default byte[] hash(byte[] input) {
			return hash(new java.io.ByteArrayInputStream(input));
		}
		public byte[] hash(InputStream input);
		public Digest getDigester();
		
		public static interface Digest {
			public void updateWith(byte[] updateWith);
			public byte[] getResult();
		}
	}
	public static interface Signer { // TODO
		public byte[] sign(byte[] message, byte[] privateKey);
		public boolean verify(byte[] message, byte[] sign, byte[] publicKey);
		public byte[] derivePublic(byte[] privateKey);
	}
	
	
	
	public static byte[] embedInitVector(byte[] noIVCiphertext, byte[] initVector) {
			byte[] retVal = new byte[4 + initVector.length + noIVCiphertext.length];
			//  1  2  3  4  5  6  7...
			// [_, _, _, _, _, _, _, ... ,  _, _, ...  ]
			// [nonce len ][   nonce     ] [ciphertext ]
			byte[] a = fn.fromSBEInt(initVector.length);
			fn.copy(a, retVal, 1);
			fn.copy(initVector, retVal, 1 + 4);
			fn.copy(noIVCiphertext, retVal, 1 + 4 + initVector.length);
			return retVal;
	}
	
	/** Generates/derives properly sized keys from the user input with a computational-power-hungry way to prevent brute force
	 *  attacks against relatively short user key.
	 *  <p>When combined with the randomness of the init vectors, there is no way to use a world-widely
	 *  known dictionary/table that everyone around the world has been computing different parts to build it up,
	 *  full of (maybe billions of) possible user passwords and their corresponding generated keys, because each init
	 *  vector (mostly stored at the beginning of a piece of encrypted data) yields a different key from the same password. */
	public static class KeyGenerator {
		// DONE: N***a U sure about leaving those in the interface-like class instead of one of the implementations???
		// Derives a properly sized key from a given password String
		private static final javax.crypto.SecretKeyFactory defaultSecretKeyFactory;
		private static final javax.crypto.SecretKeyFactory
			PBKDF2WithHmacSHA1, PBKDF2WithHmacSHA256, PBKDF2WithHmacSHA512; static {
				try {
					PBKDF2WithHmacSHA1 = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
					PBKDF2WithHmacSHA256 = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
					PBKDF2WithHmacSHA512 = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
				}
				catch (NoSuchAlgorithmException e) {throw new RuntimeException(e);}
				defaultSecretKeyFactory = PBKDF2WithHmacSHA1;
		}
		public static byte[] derivete256BitKeyFromPassword(byte[] password, byte[] salt) {
			return deriveKeyFromPassword(256, password, salt);
		}
		public static byte[] deriveKeyFromPassword(int length, byte[] password, byte[] salt) {
			return deriveKeyFromPassword(length, password, salt, 65536);
		}
		private static byte[] deriveKeyFromPassword(int length, byte[] password, byte[] salt, int iterationCount) {
			javax.crypto.SecretKeyFactory fact = defaultSecretKeyFactory;
				
			byte[] secretKey;
			{
				java.security.spec.KeySpec spec = new javax.crypto.spec.PBEKeySpec(str.toBase64(password).toCharArray(), salt, iterationCount, length);
				// Create the KeySpec to derive a proper sized cipher key (from the key/password) suitable for the cipher
				// Seemingly only accepts Strings as keys so use some universal-ish (base64) way to derive string from byte array!
				
				javax.crypto.SecretKey sk;
				try {sk = fact.generateSecret(spec);} // Generate/derive the proper sized symmetric key
				catch (InvalidKeySpecException e) {throw new RuntimeException(e);}
				secretKey = sk.getEncoded();
			}
			return secretKey;
		}
	}
	
	
	
	private static class SHA256Hasher implements Hasher {
//		private static Supplier<java.security.MessageDigest> sha256digest = null;
		public static java.security.MessageDigest getSHA256Digest() {
			try {return java.security.MessageDigest.getInstance("SHA-256");}
			catch (NoSuchAlgorithmException e) {throw new InternalError(e);}
		}
		
		/* The return value array is fixed length (as the hash output); so no need to
		 * handle the case of very large output */
		public byte[] hash(java.io.InputStream input) {
//			if (sha256digest == null) {sha256digest = fn.cachedReference(() -> {
//				return getSHA256Digest();
//			});}
//			java.security.MessageDigest digest = sha256digest.get();
			Digest digest = getDigester();
		//	int bufferSize = 0x80;
			int bufferSize = 0x10000;
			byte[] chunk = new byte[bufferSize];
			int lastAmount;
			
			while (true) {
				try {lastAmount = input.read(chunk);}
				catch (IOException e) {throw new RuntimeException(e);}
				// TODO: public int java.io.InputStream.read(byte[]) does not return 0. Remove the case of 0.
				if (lastAmount == -1 || lastAmount == 0) break; // If no data is read
				if (lastAmount < bufferSize) { // If data with length smaller than a full block is read; just narrow the last full block to that size
					byte[] _chunk = chunk;
					chunk = new byte[lastAmount];
					for (int i = 1; i <= lastAmount; i++)
						chunk[i-1] = _chunk[i-1];
					digest.updateWith(chunk);
					break;
				}
				digest.updateWith(chunk);
			}
			
			byte[] result = digest.getResult();
			return result;
		}
		
		public Digest getDigester() {
			java.security.MessageDigest enclosedJavaSecrDigest = getSHA256Digest();
			return new Digest() {
				public void updateWith(byte[] updateWith) {enclosedJavaSecrDigest.update(updateWith);}
				public byte[] getResult() {return enclosedJavaSecrDigest.digest();}
			};
		}
	}
	
	
	
	private static class AES256GCMImpl implements SymmetricCipher {
		
		
		public byte[] encryptWithPassword(byte[] data_plain, byte[] passwordAnyLength) {
			return encrypt_AES256GCM_userPassword(data_plain, passwordAnyLength);
		}
		public byte[] encrypt(byte[] data_plain, byte[] cipherkey) {
			return encrypt_AES256GCM(data_plain, cipherkey);
		}
		public byte[] encrypt(byte[] data_plain, byte[] initVector, byte[] cipherkey) {
			return encrypt_AES256GCM(data_plain, cipherkey, initVector);
		}
		public byte[] decryptWithPassword(byte[] data_plain, byte[] passwordAnyLength) {
			return decrypt_AES256GCM_userPassword(data_plain, passwordAnyLength);
		}
		public byte[] decrypt(byte[] data_cipher_noIV, byte[] initVector, byte[] key) {
			return decrypt_AES256GCM(data_cipher_noIV, key, initVector);
		}
		public byte[] decrypt(byte[] data_cipher_withIV, byte[] key) {
			return decrypt_AES256GCM(data_cipher_withIV, key);
		}
		
		
		/** [initVector, cipherText] */
		public byte[][] extractInitVectorAndCipherText(byte[] ciphertextWithIV) {
			byte[] initV, cipherText;
			
			int initVLen; {
				byte[] a = new byte[4];
				fn.copy(ciphertextWithIV, 1, a);
				initVLen = fn.toSBEInt(a);
			}
			int ctLen = ciphertextWithIV.length - 4 - initVLen;
			initV = new byte[initVLen];
			cipherText = new byte[ctLen];
			
			fn.copy(ciphertextWithIV, 1+4,          initV);
			fn.copy(ciphertextWithIV, 1+4+initVLen, cipherText);
			
			return new byte[][] {initV, cipherText};
		}
		
		public byte[] encrypt_AES256GCM_userPassword(byte[] plaintext, byte[] password) {
			byte[] iv = generateRandomInitVector();
			return encrypt_AES256GCM(plaintext, deriveSecretKeyFromByteArray(password, iv), iv);
		}
		public byte[] encrypt_AES256GCM(byte[] plaintext, byte[] cipherkey) {
			byte[] iv = generateRandomInitVector();
			return encrypt_AES256GCM(plaintext, cipherkey, iv);
		}
		
		
		private static final int gcmTagsize = 128;
		
		public byte[] encrypt_AES256GCM(byte[] plaintext, byte[] cipherkey, byte[] initVector) {
			javax.crypto.Cipher cipher = defaultCipher.get();
			{
				java.security.spec.AlgorithmParameterSpec gcmParams = new javax.crypto.spec.GCMParameterSpec(gcmTagsize, initVector);
				java.security.Key ckey = new javax.crypto.spec.SecretKeySpec(cipherkey, "AES");
				try {
					cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, ckey, gcmParams);
				} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
					throw new RuntimeException(e);
				}
			}
			byte[] ciphertext;
			try {ciphertext = cipher.doFinal(plaintext);}
			catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new RuntimeException(e);
			}
//			{
//				cipher = defaultCipher.get();
//				
//				java.security.spec.AlgorithmParameterSpec gcmParams = new javax.crypto.spec.GCMParameterSpec(gcmTagsize, initVector);
//				java.security.Key ckey = new javax.crypto.spec.SecretKeySpec(cipherkey, "AES");
//				try {
//					cipher.init(javax.crypto.Cipher.DECRYPT_MODE, ckey, gcmParams);
//				} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//					throw new RuntimeException(e);
//				}
//				fn.printe(str.toHex(cipherkey));
//				fn.printe(str.toHex(initVector));
//				fn.printe(str.toHex(ciphertext));
//				try {fn.printe(str.utf8decode(cipher.doFinal(ciphertext)));}
//				catch (IllegalBlockSizeException | BadPaddingException e) {
//					throw new RuntimeException(e);
//				}
//			}
			ciphertext = embedInitVector(ciphertext, initVector);
			return ciphertext;
		}
		
		public byte[] decrypt_AES256GCM_userPassword(byte[] ciphertextWithIV, byte[] password) {
			byte[] iv = extractInitVectorAndCipherText(ciphertextWithIV)[1-1];
			// TODO: Unnecessarily copies the ciphertext into new array but we only use the tiny IV
			return decrypt_AES256GCM(ciphertextWithIV, deriveSecretKeyFromByteArray(password, iv));
		}
		public byte[] decrypt_AES256GCM(byte[] ciphertextWithIV, byte[] cipherkey) {
			byte[] initVector, noIVciphertext; {
				byte[][] tuple = extractInitVectorAndCipherText(ciphertextWithIV);
				initVector = tuple[1-1]; noIVciphertext = tuple[2-1];
			}
			return decrypt_AES256GCM(noIVciphertext, cipherkey, initVector);
		}
		
		public byte[] decrypt_AES256GCM(byte[] noIVciphertext, byte[] cipherkey, byte[] initVector) {
			javax.crypto.Cipher cipher = defaultCipher.get();
			{
				java.security.spec.AlgorithmParameterSpec gcmParams = new javax.crypto.spec.GCMParameterSpec(gcmTagsize, initVector);
				java.security.Key ckey = new javax.crypto.spec.SecretKeySpec(cipherkey, "AES");
				try {cipher.init(javax.crypto.Cipher.DECRYPT_MODE, ckey, gcmParams);}
				catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
					throw new RuntimeException(e);
				}
			}
			byte[] plaintext;
			
			try {plaintext = cipher.doFinal(noIVciphertext);}
			catch (IllegalBlockSizeException | BadPaddingException e) {
				throw new RuntimeException(e);
			}
			
			return plaintext;
		}
		
		/** Absorbs any throwables of type Exception, wraps into RuntimeException and throws back. */
		public static interface ExceptionFilteringSupplier<E> extends Supplier<E> {
			public abstract E getHandleExc() throws Exception;
			// Lambda implements this but get is called instead of this, then will call this.
			public default E get() {try {return getHandleExc();}
			catch (Exception e) {throw new RuntimeException(e);}}
		}
		
		private static final Supplier<javax.crypto.Cipher> defaultCipher; static {
			ExceptionFilteringSupplier<javax.crypto.Cipher> AES_GCM_Nopadding, AES_GCM_PKCS5padding;
			// Everytime of encryption we need to have a new Cipher instance,
			// unlike the secretkeyfactory instances, therefore we will store getters instead.
			
			// The only abstract method of the interface (getHandleExc) is declared as to throw exception
			// and the other method (get) handles the exception appropriately
			AES_GCM_Nopadding = () -> javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
			AES_GCM_PKCS5padding = () -> javax.crypto.Cipher.getInstance("AES/GCM/PKCS5Padding"); // No such thing in openjdk!!!
			// XTS does not work because is not even implemented!!!
			
			defaultCipher = AES_GCM_Nopadding;
//			defaultCipher = AES_GCM_PKCS5padding;
			if (false) assert fn.act(() -> { // Test if the underlying JVM has those ciphers, and fail to init. this class if not.
				AES_GCM_Nopadding.get();
				AES_GCM_PKCS5padding.get();
			});
		}
		
		
		
		
		public byte[] generateRandomInitVector() {
			byte[] iv = new byte[12];
			for (int i: fn.range(12)) iv[i-1] = fn.toByte(fn.Random.random(0, 255));
			return iv;
		}
		
		// Derive a proper sized cipher key (from the key/password) suitable for the cipher
		public byte[] deriveSecretKeyFromByteArray(byte[] password, byte[] salt) {
			return KeyGenerator.deriveKeyFromPassword(256, password, salt);
		}
		
		public byte[] generateSecretKeyFromPassword(String password, byte[] initVector) {
			return deriveSecretKeyFromByteArray(str.utf8encode(password), initVector);
		}
		
		
		
		
		
	//	private static byte[] generateSecretKey(byte[] password, byte[] nonceInitVector) {
	//		java.security.spec.KeySpec spec = new javax.crypto.spec.PBEKeySpec(password, nonceInitVector, 65536, 256); // AES-256
	//		javax.crypto.spec.PBEKeySpec r;
	//		SecretKeyFactory fact = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	//	}
	}
	
	
	
	
	
	
	
	
	
}
/*
 * This file is part of Brewday.
 *
 * Brewday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brewday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Brewday.  If not, see https://www.gnu.org/licenses.
 */

package mclachlan.maze.data.v2.sensitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.spec.SecretKeySpec;

/**
 * The class provides local storage for sensitive information. It stores data in
 * a Java keystore. Keystore access is protected with a three-part password that
 * is locally obfuscated and unique to every deployment.
 * <p>
 * This storage is appropriate for non critical data like API keys.
 * Do not use it to store information the needs a high level of security.
 */
public class SensitiveStore
{
	public static final String STORE_TYPE = "JCEKS";
	private byte[] classKey = new byte[]
		{
			0x2c, (byte)0xd7, 0x4b, (byte)0xd2, 0x46, 0x4c, (byte)0xfc, (byte)0xb2,
			0x03, (byte)0xb8, 0x77, (byte)0xf8, (byte)0x84, (byte)0xbf, 0x71,
			(byte)0xac, (byte)0x99, (byte)0x8f, 0x14, (byte)0xb6
		};
	private char[] chars;
	private String rootDir;
	private String prefix;

	/*-------------------------------------------------------------------------*/
	public SensitiveStore(String rootDir, String prefix)
	{
		this.rootDir = rootDir;
		this.prefix = prefix;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Return the password portion unique to this deployment. If it's not
	 * present, generate a new one and store it for future use.
	 */
	private byte[] getDistKey() throws IOException
	{
		File file = new File(rootDir, prefix+".dist.v2");
		byte[] result = new byte[20];

		if (file.exists())
		{
			FileInputStream fis = new FileInputStream(file);
			if (fis.read(result) < result.length)
			{
				throw new IOException("wrong dist key length");
			}
			return result;
		}
		else
		{
			SecureRandom r = new SecureRandom();
			r.nextBytes(result);

			new File(rootDir).mkdirs();
			file.createNewFile();

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(result);
			fos.flush();
			fos.close();

			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	private File getStorePath()
	{
		return new File(rootDir, prefix+".sensitive.v2");
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Concatenates the three password pieces to form the keystore password.
	 */
	private char[] createChars(String appKey) throws IOException
	{
		String s = new String(asciify(getDistKey())) +
			new String(asciify(classKey)) +
			appKey;

		return s.toCharArray();
	}

	/*-------------------------------------------------------------------------*/
	private char[] asciify(byte[] bytes)
	{
		char[] result = new char[bytes.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = (char)Math.max(32, Math.abs(bytes[i] % 127));
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Initialise this sensitive store.
	 * @param appKey
	 * 	The application supplied part of the keystore password.
	 */
	public void init(String appKey) throws Exception
	{
		chars = createChars(appKey);

		if (getStorePath().exists())
		{
			KeyStore ks = KeyStore.getInstance(STORE_TYPE);
			ks.load(new FileInputStream(getStorePath()), chars);
		}
		else
		{
			KeyStore ks = KeyStore.getInstance(STORE_TYPE);
			ks.load(null, chars);
			ks.store(new FileOutputStream(getStorePath()), chars);
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Returns the data stores with the given key, or null if non is present.
	 */
	public String get(String key) throws Exception
	{
		KeyStore ks = KeyStore.getInstance(STORE_TYPE);
		FileInputStream stream = new FileInputStream(getStorePath());
		ks.load(stream, chars);
		stream.close();

		Key value = ks.getKey(key, chars);

		if (value == null)
		{
			return null;
		}
		{
			return new String(value.getEncoded());
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Adds to given key-value pair to sensitive storage.
	 */
	public void set(String key, String value) throws Exception
	{
		KeyStore ks = KeyStore.getInstance(STORE_TYPE);
		ks.load(new FileInputStream(getStorePath()), chars);
		byte[] bytes = value.getBytes();

		// these aren't really AES keys, we fake it
		KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(
			new SecretKeySpec(bytes, "AES"));

		KeyStore.PasswordProtection protParam = new KeyStore.PasswordProtection(chars);
		ks.setEntry(key, secretKeyEntry, protParam);

		FileOutputStream stream = new FileOutputStream(getStorePath());
		ks.store(stream, chars);
		stream.flush();
		stream.close();
	}
}

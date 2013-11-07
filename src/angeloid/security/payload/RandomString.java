package angeloid.security.payload;

/*
 * AngeloidSecurity ����
 * 
 * �� ���̺귯���� Angeloid Team �ȿ��� �����Ǵ� ���� ���ø����̼��� ��ȣȭ�� ���Դϴ�.
 * 
 * �Ʒ� ����� ������ �� �ֽ��ϴ�.
 * 
 * CRC Check - 8bit
 * MD5 Check - 128bit
 * AES Encrypt / Decrypt - 128 ~ 256bit [��������]
 * RSA Encrypt / Decrypt - 1024bit [��������]
 * keystore check 
 * [in-app billing] payload [��������]
 * 
 * �� ���̺귯���� ���ÿ� �ݵ�� ��� �κ��� Ŭ������ ����ȭ ���ֽʽÿ�.
 * 
 * ���� publickey �Ǵ� privatekey�� �����ϴ� ���� �� ���̺귯���� ũ���ȴٴ� ���� �����ϴ� �Ͱ� �����ϴ�.
 * 
 * �Ʒ� ������ ���� ���̺귯�� �������� �����ϴ� �����Դϴ�. 
 * 
 * �� �޼��忡 ���� �ڼ��� ������ Javadoc�� �����ϼ���
 * 
 * ���� - payload =================
 * 
 * getPayload().getNextpayload() 
 * 
 * ���� - AES�� ��ȣȭ�ϱ� ========
 * 
 * Encrypt_AES().encrypt(String seed, String text);
 * 
 * ���� - AES�� ��ȣȭ�ϱ� =============
 * 
 * Decrypt_AES().decrypt(String seed, String encryptedtext);
 * 
 * ���� - RSA ����ϱ� - �⺻ ==========
 * 
 * getKey.getPublicKey();
 * getKey.getPrivateKey();
 * 
 * ���� - RSA�� ��ȣȭ�ϱ� ===========
 * 
 * Encrypt_RSA(Key privatekey, String text).Encrypt();
 * 
 * ���� - RSA�� ��ȣȭ�ϱ� =========
 * 
 * Decrypt_RSA(Key publickey, byte[] encodedBytes).Decrypt();
 * 
 */

import java.util.Random;

public class RandomString {

	private static final char[] symbols = new char[36];

	static {
		for (int idx = 0; idx < 10; ++idx)
			symbols[idx] = (char) ('0' + idx);
		for (int idx = 10; idx < 36; ++idx)
			symbols[idx] = (char) ('a' + idx - 10);
	}

	private final Random random = new Random();

	private final char[] buf;

	public RandomString(int length) {
		if (length < 1) throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}

	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}

}
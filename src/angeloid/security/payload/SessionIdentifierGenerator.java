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

import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdentifierGenerator {

	private SecureRandom random = new SecureRandom();

	public String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}

}
package angeloid.security.payload;

/*
 * AngeloidSecurity 도움말
 * 
 * 이 라이브러리는 Angeloid Team 안에서 배포되는 많은 어플리케이션의 암호화에 쓰입니다.
 * 
 * 아래 기능을 구현할 수 있습니다.
 * 
 * CRC Check - 8bit
 * MD5 Check - 128bit
 * AES Encrypt / Decrypt - 128 ~ 256bit [구현가능]
 * RSA Encrypt / Decrypt - 1024bit [구현가능]
 * keystore check 
 * [in-app billing] payload [구현가능]
 * 
 * 이 라이브러리를 사용시에 반드시 사용 부분의 클래스를 난독화 해주십시오.
 * 
 * 또한 publickey 또는 privatekey를 공개하는 것은 이 라이브러리가 크랙된다는 것을 공개하는 것과 같습니다.
 * 
 * 아래 사용법은 현재 라이브러리 버전에서 제공하는 사용법입니다. 
 * 
 * 각 메서드에 대한 자세한 설명은 Javadoc를 참고하세요
 * 
 * 사용법 - payload =================
 * 
 * getPayload().getNextpayload() 
 * 
 * 사용법 - AES로 암호화하기 ========
 * 
 * Encrypt_AES().encrypt(String seed, String text);
 * 
 * 사용법 - AES로 복호화하기 =============
 * 
 * Decrypt_AES().decrypt(String seed, String encryptedtext);
 * 
 * 사용법 - RSA 사용하기 - 기본 ==========
 * 
 * getKey.getPublicKey();
 * getKey.getPrivateKey();
 * 
 * 사용법 - RSA로 암호화하기 ===========
 * 
 * Encrypt_RSA(Key privatekey, String text).Encrypt();
 * 
 * 사용법 - RSA로 복호화하기 =========
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
package webchat.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static webchat.util.StringUtils.*;

public class Hasher {

	public static void main(String[] args) throws Exception {
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter password to hash or empty line to quit: ");
		String password;
		while ( !isNullOrEmpty((password = rdr.readLine())) ) {
			
			String hash = PasswordHash.createHash(password);
			System.out.println("Hash: " + hash);
			System.out.println("Next password: ");
		}
		
	}
}

package webchat.util;

import java.io.Closeable;

public class Util {


 
	public static void closeQuietly(Closeable c) { 
		if ( c != null) {
			try {
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}

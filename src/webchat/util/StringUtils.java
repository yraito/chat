package webchat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;



public class StringUtils {
	

	static Pattern p = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	
	//not implemented yet
	public static String safeMessage(String msg) { 
		return msg;
	}
	
	public static boolean isNullOrEmpty(String s) { 
		return s == null || s.trim().isEmpty();
	}
	
	public static boolean hasLengthBetween(String s, int a, int b) {
		return s!= null && s.length() >= a && s.length() <= b;
	}
	
	/**
	 * Split a String around whitespaces, respecting quoted groups. Ie the "quick" "brown fox"
	 * is aplit into {the, quick, brown fox}
	 * 
	 * @param msg 
	 * @return
	 */
	public static String[] splitQuoted(String msg) {
                if (msg == null) {
                    return new String[0];
                }
		Matcher m = p.matcher(msg);
		ArrayList<String> lst = new ArrayList<>();
		while (m.find()) {
			lst.add(m.group(1).replace("\"", ""));
		}
		return lst.toArray(new String[lst.size()]);
	}
		
	/**
	 * Read entirety of an InputStream into a String, decoding as UTF-8. Does not close
	 * the stream. 
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String readFully(InputStream is) throws IOException  {
		
		/*
		ArrayList<byte[]> bbuffs = new ArrayList<>();
		byte[] bbuff = new byte[1024];
		int read = 0, off = 0;
		while ( (read = is.read(bbuff, off, bbuff.length - off)) != -1 ) {
			off += read;
			if (off == bbuff.length) {
				bbuffs.add(bbuff);
				bbuff = new byte[1024];
				off = 0;
			} 
		}

		byte[] totbuff = new byte[bbuffs.size()*1024 + off];
		int index = 0;
		for ( int j = 0; j < bbuffs.size(); j++) {
			byte[] buff = bbuffs.get(j);
			for (int k = 0; k < buff.length; k++, index++) {
				totbuff[index] = buff[k];
			}
		}
		for (int j = 0; j < off; j++, index++) {
			totbuff[index] = bbuff[j];
		}
		return new String(totbuff, "UTF-8");
		*/
		
		try {
			InputStreamReader rdr = new InputStreamReader(is, "UTF-8");
			StringWriter wrt = new StringWriter();
			char[] charbuff = new char[8192];
			int read = 0;
			while ( (read = rdr.read(charbuff)) != -1 ) {
				wrt.write(charbuff, 0, read);
			}
			return wrt.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

        public static String newString(List<?> objs, String delimiter) {
            StringJoiner sj = new StringJoiner(delimiter);
            objs.stream().forEach(t->sj.add(t.toString()));
            return sj.toString();
        }
        public static String newString(List<?> objs) {
            return newString(objs, ",");
        }
        
        public static String newString(String s, int n) {
            StringJoiner sj = new StringJoiner(",");
            Stream.generate(()->s).limit(n).forEach(t->sj.add(t));
            return sj.toString();
        }
}

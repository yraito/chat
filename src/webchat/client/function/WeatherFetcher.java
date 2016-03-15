package webchat.client.function;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class WeatherFetcher {

	
	private String apiKey = "7a8c31db1686c60299f13ee1ee425e29";

	private String urlS = "http://api.openweathermap.org/data/2.5/weather";
	
	//alphanumeric safe
	
	
	public WeatherConditions fetch(String city) throws IOException  {
		
		URL url = null;
		try {
			url = new URL(urlS + "?q=" + city + "&appid=" + apiKey + "&mode=xml&units=imperial");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		HttpURLConnection hc = (HttpURLConnection) url.openConnection();
		try (InputStream is = hc.getInputStream()) {
			
			String charset  = getCharset(hc);
			System.out.println(charset);
			System.out.println(hc.getResponseCode());
			if (charset == null) { 
				charset = "UTF-8";
			}
			
			try (InputStreamReader isr = new InputStreamReader(is, charset);
					BufferedReader br= new BufferedReader(isr)) {
				
				return unmarshal(br);
				
			} catch (JAXBException e) {
				throw new IOException(e);
			}
			
		} 
	}

	private String getCharset(HttpURLConnection hc) {
		//so
		String contentType = hc.getHeaderField("Content-Type");
		String charset = null;

		for (String param : contentType.replace(" ", "").split(";")) {
		    if (param.startsWith("charset=")) {
		        charset = param.split("=", 2)[1];
		        break;
		    }
		}
		return charset;
		//so
	}
	
	private WeatherConditions unmarshal(Reader rdr) throws JAXBException {
		JAXBContext ctx = JAXBContext.newInstance(WeatherConditions.class);
		Unmarshaller sm = ctx.createUnmarshaller();
		WeatherConditions wc = (WeatherConditions) sm.unmarshal(rdr);
		return wc;
	}
	
	public static void main(String[] args) throws IOException, JAXBException {
		String urlS = "http://api.openweathermap.org/data/2.5/weather";
		String apiKey = "2de143494c0b295cca9337e1e96b00e0";
		String city = "hgkjhgkjgkjgkjgjkgjkgjkhg";
		URL url = new URL(urlS + "?q=" + city + "&appid=" + apiKey + "&mode=xml&units=imperial");
		HttpURLConnection hc = (HttpURLConnection) url.openConnection();
		InputStream is = hc.getInputStream();
		InputStreamReader rdr = new InputStreamReader(is);
		BufferedReader b = new BufferedReader(rdr);
		String s;
		StringWriter sw = new StringWriter();
		while( (s =b.readLine()) != null ) {
			sw.write(s);
			System.out.println(s);
		}
		byte[] bt = sw.toString().getBytes();
				JAXBContext ctx = JAXBContext.newInstance(WeatherConditions.class);
				Marshaller m = ctx.createMarshaller();
				Unmarshaller sm = ctx.createUnmarshaller();
			
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				WeatherConditions wc = (WeatherConditions) sm.unmarshal(new ByteArrayInputStream(bt));
				System.out.println(wc); 
		//WeatherFetcher wf = new WeatherFetcher();
		//System.out.println(wf.fetch("hgkjhgkjgkjgkjgjkgjkgjkhg"));

	}
	
}

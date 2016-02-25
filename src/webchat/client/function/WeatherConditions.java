package webchat.client.function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="current")
public class WeatherConditions {

	public static class Temperature {
		
		@XmlAttribute(name="value")
		public double value;
		
		@XmlAttribute(name="unit")
		public String unit;

		public String toString() {
			return value + " deg. " + unit; 
		}
	}
	
	public static class Wind {
		
		@XmlElement(name="speed")
		private WindSpeed windSpeed;
		
		@XmlElement(name="direction")
		private WindDirection windDir;

		public String toString() {
			return "Wind " + windSpeed.value + "mph " + windDir.code;
		}
	}
	
	private static class Clouds{
		
		@XmlAttribute(name="name")
		String name;
		
		public String toString() {
			return name;
		}
	}
	
	private static class Humidity {
		
		@XmlAttribute(name="value")
		int value;
		
		public String toString() {
			return "Humidity " + value + "%";
		}
	}
	
	private static class Precipitation {
		
		@XmlAttribute(name="mode")
		String mode;
		
		public String toString() {
			return mode;
		}
	}
	
	private static class WindSpeed {
		
		@XmlAttribute(name="value")
		double value;
	}
	
	private static class WindDirection {
		
		@XmlAttribute(name="code")
		String code;
	}
	
	@XmlElement(name="temperature")
	private Temperature temperature;

	@XmlElement(name="clouds")
	private Clouds clouds;

	@XmlElement(name="humidity")
	private Humidity humidity;
	
	@XmlElement(name="precipitation")
	private Precipitation precipitation;

	@XmlElement(name="wind")
	private Wind wind;
	
	public Temperature getTemperature() {
		return temperature;
	}

	public String getClouds() {
		return clouds.toString();
	}

	public String getHumidity() {
		return humidity.toString();
	}

	public String getPrecipitation() {
		return precipitation.toString();
	}

	public Wind getWind() {
		return wind;
	}

	public String toString() {
		if ( "no".equalsIgnoreCase( getPrecipitation() ) ){
			return getTemperature() + " with " + getClouds() + ", " 
					+ getWind() + ", " + getHumidity(); 
		} else {
			return getTemperature() + " with " + getPrecipitation() + ", " 
					+ getWind() + ", " + getHumidity(); 
		}
	}
	
	public static void main(String[] args) throws JAXBException {
		
		JAXBContext ctx = JAXBContext.newInstance(WeatherConditions.class);
		Marshaller m = ctx.createMarshaller();
		Unmarshaller sm = ctx.createUnmarshaller();

		
	}






	
	
}

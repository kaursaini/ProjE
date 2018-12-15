package model;

import java.math.BigInteger;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Engine
{
	public static final double LAT_MIN = -90;
	public static final double LAT_MAX = 90;
	public static final double LON_MIN = -180;
	public static final double LON_MAX = 180;
	static final double RAD_PER_DEG = Math.PI / 180;
	static final double GPS_CALC_FIX = 12742;
	static final double DRONE_SPEED = 150;
	static final long MIN_PER_HOUR = 60;
	static final double TRAFFIC_COST_PER_MIN = 0.5;

	static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/";
	static final String DISTANCEMATRIX_URL="https://maps.googleapis.com/maps/api/distancematrix/";
	final int HTTPSuccessCodeRange = 299;
	private final String API_KEY="AIzaSyD8aAcZILnrVRYPMQoo4MZxq3eQBAVFtM8";

	private static Engine instance = null;

	private Engine()
	{	
	}
	
	public synchronized static Engine getInstance()
	{
		if (instance == null)
		{
			instance = new Engine();
		}
		return instance;
	}
	
	public BigInteger doPrime(String min, String max) throws Exception
	{
		BigInteger min_bi;
		BigInteger max_bi;
		//check if entries are valid
		try
		{
			min_bi = new BigInteger(min);
			max_bi = new BigInteger(max);
		}
		catch (Exception e)
		{
			throw new Exception ("Invalid Entries!", e);
		}
		
		//reject if min > max.
		if(min_bi.compareTo(max_bi) > 0)
		{
			throw new Exception ("No more primes in range.");
		}
		
		BigInteger result = min_bi.nextProbablePrime();
		if(result.compareTo(max_bi) > 0)
		{
			throw new Exception ("No more primes in range.");
		}
	
		return result;
	}
	
	//return value is in unit of km
	double computeDistance(double lat1, double lon1, double lat2, double lon2)
	{
		double t1=lat1 * RAD_PER_DEG;
		double t2=lat2 * RAD_PER_DEG;
		double n1=lon1 * RAD_PER_DEG;
		double n2=lon2 * RAD_PER_DEG;
		
		double y = Math.cos(t1) * Math.cos(t2);
		double x = Math.pow(Math.sin((t2-t1)/2),2) + 
			   y * Math.pow(Math.sin((n2-n1)/2),2);
		double result = GPS_CALC_FIX * Math.atan2(Math.sqrt(x), Math.sqrt(1-x));
		return result;
	}
	public double doGps(String lat1, String lon1, String lat2, String lon2) throws Exception
	{
		double lat1_d;
		double lat2_d;
		double lon1_d;
		double lon2_d;		
		double result;
		try
		{
			lat1_d = Double.parseDouble(lat1);
			lat2_d = Double.parseDouble(lat2);
			lon1_d = Double.parseDouble(lon1);
			lon2_d = Double.parseDouble(lon2);			
		}
		catch(Exception e)
		{
			throw new Exception ("Invalid Entries!",e);
		}
		try
		{
			return computeDistance(lat1_d, lon1_d, lat2_d, lon2_d);
		}
		catch (Exception e)
		{
			throw new Exception ("Fail to calculate distance",e);
		}
	}
	
	private class GeoCoordinate
	{
		double lat;
		double lon;
		
		GeoCoordinate(String lat, String lon)
		{
			this.lat = Double.parseDouble(lat);
			this.lon = Double.parseDouble(lon);
		}
		
		@Override
		public String toString()
		{
			return lat + "," + lon;
		}
	}
	
	//will throw exception if address is not found by google geocode
	GeoCoordinate getGeoCoordinate(String address) throws Exception
	{
		try
		{
			String add = address.replace(" ", "%20");
			String query = "xml?address="+add+"&key=" + API_KEY;
			String url_path = GEOCODE_URL  + query; 
			System.out.println("url_path="+ url_path);
			URL url = new URL(url_path);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			if (con.getResponseCode()>HTTPSuccessCodeRange)
			{
				throw new ConnectException ("fail to get coordinate of address " + address);
			}
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(con.getInputStream());
			con.disconnect();

			//check status return by the response of the query
			String status = ((Element)doc.getElementsByTagName("status").item(0)).
											getTextContent();
			if(!status.toUpperCase().equals("OK"))
			{
				throw new Exception (address + ": " + status);
			}
			Element geo_location = (Element)((Element)doc.getElementsByTagName("geometry").item(0)).
						  getElementsByTagName("location").item(0);
			GeoCoordinate coor = new GeoCoordinate(geo_location.getElementsByTagName("lat").item(0).getTextContent(),
												   geo_location.getElementsByTagName("lng").item(0).getTextContent());
			return coor;
		}
		catch (Exception e)
		{
			throw new Exception("fail to get coordinate of address " + address, e);
		}
	}
	//return result in minute
	public double doDrone(String from, String dest) throws Exception
	{
		//Note: any exception caused by failure to get coord, will be thrown by getCoordinate method.
		GeoCoordinate from_coord = getGeoCoordinate(from);
		GeoCoordinate dest_coord = getGeoCoordinate(dest);
		double distance = computeDistance(from_coord.lat, from_coord.lon, dest_coord.lat, dest_coord.lon);
		System.out.println(distance);
		return distance * MIN_PER_HOUR/ DRONE_SPEED ;
	}
	
	//return the duration in second
	double getDistanceMatrix(String from, String dest) throws Exception
	{
		String from_query = from.replace(" ", "%20");
		String dest_query = dest.replace(" ", "%20");
		String query ="xml?origins=\"" + from_query + "\"&destinations=\"" + dest_query + "\"&departure_time=now&key=" + API_KEY; 		
		String url_path = DISTANCEMATRIX_URL + query;
		System.out.println(url_path);
		URL url = new URL(url_path);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		con.connect();
		if(con.getResponseCode()>HTTPSuccessCodeRange)
		{
			throw new ConnectException("Fail to get distance matrix");
		}
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = dBuilder.parse(con.getInputStream());
		con.disconnect();
		String duration = ((Element)doc.getElementsByTagName("duration_in_traffic").item(0)).
				getElementsByTagName("value").item(0).getTextContent();
	
		return Long.parseLong(duration);
	}
	//return cost in dollar. Exception is thrown by method that compute the distance matrix;
	public double doRide(String from, String dest) throws Exception
	{
		
		double cost = getDistanceMatrix(from, dest) / MIN_PER_HOUR * TRAFFIC_COST_PER_MIN;
		System.out.println(cost);
		return cost;
	}
	

	public List<StudentBean> doSis(String prefix, String minGpa, String sortBy) throws Exception
	{
		try
		{
			return (StudentDAO.query(prefix, minGpa, sortBy));			
		}
		catch (Exception e)
		{
			throw new Exception(e);
		}
	}
	
}

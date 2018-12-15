package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

class StudentDAO
{
	private static class ConnectDerbyFactory
	{
		
		private static final String DB_URL = "jdbc:derby://localhost:64413/EECS;user=student;password=secret";	
		private static ConnectDerbyFactory factory = null;
		private ConnectDerbyFactory() throws Exception
		{
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
		}
		static synchronized ConnectDerbyFactory getFactory() throws Exception
		{
			if (factory == null)
			{
				factory = new ConnectDerbyFactory();
			}
			return factory;
		}
		Connection getConnection() throws Exception
		{
			Connection con = DriverManager.getConnection(DB_URL);
			return con;
		}
	}
	
	static private ConnectDerbyFactory factory = null;
		
	private static String toTitleCase(String s)
	{
		if (s.isEmpty())
			return s;
		String result = s.toLowerCase();
		String firstLetter =result.substring(0, 1); 
		return result.replaceFirst(firstLetter, firstLetter.toUpperCase());
	}
	
	static List<StudentBean> query(String prefix, String minGpa, String sortBy) throws Exception
	{
		Connection con = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try
		{
			if (factory == null)
			{
				factory = new ConnectDerbyFactory();
			}
			con = factory.getConnection();
			
			s = con.prepareStatement("SET SCHEMA ROUMANI");
			s.executeUpdate();
			s.close();
			
			String sql = "SELECT * FROM SIS WHERE SURNAME LIKE ? AND GPA >= ? ";
			
			String name_prefix = prefix==null? 
					"" : toTitleCase(prefix);
			float gpaMin_f = minGpa==null||minGpa.isEmpty()? 
					0 : Float.parseFloat(minGpa);
			if (!sortBy.toUpperCase().equals("NONE"))
				sql = sql + " ORDER BY " + sortBy;
			
			s = con.prepareStatement(sql);
			s.setString(1, name_prefix+"%");
			s.setFloat(2,gpaMin_f);
		
			rs= s.executeQuery();
			List<StudentBean> result = new ArrayList<StudentBean>();
			while(rs.next())
			{
				String name = rs.getString("SURNAME") + "," + rs.getString("GIVENNAME");
				String major = rs.getString("MAJOR");
				double  gpa = rs.getDouble("GPA");
				int courses = rs.getInt("COURSES");
				StudentBean e = new StudentBean(name, major, gpa, courses);
				result.add(e);
			}
			return result;
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (rs!=null && !rs.isClosed()) rs.close();
			if(s!=null && !s.isClosed()) s.close();
			if(con !=null && !con.isClosed()) con.close();
		}
	}
}

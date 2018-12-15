package model;

public class StudentBean
{
	String name;
	String major;
	double gpa;
	int courses;
	public StudentBean(String name, String major, double gpa, int courses)
	{
		super();
		this.name = name;
		this.major = major;
		this.gpa = gpa;
		this.courses = courses;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getMajor()
	{
		return major;
	}
	public void setMajor(String major)
	{
		this.major = major;
	}
	public double getGpa()
	{
		return gpa;
	}
	public void setGpa(double gpa)
	{
		this.gpa = gpa;
	}
	public int getCourses()
	{
		return courses;
	}
	public void setCourses(int courses)
	{
		this.courses = courses;
	}
	@Override
	public String toString()
	{
		return "StudentBean [name=" + name + ", major=" + major + ", gpa=" + gpa + ", courses=" + courses + "]";
	}
	
}

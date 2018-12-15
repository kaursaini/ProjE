package ctrl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Engine;
import model.StudentBean;

@WebServlet("/Sis.do")
public class Sis extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    public Sis() 
    {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String jsonString = null;;
		try
		{
			Engine engine = Engine.getInstance();
			String prefix = request.getParameter("prefix");
			String minGpa = request.getParameter("minGpa");
			String sortBy = request.getParameter("sortBy");
			
			List<StudentBean> result = engine.doSis(prefix, minGpa, sortBy);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			jsonString = "{\"status\":0,\"result\":"+ gson.toJson(result) +"}";
		}
		catch(Exception e)
		{
			String err = e.getMessage();
			err = err.replaceAll("\"", "'");
			jsonString = "{\"status\":1,\"error\":\"" + err + "\"}";
		}
		finally
		{
			System.out.println(jsonString);
			response.setContentType("text/json");
			Writer out = response.getWriter();
			out.write(jsonString);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}

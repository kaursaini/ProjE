package ctrl;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Engine;

@WebServlet("/Prime.do")
public class Prime extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    public Prime() 
    {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Engine engine = Engine.getInstance();
		String min = request.getParameter("min");
		String max = request.getParameter("max");
		String jsonString = null;
		
		BigInteger result;
		try
		{
			result = engine.doPrime(min, max);
			jsonString = "{\"status\":0,\"min\":"+ min +",\"max\":"+ max +
									",\"result\":"+ result +"}";
		}
		catch (Exception e)
		{
			jsonString = "{\"status\":1,\"error\":\"" + e.getMessage() + "\"}";
		}
		finally
		{
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

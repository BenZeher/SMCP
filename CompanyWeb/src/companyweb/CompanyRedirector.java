package companyweb;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.*;

public class CompanyRedirector extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		//System.out.println("request.getRemoteAddr() = " + request.getRemoteAddr());
		//System.out.println("request.getHeader('user-agent') = " + request.getHeader("user-agent"));
		
		//System.out.println("request.getRequestURL() = " + request.getRequestURL());
		
		//Insert a record into the hit counter table:
		String SQL = "INSERT INTO sitehits "
			+ "(dathitdate, ssitename, sbrowsertype, sremoteaddress, suserheader, suseros)"
			+ " VALUES ("
			+ "NOW()"
			+ ", '" + request.getParameter("sitename") + "'"
			+ ", '" + getBrowserType(request) + "'"
			+ ", '" + request.getRemoteAddr() + "'" 
			+ ", '" + request.getHeader("user-agent") + "'"
			+ ", '" + getUserOS(request) + "'"
			+ ")";

		try{
	    	if (!clsDatabaseFunctions.executeSQL(SQL, getServletContext(), request.getParameter("conffile"))){
	    		out.println(this.toString() + "Could not insert hit record.<BR>");
	    	}else{
	    		//System.out.println(this.toString() + "Successfully updated " + "entry" + ": " + sEntryNumber() + ".");
	    	}
    	}catch(SQLException ex){
    		System.out.println("Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	}
		
    	/*
    	//Check out the headers here:
    	out.println("<HTML><BODY>");
        Enumeration paramNames = request.getHeaderNames();
        while(paramNames.hasMoreElements()) {
        	String sParam = (String)paramNames.nextElement();
        	out.println("PARAM: " + sParam  + ": " + request.getHeader(sParam));
        }
    	
    	out.println("</BODY></HTML>");
    	*/
    	out.println("<HTML><BODY><META http-equiv='Refresh' content='0;URL=http://www.odcdc.com/index.htm'></BODY></HTML>");
	
	return;
	
	}
	
	public String getBrowserType (HttpServletRequest req)
	{
	   String s = req.getHeader("user-agent");
	   if (s == null)
	      return "NOT FOUND";
	   if (s.indexOf("MSIE") > -1)
	      return "Internet Explorer";
	   else if (s.indexOf("Netscape") > -1)
	      return "Netscape";
	   else if (s.indexOf("BlackBerry") > -1)
			  return "BlackBerry";
	   else if (s.indexOf("Googlebot") > -1)
			  return "Google Web Crawler";
	   else if (s.indexOf("Baiduspider") > -1)
			  return "Baidu Web Spider";
	   else if (s.indexOf("Nutch") > -1)
			  return "Nutch Web Crawler";
	   else if (s.indexOf("Yahoo! Slurp") > -1)
			  return "Yahoo Web Crawler";
	   else if (s.indexOf("Sosospider") > -1)
			  return "Soso Web Spider";
	   else if (s.indexOf("Opera") > -1)
			  return "Opera";
	   //Keep Mozilla at the bottom, because it is referenced by some crawlers above, and we want
	   // to pick them up first - TJR
	   else if (s.indexOf("Mozilla") > -1)
			  return "Mozilla";	   //else:
	   return "UNKNOWN";

	}
	
	public String getUserOS (HttpServletRequest req)
	{
	   String s = req.getHeader("user-agent");
	   if (s == null)
	      return "NOT FOUND";
	   if (s.indexOf("Win95") > -1)
	      return "Windows 95";
	   else if (s.indexOf("BlackBerry") > -1)
		      return "BlackBerry";
	   else if (s.indexOf("Macintosh") > -1)
	      return "Macintosh";
	   else if (s.indexOf("Win98") > -1)
		  return "Windows 98";
	   else if (s.indexOf("Windows 98") > -1)
			  return "Windows 98";
	   else if (s.indexOf("Windows NT 5.0") > -1)
			  return "Windows 2000";
	   else if (s.indexOf("Windows NT 5.2") > -1)
			  return "Windows 2003 Server";
	   else if (s.indexOf("Windows NT 5.1") > -1)
			  return "Windows XP";
	   else if (s.indexOf("Windows NT 6.0") > -1)
			  return "Windows Vista";
	   else if (s.indexOf("Linux") > -1)
			  return "Linux";
	   //else:
	   return "UNKNOWN";

	}
	
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
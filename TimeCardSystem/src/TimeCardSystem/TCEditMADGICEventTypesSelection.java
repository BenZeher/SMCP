package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablemadgiceventtypes;

public class TCEditMADGICEventTypesSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final String CREATE_NEW_TYPE_VALUE = "-1";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String sTitle = "Time Card System";
	    String sSubtitle = "Edit MADGIC Event Types";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

	    HttpSession CurrentSession = request.getSession();
    	
    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error [1486576245] editing MADGIC Event Type - " + sWarning + ".</FONT></B><BR>");
    	}
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=GREEN>" + sStatus + "</FONT></B><BR>");
    	}
    	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCEditMADGICEventTypesEdit\" METHOD = \"POST\">");
    	
    	out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">");
    	
    	out.println("<B>Current MADGIC Event Type(s):</B><BR>");
    	out.println ("<SELECT NAME=\"" + TCSTablemadgiceventtypes.lid + "\">" );
    	out.println ("<OPTION VALUE=" + CREATE_NEW_TYPE_VALUE + ">----Create New Event Type---- ");
    	
    	String sSQL = "";
	    try {
	        sSQL = "SELECT * FROM " + TCSTablemadgiceventtypes.TableName
	        	+ " ORDER BY " + TCSTablemadgiceventtypes.sname
	        ;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)); 
        	
        	while (rs.next()){
        		out.println ("<OPTION VALUE=" + Long.toString(rs.getLong(TCSTablemadgiceventtypes.lid)) + ">" 
        			+ rs.getString (TCSTablemadgiceventtypes.sname) + " - " + rs.getString(TCSTablemadgiceventtypes.sdescription));
        	}
        	rs.close();
        	
	    } catch (SQLException ex) {
			out.println("</SELECT><BR><BR><B><FONT COLOR=RED>Error [1486593405] listing MADGIC Event Types with SQL: " + sSQL + " - " + ex.getMessage() + "</FONT></B><BR>");
		}
	    
        out.println ("</SELECT>");
        out.println ("<BR><BR>");
    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Edit----\">");
    	out.println ("</FORM>");
		out.println("</BODY></HTML>");
	}
}



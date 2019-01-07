package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablemadgicevents;

public class TCEditMADGICEventsSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static final String CREATE_NEW_TYPE_VALUE = "-1";
	public static final String BUTTON_EDIT_EVENT_NAME = "EDIT";
	public static final String BUTTON_EDIT_EVENT_LABEL = "Edit an existing event";
	public static final String BUTTON_ADD_NEW_NAME = "ADDNEW";
	public static final String BUTTON_ADD_NEW_LABEL = "Add a new event";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String sTitle = "Time Card System";
	    String sSubtitle = "Edit MADGIC Events";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
    	
    	out.println("<I>To modify an existing entry, enter the ID number and click '" + BUTTON_EDIT_EVENT_LABEL + "'.</I>");
    	out.println("<BR><I>To add a new entry, just click '" + BUTTON_ADD_NEW_LABEL + "'.</I><BR>");
    	
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error editing MADGIC Event - " + sWarning + ".</FONT></B><BR>");
    	}
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=GREEN>" + sStatus + "</FONT></B><BR>");
    	}
    	out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCEditMADGICEventsEdit\" METHOD = \"POST\">");
    	
    	out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">");
    	
    	out.println("<BR>Enter the MADGIC Event ID:&nbsp;"
    		+ "<INPUT TYPE=TEXT NAME=\"" + TCSTablemadgicevents.lid + "\""
    		+ " VALUE=\"\""
    		//+ " SIZE=8"
    		+ " MAXLENGTH=" + "9"
    		+ " STYLE=\"width: 1in; height: 0.22in\""
    		+ ">"
    	);
    	
    	out.println ("<BR><BR><INPUT TYPE=\"SUBMIT\" NAME=\"" + BUTTON_EDIT_EVENT_NAME + "\" VALUE=\"" + BUTTON_EDIT_EVENT_LABEL + "\">"
    		+ "&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" + BUTTON_ADD_NEW_NAME + "\" VALUE=\"" + BUTTON_ADD_NEW_LABEL + "\">"
    	);
    	out.println ("</FORM>");
		out.println("</BODY></HTML>");
	}
}



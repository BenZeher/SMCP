package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;

public class SMLoadWageScaleDataSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "SMLoadWageScaleDataAction";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMLoadWageScaleData))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Import wage scale job data.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    /*
	    out.println("<B>NOTE:</B> The import file must have a separate count on each line,"
	    		+ " and each column must be separated by a comma."
	    		+ "  The first column must have the item number"
	    		+ " and the second column must have the quantity (zeroes allowed, but no blanks, and no commas)."
	    		+ "  The file can have more columns (if you want to include"
	    		+ " things like description, unit of measure, etc.), for your own purposes, but the"
	    		+ " import will ignore these:"
	    		+ " it only looks for the item number and the quantity, separated by a comma.<BR>"
	    );
	    */
	    out.println("<FORM NAME=\"MAINFORM\" action=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "\"" 
	    		+ " method=\"post\""
	    		+ " enctype=\"multipart/form-data\""
	    		+ ">");

	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    /*
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICPhysicalInventoryEntry.ParamID 
	    		+ "' VALUE='" 
	    		+ SMUtilities.get_Request_Parameter(ICPhysicalInventoryEntry.ParamID, request) 
	    		+ "'>"
	    );
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICPhysicalInventoryEntry.ParamStartingItemNumber 
	    		+ "' VALUE='" 
	    		+ SMUtilities.get_Request_Parameter(ICPhysicalInventoryEntry.ParamStartingItemNumber, request) 
	    		+ "'>"
	    );
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICPhysicalInventoryEntry.ParamEndingItemNumber 
	    		+ "' VALUE='" 
	    		+ SMUtilities.get_Request_Parameter(ICPhysicalInventoryEntry.ParamEndingItemNumber, request) 
	    		+ "'>"
	    );
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICPhysicalInventoryEntry.ParamLocation 
	    		+ "' VALUE='" 
	    		+ SMUtilities.get_Request_Parameter(ICPhysicalInventoryEntry.ParamLocation, request) 
	    		+ "'>"
	    );
*/
	    out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" 
	    	+ SMUtilities.getFullClassName(this.toString()) + "'>");
	    
		out.println("<TABLE BORDER=1>");

	    //Choose import file:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("Choose import file:");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println("<INPUT TYPE=FILE NAME='ImportFile'>");
	    out.println("</TD>");
	    //out.println("<TD>");
	    //out.println("&nbsp;");
	    //out.println("</TD>");
	    out.println("</TR>");
	    
	    //Checkbox to indicate if the file includes a header row:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("File to be imported includes a header row?");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println(
			"<INPUT TYPE=CHECKBOX NAME=" + "\"INCLUDESHEADERROW\" width=0.25>" 
			+ ""
		);
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    //Enter encryption key:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("Enter unique encryption key:");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println(
			"<INPUT TYPE=TEXT NAME=" + "\"" + SMWageScaleDataEntry.ParamEncryptionKey +"\" VALUE=\"\" MAXLENGTH=\"20\">" 
		);
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    /*
	    //Description:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("Count description:");
	    out.println("</TD>");
	    out.println("<TD>");
	    
	    out.println("<INPUT TYPE=TEXT NAME=\"" + ICPhysicalCountEntry.ParamDesc + "\""
	    		+ " VALUE=\"" 
	    			+ SMUtilities.get_Request_Parameter(ICPhysicalCountEntry.ParamDesc, request)+ "\""
	    		+ " MAXLENGTH='" + Integer.toString(SMTableicphysicalcounts.sdescLength) + "'"
	    		+ " SIZE='50'"
	    		+ ">"
	    );
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println("&nbsp;");
	    out.println("</TD>");
	    out.println("</TR>");
*/
	    out.println("</TABLE>");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitFile' VALUE='Import file' STYLE='width: 2.00in; height: 0.24in'>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
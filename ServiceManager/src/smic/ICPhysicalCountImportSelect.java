package smic;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicphysicalcounts;
import ServletUtilities.clsManageRequestParameters;

public class ICPhysicalCountImportSelect extends HttpServlet {
	
	public static final String PARAM_INCLUDES_HEADER_ROW = "INCUDESHEADERROW";
	public static final String PARAM_INCLUDE_NEW_ITEMS = "INCUDENEWITEMS";
	public static final String PARAM_SUBMIT_BUTTON_NAME = "SubmitFile";
	public static final String PARAM_SUBMIT_BUTTON_LABEL = "Import File";
	public static final String IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT = "ICPHYSICALIMPORTWARNING";
	
	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "ICPhysicalCountImportAction";

	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditPhysicalInventory))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Import physical inventory counts.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
		//If there is a warning from trying to input previously, print it here:
	    String sWarning = (String)CurrentSession.getAttribute(IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT);
	    CurrentSession.removeAttribute(IC_PHYSICAL_IMPORT_SESSION_WARNING_OBJECT);
		if (sWarning != null){
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    
	    //Add a link to physical inventory list:
	    out.println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICListPhysicalInventories?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ sDBID + "\">Return to physical inventory list</A><BR>");
	    
	    //Add a link to parent physical inventory:
	    out.println(
		    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory"
		    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" 
		    		+ clsManageRequestParameters.get_Request_Parameter(ICPhysicalInventoryEntry.ParamID, request)
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">Return to physical inventory</A><BR>");
	    
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPhysicalInventory) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("<B>NOTE:</B> The import file must have a separate count on each line,"
	    		+ " and each column must be separated by a comma."
	    		+ "  The first column must have the item number"
	    		+ " and the second column must have the quantity (zeroes allowed, but no blanks, and no commas)."
	    		+ "  The file can have more columns (if you want to include"
	    		+ " things like description, unit of measure, etc.), for your own purposes, but the"
	    		+ " import will ignore these:"
	    		+ " it only looks for the item number and the quantity, separated by a comma.<BR>"
	    );
	    
	    out.println("<FORM NAME=\"MAINFORM\" action=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCalledClassName + "\"" 
	    		+ " method=\"post\""
	    		+ " enctype=\"multipart/form-data\""
	    		+ ">");

	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICPhysicalInventoryEntry.ParamID 
	    		+ "' VALUE='" 
	    		+ clsManageRequestParameters.get_Request_Parameter(ICPhysicalInventoryEntry.ParamID, request) 
	    		+ "'>"
	    );

	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICPhysicalInventoryEntry.ParamLocation 
	    		+ "' VALUE='" 
	    		+ clsManageRequestParameters.get_Request_Parameter(ICPhysicalInventoryEntry.ParamLocation, request) 
	    		+ "'>"
	    );

	    out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" 
	    	+ SMUtilities.getFullClassName(this.toString()) + "'>");
	    
		out.println("<TABLE BORDER=1>" + "\n");

	    //Choose import file:
	    out.println("  <TR>" + "\n");
	    out.println("    <TD ALIGN=RIGHT>");
	    out.println("Choose import file:");
	    out.println("</TD>" + "\n");
	    out.println("    <TD>");
	    out.println("<INPUT TYPE=FILE NAME='ImportFile'>");
	    out.println("</TD>" + "\n");
	    out.println("    <TD>");
	    out.println("&nbsp;");
	    out.println("</TD>" + "\n");
	    out.println("  </TR>" + "\n");
	    
	    //Checkbox to indicate if the file includes a header row:
	    out.println("  <TR>" + "\n");
	    out.println("    <TD ALIGN=RIGHT>" + "\n");
	    out.println("File to be imported includes a header row?");
	    out.println("</TD>" + "\n");
	    out.println("    <TD>" + "\n");
	    out.println(
			"<INPUT TYPE=CHECKBOX NAME=" + "\"" + PARAM_INCLUDES_HEADER_ROW + "\" " + "CHECKED" + " width=0.25>" 
			+ ""
		);
	    out.println("    </TD>" + "\n");
	    out.println("    <TD>&nbsp;</TD>" + "\n");
	    out.println("  </TR>" + "\n");
	    
	    //Checkbox to indicate if user wants items ADDED to the physical inventory:
	    out.println("  <TR>" + "\n");
	    out.println("    <TD ALIGN=RIGHT>");
	    out.println("Add items to this physical inventory?");
	    out.println("</TD>" + "\n");
	    out.println("    <TD>");
	    out.println(
			"<INPUT TYPE=CHECKBOX NAME=" + "\"" + PARAM_INCLUDE_NEW_ITEMS + "\" " + "" + " width=0.25>" 
			+ ""
		);
	    out.println("</TD>" + "\n");
	    out.println("    <TD>"
	    	+ "<I>Check this if the count you are importing has items which are NOT YET in the physical inventory worksheet,"
	    	+ " but you want to have them added.  Quantities on hand for these items will be AS OF this import.</I>"
	    	+ "</TD>" + "\n");
	    out.println("  </TR>" + "\n");
	    
	    //Description:
	    out.println("  <TR>" + "\n");
	    out.println("    <TD ALIGN=RIGHT>");
	    out.println("Count description:<FONT COLOR=RED><B>*Required*</B></FONT>");
	    out.println("</TD>" + "\n");
	    out.println("  <TD>");
	    
	    out.println("<INPUT TYPE=TEXT NAME=\"" + ICPhysicalCountEntry.ParamDesc + "\""
	    		+ " VALUE=\"" 
	    			+ clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamDesc, request)+ "\""
	    		+ " MAXLENGTH='" + Integer.toString(SMTableicphysicalcounts.sdescLength) + "'"
	    		+ " SIZE='50'"
	    		+ ">"
	    );
	    out.println("</TD>" + "\n");
	    out.println("    <TD>");
	    out.println("&nbsp;");
	    out.println("</TD>" + "\n");
	    out.println("  </TR>" + "\n");

	    out.println("</TABLE>" + "\n" + "\n");
	    out.println("<INPUT TYPE=SUBMIT NAME='" + PARAM_SUBMIT_BUTTON_NAME + "'"
	    	+ " VALUE='" + PARAM_SUBMIT_BUTTON_LABEL + "'"
	    	+ " STYLE='width: 2.00in; height: 0.24in'"
	    	+ ">" + "\n");
		out.println("</FORM>" + "\n");
		out.println("</BODY></HTML>" + "\n");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
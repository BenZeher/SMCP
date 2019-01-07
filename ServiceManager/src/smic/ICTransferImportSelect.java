package smic;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICTransferImportSelect extends HttpServlet {
	public static final String PARAM_INCLUDES_HEADER_ROW = "IncludesHeaderRow";
	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "ICTransferImportAction";
	private static String sCompanyName = "";
	private String sDBID = "";
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sBatchType;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    get_request_parameters(request);
	    String title = "Import inventory transfers.";
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
	    
	    //Print a link to the inventory menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");

	    //Print a link to return to the 'edit entry' page:
	    //Print a link to return to the 'edit entry' page:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditTransferEntry" 
				+ "?BatchNumber=" + m_sBatchNumber
				+ "&EntryNumber=" + m_sEntryNumber
				+ "&BatchType=" + m_sBatchType
				+ "&Editable=Yes"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning="
	    		+ "\">Return to Edit Entry " + m_sEntryNumber + "</A><BR><BR>");
	    
	    out.println("<B>NOTE:</B> The import file must have a separate count on each line,"
	    		+ " and each column must be separated by a comma."
	    		+ " The first column must have the quantity (zeroes allowed, but no blanks, and no commas),"
	    		+ "  the second column must have the item number"
	    		+ ", the third column must have the FROM location code,"
	    		+ ", and the fourth column must have the TO location code."
	    		+ "  The file can have more columns (if you want to include"
	    		+ " things like description, unit of measure, etc.), for your own purposes, but the"
	    		+ " import will ignore these:"
	    		+ " it only looks for the item number, quantity, FROM location, and TO location - separated by a comma.<BR>"
	    );
	    
	    out.println("<FORM NAME=\"MAINFORM\" action=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCalledClassName + "\"" 
	    		+ " method=\"post\""
	    		+ " enctype=\"multipart/form-data\""
	    		+ ">");

	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICEntry.ParamBatchNumber
	    		+ "' VALUE='" 
	    		+ m_sBatchNumber
	    		+ "'>"
	    );
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICEntry.ParamEntryNumber
	    		+ "' VALUE='" 
	    		+ m_sEntryNumber
	    		+ "'>"
	    );
	    out.println("<INPUT TYPE=HIDDEN NAME='" 
	    		+ ICEntry.ParamBatchType
	    		+ "' VALUE='" 
	    		+ m_sBatchType
	    		+ "'>"
	    );

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
	    out.println("</TR>");
	    
	    //Checkbox to indicate if the file includes a header row:
	    out.println("<TR>");
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("File to be imported includes a header row?");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println(
			"<INPUT TYPE=CHECKBOX NAME=" + PARAM_INCLUDES_HEADER_ROW + " CHECKED" + " width=0.25>" 
			+ ""
		);
	    out.println("</TD>");
	    out.println("</TR>");

	    out.println("</TABLE>");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitFile' VALUE='Import file' STYLE='width: 2.00in; height: 0.24in'>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	private void get_request_parameters(HttpServletRequest req){
		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter(ICEntry.ParamBatchNumber, req);
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter(ICEntry.ParamEntryNumber, req);
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter(ICEntry.ParamBatchType, req);
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
}
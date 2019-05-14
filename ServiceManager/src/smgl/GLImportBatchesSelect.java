package smgl;

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
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionbatchlines;
import ServletUtilities.clsManageRequestParameters;

public class GLImportBatchesSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "GL Batch";
	public static final String PARAM_INCLUDES_HEADER_ROW = "INCLUDESHEADERROW";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLImportBatches))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Import " + sObjectName + "es";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLImportBatches) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("<B>NOTE:</B> The import file must be 'comma-delimited, have a separate record on each line,"
	    		+ " and each column must be separated by a comma.  Numbers (entry number, fiscal year, fiscal period, line number, debit amount, and credit amount) "
	    		+ " should not be enclosed in double quotes, but all other fields, including dates, should.  The batch description and entry fields will repeat on "
	    		+ "each line if the batch has multiple entries, or the entries have multiple lines.  "
	    		+ "Each line must have these fields, in the following order:<BR><BR>\n\n"
	    );

	    out.println(
	    	"<B>Batch date</B> (in mm/dd/yyyy format)<BR>\n"
	    	+ "<B>Batch description</B> (up to " + Integer.toString(SMTablegltransactionbatches.sBatchDescriptionLength) + " characters)<BR>\n"
	    	+ "<B>Entry number</B><BR>\n"
	    	+ "<B>Entry description</B> (up to " + Integer.toString(SMTablegltransactionbatchentries.sentrydescriptionLength) + " characters)<BR>\n"
	    	+ "<B>Entry date</B> (in mm/dd/yyyy format)<BR>\n"
	    	+ "<B>Entry document date</B> (in mm/dd/yyyy format)<BR>\n"
	    	+ "<B>Entry fiscal year (e.g. 2019)</B><BR>\n"
	    	+ "<B>Entry fiscal period</B> (e.g. 7)<BR>\n"
	    	+ "<B>Entry source ledger</B> ('AP', 'AR', 'FA', 'IC', 'JE', or 'PR')<BR>\n"
	    	+ "<B>Entry auto-reverse?</B> (either 'Y' or 'N')<BR>\n"
	    	+ "<B>Line number</B><BR>\n"
	    	+ "<B>Line description</B> (up to " + Integer.toString(SMTablegltransactionbatchlines.sdescriptionLength) + " characters)<BR>\n"
	    	+ "<B>Line reference</B> (up to " + Integer.toString(SMTablegltransactionbatchlines.sreferenceLength) + " characters)<BR>\n"
	    	+ "<B>Line comment</B> (up to " + Integer.toString(SMTablegltransactionbatchlines.scommentLength) + " characters)<BR>\n"
	    	+ "<B>Line transaction date</B> (in mm/dd/yyyy format)<BR>\n"
	    	+ "<B>Line GL account</B> (UNformatted, up to " + Integer.toString(SMTablegltransactionbatchlines.sacctidLength) + " characters)<BR>\n"
	    	+ "<B>Line debit amount</B> (in 000000.00 format, 0.00 if line is a credit)<BR>\n"
	    	+ "<B>Line credit amount</B> (in 000000.00 format, 0.00 if line is a debit)<BR>\n"
	    	+ "<B>Line source type</B> (must be one of the designated types (see below), e.g. 'IN' or 'CR')<BR>\n"
	    	+ "<BR>"
	    );
	    
	    out.println("<FORM NAME=\"MAINFORM\" action=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    	+ "smgl." + "GLImportBatchesAction" + "\"" 
	    		+ " method=\"post\""
	    		+ " enctype=\"multipart/form-data\""
	    		+ ">");

	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
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
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableapoptions;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APVendorNumberChangeSelect  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sCompanyName = "";
	
	public static final String PROCESS_TYPE = "CHANGEMERGEFUNCTIONTYPE";
	public static final String VENDOR_CHANGE = "VENDORCHANGE";
	public static final String VENDOR_MERGE = "VENDORMERGE";
	public static final String VENDOR_CHANGE_LABEL = "Change to";
	public static final String VENDOR_MERGE_LABEL = "Merge to";
	public static final String FROM_VENDOR = "FROMVENDOR";
	public static final String TO_VENDOR = "TOVENDOR";
	public static final String PROCESS_BUTTON_LABEL = "----Process change/merge----";
	public static final String CONFIRM_CHECKBOX_NAME = "CONFIRMCHECKBOXNAME";
	
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APChangeOrMergeVendorAccounts))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Change/Merge Vendor Account";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B><FONT>NOTE: " + sStatus + "</FONT></B><BR>");
		}
		
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APChangeOrMergeVendorAccounts) 
	    		+ "\">Summary</A><BR>");
	    
	    
	    //If the USES SMCP AP flag is NOT set, don't allow vendor changes or merges because we don't
	    //want to write to any data in SM:
	    APOptions apopt = new APOptions();
	    try {
			apopt.load(sDBID, getServletContext(), sUserID);
		} catch (Exception e) {
			out.println("Error reading the 'flag invoice' flag - " + e.getMessage() + ".  Customers cannot "
					+ "be processed at this time.</BODY></HTML>");
			return;
		}
	    
	    if (apopt.getUsesSMCPAP().compareToIgnoreCase("1") != 0){
	    	//Double check that the 'testing' flag isn't on:
	    	boolean bTestingFlagIsOn = false;
	    	
	    	String SQL = "SELECT"
	    		+ " " + SMTableapoptions.icreatetestbatchesfrompoinvoices
	    		+ " FROM " + SMTableapoptions.TableName
	    	;
	    	try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
				if (rs.next()){
					if (rs.getInt(SMTableapoptions.icreatetestbatchesfrompoinvoices) == 1){
						bTestingFlagIsOn = true;
					}
				}
				rs.close();
			} catch (SQLException e) {
	    		out.println("Error [1507298687] checking for AP 'testing' flag - " + e.getMessage()
					+ "</BODY></HTML>");
			}

	    	if (!bTestingFlagIsOn){
	    		out.println("Message [1507298686] - The system is not set to use SMCP AP, so you cannot use this function."
					+ "</BODY></HTML>");
	    		return;
	    	}
			
	    }

	    out.println("<BR>If you selected CHANGE TO, then all of the vendor's information will be" 
	    		+ " marked with the NEW vendor account.  This will include the following:<BR><BR>\n\n"
	    		
				+ " ** - The vendor master will have the vendor account number changed.<BR>\n"
	    		+ " ** - All current and historical AP batch entries will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical AP checks will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical AP 'matching lines' (apply-to-details) will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical AP transactions will have the vendor account changed.<BR>\n"
	    		+ " ** - All vendor remit-to locations will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical vendor statistics will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical Purchase Orders will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical Purchase Order Invoices will have the vendor account changed.<BR>\n"
	    		+ " ** - All Vendor Items (used in the inventory system to record vendor item numbers) will have the vendor account changed.<BR>\n"
	    		+ " ** - All current and historical Labor Backcharges will have the vendor account changed.<BR>\n"
	    			    		
	    		+ "<BR>\n"
	    		+ "If you selected MERGE INTO, then all of the MERGE FROM vendor's information "
	    		+ "will be changed"
	    		+ " to the MERGE INTO vendor account, and the MERGE FROM vendor master will be deleted.<BR>\n"
	    		+ "The changes effected by a MERGE are detailed below:<BR><BR>\n\n"
	    		
				+ " ** - The MERGE FROM vendor master will be deleted.<BR>\n"
	    		+ " ** - All current and historical AP batch entries will have the MERGE FROM vendor account and name changed to the MERGE TO vendor.<BR>\n"
	    		+ " ** - All current and historical AP checks will have the MERGE FROM vendor account and name changed to the MERGE TO vendor.<BR>\n"
	    		+ " ** - All current and historical AP 'matching lines' (apply-to-details) will have the MERGE FROM vendor account changed to the MERGE TO vendor.<BR>\n"
	    		+ " ** - All current and historical AP transactions will have the MERGE FROM vendor account changed to the MERGE TO vendor.<BR>\n"
	    		+ " ** - All the remit to locations of the MERGE FROM vendor will be added to the remit to locations of the MERGE TO vendor.<BR>\n"
	    		+ " ** - All the vendor statistics of the MERGE FROM vendor will be added to (or combined with) the statistics of the MERGE TO vendor.<BR>\n"
	    		+ " ** - All current and historical Purchase Orders for the MERGE FROM vendor will have the vendor account and name changed to the MERGE TO vendor.<BR>\n"
	    		+ " ** - All current and historical Purchase Order Invoices for the MERGE FROM vendor will have the vendor account and name changed to the MERGE TO vendor.<BR>\n"
	    		+ " ** - All Vendor Items (used in the inventory system to record vendor item numbers) for the MERGE FROM vendor will be added to the items in the MERGE TO vendor's list of items.<BR>\n"
	    		+ " ** - All current and historical Labor Backcharges for the MERGE FROM vendor will be changed to the MERGE TO vendor.<BR>\n"
	    		
	    		+ "<BR>\n"
	    		+ "If the MERGE INTO vendor is NOT already in AP, it will warn you and it won't make the change.<BR>\n"
	    		+ "If there are duplicate document numbers, remit to location codes, or vendor item numbers in the system for the CURRENT and the MERGE INTO customer, it will warn"
	    		+ " you and it won't make the change.<BR>\n"
	    		+ "<BR><BR>"
	    		);
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorNumberChangeAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE BORDER=1>");
		out.println("<TR><TD><B>Change type:</B></TD></TR>");
		
		
		out.println("<TR><TD><LABEL><INPUT TYPE=\"radio\" name=\"" + PROCESS_TYPE + "\" value=\"" + VENDOR_CHANGE + "\">" + VENDOR_CHANGE_LABEL + " <LABEL></TD></TR>");
		out.println("<TR><TD><LABEL><INPUT TYPE=\"radio\" name=\"" + PROCESS_TYPE + "\" value=\"" + VENDOR_MERGE + "\">" + VENDOR_MERGE_LABEL + " </LABEL></TD></TR>");
		
		out.println("</TABLE>");
		out.println("<B>Change (merge) FROM vendor account:</B>&nbsp;(Type the CURRENT vendor account here):");
		out.println("<INPUT TYPE=TEXT NAME=\"" + FROM_VENDOR + "\" SIZE=" 
				+ SMTableicvendors.svendoracctLength + "; MAXLENGTH=" + Integer.toString(SMTableicvendors.svendoracctLength) + " >");
		out.println ("<BR>");
		out.println("<B>Change (merge) TO vendor account:</B>&nbsp;(Type the NEW vendor account here):");
		out.println("<INPUT TYPE=TEXT NAME=\"" + TO_VENDOR + "\" SIZE=" 
				+ SMTableicvendors.svendoracctLength + "; MAXLENGTH=" + Integer.toString(SMTableicvendors.svendoracctLength) + " >");
		out.println ("<BR><BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"" + PROCESS_BUTTON_LABEL + "\">");
		out.println("  Check to confirm change: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CHECKBOX_NAME + "\">");
		out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

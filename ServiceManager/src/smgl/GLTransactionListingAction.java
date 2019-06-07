package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLTransactionListingAction extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	private long lStartingTime = 0;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.GLTransactionListing)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sEndingAccount = request.getParameter(GLTransactionListingSelect.PARAM_ENDING_ACCOUNT);
		String sStartingAccount = request.getParameter(GLTransactionListingSelect.PARAM_STARTING_ACCOUNT);
		String sEndingAccountGroup = request.getParameter(GLTransactionListingSelect.PARAM_ENDING_ACCOUNT_GROUP);
		String sStartingAccountGroup = request.getParameter(GLTransactionListingSelect.PARAM_STARTING_ACCOUNT_GROUP);
		String sStartingFiscalPeriod = request.getParameter(GLTransactionListingSelect.PARAM_STARTING_FISCAL_PERIOD_SELECTION);
		String sEndingFiscalPeriod = request.getParameter(GLTransactionListingSelect.PARAM_ENDING_FISCAL_PERIOD_SELECTION);
		boolean bIncludeAccountsWithNoActivity = request.getParameter(GLTransactionListingSelect.PARAM_PROCESS_FOR_NO_ACTIVITY) != null;

		//Get the starting and ending segment values:
		ArrayList<String>alStartingSegmentNames = new ArrayList<String>(0);
		ArrayList<String>alEndingSegmentNames = new ArrayList<String>(0);
		ArrayList<String>alStartingSegmentIDs = new ArrayList<String>(0);
		ArrayList<String>alEndingSegmentIDs = new ArrayList<String>(0);
		ArrayList<String>alStartingSegmentValueIDs = new ArrayList<String>(0);
		ArrayList<String>alEndingSegmentValueIDs = new ArrayList<String>(0);
		ArrayList<String>alStartingSegmentValueDescriptions = new ArrayList<String>(0);
		ArrayList<String>alEndingSegmentValueDescriptions = new ArrayList<String>(0);
		
    	Enumeration <String> eParams = request.getParameterNames();
    	String sParameter = "";
    	while (eParams.hasMoreElements()){
    		sParameter = eParams.nextElement();
    		if (sParameter.startsWith(GLTransactionListingSelect.PARAM_STARTING_SEGMENT_BASE)){
    			String sParamWithoutBase = sParameter.replace(GLTransactionListingSelect.PARAM_STARTING_SEGMENT_BASE, "");
    			//System.out.println("[1553288613] - sParamWithoutBase = '" + sParamWithoutBase + "'.");
    			String sStartingSegmentID = sParamWithoutBase.substring(0, sParamWithoutBase.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
    			//System.out.println("[1553288614] - sStartingSegmentID = '" + sStartingSegmentID + "'.");
    			String sStartingSegmentName = sParamWithoutBase.substring((sStartingSegmentID + GLTransactionListingSelect.PARAM_VALUE_DELIMITER).length());
    			//System.out.println("[1553288615] - sStartingSegmentName = '" + sStartingSegmentName + "'.");
    			String sStartingSegmentValueForSegment = request.getParameter(sParameter);
    			//System.out.println("[1553288616] - sStartingSegmentValueForSegment = '" + sStartingSegmentValueForSegment + "'.");
    			String sStartingSegmentValueIDAndDescription = sStartingSegmentValueForSegment.substring((sStartingSegmentID + GLTransactionListingSelect.PARAM_VALUE_DELIMITER).length());
    			//System.out.println("[1553288617] - sStartingSegmentValueIDAndDescription = '" + sStartingSegmentValueIDAndDescription + "'.");
    			String sStartingSegmentValueID = sStartingSegmentValueIDAndDescription.substring(0, sStartingSegmentValueIDAndDescription.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
    			//System.out.println("[1553288618] - sStartingSegmentValueID = '" + sStartingSegmentValueID + "'.");
    			String sStartingSegmentValueDescription = sStartingSegmentValueIDAndDescription.substring((sStartingSegmentValueID + GLTransactionListingSelect.PARAM_VALUE_DELIMITER).length());
    			//System.out.println("[1553288619] - sStartingSegmentValueDescription = '" + sStartingSegmentValueDescription + "'.");
    			String sEndingSegmentName = sStartingSegmentName;
    			//System.out.println("[1553288620] - sEndingSegmentName = '" + sEndingSegmentName + "'.");
    			String sEndingSegmentID = sStartingSegmentID;
    			//System.out.println("[1553288621] - sEndingSegmentID = '" + sEndingSegmentID + "'.");
    			String sEndingSegmentValueForSegment = request.getParameter(sParameter.replaceAll(GLTransactionListingSelect.PARAM_STARTING_SEGMENT_BASE, GLTransactionListingSelect.PARAM_ENDING_SEGMENT_BASE));
    			//System.out.println("[1553288622] - sEndingSegmentValueForSegment = '" + sEndingSegmentValueForSegment + "'.");
    			String sEndingSegmentValueIDAndDescription = sEndingSegmentValueForSegment.substring((sEndingSegmentID + GLTransactionListingSelect.PARAM_VALUE_DELIMITER).length());
    			//System.out.println("[1553288623] - sEndingSegmentValueIDAndDescription = '" + sEndingSegmentValueIDAndDescription + "'.");
    			String sEndingSegmentValueID = sEndingSegmentValueIDAndDescription.substring(0, sEndingSegmentValueIDAndDescription.indexOf(GLTransactionListingSelect.PARAM_VALUE_DELIMITER));
    			//System.out.println("[1553288624] - sEndingSegmentValueID = '" + sEndingSegmentValueID + "'.");
    			String sEndingSegmentValueDescription = sEndingSegmentValueIDAndDescription.substring((sEndingSegmentValueID + GLTransactionListingSelect.PARAM_VALUE_DELIMITER).length());
    			//System.out.println("[1553288625] - sEndingSegmentValueDescription = '" + sEndingSegmentValueDescription + "'.");
    			
    			//Load up the arrays:
    			alStartingSegmentNames.add(sStartingSegmentName);
    			alEndingSegmentNames.add(sEndingSegmentName);
    			alStartingSegmentIDs.add(sStartingSegmentID);
    			alEndingSegmentIDs.add(sEndingSegmentID);
    			alStartingSegmentValueIDs.add(sStartingSegmentValueID);
    			alEndingSegmentValueIDs.add(sEndingSegmentValueID);
    			alStartingSegmentValueDescriptions.add(sStartingSegmentValueDescription);
    			alEndingSegmentValueDescriptions.add(sEndingSegmentValueDescription);
    		}
    	}
		
		/*******************************************************/
		
		String sParamString = "";
		sParamString += "&CallingClass=" + sCallingClass;
		sParamString += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		
		//Customized title
		String sReportTitle = "GL Transaction Listing";
		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"#FFFFFF\">\n" 
			+ "    <TABLE BORDER=0 WIDTH=100%>\n" 
			+ "      <TR>\n"
			+ "        <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + "</FONT></TD>\n"
			+ "        <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
			+ "      <TR>\n"
			+ "        <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
		);
		
		out.println("  <TR>\n"
			+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to General Ledger Main Menu</A></TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>"
		);
		
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<BR>\n");
		out.println("<TABLE BORDER=0>\n");
		
		String s = "";
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "FROM period:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sStartingFiscalPeriod + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;

		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "TO period:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sEndingFiscalPeriod + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		String sNoActivity = "N";
		if (bIncludeAccountsWithNoActivity){
			sNoActivity = "Y";
		}
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Include accounts with no activity?:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sNoActivity + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;

		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Starting with account:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sStartingAccount + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;

		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Ending with account:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sEndingAccount + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Starting with account group:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sStartingAccountGroup + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Ending with account group:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sEndingAccountGroup + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		for (int i = 0; i < alStartingSegmentNames.size(); i++){
			s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ alStartingSegmentNames.get(i) + ":&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "FROM:&nbsp;"
				+ "<B>" + alStartingSegmentValueDescriptions.get(i) + "</B>"
				+ "&nbsp;TO:&nbsp;"
				+ "<B>" + alEndingSegmentValueDescriptions.get(i) + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		}

		out.println(s);
		out.println("</TABLE>\n");
		out.println("<BR>\n");
		
		//Retrieve information
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".doGet - UserID: " + sUserID
			);
		} catch (Exception e1) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + e1.getMessage()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString.replace("*", "&")
			);			
			return;
		}
		
		boolean bAllowBatchViewing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.GLEditBatches, sUserID, conn, sLicenseModuleLevel);
		
		lStartingTime = System.currentTimeMillis();
		GLTransactionListingReport rpt = new GLTransactionListingReport();
		try {
			out.println(
				rpt.processReport(
					conn,
					sDBID,
					getServletContext(),
					sStartingAccount,
					sEndingAccount,
					sStartingAccountGroup,
					sEndingAccountGroup,
					sStartingFiscalPeriod,
					sEndingFiscalPeriod,
					bIncludeAccountsWithNoActivity,
					alStartingSegmentIDs,
					alStartingSegmentValueDescriptions,
					alEndingSegmentIDs,
					alEndingSegmentValueDescriptions,
					bAllowBatchViewing
				)
			);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn,"[1553715863]");
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + e.getMessage()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1553715864]");
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID ,SMLogEntry.LOG_OPERATION_GLTRANSACTIONLISTING, "REPORT", "GL Transaction Listing", "[1557346149]");
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000L + " seconds.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
	}
}

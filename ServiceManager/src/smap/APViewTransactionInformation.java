package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicporeceiptheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smic.ICPOHeader;
import smic.ICPOReceiptHeader;

public class APViewTransactionInformation  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APViewTransactionInformation))
			{
				return;
			}
		
		String sTransactionID = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.lid, request);
		String sTransactionDocNumber = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.sdocnumber, request);
		String sVendorID = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.svendor, request);
		String sBatchNumber = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.loriginalbatchnumber, request);
		String sEntryNumber = clsManageRequestParameters.get_Request_Parameter(SMTableaptransactions.loriginalentrynumber, request);
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
    	//Customized title
    	String sReportTitle = "View AP Transaction Information";
    	String subtitle = "Viewing transaction ID " + sTransactionID;
    	if (sTransactionID.compareToIgnoreCase("") ==0){
    		subtitle = "Viewing transaction with document number '" + sTransactionDocNumber + "'";
    	}
    	out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A></FONT><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to Accounts Payable Main Menu</A><BR><BR>");
    	//log usage of this this report
	    //SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    //log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_DISPLAYJOBCOSTINFO, "REPORT", "SMDisplayJobCostInformation", "[1376509319]");
	    
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    
	    out.println(sCommandScript());
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(), 
    		sDBID, 
    		"MySQL", 
    		SMUtilities.getFullClassName(this.toString()) 
    		+ ".doPost - userID: " 
    		+ sUserID
    		+ " - "
    		+ sUserFirstName
    		+ " "
    		+ sUserLastName
    		+ "@" 
    		+ (new Timestamp(System.currentTimeMillis())).toString());
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		redirectAfterError(
				getServletContext(), 
				sCallingClass, 
				sDBID, 
				"Unable to get data connection.", 
				"", 
				response,
				out
    		);		
        	return;
    	}
	    
    	try {
			out.println(getTransactionInformation(
				conn, 
				sTransactionID, 
				sTransactionDocNumber,
				sBatchNumber,
				sEntryNumber,
				sVendorID, 
				CurrentSession, 
				sUserID, 
				sDBID, 
				sLicenseModuleLevel)
			);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059515]");
    		redirectAfterError(
    				getServletContext(), 
    				sCallingClass, 
    				sDBID, 
    				"Could not display transaction information - " + e.getMessage(), 
    				"", 
    				response,
    				out
    		);
			return;
		}
	    
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059516]");
	    return;
	    
	}
	
	private void redirectAfterError(
		ServletContext context, 
		String sCallingClass, 
		String sDBID, 
		String sWarning, 
		String sStatus, 
		HttpServletResponse response,
		PrintWriter pwOut
		){
		
		
		if (sCallingClass.compareToIgnoreCase("") != 0){
			String sRedirectString = 
				SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
			if (sWarning.compareToIgnoreCase("") != 0){
				sRedirectString += "&Warning=" + sWarning;
			}
			if (sStatus.compareToIgnoreCase("") != 0){
				sRedirectString += "&Status=" + sWarning;
			}
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				System.out.println("Error [1496184173] - unable to redirect - " + e.getMessage());
			}
		}else{
			pwOut.println("<BR><BR>");
			if (sWarning.compareToIgnoreCase("") != 0){
				pwOut.println("<B><FONT COLOR=RED>WARNING: " + sWarning + "</FONT></B><BR>");
			}
			if (sStatus.compareToIgnoreCase("") != 0){
				pwOut.println("<B>STATUS: " + sStatus + "</B><BR>");
			}
		}
	}
	private String getTransactionInformation(
		Connection conn, 
		String sTransactionID,
		String sTransactionDocNumber,
		String sBatchNumber,
		String sEntryNumber,
		String sVendorID,
		HttpSession session, 
		String sUserID, 
		String sDBID, 
		String sLicenseModuleLevel) throws Exception{

		String s = "";
		String sAcquiredTransactionID = "0";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">\n";
		
		String SQL = "SELECT " + SMTableaptransactions.TableName + ".*"
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid
			+ " FROM " + SMTableaptransactions.TableName
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON (" 
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber + "=" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber + "=" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")"
			+ ")"
			+ " WHERE ("
			;
		
			//If we've got a transaction ID, use that
			if (sTransactionID.compareToIgnoreCase("") != 0){
				SQL += "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + " = " + sTransactionID + ")";
				
			//If we DON'T have a transaction ID, then try using the (unique) doc number and vendor:
			}else{
				//If we don't have a vendor ID, 
				if (sVendorID.compareToIgnoreCase("") != 0)	{
					SQL += "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + " = '" + sTransactionDocNumber + "')"
						+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = '" + sVendorID + "')"
					;
				//But if we don't have a transaction ID or a vendor ID, we might try using the original batch number and entry number:
				}else{
					SQL += "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber + " = '" + sBatchNumber + "')"
						+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber + " = '" + sEntryNumber + "')"
						//We added this to make sure that we didn't pick up transactions that were migrated over from the old ACCPAC data.  If we are retrieving
						// transactions based on batch and entry number, they should be from the SMCP system, not leftover values from ACCPAC.
						//  Adding this clause insures that we'll only get AP transactions with real batchs in SMCP.
						+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lbatchentryid + " > 0)"
					;
				}
			}
			SQL += ")"
		;
		//System.out.println("[1498498485] - SQL = '" + SQL + "'");
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
		if (rs.next()){
			//Get the transaction ID, just in case it wasn't passed in:
			sAcquiredTransactionID = Long.toString(rs.getLong(SMTableaptransactions.lid));
			
			//Doc ID
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>Document ID:</TD>\n"
				+ "    <TD><B>" 
				+ Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid))
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			//Doc type:
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>Document type:</TD>\n"
				+ "    <TD><B>" 
				+ SMTableapbatchentries.getDocumentTypeLabel(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype))
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			//Vendor
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>Vendor:</TD>\n"
				+ "    <TD><B>" 
				+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor)
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			//Doc Number:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Document Number:</TD>\n"
					+ "    <TD><B>" 
					+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber)
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Check number:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Check Number:</TD>\n"
					+ "    <TD><B>" 
					+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber)
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//On hold?
			String sOnHold = "N";
			if (rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold) == 1){
				sOnHold = "Y by User ID: " 
					+ Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lonholdbyuserid)) 
					+ " - " + rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sonholdbyfullname)
				;
				if (rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lonholdpoheaderid) != 0){
					String sOnHoldPOHeaderID = Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lonholdpoheaderid));
					sOnHold += " on PO #";
					
					boolean bAllowPOViewing = SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.ICEditPurchaseOrders, 
							sUserID, 
							getServletContext(), 
							sDBID, 
							sLicenseModuleLevel
						);
					if(bAllowPOViewing){
						sOnHold += " on "
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
							+ "?" + ICPOHeader.Paramlid + "=" + sOnHoldPOHeaderID
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">PO #" + sOnHoldPOHeaderID + "</A>"
						;			
					}else{
						sOnHold += " on PO #" + rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lonholdpoheaderid);
					}
				}
			}
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>On Hold?:</TD>\n"
				+ "    <TD><B>" 
				+ sOnHold
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>Placed on hold:</TD>\n"
				+ "    <TD><B>" 
				+ ServletUtilities.clsDateAndTimeConversions.resultsetDateTimeToTheSecondStringToString(
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datplacedonhold) 
					)
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>On Hold Reason:</TD>\n"
				+ "    <TD><B>" 
				+ "<I>" + rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.monholdreason) + "</I>"
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			//Doc Date:
			String sDocDate = SMUtilities.EMPTY_DATE_VALUE;
			try {
				sDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e1) {
				//Don't need to do anything here
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Document Date:</TD>\n"
					+ "    <TD><B>" 
					+ sDocDate
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;

			String sDueDate = SMUtilities.EMPTY_DATE_VALUE;
			try {
				sDueDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e1) {
				//Don't need to do anything here
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Due Date:</TD>\n"
					+ "    <TD><B>" 
					+ sDueDate
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Is Tax included?
			if (rs.getLong(SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE){
				String sTaxIncluded = "NO";
		   		if (rs.getLong(SMTableaptransactions.iinvoiceincludestax) == 1){
		   			sTaxIncluded = "YES";
	    		}
				s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Tax included in invoice?:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
					+ sTaxIncluded
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			}
			
    		//Tax:
    		String sTax = "(N/A)";
    		if (rs.getString(SMTableaptransactions.staxjurisdiction).compareToIgnoreCase("") != 0){
    			sTax = rs.getString(SMTableaptransactions.staxjurisdiction) + " - " + rs.getString(SMTableaptransactions.staxtype);
    		}
			s += "  <TR>\n"
				+ "    <TD ALIGN=RIGHT>Tax:</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
				+ sTax
				+ "</B></TD>\n"
				+ "    <TD>&nbsp;</TD>\n"
				+ "  </TR>\n"
			;
			
			//Original amt:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Original amt:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;

			//Current amt:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Current amt:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Original discount amt:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Original discount amt:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginaldiscountavailable))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Discount amt available:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Discount amt remaining:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + "\" ><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentdiscountavailable))
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			String sDiscountDate = SMUtilities.EMPTY_DATE_VALUE;
			try {
				sDiscountDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
			} catch (Exception e1) {
				//Don't need to do anything here
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Discount Date:</TD>\n"
					+ "    <TD><B>" 
					+ sDiscountDate
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//New table here:
			try {
				s += "  <TR>"
					+ "    <TD COLSPAN=3 style = \" font-weight:bold; font-style:italic; font-size:small; \">Applied Documents:</TD>"
					+ "  </TR>"
					
					+ "  <TR>\n"
					+ "    <TD COLSPAN=3>\n"
					+ getAppliedDocsTable(
						Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid)),
						conn,
						sUserID, 
						sLicenseModuleLevel, 
						sDBID
						)
					+ "    </TD>"
					+ "  </TR>"
				;
			} catch (Exception e) {
				s += "  <TR>\n"
					+ "    <TD COLSPAN=3 style = \" color:red; font-weight:bold' \" >"
					+ e.getMessage()
					+ "</TD>\n"
					+ "  </TR>\n"
				;
			} 
			
			//Control Acct:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Control account:</TD>\n"
					+ "    <TD><B>" 
					+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.scontrolacct)
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Description:
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Description:</TD>\n"
					+ "    <TD><B>" 
					+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocdescription)
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Apply to invoice:
			String sApplyToInvoiceNumber = rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sapplytoinvoicenumber);
			if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APViewTransactionInformation, 
						sUserID, 
						conn, 
						(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				&& (sApplyToInvoiceNumber.compareToIgnoreCase("") != 0)
			){
				sApplyToInvoiceNumber =
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APViewTransactionInformation?"
						+ SMTableaptransactions.sdocnumber + "=" + rs.getString(SMTableaptransactions.sapplytoinvoicenumber)
						+ "&" + SMTableaptransactions.svendor + "=" + rs.getString(SMTableaptransactions.svendor)
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			     		+ "\">" 
			     		+ sApplyToInvoiceNumber
			     		+ "</A>"
			     ;
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Apply-to invoice:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
					+ sApplyToInvoiceNumber
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;

			//Apply to order:
			String sApplyToOrderNumber = Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytosalesorderid));
			if (sApplyToOrderNumber.compareToIgnoreCase("0") == 0){
				sApplyToOrderNumber = "";
			}
			if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewOrderInformation, 
						sUserID, 
						conn, 
						(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				&& (sApplyToOrderNumber.compareToIgnoreCase("") != 0)
			){
				sApplyToOrderNumber = 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
						+ sApplyToOrderNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sApplyToOrderNumber + "</A>"
				;
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Apply-to order:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
					+ sApplyToOrderNumber
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Apply to PO:
			String sApplyToPurchaseOrder = Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid));
			if (sApplyToPurchaseOrder.compareToIgnoreCase("0") == 0){
				sApplyToPurchaseOrder = "";
			}
			if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.ICEditPurchaseOrders, 
						sUserID, 
						conn, 
						(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				&& (sApplyToPurchaseOrder.compareToIgnoreCase("") != 0)
					
			){
				sApplyToPurchaseOrder = 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
						+ "?" + ICPOHeader.Paramlid + "=" + sApplyToPurchaseOrder
						+ "&CallingClass=" + "smic.ICEditPOSelection"
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			    		+ "\">" + sApplyToPurchaseOrder + "</A>"
				;
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Apply-to purchase order:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
					+ sApplyToPurchaseOrder
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Original batch number:
			//Get the batch type:
			String sBatchType = Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE);
			if (
				(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO)
				|| (rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT)
				|| (rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT)
				|| (rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT)
			){
				sBatchType = Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT);
			}
			
			String sOriginalBatchNumber = Long.toString(rs.getLong(SMTableaptransactions.loriginalbatchnumber));
			if (sOriginalBatchNumber.compareToIgnoreCase("0") == 0){
				sOriginalBatchNumber = "";
			}
			if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APEditBatches, 
						sUserID, 
						conn, 
						(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				&& (sOriginalBatchNumber.compareToIgnoreCase("") != 0)
					
			){
				sOriginalBatchNumber =
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit?"
			    		+ SMTableapbatches.lbatchnumber + "=" + sOriginalBatchNumber
			    		+ "&" + SMTableapbatches.ibatchtype + "=" + sBatchType
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\">" + sOriginalBatchNumber + "</A>"
				;
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Original batch number:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
					+ sOriginalBatchNumber
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
			//Original batch entry number:
			
			String sOriginalEntryNumber = Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber));
			if (sOriginalEntryNumber.compareToIgnoreCase("0") == 0){
				sOriginalEntryNumber = "";
			}
			if (
				(SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APEditBatches, 
						sUserID, 
						conn, 
						(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)))
				&& (sOriginalEntryNumber.compareToIgnoreCase("") != 0)
			){
	       		if (sBatchType.compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)) == 0){
       				sOriginalEntryNumber = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditInvoiceEdit" 
	    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber))
	    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber))
	    	    		+ "&" + SMTableapbatchentries.lid + "=" + Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid))
	    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype))
	    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ sOriginalEntryNumber
	    	    		+ "</A>"
	    	    		+ "\n"
    	    	    ;
	    		}
	       		
	       		if (sBatchType.compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
       				sOriginalEntryNumber = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditPaymentEdit" 
	    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber))
	    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber))
	    	    		+ "&" + SMTableapbatchentries.lid + "=" + Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid))
	    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype))
	    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ sOriginalEntryNumber 
	    	    		+ "</A>"
	    	    		+ "\n"
	    	    	;
	    		}
			}
			s += "  <TR>\n"
					+ "    <TD ALIGN=RIGHT>Original entry number:</TD>\n"
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + "\" ><B>" 
					+ sOriginalEntryNumber
					+ "</B></TD>\n"
					+ "    <TD>&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			
		}else{
			rs.close();
			throw new Exception("No record found.");
		}

		s += "</TABLE>";
		
		try {
			s += getTransactionLines(sAcquiredTransactionID, conn, sUserID, sLicenseModuleLevel, sDBID);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return s;
	}
	private String getTransactionLines(String sTransactionID, Connection conn, String sUserID, String sLicenseModuleLevel, String sDBID) throws Exception{
		String s = "";
		
		s += "<BR>";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + " \" >\n";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Original&nbsp;batch<BR>line&nbsp;no."
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Dist&nbsp;code"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Dist&nbsp;acct"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Description"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Comment"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Amount"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ "Disc&nbsp;Amt&nbsp;Applied"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "PO&nbsp;no."
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Receipt&nbsp;no."
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Receipt&nbsp;line"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+ "Apply-to&nbsp;document"
			+ "</TD>\n"

			+ "  </TR>\n"
		;
		
		boolean bViewPOs = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEditPurchaseOrders, 
				sUserID, 
				conn, 
				sLicenseModuleLevel);
		boolean bViewPOReceipts = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEditReceipts, 
				sUserID, 
				conn, 
				sLicenseModuleLevel);
		boolean bViewAPTransactions = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.APViewTransactionInformation, 
				sUserID, 
				conn, 
				sLicenseModuleLevel);
		
		String SQL = "SELECT * FROM " + SMTableaptransactionlines.TableName
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName
			+ " ON " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lreceiptheaderid + "=" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " LEFT JOIN " + SMTableglaccounts.TableName
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID + " = " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sdistributionacct
			+ " WHERE ("
				+ " (" + SMTableaptransactionlines.ltransactionheaderid + " = " + sTransactionID + ")"
			+ ") ORDER BY " + SMTableaptransactionlines.loriginallinenumber
		;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			boolean bOddRow = true;
			while (rs.next()){
				String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
				if (bOddRow){
					sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
				}
				
				String sGLAcctDesc = rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc);
				if (sGLAcctDesc == null){
					sGLAcctDesc = "(Not Found)";
				}
				//s += "  <TR class = \"" + sClass + "\" >\n"
				s += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
						+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
						+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
						+ ">\n"
						
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ Long.toString(rs.getLong(SMTableaptransactionlines.loriginallinenumber))
					+ "    </TD>\n"
						
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ rs.getString(SMTableaptransactionlines.sdistributioncodename)
					+ "    </TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ rs.getString(SMTableaptransactionlines.sdistributionacct) + " - " + sGLAcctDesc
					+ "    </TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ rs.getString(SMTableaptransactionlines.sdescription)
					+ "    </TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ rs.getString(SMTableaptransactionlines.scomment)
					+ "    </TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactionlines.bdamount))
					+ "    </TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableaptransactionlines.bddiscountappliedamount))
					+ "    </TD>\n"
				;
				
				String sPONumber = Long.toString(rs.getLong(SMTableaptransactionlines.lpoheaderid));
				if (sPONumber.compareToIgnoreCase("0") == 0){
					sPONumber = "";
				}else if (bViewPOs){
					sPONumber = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
							+ "?" + ICPOHeader.Paramlid + "=" + sPONumber
							+ "&CallingClass=" + "smic.ICEditPOSelection"
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    		+ "\">" + sPONumber + "</A>"
				    	;
				}
					
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ sPONumber
					+ "    </TD>\n"
				;
				
				String sPOReceiptNumber = Long.toString(rs.getLong(SMTableaptransactionlines.lreceiptheaderid));
				if (sPOReceiptNumber.compareToIgnoreCase("0") == 0){
					sPOReceiptNumber = "";
				}else if (bViewPOReceipts){
					sPOReceiptNumber = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptEdit"
							+ "?" + ICPOReceiptHeader.Paramlpoheaderid + "=" + Long.toString(rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid))
							+ "&" + ICPOReceiptHeader.Paramlid + "=" + sPOReceiptNumber
							+ "&CallingClass=" + "smic.ICEditPOSelection"
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    		+ "\">" + sPOReceiptNumber + "</A>"
				    	;
				}
					
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ sPOReceiptNumber
					+ "    </TD>\n"
				;
				
				String sReceiptLineNumber = Long.toString(rs.getLong(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lporeceiptlineid));
				if (sReceiptLineNumber.compareToIgnoreCase("0") == 0){
					sReceiptLineNumber = "";
				}
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ sReceiptLineNumber
					+ "    </TD>\n"
				;
				
				String sApplyToDocument = rs.getString(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sapplytodocnumber);
				String sTransactionLines = Long.toString(rs.getLong(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lapplytodocid));
				if (
						(
								(sApplyToDocument.compareToIgnoreCase("") != 0) 
								|| (sTransactionLines.compareToIgnoreCase("") != 0)
								|| (sTransactionLines.compareToIgnoreCase("0") != 0)
								)
						&& (bViewAPTransactions)

						){
					sApplyToDocument = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APViewTransactionInformation?"
						+ SMTableaptransactions.lid + "=" + sTransactionLines + "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    	+ "\">" 
				    	+ sApplyToDocument
				    	+ "</A>"
				    ;
				}
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >\n"
					+ sApplyToDocument
					+ "    </TD>\n"
				;
				
				s += "  </TR>\n";
				
				bOddRow = !bOddRow;
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1496177692] reading transaction lines with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		s += "</TABLE>\n";
		
		return s;
	}
	private String getAppliedDocsTable(
		String sTransactionID, 
		Connection conn, 
		String sUser, 
		String sLicenseModuleLevel, 
		String sDBID) throws Exception{
		
		String s = "";
		
		s += 
			"      <TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n"
			+ "        <TR>\n"
			+ "          <TD style = \" font-weight:bold; background-color:black; color:white; font-size:small; \" >Doc. date</TD>\n"
			+ "          <TD style = \" font-weight:bold; background-color:black; color:white; font-size:small; \" >Type</TD>\n"
			+ "          <TD style = \" font-weight:bold; background-color:black; color:white; font-size:small; \" >Check #</TD>\n"
			+ "          <TD style = \" font-weight:bold; background-color:black; color:white; font-size:small; \" >Doc #</TD>\n"
			+ "          <TD style = \" font-weight:bold; background-color:black; color:white; font-size:small; \" >Applied amt.</TD>\n"
			+ "          <TD style = \" font-weight:bold; background-color:black; color:white; font-size:small; \" >Applied disc.</TD>\n"
			+ "        </TR>\n"
		;
		
		//Get any applying document info here:
		String SQL = "SELECT"
			+ " " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber
			+ ", " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bdappliedamount
			+ ", " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bddiscountappliedamount
			+ ", " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid
			+ " FROM " + SMTableapmatchinglines.TableName
			+ " LEFT JOIN " + SMTableaptransactions.TableName + " ON "
			+ SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid
			+ " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
			+ " WHERE ("
				+ "(" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedtoid + " = " + sTransactionID + ")"
				+ " AND (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid + " != " + sTransactionID + ")"
			+ ")"
		;
		
		boolean bIncludeLinkToTransactions = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.APViewTransactionInformation, sUser, conn, sLicenseModuleLevel);
		
		boolean bOddRow = true;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		while (rs.next()){
			String sTableRowClass = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE;
			if (!bOddRow){
				sTableRowClass = SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY;
			}
			
			String sDocNumberLink = rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber);
			if (bIncludeLinkToTransactions){
				sDocNumberLink =
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APViewTransactionInformation?"
					+ SMTableaptransactions.lid + "=" + Long.toString(rs.getLong(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid))
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "\">" + sDocNumberLink + "</A>"
				;
			}
			
			s += "        <TR class=\"" + sTableRowClass + "\">\n"
				+ "          <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >" 
					+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE) 
					+ "</TD>\n"
				+ "          <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER + "\" >" 
					+ SMTableapbatchentries.getDocumentTypeLabel(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype)) + "</TD>\n"
				+ "          <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER 
					+ "\" >" + rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber) + "</TD>\n"
				+ "          <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER 
					+ "\" >" + sDocNumberLink + "</TD>\n"
				+ "          <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\" >" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bdappliedamount)) + "</TD>\n"
				+ "          <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER + "\" >" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bddiscountappliedamount)) + "</TD>\n"
				+ "        </TR>\n"
			;
			bOddRow = !bOddRow;
		}
		rs.close();
		
		//Finish off the table:
		s	+= "      </TABLE>\n"
		;
		return s;
	}
	private String sCommandScript(){
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		s += "</script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

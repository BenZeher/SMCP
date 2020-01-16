package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICEditReceiptEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private boolean bDebugMode = false;
	
	public static final String SUBMIT_BUTTON_RECEIVE_ALL = "RECEIVE_ALL";
	public static final String SUBMIT_BUTTON_RECEIVE_ALL_LABEL = "Receive all outstanding lines";
	public static final String SUBMIT_BUTTON_RECEIVE_OUTSTANDING = "RECEIVE_OUTSTANDING";
	public static final String SUBMIT_BUTTON_RECEIVE_OUTSTANDING_LABEL = "Receive this line";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		ICPOReceiptHeader entry = new ICPOReceiptHeader(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEditReceiptAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditReceipts
				);
		
	    String sCurrentCompleteURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
	    		 + clsManageRequestParameters.getQueryStringFromPost(request)).replace("&", "*");
	    if (bDebugMode){
	    	System.out.println("[1579191658] In " + this.toString() 
	    		+ "sCurrentCompleteURL = " + sCurrentCompleteURL);
	    }
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditReceipts)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the Action class, then
		//the session will have a receipt entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
		//Record this URL so we can return to it later:
		if (entry.getsID().compareToIgnoreCase("-1") != 0){
			smedit.addToURLHistory("Editing Receipt Number " + entry.getsID());
		}else{
			smedit.addToURLHistory("Adding a new receipt");
		}
		
	    if (currentSession.getAttribute(ICPOReceiptHeader.ParamObjectName) != null){
	    	entry = (ICPOReceiptHeader) currentSession.getAttribute(ICPOReceiptHeader.ParamObjectName);
	    	currentSession.removeAttribute(ICPOReceiptHeader.ParamObjectName);

	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + ICPOHeader.Paramlid + "=" + entry.getspoheaderid()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
		    	}
	    	}
	    }
	    
	    smedit.printHeaderTable();
	    	    
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A><BR>");
		
		boolean bEditingReceiptsPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEditReceipts, 
				smedit.getUserID(), 
				getServletContext(), 
				smedit.getsDBID(),
				(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Cannot edit receipts after they have been posted to IC:
		if (
				(entry.getspostedtoic().compareToIgnoreCase("0") != 0)
				|| (entry.getsstatus().compareToIgnoreCase(
				Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED)) == 0)
		){
			bEditingReceiptsPermitted = false;
			smedit.setbIncludeDeleteButton(false);
			smedit.setbIncludeUpdateButton(false);
		}
		
	    try {
			smedit.createEditPage(
				getEditHTML(smedit, entry, bEditingReceiptsPermitted), 
				createReceivingForm(
					smedit,
					getServletContext(), 
					entry,
					bEditingReceiptsPermitted
				)
			);
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPOHeader.Paramlid + "=" + entry.getsID()
				+ "&Warning=Could not load Receipt #: " + entry.getsID() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}

	    return;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			ICPOReceiptHeader entry, 
			boolean bEditingReceiptsPermitted
			) throws SQLException{

		String s = "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		
		//Store the ID so it can be passed back and forth:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramlid 
			+ "\" VALUE=\"" + entry.getsID() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramlpoheaderid 
			+ "\" VALUE=\"" + entry.getspoheaderid() + "\">\n";
		
		//New Row
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Purchase order #:</TD>\n"
			+ "<TD>" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
			+ "?" + ICPOHeader.Paramlid + "=" + entry.getspoheaderid()
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + entry.getspoheaderid() + "</A>"
			+ "</TD>\n"
			;
		s += "<TD>&nbsp;</TD>\n</TR>";
		
		//New Row
		String sReceiptID = "NEW";
		if (entry.getsID().compareToIgnoreCase("-1") != 0){
			sReceiptID = entry.getsID();
		}
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Receipt #:</TD>\n"
			+ "<TD>" 
			+ sReceiptID
			+ "</TD>\n"
			;
		//Status:
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Status:</TD>\n"
			+ "<TD>" 
			+ SMTableicporeceiptheaders.getStatusDescription(Integer.parseInt(entry.getsstatus()))
		;
		if (Integer.parseInt(entry.getsstatus()) == SMTableicporeceiptheaders.STATUS_DELETED){
			s += " by " + entry.getsdeletedby() + " on " + entry.getsdatdeleted()
				+ "<BR>\n"
				+ entry.getslastupdatedprocess()
			;
		}
		s += "</TD>\n"
			;
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramlpostedtoic 
			+ "\" VALUE=\"" + entry.getspostedtoic() + "\">\n";

		s += "</TR>";
		
		String sPostedToIC = "NO";
		if (entry.getspostedtoic().compareToIgnoreCase("1") == 0){
			sPostedToIC = "YES";
		}
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Posted to IC?:</TD>\n"
			+ "<TD>" 
			+ sPostedToIC
			+ "</TD>\n"
			;
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramlpostedtoic 
			+ "\" VALUE=\"" + entry.getspostedtoic() + "\">\n";

		s += "</TR>";
		
		//New Row:
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Date received:</TD>\n";
		s += "<TD style=\" text-align:left; \">";
		if (
				(entry.getspostedtoic().compareToIgnoreCase("0") == 0)
				&& bEditingReceiptsPermitted
				
		){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOReceiptHeader.Paramdatreceived + "\""
				+ " VALUE=\"" + entry.getsdatreceived() + "\""
				+ " SIZE=28"
				+ " MAXLENGTH=10"
				+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(ICPOReceiptHeader.Paramdatreceived, getServletContext())
			;			
		}else{
			s += entry.getsdatreceived();
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramdatreceived + "\" VALUE=\"" 
			+ entry.getsdatreceived() + "\">\n";
		}
		s += "</TD>\n";
		
		//Created by:
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Created by:</TD>\n"
			+ "<TD>" 
			+ entry.getscreatedbyfullname()
			+ "</TD>\n"
			;
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramscreatedbyfullname 
			+ "\" VALUE=\"" + entry.getscreatedbyfullname() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramlcreatedbyid
				+ "\" VALUE=\"" + entry.getlcreatedbyid() + "\">\n";

		//Store the last updated information here, but don't show the user:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramsdattimelastupdated
			+ "\" VALUE=\"" + entry.getsdattimelastupdated() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramslastupdatedbyfullname
			+ "\" VALUE=\"" + entry.getslastupdatedbyfullname() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramllastupdatedbyid
				+ "\" VALUE=\"" + entry.getllastupdatedbyid() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramslastupdatedprocess 
			+ "\" VALUE=\"" + entry.getslastupdatedprocess() + "\">\n";
		
		s += "</TR>";

		//Old Receipt number
		if (entry.getsreceiptnumber().compareToIgnoreCase("") == 0){
			s += "<TD>&nbsp</TD>\n<TD>&nbsp;" + "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramsreceiptnumber + "\" VALUE=\"" 
			+ entry.getsreceiptnumber() + "\">" + "</TD>\n";
		}else{
			s += "<TD style=\" text-align:right; font-weight:bold; \">Old receipt #:</TD>\n<TD>"; 
				s += entry.getsreceiptnumber();
				s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramsreceiptnumber + "\" VALUE=\"" 
					+ entry.getsreceiptnumber() + "\">";
			s += "</TD>\n";
		}
		s += "</TR>";
		
		//New row
		//The invoice number only shows if it's NOT a new receipt:
		//if (entry.getsID().compareToIgnoreCase("-1") != 0){
		//	s += "<TR>";
		//	//Invoice number:
		//	s += "<TD style=\" text-align:right; font-weight:bold; \">Invoice #:</TD>\n<TD>"; 
		//	s += entry.getsinvoicenumber();
		//	s += "</TD>\n";
		//	s += "<TD>&nbsp;</TD>\n<TD>&nbsp;</TD>\n";
		//	s += "</TR>";
		//}
		s += "</TABLE>\n";
		return s;
	}
	private String createReceivingForm(
			SMMasterEditEntry smedit,
			ServletContext context, 
			ICPOReceiptHeader entry,
			boolean bEditingReceiptsPermitted
	){
		String s = "";
		
		//List the po receipt lines here:
		if (!smedit.getAddingNewEntryFlag()){
			//Create a new form for the receiving:
			s += "<U><B>Purchase Order Lines:</B></U><BR>";
			
			//If the receipt has NOT been posted to IC, we display a button to receive all outstanding items:
			if (
					(entry.getspostedtoic().compareToIgnoreCase("0") == 0)
					&& (bEditingReceiptsPermitted)
				){
				s += "<INPUT TYPE=SUBMIT NAME='" 
					+ SUBMIT_BUTTON_RECEIVE_ALL
					+ "'" 
					+ "' VALUE='"
					+ SUBMIT_BUTTON_RECEIVE_ALL_LABEL
					+ "'" + " STYLE='height: 0.24in'>";
			}
			s += listPOLines(
					context, 
					smedit.getsDBID(), 
					smedit.getUserID(), 
					entry,
					bEditingReceiptsPermitted,
					(String) smedit.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		}
		return s;
	}
	private String listPOLines(
			ServletContext context, 
			String sDBID, 
			String sUserID,
			ICPOReceiptHeader entry,
			boolean bEditingReceiptsPermitted,
			String sLicenseModuleLevel
	){
		
		String s = "";
		boolean bViewItemAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation, 
				sUserID, 
				context, 
				sDBID,
				sLicenseModuleLevel);
		boolean bEditInvoiceAllowed = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEnterInvoices, 
				sUserID, 
				context, 
				sDBID,
				sLicenseModuleLevel);
		s+=SMUtilities.getMasterStyleSheetLink();
		s+="<TABLE BGCOLOR=\"#FFFFFF\" WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">\n";
		s+="<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">PO Line #</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Location on<BR>PO line</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Qty<BR>ordered</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Qty<BR>received</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Ordered<BR>cost</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Received<BR>cost</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + "\">Receive&nbsp;ALL<BR>outstanding</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Recv'd on<BR>this receipt</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Cost<BR>on this receipt</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Invoice&nbsp;ID</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Invoice&nbsp;#</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">On<BR>Hold?</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Item<BR>number</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Non-Inv<BR>Item?</TD>\n\n";
		s+="<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Item&nbsp;description</TD>\n\n";
	

		//If the PO header ID is blank, display an error:
		if (entry.getspoheaderid().trim().compareToIgnoreCase("") == 0){
			s += "<BR><FONT COLOR=RED><B>Error [1480622066] - PO ID is blank.</B></FONT><BR>";
			return s;
		}
		
		String SQL = "SELECT"
			+ " " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedreceivedcost
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.lid
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem
			+ " FROM " + SMTableicpolines.TableName
			+ " WHERE ("
			
				+ "(" + SMTableicpolines.TableName + "." 
				+ SMTableicpolines.lpoheaderid + " = " + entry.getspoheaderid() + ")"
			+ ")"
			+ " ORDER BY " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			;
		if (bDebugMode){
			System.out.println("[1579191667] In " + this.toString() + ".listPOLines - SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".listPOLines - userID: " + sUserID)
					);
			int iCount =0;
			while (rs.next()){
				if(iCount % 2 == 0) {
					s += "<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">\n";
				}else {
					s += "<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">\n";
				}

				
				//PO Line #
				s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ Long.toString(rs.getLong(
						SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber)) 
						+ "</TD>\n";
				
				//Location on PO line:
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  
					+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.slocation)
					+ "</TD>\n"
				;
				
				//Qty ordered
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdqtyorderedScale, rs.getBigDecimal(
								SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered))
					+ "</TD>\n"
				;
				
				//Qty received
				s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableicpolines.bdqtyreceivedScale, rs.getBigDecimal(
							SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived))
						+ "</TD>\n"
				;
				
				//total ordered cost
				s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdextendedordercostScale, rs.getBigDecimal(
								SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedordercost))
					+ "</TD>\n"
				;
				
				//total received cost
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableicpolines.bdextendedreceivedcostScale, rs.getBigDecimal(
							SMTableicpolines.TableName + "." + SMTableicpolines.bdextendedreceivedcost))
						+ "</TD>\n"
				;
				
				//Receive ALL outstanding:
				if (
						(entry.getspostedtoic().compareToIgnoreCase("0") == 0)
						&& bEditingReceiptsPermitted
				){
					s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
							+ "<INPUT TYPE=SUBMIT NAME='" 
							+ SUBMIT_BUTTON_RECEIVE_OUTSTANDING 
							+ Long.toString(rs.getLong(SMTableicpolines.TableName 
								+ "." + SMTableicpolines.lid))
							+ "'" 
							+ "' VALUE=\"" 
							+ SUBMIT_BUTTON_RECEIVE_OUTSTANDING_LABEL
							+ "\"' STYLE='height: 0.24in'>"
							+ "</TD>\n"
					;
				}else{
					s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
							+ "&nbsp;N/A&nbsp;"
							+ "</TD>\n"
					;
				}
				
				//Get the qty and cost received on this receipt:
				SQL = "SELECT"
					+ " " + SMTableicporeceiptlines.bdqtyreceived
					+ ", " + SMTableicporeceiptlines.bdextendedcost
					+ ", " + SMTableicporeceiptlines.lpoinvoiceid
					+ ", " + SMTableicporeceiptlines.lid
					+ " FROM " + SMTableicporeceiptlines.TableName
					+ " WHERE ("
						+ "(" + SMTableicporeceiptlines.lpolineid + " = " 
							+ Long.toString(rs.getLong(SMTableicpolines.lid)) + ")"
						+ " AND (" + SMTableicporeceiptlines.lreceiptheaderid + " = " 
						+ entry.getsID() + ")"
					+ ")"
					;
				
				BigDecimal bdQtyReceived = new BigDecimal(0);
				BigDecimal bdCostReceived = new BigDecimal(0);
				long lPOReceiptLineID = -1;
				long lPOInvoiceID = -1;
				try {
					ResultSet rsReceiptLine = clsDatabaseFunctions.openResultSet(
							SQL, 
							getServletContext(),
							sDBID, 
							"MySQL", 
							SMUtilities.getFullClassName(this.toString()) 
							+ ".listPOLines.getting recpt qty and cost - userID: " + sUserID
							);
					if (rsReceiptLine.next()){
						bdQtyReceived = rsReceiptLine.getBigDecimal(SMTableicporeceiptlines.bdqtyreceived);
						bdCostReceived = rsReceiptLine.getBigDecimal(SMTableicporeceiptlines.bdextendedcost);
						lPOReceiptLineID = rsReceiptLine.getLong(SMTableicporeceiptlines.lid);
						lPOInvoiceID = rsReceiptLine.getLong(SMTableicporeceiptlines.lpoinvoiceid);
					}
					rsReceiptLine.close();
				} catch (SQLException e) {
					return "Error reading po receipt line for po line ID: " 
					+ Long.toString(rs.getLong(SMTableicpolines.lid)) + " - " + e.getMessage();
				}
				
				//Recv'd on this receipt
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ getQtyReceivedOnThisReceiptLink(
							Long.toString(rs.getLong(SMTableicpolines.lid)),
							lPOReceiptLineID,
							bdQtyReceived,
							sDBID,
							entry,
							bEditingReceiptsPermitted
						)
						+ "</TD>\n"
				;
				
				//Cost Recv'd on this receipt
				s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ getCostReceivedOnThisReceiptLink(
						Long.toString(rs.getLong(SMTableicpolines.lid)),
						lPOReceiptLineID,
						bdCostReceived,
						sDBID,
						entry,
						bEditingReceiptsPermitted
					)
					+ "</TD>\n"
				;
				
				//Invoice
				String sInvoiceValueLink = "";
				String sInvoiceNumber = "";
				String  sInvoiceVendor = "";
				if (lPOInvoiceID == 0){
					sInvoiceValueLink = "N/A";
				}
				if (lPOInvoiceID > 0){
					if (bEditInvoiceAllowed){
						sInvoiceValueLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smic.ICEnterInvoiceEdit?" + ICPOInvoice.ParamlID + "=" 
							+ Long.toString(lPOInvoiceID)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + Long.toString(lPOInvoiceID) + "</A>";
					}else{
						sInvoiceValueLink = Long.toString(lPOInvoiceID);
					}
					
					//get invoice number
					SQL = "SELECT * FROM" +
							" " + SMTableicpoinvoiceheaders.TableName +
							" WHERE" +
							" " + SMTableicpoinvoiceheaders.lid + " = " + lPOInvoiceID;
					ResultSet rsICPOInvoice = clsDatabaseFunctions.openResultSet(SQL, 
																		getServletContext(),
																		sDBID, 
																		"MySQL", 
																		SMUtilities.getFullClassName(this.toString()) 
																		+ ".listPOLines.getting po invoice number - userID: " + sUserID
																		);
					if (rsICPOInvoice.next()){
						sInvoiceNumber = rsICPOInvoice.getString(SMTableicpoinvoiceheaders.sinvoicenumber);
						sInvoiceVendor = rsICPOInvoice.getString(SMTableicpoinvoiceheaders.svendor);
					}else{
						sInvoiceNumber = "N/A";
					}
					rsICPOInvoice.close();
				}
				
				s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ sInvoiceValueLink
					+ "</TD>\n"
				;
				
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ sInvoiceNumber
					+ "</TD>\n"
				;
				if(sInvoiceNumber.equals("N/A") || sInvoiceNumber.equals("")) {
					
					s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
							+ "N/A"
							+ "</TD>\n"
						;
				}else {
					SQL = "SELECT "+SMTableaptransactions.ionhold+" FROM "+SMTableaptransactions.TableName+" WHERE "
						+ " ( "
						+ "("+SMTableaptransactions.sdocnumber +" = '"+sInvoiceNumber+"' )"
						+ " AND "
						+ "("+SMTableaptransactions.svendor+" = '"+sInvoiceVendor+"')"
						+ " )";
					
					ResultSet rsOnHold = clsDatabaseFunctions.openResultSet(SQL, 
							getServletContext(),
							sDBID, 
							"MySQL", 
							SMUtilities.getFullClassName(this.toString()) 
							+ ".listPOLines.getting po invoice number - userID: " + sUserID
							);
					String sOnHold = "";
					if(rsOnHold.next()) {
						sOnHold = String.valueOf(rsOnHold.getInt(SMTableaptransactions.ionhold));
					}
					sOnHold = sOnHold.equals("1") ? "Y" : "N";
					s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
							+ sOnHold
							+ "</TD>\n"
						;
				}
				
				
				
				//Item
				String sItemNumber = rs.getString(SMTableicpolines.TableName + "." 
					+ SMTableicpolines.sitemnumber);
				String sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?ItemNumber=" 
		    		+ sItemNumber
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				if (
						bViewItemAllowed
						&& (rs.getLong(
							SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem) == 0)
						
				){
					s += "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + sItemNumberLink + "</TD>\n";
				}else {
					s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sItemNumber + "</TD>\n";
				}
				
				//Non-inventory item?
				String sNonInventoryItem = "N";
				if (rs.getInt(SMTableicpolines.TableName + "." 
						+ SMTableicpolines.lnoninventoryitem) == 1){
					sNonInventoryItem = "Y";
				}
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">"  + sNonInventoryItem + "</TD>\n";
				
				//Desc
				s +=  "<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ rs.getString(SMTableicpolines.sitemdescription) + "</TD>\n"
				;

				s += "</TR>";
				iCount++;
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s += "</TABLE>";
		return s;
	}
	private String getQtyReceivedOnThisReceiptLink(
			String sPOLineID,
			long lPOReceiptLineID,
			BigDecimal bdQtyReceived,
			String sDBID,
			ICPOReceiptHeader entry,
			boolean bEditingReceiptsPermitted
			) {
		
		String sReceivedLink = "";
		if (
				(entry.getspostedtoic().compareToIgnoreCase("0") == 0)
				&& bEditingReceiptsPermitted
		
		){
			sReceivedLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptLineEdit"
				+ "?" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsID();
			//If there IS no receipt line yet, indicate that this is a NEW line:
			if (lPOReceiptLineID == -1L){
				sReceivedLink += "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=YES";
			}
			sReceivedLink +=
			"&" + ICPOReceiptLine.Paramlid + "=" + Long.toString(lPOReceiptLineID)
			+ "&" + ICPOReceiptLine.Paramlpolineid + "=" + sPOLineID
			+ "&" + ICPOReceiptHeader.Paramlpoheaderid + "=" + entry.getspoheaderid()
			+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    		+ "\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
    			SMTableicporeceiptlines.bdqtyreceivedScale, bdQtyReceived) + "</A>"
    		;

		}else{
			sReceivedLink = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableicporeceiptlines.bdqtyreceivedScale, bdQtyReceived);
		}
		
		return sReceivedLink;
	}
	private String getCostReceivedOnThisReceiptLink(
			String sPOLineID,
			long lPOReceiptLineID,
			BigDecimal bdCostReceived,
			String sDBID,
			ICPOReceiptHeader entry,
			boolean bEditingReceiptsPermitted
			) {
		
		String sReceivedLink = "";
		if (
				(entry.getspostedtoic().compareToIgnoreCase("0") == 0)
				&& bEditingReceiptsPermitted
		){
			sReceivedLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptLineEdit"
				+ "?" + ICPOReceiptLine.Paramlreceiptheaderid + "=" + entry.getsID();
			//If there IS no receipt line yet, indicate that this is a NEW line:
			if (lPOReceiptLineID == -1L){
				sReceivedLink += "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=YES";
			}
			sReceivedLink +=
			"&" + ICPOReceiptLine.Paramlid + "=" + Long.toString(lPOReceiptLineID)
			+ "&" + ICPOReceiptLine.Paramlpolineid + "=" + sPOLineID
			+ "&" + ICPOReceiptHeader.Paramlpoheaderid + "=" + entry.getspoheaderid()
			+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    		+ "\">" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
    			SMTableicporeceiptlines.bdextendedcostScale, bdCostReceived) + "</A>"
    		;
		}else{
			sReceivedLink = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableicporeceiptlines.bdextendedcostScale, bdCostReceived);
		}
		
		return sReceivedLink;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

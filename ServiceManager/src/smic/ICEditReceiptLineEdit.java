package smic;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICEditReceiptLineEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String CALCULATECOSTS_BUTTON = "CALCULATECOSTS"; 
	public static final String CALCULATECOSTS_BUTTON_LABEL = "Calculate costs"; 
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		ICPOReceiptLine entry = new ICPOReceiptLine(request);
		String sObjectName = entry.getObjectName();
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				sObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEditReceiptLineAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditReceipts
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditReceipts)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by an 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(ICPOReceiptLine.ParamObjectName) != null){
	    	entry = (ICPOReceiptLine) currentSession.getAttribute(ICPOReceiptLine.ParamObjectName);
	    	currentSession.removeAttribute(ICPOReceiptLine.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + ICPOReceiptHeader.Paramlid + "=" + entry.getsreceiptheaderid()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
		    	}
	    	}
	    }
		//First, load all the po receipt data we can from the po line:
		try {
			loadPOLineData(
					entry, 
					smedit.getsDBID(), 
					smedit.getUserName(), 
					smedit.getAddingNewEntryFlag());
		} catch (SQLException e1) {
			smedit.getPWOut().println("Error loading PO line data.<BR>");
		}
	    smedit.printHeaderTable();
	    
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
	    
		//Add a link to the 'return to . . . ' class:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");
		
		smedit.setbIncludeDeleteButton(false);
/*    
		//Load gl accounts
		ArrayList<String> m_sGLValues = new ArrayList<String>();
	    ArrayList<String> m_sGLDescriptions = new ArrayList<String>();
		m_sGLValues.clear();
	    m_sGLDescriptions.clear();
	    try{
	    	String sSQL = MySQLs.Get_GL_Account_List_SQL(false);

		    ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
		    		sSQL, 
		    		getServletContext(), 
		    		smedit.getsDBID(),
		    		"MySQL",
		    		this.toString() + ".loadGLList (1) - User: " + smedit.getUserName());
		        
		    //Print out directly so that we don't waste time appending to string buffers:
		    while (rsGLAccts.next()){
		    	m_sGLValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
		    	m_sGLDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
		    }
		    rsGLAccts.close();
		
	    }catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	    	System.out.println("SQLException: " + ex.getMessage());
	    	System.out.println("SQLState: " + ex.getSQLState());
	    	System.out.println("SQL: " + ex.getErrorCode());
	    	return;
	    }
			
		
	   //load locations
	   ArrayList<String> m_sLocationValues = new ArrayList<String>();
	   ArrayList<String> m_sLocationDescriptions = new ArrayList<String>();
	   m_sLocationValues.clear();
	   m_sLocationDescriptions.clear();
	   try{
		   String sSQL = "SELECT "
				   + SMTablelocations.sLocation
				   + ", " + SMTablelocations.sLocationDescription
				   + " FROM " + SMTablelocations.TableName
				   + " ORDER BY " + SMTablelocations.sLocation;

		   ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
				   sSQL, 
				   getServletContext(), 
				   smedit.getsDBID(),
				   "MySQL",
				   this.toString() + ".loadLocationList (1) - User: " + smedit.getUserName());
		        
		   //Print out directly so that we don't waste time appending to string buffers:
		   while (rsLocations.next()){
			   m_sLocationValues.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
			   m_sLocationDescriptions.add(
					   (String) rsLocations.getString(SMTablelocations.sLocation).trim() 
					   + " - " + (String) rsLocations.getString(SMTablelocations.sLocationDescription).trim());
		   }
		   rsLocations.close();

	   }catch (SQLException ex){
		   System.out.println("Error in " + this.toString()+ " class!!");
		   System.out.println("SQLException: " + ex.getMessage());
		   System.out.println("SQLState: " + ex.getSQLState());
		   System.out.println("SQL: " + ex.getErrorCode());
		   return;
	   }
*/		
	   try {
		   smedit.createEditPage(
				   getEditHTML(
						   smedit, 
						   entry, 
						   clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramlpoheaderid, request)
						   )
					, ""
			);
	   } catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPOReceiptHeader.Paramlid + "=" + entry.getsreceiptheaderid()
				+ "&Warning=Could not load receipt line number: " + entry.getsitemnumber() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, ICPOReceiptLine entry, String sPOId) throws SQLException{

	    	    
	    String s = "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">\n";
		
		//Store the ID so it can be passed back and forth:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramlid + "\" VALUE=\"" + entry.getsID() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramlreceiptheaderid + "\" VALUE=\"" + entry.getsreceiptheaderid() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramllinenumber + "\" VALUE=\"" + entry.getslinenumber() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramlnoninventoryitem + "\" VALUE=\"" + entry.getsnoninventoryitem() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramlpolineid + "\" VALUE=\"" + entry.getspolineid() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramsglexpenseacct + "\" VALUE=\"" + entry.getsglexpenseacct() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramsitemdescription + "\" VALUE=\"" + entry.getsitemdescription().replace("\"", "&quot;") + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramsitemnumber + "\" VALUE=\"" + entry.getsitemnumber() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramslocation + "\" VALUE=\"" + entry.getslocation() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramsunitofmeasure + "\" VALUE=\"" + entry.getsunitofmeasure() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">\n";
		
		//Get the receipt's 'concurrency' information, so we can pass it to the saving function and make sure we're not saving from a stale version of the receipt:
		ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
		rcpt.setsID(entry.getsreceiptheaderid());
		String sReceiptLoadingError = "";
		try {
			if(!rcpt.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
				sReceiptLoadingError = rcpt.getErrorMessages();
			}
		} catch (Exception e) {
			sReceiptLoadingError = e.getMessage();
		}
		if (sReceiptLoadingError.compareToIgnoreCase("") != 0){
			throw new SQLException("Error [1488390312] - " + sReceiptLoadingError);
		}
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramsdattimelastupdated + "\" VALUE=\"" + rcpt.getsdattimelastupdated() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramslastupdatedprocess + "\" VALUE=\"" + rcpt.getslastupdatedprocess() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramslastupdatedbyfullname + "\" VALUE=\"" + rcpt.getslastupdatedbyfullname() + "\">\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptHeader.Paramllastupdatedbyid+ "\" VALUE=\"" + rcpt.getllastupdatedbyid() + "\">\n";
		//New Row
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Purchase order #:</TD>"
			+ "<TD>" 
			+ sPOId
			+ "</TD>"
			;
		s += "<TD style=\" text-align:right; font-weight:bold; \">Receipt #:</TD>"
			+ "<TD>"
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptEdit"
			+ "?" + ICPOHeader.Paramlid + "=" + entry.getsreceiptheaderid()
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + entry.getsreceiptheaderid() + "</A>"
			+ "</TD>"
			+ "</TR>"
			;
		
		//New Row:
		//Line number
		String sLineNumber = entry.getslinenumber().trim();
		if (sLineNumber.compareToIgnoreCase("-1") == 0){
			sLineNumber = "NEW";
		}
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Line #:</TD>";
		s += "<TD>"
			+ sLineNumber
			+ "</TD>"
		;
		String sNonInventoryItem = "NO";
		if (entry.getsnoninventoryitem().compareToIgnoreCase("1") == 0){
			sNonInventoryItem = "YES";
		}
		s += "<TD style=\" text-align:right; font-weight:bold; \">NON-Inventory item?</TD>";
		s += "<TD>"
			+ sNonInventoryItem
			;

		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOReceiptLine.Paramlnoninventoryitem + "\" VALUE=\"" 
			+ entry.getsnoninventoryitem() + "\">";
		
		s += "</TD>";
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Quantity received:</TD>";
		s += "<TD>";

		s += "<INPUT TYPE=TEXT NAME=\"" + ICPOReceiptLine.Parambdqtyreceived + "\""
			+ " VALUE=\"" + entry.getsqtyreceived().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + "16"
			+ ">"
			;
		s += "</TD>";
		
		//Unit cost:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Unit cost:</TD>";
		s += "<TD>";

		s += "<INPUT TYPE=TEXT NAME=\"" + ICPOReceiptLine.Parambdunitcost + "\""
			+ " VALUE=\"" + entry.getsunitcost().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + "16"
			+ ">"
			;
		s += "</TD>";
		
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">&nbsp;</TD>";
		s += "<TD>";

		s += "<INPUT TYPE=SUBMIT NAME='" + CALCULATECOSTS_BUTTON + "'" 
			+ " VALUE='" + CALCULATECOSTS_BUTTON_LABEL + "'" 
			+ " STYLE='height: 0.24in'>"
			;
		s += "</TD>";
		
		//Received cost:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Cost received:</TD>";
		s += "<TD>";

		s += "<INPUT TYPE=TEXT NAME=\"" + ICPOReceiptLine.Parambdextendedcost + "\""
			+ " VALUE=\"" + entry.getsextendedcost().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + "16"
			+ ">"
			;
		s += "</TD>";
		s += "</TR>";
		
		//Work order comment:
		s += "<TR>";
		s += "<TD style=\" text-align:left; font-weight:bold; \">Work order item comment:</TD>";
		s += "<TD>";
		s += "</TR>";
		s += "<TR>";
		s += "<TD COLSPAN = 4>";
		s += "<TEXTAREA NAME=\"" + ICPOReceiptLine.Paramsworkordercomment + "\""
		+ " rows=\"2\""
		+ " cols=\"50\""
		+ ">"
		+ entry.getsworkordercomment().replace("\"", "&quot;")
		+ "</TEXTAREA>";
		s += "</TD>";
		s += "</TR>";
		
		s += "</TABLE>";
		s += "</TD>";
		s += "</TR>";
		//End of embedded table
		
		//Validate button:
		//s += "<TR>";
		//s += "<TD style=\" vertical-align:top; text-align:center; \" colspan=4>"
		//	+ "<INPUT TYPE=SUBMIT NAME='" + "VALIDATE" + "'" 
		//		+ " VALUE='Validate this line before updating'" 
		//		+ " STYLE='height: 0.24in'>"
		//	+ "</TD>";
		//s += "</TR>";
		
		s += "</TABLE>";
		return s;
	}
	private void loadPOLineData(
			ICPOReceiptLine line, 
			String sDBID, 
			String sUser, 
			boolean bAddingNewReceiptLine) throws SQLException{
	    //Load information from the po line so we can populate the receipt line data:
	    String SQL = "SELECT"
	    	+ " * FROM " + SMTableicpolines.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTableicpolines.lid + " = " + line.getspolineid() + ")"
	    	+ ")"
	    	;

	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".loadPOLineData - user: " + sUser
					);
			if (rs.next()){
				line.setsglexpenseacct(rs.getString(SMTableicpolines.sglexpenseacct));
				line.setsitemdescription(rs.getString(SMTableicpolines.sitemdescription));
				line.setsitemnumber(rs.getString(SMTableicpolines.sitemnumber));
				line.setslocation(rs.getString(SMTableicpolines.slocation));
				line.setsnoninventoryitem(Long.toString(rs.getLong(SMTableicpolines.lnoninventoryitem)));
				line.setsunitofmeasure(rs.getString(SMTableicpolines.sunitofmeasure));
				//If it's a new record, get the unit cost from the PO line:
				if (bAddingNewReceiptLine){
					line.setsunitcost(
						clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableicporeceiptlines.bdunitcostScale, rs.getBigDecimal(
								SMTableicpolines.bdunitcost)));
				}
				rs.close();
			}else{
				rs.close();
				throw new SQLException("PO line with ID '" + line.getspolineid() + "' was not found.");
			}
		} catch (SQLException e) {
			throw new SQLException("SQL error loading PO line with SQL: " + SQL + " - " + e.getMessage());
		}

	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

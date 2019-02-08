package smic;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smgl.GLAccount;

public class ICEditPOLineEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String TOGGLE_NONINVENTORY_BUTTON_NAME = "NONINVTOGGLE";
	public static final String TOGGLE_TO_NONINVENTORY_LABEL = "Change to NON-INVENTORY item";
	public static final String TOGGLE_TO_INVENTORY_LABEL = "Change to INVENTORY item";
	public static final String GET_MOST_RECENT_COST_BUTTON = "GETMOSTRECENTCOST";
	public static final String GET_MOST_RECENT_COST_BUTTON_LABEL = "Get most recent cost";
	public static final String GET_VENDOR_ITEM_NO_BUTTON = "GETVENDORITEM";
	public static final String GET_VENDOR_ITEM_NO_BUTTON_LABEL = "Get vendor item info";
	public static final String FIND_ITEM_BUTTON = "FINDITEM";
	public static final String FIND_ITEM_BUTTON_LABEL = "Find item";
	public static final String CREATE_ITEM_BUTTON = "CREATEITEMFROMPO";
	public static final String CREATE_ITEM_BUTTON_LABEL = "Create item";
	public static final String ADD_ITEM_TO_ORDER_BUTTON = "ADDITEMTOORDER";
	public static final String ADD_ITEM_TO_ORDER_BUTTON_LABEL = "Add item to order";
	public static final String UPDATEANDADD_BUTTON = "UPDATEANDADD";
	public static final String UPDATEANDADD_LABEL = "Save line and add another";
	public static final String UPDATE_LABEL = "Save line";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		String sPONumber = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramsponumber, request);
		
		ICPOLine entry = new ICPOLine(request);
		String sObjectName = entry.getObjectName();
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				sObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEditPOLineAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditPurchaseOrders
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by an 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(ICPOLine.ParamObjectName) != null){
	    	entry = (ICPOLine) currentSession.getAttribute(ICPOLine.ParamObjectName);
	    	currentSession.removeAttribute(ICPOLine.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		//IF we're not just returning from a finder:
	    		if (request.getParameter("RETURNINGFROMFINDER") == null){
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
	    }
	    
	    //If we are returning from finding an item, update that item and item info:
	    if (request.getParameter("FOUND" + ICPOLine.Paramsitemnumber) != null){
	    	entry.setsitemnumber(request.getParameter("FOUND" + ICPOLine.Paramsitemnumber));
	    	entry.validate_entry_fields(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
	    }
	    //If we are returning from finding an expense account, update that account and account info:
	    if (request.getParameter("FOUND" + GLAccount.Paramobjectname) != null){
	    	entry.setsglexpenseacct(request.getParameter("FOUND" + GLAccount.Paramobjectname));
	    	entry.validate_entry_fields(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
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
		
		//We're going to create our own button instead:
		smedit.setbIncludeUpdateButton(false);
		
		
		//Load gl accounts
		ArrayList<String> m_sGLValues = new ArrayList<String>();
		ArrayList<String> m_sGLDescriptions = new ArrayList<String>();
		m_sGLValues.clear();
        m_sGLDescriptions.clear();
        try{
	        String sSQL = "SELECT" 
	        	+ " " + SMTableglaccounts.sAcctID 
	        	+ ", " + SMTableglaccounts.sDesc 
	        	+ " FROM " + SMTableglaccounts.TableName
	        	+ " WHERE ("
    				+ "(" + SMTableglaccounts.lActive + " = 1)"
    				+ " AND (" + SMTableglaccounts.iallowaspoexpense + " = 1)"
    			+ ")"
    			+ " ORDER BY " + SMTableglaccounts.sAcctID;

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
			String sError = "Could not load glaccounts - " + ex.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPOHeader.Paramsponumber + "=" + sPONumber
				+ "&Warning=Could not glaccounts - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		
        //Load locations
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
			String sError = "Could not load locations - " + ex.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPOHeader.Paramsponumber + "=" + sPONumber
				+ "&Warning=Could not locatinos - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		

		try {
			smedit.createEditPage(getEditHTML(smedit, entry, m_sGLValues, m_sGLDescriptions, m_sLocationValues, m_sLocationDescriptions), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPOHeader.Paramsponumber + "=" + sPONumber
				+ "&Warning=Could not load line #: " + entry.getsitemnumber() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		
	    return;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			ICPOLine entry,
			ArrayList<String> m_sGLValues,
			ArrayList<String> m_sGLDescriptions,
			ArrayList<String> m_sLocationValues,
			ArrayList<String> m_sLocationDescriptions) throws SQLException{
		String s = "";

	    s = "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";
		
		//Store the ID so it can be passed back and forth:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Paramlid + "\" VALUE=\"" + entry.getsID() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Paramlpoheaderid + "\" VALUE=\"" + entry.getspoheaderid() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Paramllinenumber + "\" VALUE=\"" + entry.getslinenumber() + "\">";
		//s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOHeader.Paramsponumber + "\" VALUE=\"" + sPONumber + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Paraminoninventoryitem + "\" VALUE=\"" + entry.getsnoninventoryitem() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Parambdqtyreceived + "\" VALUE=\"" + entry.getsqtyreceived() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Parambdextendedreceivedcost + "\" VALUE=\"" + entry.getsextendedreceivedcost() + "\">";
		
		//New Row:
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Purchase order #:</TD>";
		s += "<TD>"
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
			+ "?" + ICPOHeader.Paramlid + "=" + entry.getspoheaderid()
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + entry.getspoheaderid() + "</A>"
			+ "</TD>"
		;
		s += "<TD>&nbsp;</TD>";
		
		//Line number
		String sLineNumber = entry.getslinenumber().trim();
		if (sLineNumber.compareToIgnoreCase("-1") == 0){
			sLineNumber = "NEW";
		}
		s += "<TD style=\" text-align:right; font-weight:bold; \">Line #:</TD>";
		s += "<TD>"
			+ sLineNumber
			+ "</TD>"
			+ "</TR>"
		;
		
		//New Row:
		//Location
		//If there's no location yet, default to the SHIP TO location on the PO header:
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.sshipcode
			+ " FROM " + SMTableicpoheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.lid + " = " + entry.getspoheaderid() + ")"
			+ ")"
		;
		try {
			ResultSet rsPO = clsDatabaseFunctions.openResultSet(SQL,
					getServletContext(), sm.getsDBID(), "MySQL", this
							.toString()
							+ ".getEditHTML - user: " + sm.getUserName());
			if (rsPO.next()) {
				if (entry.getslocation().compareToIgnoreCase("") == 0){
					entry.setslocation(rsPO.getString(SMTableicpoheaders.sshipcode));
				}
			}
			rsPO.close();
		} catch (SQLException e) {
			// Don't do anything - user will just have to pick a location
		}
		s += "<TR><TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Location:</TD>";
		s += "<TD>";
		if (
				(entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
				|| (entry.getstatus() == SMTableicpolines.STATUS_PARTIALLY_RECEIVED)
			){		
			s += "<SELECT NAME=\"" + ICPOLine.Paramslocation + "\"" + ">";
			//Add one for the 'Other':
			for (int i = 0; i < m_sLocationValues.size(); i++){
				s += "<OPTION";
				if (m_sLocationValues.get(i).compareToIgnoreCase(entry.getslocation()) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + m_sLocationValues.get(i).toString() + "\">" + m_sLocationDescriptions.get(i).toString();
			}
        	s += "</SELECT>";
		}else{
			s += entry.getslocation().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Paramslocation
			+ "\" VALUE=\"" + entry.getslocation() + "\">";
		}
        	s += "</TD>";
		
		String sNonInventoryItem = "NO";
		String sToggleButtonLabel = TOGGLE_TO_NONINVENTORY_LABEL;
		if (entry.getsnoninventoryitem().compareToIgnoreCase("1") == 0){
			sNonInventoryItem = "YES";
			sToggleButtonLabel = TOGGLE_TO_INVENTORY_LABEL;
		}
		s += "<TD style=\" text-align:right; font-weight:bold; \">NON-Inventory item?</TD>";
		s += "<TD>"
			+ "&nbsp;" + sNonInventoryItem
			;
		
		if (
			(entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
			|| (entry.getstatus() == SMTableicpolines.STATUS_PARTIALLY_RECEIVED)
		){
			s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + TOGGLE_NONINVENTORY_BUTTON_NAME + "'" 
			+ " VALUE='" + sToggleButtonLabel + "'" 
			+ " STYLE='height: 0.24in'>"
			;
		}
		s += "</TD>";
		s += "</TR>";
		
		//New Row:
		//Item
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Item #: <FONT COLOR=RED>*Required*</FONT></TD>";
		s += "<TD>";
		if (entry.getstatus() == SMTableicpolines.STATUS_ENTERED){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Paramsitemnumber + "\""
			+ " VALUE=\"" + entry.getsitemnumber().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + SMTableicpolines.sitemnumberLength
			+ ">";

			//Put a finder here, if it's not a NON-INVENTORY item:
			if (entry.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + FIND_ITEM_BUTTON + "'" 
				+ " VALUE='" + FIND_ITEM_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				;
				
				if (SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.ICEditItems, 
						sm.getUserID(), 
						getServletContext(), 
						sm.getsDBID(),
						(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
							//Now put a button to create a new item here:
							s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + CREATE_ITEM_BUTTON + "'" 
							+ " VALUE='" + CREATE_ITEM_BUTTON_LABEL + "'" 
							+ " STYLE='height: 0.24in'>"
							;
				}
			}
		}else{
			s += entry.getsitemnumber().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Paramsitemnumber 
			+ "\" VALUE=\"" + entry.getsitemnumber() + "\">";
		}
		s += "</TD>";

		//Description
		s += "<TD style=\" text-align:right; font-weight:bold; \">Description:</TD>";
		s += "<TD>";
		if (entry.getstatus() == SMTableicpolines.STATUS_ENTERED){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Paramsitemdescription + "\""
			+ " VALUE=\"" + entry.getsitemdescription().replace("\"", "&quot;") + "\""
			+ " SIZE=50"
			+ " MAXLENGTH=" + SMTableicpolines.sitemdescriptionLength
			+ ">";
		}else{
			s += entry.getsitemdescription().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Paramsitemdescription
			+ "\" VALUE=\"" + entry.getsitemdescription() + "\">";
		}
			
		s += "</TD>";
		s += "</TR>";
		
		//New Row:
		s += "<TR>";
		//Unit of measure:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Unit of measure:</TD>";
		s += "<TD>";
		if (
			(entry.getsnoninventoryitem().compareToIgnoreCase("1") == 0)
			&& (entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
		){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Paramsunitofmeasure + "\""
			+ " VALUE=\"" + entry.getsunitofmeasure().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + SMTableicpolines.sunitofmeasureLength
			+ ">"
			;
		}else{
			s += entry.getsunitofmeasure().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Paramsunitofmeasure
			+ "\" VALUE=\"" + entry.getsunitofmeasure() + "\">";
		}
		s += "</TD>";

		//Vendor's item number:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Vendor's item #:</TD>";
		s += "<TD>";
		if (entry.getstatus() == SMTableicpolines.STATUS_ENTERED){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Paramsvendorsitemnumber + "\""
			+ " VALUE=\"" + entry.getsvendorsitemnumber().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + SMTableicpolines.svendorsitemnumberLength
			+ ">";
			
			//Put a button here, if it's not a NON-INVENTORY item:
			if (entry.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + GET_VENDOR_ITEM_NO_BUTTON + "'" 
				+ " VALUE='" + GET_VENDOR_ITEM_NO_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				;
			}
		}else{
			s += entry.getsvendorsitemnumber().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Paramsvendorsitemnumber
			+ "\" VALUE=\"" + entry.getsvendorsitemnumber() + "\">";
		}
		s += "</TD>";
		s += "</TR>";
		
		//New Row:
		//Qty
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Quantity: <FONT COLOR=RED>*Required*</FONT></TD>";
		s += "<TD>";
		if (
			(entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
			|| (entry.getstatus() == SMTableicpolines.STATUS_PARTIALLY_RECEIVED)
		){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Parambdqtyordered + "\""
			+ " VALUE=\"" + entry.getsqtyordered().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + "16"
			+ ">"
			;
		}else{
			s += entry.getsqtyordered().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Parambdqtyordered
			+ "\" VALUE=\"" + entry.getsqtyordered() + "\">";
		}
		s += "</TD>";
			
		//Vendor's item comments:
		s += "<TD style=\" text-align:right; font-weight:bold; \">Vendor's item comment:</FONT></TD>";
		s += "<TD>";
		s += entry.getsvendorsitemcomment().replace("\"", "&quot;")
		+ "<INPUT TYPE=HIDDEN NAME=\"" 
		+ ICPOLine.Paramsvendorsitemcomment
		+ "\" VALUE=\"" + entry.getsvendorsitemcomment() + "\">";
		s += "</TD>";
		
		s += "</TR>";	
		
		//Number of labels:
		s += "<TR>";
		if (entry.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
			s += "<TD style=\" text-align:right; font-weight:bold; \">Number of labels:</FONT></TD>";
			s += "<TD>";
			if (
				(entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
				|| (entry.getstatus() == SMTableicpolines.STATUS_PARTIALLY_RECEIVED)
			){
				s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Parambdnumberoflabels + "\""
				+ " VALUE=\"" + entry.getsnumberoflabels().replace("\"", "&quot;") + "\""
				+ " SIZE=15"
				+ " MAXLENGTH=" + "16"
				+ ">"
				;
			}else{
				s += entry.getsnumberoflabels().replace("\"", "&quot;")
				+ "<INPUT TYPE=HIDDEN NAME=\"" 
				+ ICPOLine.Parambdnumberoflabels
				+ "\" VALUE=\"" + entry.getsnumberoflabels() + "\">";
			}
			s += "</TD>";
		}
		s += "<TR>";
		
		//New Row:
		//Unit cost
		s += "<TR>";
		s += "<TD style=\" text-align:right; font-weight:bold; \">Unit cost:</TD>";
		s += "<TD>";
		if (entry.getstatus() == SMTableicpolines.STATUS_ENTERED){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Parambdunitcost + "\""
			+ " VALUE=\"" + entry.getsunitcost().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + "13"
			+ ">";
			
			//Put a button here, if it's not a NON-INVENTORY item:
			if (entry.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + GET_MOST_RECENT_COST_BUTTON + "'" 
				+ " VALUE='" + GET_MOST_RECENT_COST_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				;
			}
			
		}else{
			s += entry.getsunitcost().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Parambdunitcost
			+ "\" VALUE=\"" + entry.getsunitcost() + "\">";
		}
		s += "</TD>";
		
		//Extended cost
		s += "<TD style=\" text-align:right; font-weight:bold; \">Extended order cost:</TD>";
		s += "<TD>";
		if (
				(entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
				|| (entry.getstatus() == SMTableicpolines.STATUS_PARTIALLY_RECEIVED)
			){
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPOLine.Parambdextendedordercost + "\""
			+ " VALUE=\"" + entry.getsextendedordercost().replace("\"", "&quot;") + "\""
			+ " SIZE=15"
			+ " MAXLENGTH=" + "16"
			+ ">";
		}else{
			s += entry.getsextendedordercost().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Parambdextendedordercost
			+ "\" VALUE=\"" + entry.getsextendedordercost() + "\">";
		}
		s+= "</TD>";
		s += "</TR>";
		
		//The user needs to choose an expense account for EITHER a non-inventory item OR for a non-stock INVENTORY item:
		//If this is an INVENTORY item:
		if (entry.getsnoninventoryitem().compareToIgnoreCase("1") != 0){
			//Then determine whether it's a STOCK or NON-STOCK (expensed) item:
		}
		
		//We hide the expense account drop down is this is a stock inventory item:
		//if (!bIsStockInventoryItem){
			//New Row:
			//GL Expense acct:
			s += "<TR>";
			s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Expense account<SUP>1</SUP>:</TD>";
			s += "<TD>";
			if (entry.getstatus() == SMTableicpolines.STATUS_ENTERED){
				s += "<SELECT NAME=\"" + ICPOLine.Paramsglexpenseacct + "\"" + ">";
				//Add one for the 'blank':
				s += "<OPTION VALUE=\"" + "" + "\"";
				if (entry.getsglexpenseacct().compareToIgnoreCase("") == 0){
					s += " selected=YES ";
				}
				s += ">** Choose an expense account **";
				s += "</OPTION>";
				int i = 0;
				try {
					for (i = 0; i < m_sGLValues.size(); i++){
						s += "<OPTION";
						if (m_sGLValues.get(i).compareToIgnoreCase(entry.getsglexpenseacct()) == 0){
							s += " selected=YES ";
						}
						s += " VALUE=\"" + m_sGLValues.get(i).toString() + "\">" + m_sGLDescriptions.get(i).toString();
						s += "</OPTION>";
					}
				} catch (Exception e) {
					System.out.println("Error [1389641947] - i = " + i + ", m_sGLValues.size = " + m_sGLValues.size() 
						+ ", m_sGLDescriptions.size = " + m_sGLDescriptions.size() + " - " + e.getMessage());
				}
	        	s += "</SELECT>";
	        	
		        s += "</TD>";
		        s += "<TD>";
				s += "<INPUT TYPE=SUBMIT NAME='" + "FINDGLEXPENSEACCT" + "'" 
					+ " VALUE='Find GL expense account'" 
					+ " STYLE='height: 0.24in'>"
				;
			}else{
				s += entry.getsglexpenseacct().replace("\"", "&quot;")
				+ "<INPUT TYPE=HIDDEN NAME=\"" 
				+ ICPOLine.Paramsglexpenseacct
				+ "\" VALUE=\"" + entry.getsglexpenseacct() + "\">";
			}
			s += "</TD><TD>&nbsp;</TD>";
			s += "</TR>";
		//}else{
		//	s += "<INPUT TYPE=HIDDEN NAME=\"" 
		//	+ ICPOLine.Paramsglexpenseacct
		//	+ "\" VALUE=\"" + entry.getsglexpenseacct() + "\">";
		//}
		//New Row:
		//Instructions:
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; text-align:right; font-weight:bold; \">Instructions:</TD>";
		s += "<TD colspan=3>";
		if (
				(entry.getstatus() == SMTableicpolines.STATUS_ENTERED)
				|| (entry.getstatus() == SMTableicpolines.STATUS_PARTIALLY_RECEIVED)
			){
			s += "<TEXTAREA NAME=\"" + ICPOLine.Paramsinstructions + "\""
	        + " rows=\"3\""
	        + " cols=\"80\""
			+ ">"
        	+ entry.getsinstructions().replace("\"", "&quot;")
        	+ "</TEXTAREA>";
		}else{
			s += entry.getsinstructions().replace("\"", "&quot;")
			+ "<INPUT TYPE=HIDDEN NAME=\"" 
			+ ICPOLine.Paramsinstructions
			+ "\" VALUE=\"" + entry.getsinstructions() + "\">";
		}
        s += "</TD>";
		s += "</TR>";

		//New Row:
		//Embedded table for the qty and cost data:
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; text-align:center; \" colspan=4>";
		s += "<TABLE style=\" border-style:none; border-color:black; font-size:small; width:100%\">";

			s += "<TR>";

			//Quantity received:
			s += "<TD style=\" text-align:right; font-weight:bold; \">Qty received:</TD>";
			s += "<TD>"
				+ entry.getsqtyreceived().replace("\"", "&quot;")
				+ "<INPUT TYPE=HIDDEN NAME=\"" 
				+ ICPOLine.Parambdqtyreceived
				+ "\" VALUE=\"" + entry.getsqtyreceived() + "\">"
				+ "</TD>"
			;

			//Extended received cost:
			s += "<TD style=\" text-align:right; font-weight:bold; \">Total received cost:</TD>";
			s += "<TD>"
				+ entry.getsextendedreceivedcost().replace("\"", "&quot;")
				+ "<INPUT TYPE=HIDDEN NAME=\"" 
				+ ICPOLine.Parambdextendedreceivedcost
				+ "\" VALUE=\"" + entry.getsextendedreceivedcost() + "\">"
				+ "</TD>"
			;

			s += "</TR>";
		
		s += "</TABLE>";
		s += "</TD>";
		s += "</TR>";
		//End of embedded table
		
		//Validate button:
		s += "<TR>";
		s += "<TD style=\" vertical-align:top; text-align:center; \" colspan=4>"
			+ "<INPUT TYPE=SUBMIT NAME='" + "VALIDATE" + "'" 
				+ " VALUE='Validate this line before updating'" 
				+ " STYLE='height: 0.24in'>"
				;
			if (entry.getsnoninventoryitem().compareToIgnoreCase("1") != 0){
				s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + ADD_ITEM_TO_ORDER_BUTTON + "'" 
				+ " VALUE='" + ADD_ITEM_TO_ORDER_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				;
			}
			s += "</TD>";
		s += "</TR>";
		
		s += "</TABLE>";
		
		//if (!bIsStockInventoryItem){
		s += "<BR>";
		s += "<SUP>1</SUP><B>Expense account</B>:&nbsp;This expense account is ONLY used for <I><B>'NON-INVENTORY'</B></I> (for example, office supplies, small tools, etc.) or <I><B>NON-STOCK</B></I> inventory (inventory items which are expensed)."
			+ " <BR><I><B>STOCK</B></I> inventory will use the Payables Clearing account in the appropriate inventory location as the 'Expense' account."
			+ "<BR>"
		;
		//}
		
		s += "<BR><INPUT TYPE=SUBMIT NAME='" + SMMasterEditEntry.SUBMIT_EDIT_BUTTON_NAME + "'" 
			+ " VALUE='" + UPDATE_LABEL + "'" 
			+ " STYLE='height: 0.24in'>"
		;
		s += "&nbsp;<INPUT TYPE=SUBMIT NAME='" + UPDATEANDADD_BUTTON + "'" 
			+ " VALUE='" + UPDATEANDADD_LABEL + "'" 
			+ " STYLE='height: 0.24in'>"
		;
		
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

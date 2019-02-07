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

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicphysicalcountlines;
import SMDataDefinition.SMTableicphysicalcounts;
import SMDataDefinition.SMTableicphysicalinventories;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICEditPhysicalCountLine extends HttpServlet {

	public static final String PARAM_INCLUDE_NEW_ITEMS = "ADDNEWITEMS";
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ICPhysicalCountLineEntry entry = new ICPhysicalCountLineEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(request, response,
				getServletContext(),
				entry.getObjectName(), SMUtilities.getFullClassName(this
						.toString()), "smic.ICEditPhysicalCountLineAction",
				"smcontrolpanel.SMUserLogin", "Go back to user login",
				SMSystemFunctions.ICEditPhysicalInventory);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)) {
			smedit.getPWOut().println(
					"Error in process session: " + smedit.getErrorMessages());
			return;
		}

		if (smedit.getAddingNewEntryFlag()) {
			entry.slid("-1");
		}

		// If this is a 'resubmit', meaning it's being called by the 'Action'
		// class, then
		// the session will have an entry object in it, and that's what
		// we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();

		if (currentSession.getAttribute(ICPhysicalCountLineEntry.ParamObjectName) != null) {
			entry = (ICPhysicalCountLineEntry) currentSession
					.getAttribute(ICPhysicalCountLineEntry.ParamObjectName);
			currentSession.removeAttribute(ICPhysicalCountLineEntry.ParamObjectName);
			// But if it's NOT a 'resubmit', meaning this class was called for
			// the first time to
			// edit, we'll pick up the ID or key from the request and try to
			// load the entry:
		} else {
			if (!smedit.getAddingNewEntryFlag()) {
				if (!entry.load(getServletContext(), smedit.getsDBID(),
						smedit.getUserID(),
						smedit.getFullUserName()
						
						)) {
					response.sendRedirect("" + SMUtilities.getURLLinkBase(getServletContext()) + ""
							+ smedit.getCallingClass() + "?"
							+ ICPhysicalCountEntry.ParamID + "="
							+ entry.getCountID() + "&Warning="
							+ entry.getErrorMessages() + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "="
							+ smedit.getsDBID());
					return;
				}
			}
		}
		smedit.printHeaderTable();

		// Add a link to the inventory menu:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "="
						+ smedit.getsDBID()
						+ "\">Return to Inventory Control Main Menu</A><BR>");

		// Add a link to physical inventory list:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICListPhysicalInventories?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "="
						+ smedit.getsDBID()
						+ "\">Return to physical inventory list</A><BR>");

		// Add a link to parent physical inventory:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory" + "?"
						+ ICPhysicalCountLineEntry.ParamID + "="
						+ entry.getPhysicalInventoryID() + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "="
						+ smedit.getsDBID()
						+ "\">Return to physical inventory</A><BR>");

		// Add a link to parent physical count:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCount" + "?"
						+ ICPhysicalCountLineEntry.ParamPhysicalInventoryID + "=" + entry.getPhysicalInventoryID() + "&"
						+ ICPhysicalCountEntry.ParamID + "=" + entry.getCountID() 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "\">Return to physical count</A><BR>");

		// Add a link to return to the original URL:
		if (smedit.getOriginalURL().trim().compareToIgnoreCase("") != 0) {
			smedit.getPWOut().println(
					"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&")
							+ "\">" + "Back to report" + "</A>");
		}

		smedit.getPWOut().println("<BR>");

		boolean bEditable = getPhysInvEditable(
				entry.getPhysicalInventoryID(),
				smedit.getsDBID(), 
				smedit.getUserName()
		);
		
		//Load the GL accts and locations:
		ArrayList<String> m_sGLAccountValues = new ArrayList<String>();
		ArrayList<String> m_sGLAccountDescriptions = new ArrayList<String>();
        m_sGLAccountValues.clear();
        m_sGLAccountDescriptions.clear();
        try{
	        String sSQL = "SELECT "
	        	+ SMTableglaccounts.sAcctID
	        	+ ", " + SMTableglaccounts.sDesc
	        	+ " FROM " + SMTableglaccounts.TableName
	        	+ " WHERE ("
        			+ SMTableglaccounts.lActive + " = 1"
        		+ ")"
	        	+ " ORDER BY " + SMTableglaccounts.sAcctID;

	        ResultSet rsGLAccounts = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	smedit.getsDBID(),
		        	"MySQL",
		        	this.toString() + ".loadGLAccountList (1) - User: " + smedit.getUserName());
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rsGLAccounts.next()){
	        	m_sGLAccountValues.add((String) rsGLAccounts.getString(SMTableglaccounts.sAcctID).trim());
	        	m_sGLAccountDescriptions.add(
	        		(String) rsGLAccounts.getString(SMTableglaccounts.sAcctID).trim() 
	        			+ " - " + (String) rsGLAccounts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccounts.close();

		}catch (SQLException ex){
			String sError = "Could not load GL accounts.";
			response.sendRedirect("" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass() + "?"
					+ ICPhysicalCountLineEntry.ParamID + "=" + entry.slid()
					+ "&Warning=" + sError
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID());
			return;
		}
		
		try {
			smedit.createEditPage(getEditHTML(smedit, entry, bEditable), "");
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect("" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass() + "?"
					+ ICPhysicalCountLineEntry.ParamID + "=" + entry.slid()
					+ "&Warning=Could not load count line ID: " + entry.slid()
					+ sError + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID());
			return;
		}

		return;
	}
	private boolean getPhysInvEditable(
			String sPhysicalInventoryID,
			String sDBID,
			String sUser
	){
		
		boolean bEditable = false;
		String SQL = "SELECT "
			+ SMTableicphysicalinventories.istatus
			+ " FROM " + SMTableicphysicalinventories.TableName
			+ " WHERE ("
				+ SMTableicphysicalinventories.lid + " = '" + sPhysicalInventoryID + "'"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sDBID, "MySQL", this.toString()
							+ ".getEditHTML, user: " + sUser);
			if (rs.next()) {
				bEditable = (rs.getInt(SMTableicphysicalinventories.istatus) 
						== SMTableicphysicalinventories.STATUS_ENTERED);
			}
			rs.close();
		} catch (SQLException e) {
			// don't need to do anything here
		}
		return bEditable;

	}
	private String getEditHTML(SMMasterEditEntry sm,
			ICPhysicalCountLineEntry entry,
			boolean bEditable
	) throws SQLException {

		String s = "";
		
		//Store the calling class in a hidden variable here:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter("CallingClass", sm.getRequest()) + "\""
			+ "\">";
		
		s += "<TABLE BORDER=1>";

		String sID = "";
		if (entry.slid().compareToIgnoreCase("-1") == 0) {
			sID = "NEW";
		} else {
			sID = entry.slid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Physical count line ID</B>:</TD>" + "<TD>"
				+ "<B>" + sID + "</B>" + "<INPUT TYPE=HIDDEN NAME=\""
				+ ICPhysicalCountLineEntry.ParamID + "\" VALUE=\""
				+ entry.slid() + "\">" + "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>";

		String SQL = "SELECT * FROM " + SMTableicphysicalinventories.TableName
				+ " WHERE (" + SMTableicphysicalinventories.lid + " = "
				+ entry.getPhysicalInventoryID() + ")";

		String sPhysicalInvDesc = "";
		try {
			ResultSet rs = clsDatabaseFunctions
					.openResultSet(
							SQL,
							getServletContext(),
							sm.getsDBID(),
							"MySQL",
							SMUtilities.getFullClassName(this.toString())
									+ ".getEditHTML - selecting physical inventory description - user: "
									+ sm.getUserName());
			if (rs.next()) {
				sPhysicalInvDesc = rs
						.getString(SMTableicphysicalinventories.sdesc);
			}
			rs.close();
		} catch (Exception e1) {
			// Don't need to do anything here:
		}

		s += "<TR><TD ALIGN=RIGHT><B>Physical inventory</B>:</TD>" + "<TD>"
				+ "<B>" + entry.getPhysicalInventoryID() + "&nbsp;-&nbsp;"
				+ sPhysicalInvDesc + "</B>" + "<INPUT TYPE=HIDDEN NAME=\""
				+ ICPhysicalCountLineEntry.ParamPhysicalInventoryID
				+ "\" VALUE=\"" + entry.getPhysicalInventoryID() + "\">"
				+ "</TD>"
				 + "<TD>&nbsp;</TD>"
				+ "</TR>";

		SQL = "SELECT * FROM " + SMTableicphysicalcounts.TableName + " WHERE ("
				+ SMTableicphysicalcounts.lid + " = " + entry.getCountID()
				+ ")";

		String sPhysicalCountDesc = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					sm.getsDBID(), "MySQL", SMUtilities
							.getFullClassName(this.toString())
							+ ".getEditHTML - selecting count desc - user: "
							+ sm.getUserName());
			if (rs.next()) {
				sPhysicalCountDesc = rs
						.getString(SMTableicphysicalcounts.sdesc);
			}
			rs.close();
		} catch (Exception e1) {
			// Don't need to do anything here:
		}

		s += "<TR><TD ALIGN=RIGHT><B>Physical count</B>:</TD>" + "<TD>" + "<B>"
				+ entry.getCountID() + "&nbsp;-&nbsp;" + sPhysicalCountDesc
				+ "</B>" + "<INPUT TYPE=HIDDEN NAME=\""
				+ ICPhysicalCountLineEntry.ParamCountID + "\" VALUE=\""
				+ entry.getCountID() + "\">" + "</TD>"
				 + "<TD>&nbsp;</TD>"
				+ "</TR>";

		// Created date
		s += "<TR><TD ALIGN=RIGHT><B>Created on</B>:</TD>" + "<TD>" + "<B>"
				+ entry.getdatCreated() + "</B>" + "<INPUT TYPE=HIDDEN NAME=\""
				+ ICPhysicalCountLineEntry.ParamdatCreated + "\" VALUE=\""
				+ entry.getdatCreated() + "\">" + "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>";

		if (bEditable){
	        	        
	        //Qty:
			s += "<TR><TD ALIGN=RIGHT><B>Quantity counted</B>:</TD>";
			s += "<TD>";
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPhysicalCountLineEntry.ParamQty
					+ "\"" + " VALUE=\""
					+ entry.getsQty().replace("\"", "&quot;") + "\""
					+ " MAXLENGTH="
					+ "14"
					+ " SIZE=30" + ">"
					+ "<TD>&nbsp;</TD>"
					+ "</TR>"
			;
	        
	        //Item number:
			s += "<TR><TD ALIGN=RIGHT><B>Item number</B>:</TD>";
			s += "<TD>";
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPhysicalCountLineEntry.ParamItemNumber
					+ "\"" + " VALUE=\""
					+ entry.getsItemNumber().replace("\"", "&quot;") + "\""
					+ " MAXLENGTH="
					+ Integer.toString(SMTableicphysicalcountlines.sitemnumberLength)
					+ " SIZE=50" + ">"					
					+ "<TD>&nbsp;</TD>"
					+ "</TR>"
			;
			
		    //Checkbox to indicate if user wants items ADDED to the physical inventory:
		    s += "  <TR>" + "\n";
		    s += "    <TD ALIGN=RIGHT>";
		    s += "<B>Add items to this physical inventory?</B>";
		    s += "</TD>" + "\n";
		    s += "    <TD>";
		    s += "<INPUT TYPE=CHECKBOX NAME=" + "\"" + PARAM_INCLUDE_NEW_ITEMS + "\" " + "" + " width=0.25>"; 
		    s += "</TD>" + "\n";
		    s += "    <TD>"
		        + "<I>Check this if the count you are importing has items which are NOT YET in the physical inventory worksheet,"
		    	+ " but you want to have them added.  Quantities on hand for these items will be AS OF this import.</I>"
		    	+ "</TD>" + "\n"
		    	+ "<TD>&nbsp;</TD>"
		    	+ "  </TR>" + "\n";
		}else{

			//Qty
			s += "<TR><TD ALIGN=RIGHT><B>Quantity counted</B>:</TD>" + "<TD>"
			+ "<B>" + entry.getsQty() + "</B>" + "<INPUT TYPE=HIDDEN NAME=\""
			+ ICPhysicalCountLineEntry.ParamQty + "\" VALUE=\""
			+ entry.getsQty() + "\">" + "</TD>"
			+ "<TD>&nbsp;</TD>"
			+ "</TR>";			
			
			//Item number
			s += "<TR><TD ALIGN=RIGHT><B>Item number</B>:</TD>" + "<TD>"
			+ "<B>" + entry.getsItemNumber() + "</B>" + "<INPUT TYPE=HIDDEN NAME=\""
			+ ICPhysicalCountLineEntry.ParamItemNumber + "\" VALUE=\""
			+ entry.getsItemNumber() + "\">" + "</TD>"
			+ "<TD>&nbsp;</TD>"
			+ "</TR>";			

		}
		s += "</TABLE>";
		return s;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}
}

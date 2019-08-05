package smic;

import java.io.IOException;
import java.io.PrintWriter;
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
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicphysicalcountlines;
import SMDataDefinition.SMTableicphysicalcounts;
import SMDataDefinition.SMTableicphysicalinventories;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICEditPhysicalCount  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		ICPhysicalCountEntry entry = new ICPhysicalCountEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEditPhysicalCountAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditPhysicalInventory
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		if (smedit.getAddingNewEntryFlag()){
			entry.slid("-1");
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(ICPhysicalCountEntry.ParamObjectName) != null){
	    	entry = (ICPhysicalCountEntry) currentSession.getAttribute(ICPhysicalCountEntry.ParamObjectName);
	    	currentSession.removeAttribute(ICPhysicalCountEntry.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
		    	}
	    	}
	    }
	    smedit.printHeaderTable();
	    
	    //Add a link to the inventory menu:
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ smedit.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
	    
	    //Add a link to physical inventory list:
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICListPhysicalInventories?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ smedit.getsDBID() + "\">Return to physical inventory list</A><BR>");
	    
	    //Add a link to parent physical inventory:
	    if (
	    		(entry.getPhysicalInventoryID().compareToIgnoreCase("") != 0)
	    		&& (entry.getPhysicalInventoryID().compareToIgnoreCase("-1") != 0)
	    ){
		    smedit.getPWOut().println(
		    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory"
		    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + entry.getPhysicalInventoryID()
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
		    		+ "\">Return to physical inventory</A><BR>");
	    }
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println("<BR>");

		boolean bEditable = getPhysInvEditable(
				entry.getPhysicalInventoryID(),
				smedit.getsDBID(), 
				smedit.getUserID(),
				smedit.getFullUserName()
		);
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry, bEditable), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPhysicalCountEntry.ParamID + "=" + entry.slid()
				+ "&Warning=Could not load count ID: " + entry.slid() + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		
	    if (
		    		(entry.slid().compareToIgnoreCase("") != 0)
		    		&& (entry.slid().compareToIgnoreCase("-1") != 0)
		    ){
			createCountLinesList(
					bEditable,
					entry.getPhysicalInventoryID(),
					entry.slid(), 
					getServletContext(), 
					smedit.getsDBID(), 
					smedit.getUserID(),
					smedit.getFullUserName(),
					smedit.getPWOut()
			);
	    }
	    return;
	}
	private boolean getPhysInvEditable(
			String sPhysicalInventoryID,
			String sDBID,
			String sUserID,
			String sUserFullName
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
							+ ".getEditHTML, user: " 
							+ sUserID
							+ " - "
							+ sUserFullName
					);
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
	private String getEditHTML(SMMasterEditEntry sm, ICPhysicalCountEntry entry, boolean bEditable)
		throws SQLException{

		String s = "";

		//Store the calling class in a hidden variable here:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter("CallingClass", sm.getRequest()) + "\""
			+ "\">";
		
		s += "<TABLE BORDER=1>";
		
		String sID = "";
		if (entry.slid().compareToIgnoreCase("-1") == 0){
			sID = "NEW";
		}else{
			sID = entry.slid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Physical count ID</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + sID + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalCountEntry.ParamID + "\" VALUE=\"" 
				+ entry.slid() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
		String SQL = "SELECT * FROM " + SMTableicphysicalinventories.TableName
			+ " WHERE ("
				+ SMTableicphysicalinventories.lid + " = " + entry.getPhysicalInventoryID()
			+ ")"
			;
		
		String sPhysicalInvDesc = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(),
				sm.getsDBID(),
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".getEditHTML - user: " 
				+ sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
				);
			if (rs.next()){
				sPhysicalInvDesc = rs.getString(SMTableicphysicalinventories.sdesc);
			}
			rs.close();
		} catch (Exception e1) {
			// Don't need to do anything here:
		}
		
		s += "<TR><TD ALIGN=RIGHT><B>Physical inventory</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + entry.getPhysicalInventoryID() + "&nbsp;-&nbsp;" + sPhysicalInvDesc + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalCountEntry.ParamPhysicalInventoryID + "\" VALUE=\"" 
				+ entry.getPhysicalInventoryID() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
		//Description
		//We can only edit the description if the status is NOT 'Batched', because once a physical count
		// has been turned into a batch, it can't be edited:
		if (!bEditable){
			s += "<TR><TD ALIGN=RIGHT><B>Description</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getDescription() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalCountEntry.ParamDesc + "\" VALUE=\"" 
					+ entry.getDescription() + "\">"
				+ "</TD>"
				//+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		}else{
			s += "<TR><TD ALIGN=RIGHT><B>Description</B>:</TD>";
			s += "<TD>";
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPhysicalCountEntry.ParamDesc + "\""
				+ " VALUE=\"" + entry.getDescription().replace("\"", "&quot;") + "\""
				+ " MAXLENGTH=" + Integer.toString(SMTableicphysicalcounts.sdescLength)
				+ " SIZE=50"
				+ "></TD></TR>";
		}
		
		//Created by
		s += "<TR><TD ALIGN=RIGHT><B>Created by</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + entry.getsCreatedByFullName() + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalCountEntry.ParamCreatedByFullName + "\" VALUE=\"" 
				+ entry.getsCreatedByFullName() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalCountEntry.ParamCreatedByID + "\" VALUE=\"" 
					+ entry.getsCreatedByID() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
		//Created date
		s += "<TR><TD ALIGN=RIGHT><B>Created on</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + entry.getdatCreated() + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalCountEntry.ParamdatCreated + "\" VALUE=\"" 
				+ entry.getdatCreated() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;

		s += "</TABLE>";
		return s;
	}
	private void createCountLinesList(
			boolean bEditable,
			String sPhysicalInventoryID,
			String sPhysicalCountID,
			ServletContext context, 
			String sDBID, 
			String sUserID,
			String sUserFullName,
			PrintWriter out){
		
		if (bEditable){
		    out.println(
		    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountLine"
		    		+ "?" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=true"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + ICPhysicalCountLineEntry.ParamPhysicalInventoryID + "=" + sPhysicalInventoryID
		    		+ "&" + ICPhysicalCountLineEntry.ParamCountID + "=" + sPhysicalCountID
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">Create a new physical count line</A>"
		    );
		}
		
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<TABLE WIDTH=100% BGCOLOR = \"#FFFFFF\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Line #</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Qty</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Item</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Description</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Unit</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Entered</TD>");
		out.println("</TR>");

		
		//List the physical count lines:
		String SQL = "SELECT"
			+ " " + SMTableicphysicalcountlines.lid
			+ ", " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.sitemnumber
			+ ", " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.bdqty
			+ ", " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.datcreated
			+ ", " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.sunitofmeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ " FROM " + SMTableicphysicalcountlines.TableName + ", " + SMTableicitems.TableName
			+ " WHERE ("
				+ SMTableicphysicalcountlines.TableName + "." 
					+ SMTableicphysicalcountlines.lcountid + " = " + sPhysicalCountID
				+ " AND (" + SMTableicphysicalcountlines.TableName + "." 
					+ SMTableicphysicalcountlines.sitemnumber 
					+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
			+ ")"
			+ " ORDER BY " + SMTableicphysicalcountlines.TableName + "." 
				+ SMTableicphysicalcountlines.lid + " ASC"
		;
		//System.out.println("In " + this.toString() + ".createCountLinesList - SQL = " + SQL);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".createCountLinesList - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
			);
			
			String sCountLineLink = "";
			int iLineNumber = 0;
			while (rs.next()){
				if(iLineNumber % 2 == 0) {
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
				iLineNumber++;
				sCountLineLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountLine"
		    		+ "?" + ICPhysicalCountLineEntry.ParamID + "=" 
		    		+ rs.getLong(SMTableicphysicalcountlines.lid)
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">" + iLineNumber + "</A>";
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sCountLineLink + "</TD>");
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalToFormattedString(
						"###,###,##0.0000", rs.getBigDecimal(SMTableicphysicalcountlines.bdqty))  + "</TD>");
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableicphysicalcountlines.sitemnumber)  + "</TD>");
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableicitems.sItemDescription) + "</TD>");
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableicphysicalcountlines.sunitofmeasure)  + "</TD>");
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicphysicalcounts.datcreated))  + "</TD>");
				out.println("</TR>");
			}
			rs.close();
		} catch (SQLException e) {
			out.println("Error reading physical count lines - " + e.getMessage());
			return;
		}
		out.println("</TABLE>");
		if (bEditable){
		    out.println(
		    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountLine"
		    		+ "?" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=true"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + ICPhysicalCountLineEntry.ParamPhysicalInventoryID + "=" + sPhysicalInventoryID
		    		+ "&" + ICPhysicalCountLineEntry.ParamCountID + "=" + sPhysicalCountID
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">Create a new physical count line</A>"
		    );
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

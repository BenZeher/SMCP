package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
import SMDataDefinition.SMTableicphysicalcounts;
import SMDataDefinition.SMTableicphysicalinventories;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ICEditPhysicalInventory  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		ICPhysicalInventoryEntry entry = new ICPhysicalInventoryEntry(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smic.ICEditPhysicalInventoryAction",
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
		
	    if (currentSession.getAttribute(ICPhysicalInventoryEntry.ParamObjectName) != null){
	    	entry = (ICPhysicalInventoryEntry) currentSession.getAttribute(ICPhysicalInventoryEntry.ParamObjectName);
	    	currentSession.removeAttribute(ICPhysicalInventoryEntry.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(),
		    			smedit.getsDBID(),
		    			smedit.getUserID(),
		    			smedit.getFullUserName())){
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
	    
	    //Add a link to physical count list:
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    	+ "smic.ICListPhysicalInventories?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ smedit.getsDBID() + "\">Return to Physical inventory list</A><BR>");
	    
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    if (
	    		(entry.slid().compareToIgnoreCase("") != 0)
	    		&& (entry.slid().compareToIgnoreCase("-1") != 0)
	    ){
			if (entry.getStatus().compareToIgnoreCase(
					Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) == 0){
		    	createLinkToVarianceReport(entry.slid(), smedit.getsDBID(), smedit.getPWOut());
		    	createLinkToCountWorksheet(smedit.getsDBID(), entry, smedit.getPWOut());
			}
	    }
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
				+ "&Warning=Could not load entry ID: " + entry.slid() + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		
		//Only show the links to create or import a new count if it's an editable physical inventory:
	    if (
	    		(entry.slid().compareToIgnoreCase("") != 0)
	    		&& (entry.slid().compareToIgnoreCase("-1") != 0)
	    ){
			if (entry.getStatus().compareToIgnoreCase(
					Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) == 0){
				smedit.getPWOut().println(
			    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICAddRemovePhysInvItemsEdit"
			    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
			    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			    		+ "\">Add or remove items from physical inventory worksheet</A>"
			    );
			}
			createCountList(
					entry.slid(),
					entry.getLocation(),
					entry.getStatus(),
					getServletContext(), 
					smedit.getsDBID(), 
					smedit.getUserName(), 
					smedit.getPWOut()
			);
	    }
	    
	    //If it's an editable physical inventory, create a form for posting the physical inventory:
	    if (entry.getStatus().compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) == 0){
			smedit.getPWOut().println(
					"<FORM NAME='POSTFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
					+ "smic.ICPostPhysicalInventory" + "' METHOD='POST'>"
					+ "<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + smedit.getsDBID() + "'>"
					+ "<INPUT TYPE=HIDDEN NAME='" + ICPhysicalInventoryEntry.ParamID 
						+ "' VALUE='" + entry.slid() + "'>"
					+ "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
							+ SMUtilities.getFullClassName(this.toString()) + "\">"
					+ "<INPUT TYPE=HIDDEN NAME=\"" + "OriginalURL" + "\" VALUE=\"" 
							+ "" + "\">"
					+ "<P><INPUT TYPE=SUBMIT NAME='" + "PostPhysicalInventory" 
							+ "' VALUE='Post physical inventory' STYLE='height: 0.24in'>"
					+ "  Check to confirm posting: <INPUT TYPE=CHECKBOX NAME=\"" 
							+ "ConfirmPosting" + "\"></P>"
					+ "</FORM>"
			);
	    }
	    return;
	}
	private void createLinkToVarianceReport(String sPhysicalInventoryID, String sDBID, PrintWriter pwOut){
		
		pwOut.println(
    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
    		+ "&OnlyShowVariances=no"
    		+ "&Summary=no"
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		+ "\">Variance report - all items</A>"		
		);
		
		pwOut.println(
	    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
	    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
	    		+ "&OnlyShowVariances=yes"
	    		+ "&Summary=no"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Variance report - variances only</A>"		
		);
		
		pwOut.println(
	    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
	    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
	    		+ "&OnlyShowVariances=no"
	    		+ "&Summary=yes"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Variance report summary - all items</A>"		
		);
		
		pwOut.println(
	    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
	    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
	    		+ "&OnlyShowVariances=yes"
	    		+ "&Summary=yes"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Variance report summary - variances only</A>"		
		);
		
		pwOut.println(
	    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
	    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
	    		+ "&OnlyShowVariances=yes"
	    		+ "&Summary=no"
	    		+ "&OnlyInactive=yes"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Variance report - inactive items</A>"		
		);
		
	}
	private void createLinkToCountWorksheet(
			String sDBID,
			ICPhysicalInventoryEntry entry,
			PrintWriter pwOut
			){
		
		pwOut.println(
    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryWorksheet"
    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
    		+ "&" + "WorksheetLocation=" + entry.getLocation()
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		+ "\">Print physical count worksheet</A>"		
		);
	}

	private String getEditHTML(SMMasterEditEntry sm, ICPhysicalInventoryEntry entry) throws SQLException{

		String s = "";
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
			        	sm.getsDBID(),
			        	"MySQL",
			        	this.toString() + ".loadLocationList (1) - User: " + sm.getUserName());
		        
				//Print out directly so that we don't waste time appending to string buffers:
		        while (rsLocations.next()){
		        	m_sLocationValues.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
		        	m_sLocationDescriptions.add(
		        		(String) rsLocations.getString(SMTablelocations.sLocation).trim() 
		        			+ " - " + (String) rsLocations.getString(SMTablelocations.sLocationDescription).trim());
				}
		        rsLocations.close();

			}catch (SQLException ex){
				s += "Could not load locations.";
				return s;
			}
		
		s += "<TABLE BORDER=1>";
		
		String sID = "";
		if (entry.slid().compareToIgnoreCase("-1") == 0){
			sID = "NEW";
		}else{
			sID = entry.slid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Physical inventory ID</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + sID + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamID + "\" VALUE=\"" 
				+ entry.slid() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
		//Description
		//We can only edit the description if the status is NOT 'Batched', OR if it's a NEW entry,
		//because once a physical count has been turned into a batch, it can't be edited:
		if (entry.getStatus().compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) != 0){
			s += "<TR><TD ALIGN=RIGHT><B>Description</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getDescription() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamDesc + "\" VALUE=\"" 
					+ entry.getDescription() + "\">"
				+ "</TD>"
				//+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		}else{
			s += "<TR><TD ALIGN=RIGHT><B>Description</B>:</TD>";
			s += "<TD>";
			s += "<INPUT TYPE=TEXT NAME=\"" + ICPhysicalInventoryEntry.ParamDesc + "\""
				+ " VALUE=\"" + entry.getDescription().replace("\"", "&quot;") + "\""
				+ " MAXLENGTH=" + Integer.toString(SMTableicphysicalinventories.sdescLength)
				+ " SIZE=50"
				+ ">"
				+ "</TD></TR>"
				;
		}

		//If it's NOT a new batch, we can't edit the starting and ending locations and item numbers:
		if (entry.slid().compareToIgnoreCase("-1") != 0){
			//Location
			s += "<TR><TD ALIGN=RIGHT><B>Location</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getLocation() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamLocation + "\" VALUE=\"" 
					+ entry.getLocation() + "\">"
				+ "</TD>"
				//+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
	    	
	    }else{
			//location
			s += "<TR><TD ALIGN=RIGHT><B>Location</B>:</TD>" + "<TD>";
			s += "<SELECT NAME = \"" + ICPhysicalInventoryEntry.ParamLocation + "\">";
			//add the first line as a default, so we can tell if they didn't pick one:
	    	s += "<OPTION";
			s += " VALUE=\"" + "" + "\">";
			s += " - Select a location - ";
	    	
	        //Read out the array list:
	        for (int iLocationCount = 0; iLocationCount<m_sLocationValues.size();iLocationCount++){
	        	s += "<OPTION";
				if (m_sLocationValues.get(iLocationCount).toString().compareToIgnoreCase(entry.getLocation()) == 0){
					s += " selected=yes ";
				}
				s += " VALUE=\"" + m_sLocationValues.get(iLocationCount).toString() + "\">";
				s += m_sLocationDescriptions.get(iLocationCount).toString();
	        }
	        s += "</SELECT>";
	        s += "</TD></TR>";
	    	
	    }
		
		//Created by
		String sCreatedBy = entry.getsCreatedByFullName().trim().replace(" ", "&nbsp;");
		
		s += "<TR><TD ALIGN=RIGHT><B>Created by</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + sCreatedBy + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamCreatedByFullName + "\" VALUE=\"" 
				+ entry.getsCreatedByFullName() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
		//Created date
		s += "<TR><TD ALIGN=RIGHT><B>Created on</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + entry.getdatCreated() + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamdatCreated + "\" VALUE=\"" 
				+ entry.getdatCreated() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
    	//Status
		s += "<TR><TD ALIGN=RIGHT><B>Status</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + SMTableicphysicalinventories.getStatusDescription(Integer.parseInt(entry.getStatus()))
				+ "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamiStatus + "\" VALUE=\"" 
				+ entry.getStatus() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;
		
		//Batchnumber
		String sBatchNumber = "(Not yet processed)";
		if (entry.getsBatchNumber().compareToIgnoreCase("0") != 0){
			sBatchNumber = entry.getsBatchNumber();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Batchnumber</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + sBatchNumber
				+ "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ICPhysicalInventoryEntry.ParamlBatchnumber + "\" VALUE=\"" 
				+ entry.getsBatchNumber() + "\">"
			+ "</TD>"
			//+ "<TD>&nbsp;</TD>"
			+ "</TR>"
			;

		s += "</TABLE>";
		return s;
	}
	
	private void createCountList(
			String sPhysicalInventoryID,
			String sLocation,
			String sPhysicalInventoryStatus,
			ServletContext context, 
			String sDBID, 
			String sUser,
			PrintWriter out){
		
		//List entry counts here:
		out.println(
			"<TABLE BORDER=1><TR>"
			+ "<TD><B><FONT SIZE=2>Count #</FONT></B></TD>"
			+ "<TD><B><FONT SIZE=2>ID</FONT></B></TD>"
			+ "<TD><B><FONT SIZE=2>Created</FONT></B></TD>"
			+ "<TD><B><FONT SIZE=2>Created by</FONT></B></TD>"
			+ "<TD><B><FONT SIZE=2>Description</FONT></B></TD>"
			+ "</TR>"
		);
		
		//List the physical counts:
		String SQL = "SELECT * FROM "
			+ SMTableicphysicalcounts.TableName
			+ " WHERE ("
				+ SMTableicphysicalcounts.lphysicalinventoryid + " = " + sPhysicalInventoryID
			+ ")"
			+ " ORDER BY " + SMTableicphysicalcounts.lid + " ASC"
		;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".createCountList - user: " + sUser
			);
			
			String sCountLink = "";
			int iCountNumber = 0;
			while (rs.next()){
				out.println("<TR>");
				iCountNumber++;
				sCountLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCount"
		    		+ "?" + ICPhysicalCountEntry.ParamID + "=" + rs.getLong(SMTableicphysicalcounts.lid)
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + ICPhysicalCountEntry.ParamPhysicalInventoryID + "=" + sPhysicalInventoryID 
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">" + iCountNumber + "</A>";
				out.println("<TD><FONT SIZE=2>" + sCountLink + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" + rs.getLong(SMTableicphysicalcounts.lid) + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" + clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableicphysicalcounts.datcreated)) + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" + rs.getString(SMTableicphysicalcounts.screatedbyfullname) + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" + rs.getString(SMTableicphysicalcounts.sdesc) + "</FONT></TD>");
				out.println("</TR>");
			}
			rs.close();
		} catch (SQLException e) {
			out.println("Error reading physical counts - " + e.getMessage());
			return;
		}
		out.println("</TABLE>");
		
		//Only show the links to create or import a new count if it's an editable physical inventory:
		if (sPhysicalInventoryStatus.compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) == 0){
		    out.println(
		    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCount"
		    		+ "?" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=true"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + ICPhysicalCountEntry.ParamPhysicalInventoryID + "=" + sPhysicalInventoryID 
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">Create a new physical count</A>"
		    );
	
		    out.println(
		    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPhysicalCountImportSelect"
		    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
		    		+ "&" + ICPhysicalInventoryEntry.ParamLocation + "=" + sLocation
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    		+ "\">Import a new physical count</A>"
		    );
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

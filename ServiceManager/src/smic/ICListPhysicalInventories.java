package smic;

//import SMDataDefinition.*;
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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicphysicalinventories;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ICListPhysicalInventories extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String title = "Physical inventories.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, "#FFFFFF", sCompanyName));

	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
	    	}
	    }
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventoryEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println("<TABLE WIDTH = 100% BGCOLOR=\""+ sColor +"\">");
	    //Print a link to the first page after login:
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPhysicalInventory) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("</TD>");
	    out.println("</TR>");

	    //Add links to create new physicals:
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory"
	    		+ "?" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=true"
	    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Create a new physical inventory</A>");
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println(
		    	"<B>NOTE:</B> When a new physical inventory is created, the current quantities on hand are recorded in the "
		    	+ "inventory worksheet.  The write off and inventory accounts associated with the location are used"
		    	+ " for ALL the items in the physical inventory."
		    );
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    out.println("</TABLE>");
	    

	    
	    //Build List
	    out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>Physical Inv. #</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>Created</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>Created By</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>Description</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>Location</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>Status</B></TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" ><B>IC Batch Number</B></TD>");
	    out.println("</TR>");
	    
	    try{
	    	String SQL = "SELECT * FROM " 
			+ SMTableicphysicalinventories.TableName
			
			+ " ORDER BY " 
			+ SMTableicphysicalinventories.lid + " DESC";
	    	
	    	//System.out.println("In " + this.toString() + " listing batches SQL = " + SQL);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	SQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserName);
	        
        	while (rs.next()){
        		//Start a row:
        		out.println("<TR>");
        		out.println(
        			Build_Row(		
	        			out,
	        			Long.toString(rs.getLong(SMTableicphysicalinventories.lid)),
	        			clsDateAndTimeConversions.resultsetDateTimeStringToString(
	    					rs.getString(SMTableicphysicalinventories.datcreated)),
	        			rs.getString(SMTableicphysicalinventories.screatedbyfullname),
	        			rs.getString(SMTableicphysicalinventories.sdesc),
	        			rs.getString(SMTableicphysicalinventories.slocation),
	        			//rs.getString(SMTableicphysicalinventories.sstartingitemnumber),
	        			//rs.getString(SMTableicphysicalinventories.sendingitemnumber),
	        			SMTableicphysicalinventories.getStatusDescription(
	        				rs.getInt(SMTableicphysicalinventories.istatus)),
	    				Long.toString(rs.getLong(SMTableicphysicalinventories.lbatchnumber)),
	    				SMUtilities.getFullClassName(this.toString()),
	        			request,
	        			getServletContext(),
	        			sDBID
        			)
        		);
        		//End the row:
        		out.println("<TR>");
        	}
        	rs.close();
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		out.println("<TABLE BORDER=0 CELLSPACING=2>");
		
		//***************************************
		out.println("</BODY></HTML>");
	}

	private static String Build_Row (
			PrintWriter pwout,
			String sPhysInvNumber,
			String sDateCreated,
			String sCreatedBy,
			String sDesc,
			String sLocation,
			//String sStartingItemNumber,
			//String sEndingItemNumber,
			String sStatus,
			String sBatchNumber,
			String sCallingClass,
			HttpServletRequest req,
			ServletContext context,
			String sDBID
			){

		String sOutPut = "<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPhysicalInventory" 
	    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysInvNumber
	    		+ "&CallingClass=" + sCallingClass
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ sPhysInvNumber
	    		+ "</A></TD>";
		sOutPut += "<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >" + sDateCreated + "</TD>";
		sOutPut += "<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >" + sCreatedBy + "</TD>";
		sOutPut += "<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >" + sDesc + "</TD>";
		sOutPut += "<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >" + sLocation + "</TD>";
		sOutPut += "<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\" >" + sStatus + "</TD>";

		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\" >" + sBatchNumber + "</TD>";
		
		return sOutPut;

	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
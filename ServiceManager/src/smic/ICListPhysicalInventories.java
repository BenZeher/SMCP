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
import SMDataDefinition.SMTableicphysicalinventories;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ICListPhysicalInventories extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserName = "";
	private static String sCompanyName = "";
	
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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String title = "Physical inventories.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPhysicalInventory) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventoryEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    //Add links to create new physicals:
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalInventory"
	    		+ "?" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=true"
	    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Create a new physical inventory</A>");
	    out.println("</TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");
	    
	    out.println(
	    	"<B>NOTE:</B> When a new physical inventory is created, the current quantities on hand are recorded in the "
	    	+ "inventory worksheet.  The write off and inventory accounts associated with the location are used"
	    	+ " for ALL the items in the physical inventory."
	    );
	    
	    //Build List
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
	    out.println("<TR>");
	    
	    out.println("<TD>");
	    out.println("<B>Physical inv.#</B>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<B>Created</B>");
	    out.println("</TD>");

	    out.println("<TD>");
	    out.println("<B>Created by</B>");
	    out.println("</TD>");

	    out.println("<TD>");
	    out.println("<B>Description</B>");
	    out.println("</TD>");

	    out.println("<TD>");
	    out.println("<B>Location</B>");
	    out.println("</TD>");

	    //out.println("<TD>");
	    //out.println("<B>From item</B>");
	    //out.println("</TD>");

	    //out.println("<TD>");
	    //out.println("<B>To item</B>");
	    //out.println("</TD>");

	    out.println("<TD>");
	    out.println("<B>Status</B>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<B>IC Batchnumber</B>");
	    out.println("</TD>");

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
	        			getServletContext()
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
			ServletContext context
			){

		String sOutPut = "<TD>";
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPhysicalInventory" 
	    		+ "?" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysInvNumber
	    		+ "&CallingClass=" + sCallingClass
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ sPhysInvNumber
	    		+ "</A>";
		sOutPut += "</TD>";
		
		sOutPut += "<TD>";
		sOutPut += sDateCreated;
		sOutPut += "</TD>";
		
		sOutPut += "<TD>";
		sOutPut += sCreatedBy;
		sOutPut += "</TD>";
		
		sOutPut += "<TD>";
		sOutPut += sDesc;
		sOutPut += "</TD>";
		
		sOutPut += "<TD>";
		sOutPut += sLocation;
		sOutPut += "</TD>";
		
		//sOutPut += "<TD>";
		//sOutPut += sStartingItemNumber;
		//sOutPut += "</TD>";
		
		//sOutPut += "<TD>";
		//sOutPut += sEndingItemNumber;
		//sOutPut += "</TD>";

		sOutPut += "<TD>";
		sOutPut += sStatus;
		sOutPut += "</TD>";

		sOutPut += "<TD ALIGN=RIGHT>";
		sOutPut += sBatchNumber;
		sOutPut += "</TD>";
		
		return sOutPut;

	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
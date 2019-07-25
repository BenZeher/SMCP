package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICViewItemPricingSelection  extends HttpServlet {
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
	    String title = "IC View Item Pricing";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItemPricing) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICViewItemPricingGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Starting Item number:
		out.println("<TD ALIGN=RIGHT>" + "<B>View item pricing for items:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					"StartingItemNumber", 
					clsManageRequestParameters.get_Request_Parameter("StartingItemNumber", request), 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					)
		);
		
		//Ending Item number:
		String sEndingItem = clsManageRequestParameters.get_Request_Parameter("EndingItemNumber", request);
		if (sEndingItem.compareToIgnoreCase("") == 0){
			sEndingItem = "ZZZZZZZZZZZZZZZZ";
		}
		out.println("&nbsp;&nbsp;And ending with:"
				+ clsCreateHTMLFormFields.TDTextBox(
					"EndingItemNumber", 
					sEndingItem, 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					));
		
		out.println("</TD></TR>");

    	out.println("<TR><TD ALIGN=RIGHT><B>Starting with price list: </B></TD>");
		out.println("<TD>");
		
		String sStartingPriceCode = clsManageRequestParameters.get_Request_Parameter("StartingPriceCode", request);
		String sEndingPriceCode = clsManageRequestParameters.get_Request_Parameter("EndingPriceCode", request);
		
	    //Add drop down list
		try{
	        String sSQL = "SELECT *" 
	    		+ " FROM " + SMTablepricelistcodes.TableName
	    		+ " ORDER BY CAST(" + SMTablepricelistcodes.spricelistcode + " AS unsigned)";
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserName);
	     	out.println ("<SELECT NAME=\"" + "StartingPriceCode" + "\">" );
        	
	     	String sLastPriceCode = "";
	     	String sOutPut = "";
	     	while (rs.next()){
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTablepricelistcodes.spricelistcode) + "\"";
        		if (sStartingPriceCode.compareToIgnoreCase(rs.getString(SMTablepricelistcodes.spricelistcode)) == 0){
        			sOutPut += " selected ";
        		}
        		out.println(sOutPut
        			+ ">"
        			+ rs.getString(SMTablepricelistcodes.spricelistcode) + " - " 
        			+ rs.getString(SMTablepricelistcodes.sdescription)
        		);        		sLastPriceCode = rs.getString(SMTablepricelistcodes.spricelistcode);
        	}

	        //End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	        
	    	out.println("<TR><TD ALIGN=RIGHT><B>And ending with price list: </B></TD>");
			out.println("<TD>");
	     	out.println ("<SELECT NAME=\"" + "EndingPriceCode" + "\">" );
	     	rs.beforeFirst();
	     	
	     	if (sEndingPriceCode.compareToIgnoreCase("") == 0){
	     		sEndingPriceCode = sLastPriceCode;
	     	}
        	while (rs.next()){
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTablepricelistcodes.spricelistcode) + "\"";
        		if (sEndingPriceCode.compareToIgnoreCase(rs.getString(SMTablepricelistcodes.spricelistcode)) == 0){
        			sOutPut += " selected ";
        		}
        		out.println(sOutPut
        			+ ">"
        			+ rs.getString(SMTablepricelistcodes.spricelistcode) + " - " 
        			+ rs.getString(SMTablepricelistcodes.sdescription)
        		);
        	}

	        //End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
	        
        	rs.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Process report----\">");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

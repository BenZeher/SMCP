package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICItemValuationReportSelection  extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICItemValuationReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Item Valuation";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICItemValuationReport) 
	    		+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICItemValuationReportGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Starting Item number:
		out.println("<TD>" + "<B>List item valuation for items starting at:</B>&nbsp;" 
				+ clsCreateHTMLFormFields.TDTextBox(
					"StartingItemNumber", 
					clsManageRequestParameters.get_Request_Parameter("StartingItemNumber", request), 
					SMTableicitems.sItemNumberLength, 
					SMTableicitems.sItemNumberLength, 
					""
					));

		//Ending Item number:
		String sEndingItemNumber = clsManageRequestParameters.get_Request_Parameter("EndingItemNumber", request);
		if (sEndingItemNumber.compareToIgnoreCase("") == 0){
			sEndingItemNumber = "ZZZZZZZZZZZZZZZZZZZZZZZZ";
		}
		out.println("&nbsp;<B>and ending with:</B>&nbsp;" 
				+ clsCreateHTMLFormFields.TDTextBox(
					"EndingItemNumber", 
					sEndingItemNumber, 
					SMTableicitems.sItemNumberLength, 
					SMTableicitems.sItemNumberLength, 
					""
					));
		
		out.println("</TD></TR>");
		
		out.println("<TR><TD><B>Show individual cost buckets?"
			+ "<INPUT TYPE=CHECKBOX NAME=\"ShowIndividualBuckets\" width=0.25>&nbsp;&nbsp;"
		);
		
		out.println("<B>Show individual location values?"
			+ "<INPUT TYPE=CHECKBOX NAME=\"ShowIndividualLocations\" width=0.25></TD></TR>"
		);
		
		out.println("<TR><TD><B>Including</B>&nbsp;"
			+ "<SELECT NAME=\"IncludingQuantities\">"
			+ "<OPTION VALUE=\"0\">All"
			+ "<OPTION VALUE=\"1\">Only Positive"
			+ "<OPTION VALUE=\"2\">Only Zero"
			+ "<OPTION VALUE=\"3\">Only Negative"
			+ "<OPTION VALUE=\"4\">Non Zero"
	        + "</SELECT>"
	        + "&nbsp;<B>Quantities</B>"
			+ "&nbsp;&nbsp;"
		);

		out.println("<B>AND Including</B>&nbsp;"
				+ "<SELECT NAME=\"IncludingCosts\">"
				+ "<OPTION VALUE=\"0\">All"
				+ "<OPTION VALUE=\"1\">Only Positive"
				+ "<OPTION VALUE=\"2\">Only Zero"
				+ "<OPTION VALUE=\"3\">Only Negative"
				+ "<OPTION VALUE=\"4\">Non Zero"
		        + "</SELECT>"
		        + "&nbsp;<B>Costs</B>"
				+ "</TD></TR>"
			);

		out.println("</TABLE>");
		
		out.println("<B><U>Including locations:</U></B>");
		
		//Add table of locations:
		try{
			String SQL = "SELECT * FROM " + SMTablelocations.TableName
				+ " ORDER BY " + SMTablelocations.sLocation;
						
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(),
				sDBID,
				"MySQL",
				this.toString() + ".doPost - User: " + sUserName
				);
			ArrayList<String> sLocationList = new ArrayList<String>(0);
			while (rs.next()){
				sLocationList.add((String) "<LABEL><INPUT TYPE=CHECKBOX " + " checked=\"Yes\"" 
	        			+ " NAME=\"LOCATION" 
	        			+ rs.getString(SMTablelocations.sLocation) 
	        			+ "\">" 
	        			+ rs.getString(SMTablelocations.sLocation)
	        			+ "&nbsp;" + rs.getString(SMTablelocations.sLocationDescription)
	        			+ "</LABEL>"
	        			);
	    	}
	        rs.close();
	        //out.println(SMUtilities.Build_HTML_Table(5, sSalespersonList,1,true));
	        
	        out.println(SMUtilities.Build_HTML_Table(4, sLocationList, 100, 1, true ,true));
	        
		}catch(SQLException e){
			out.println("<BR>Error reading locations - " + e.getMessage());
		}

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

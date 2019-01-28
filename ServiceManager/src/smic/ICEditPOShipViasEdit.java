
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
import SMDataDefinition.SMTableicshipvias;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class ICEditPOShipViasEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sPOShipViaObjectName = "PO Ship Via";
	private static final String sICEditPOShipViasEditCalledClassName = "ICEditPOShipViasAction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ICEditPOShipVias)){
	    	return;
	    }

	    String sEditCode = (String) clsStringFunctions.filter(request.getParameter(sPOShipViaObjectName));

		String title = "";
		String subtitle = "";
		String sOutPut = "";
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit:
			title = "Edit " + sPOShipViaObjectName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Inventory Main Menu</A><BR>");
		    
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPOShipVias) 
		    		+ "\">Summary</A><BR><BR>");
		    try {
				Edit_Record(sEditCode, out, sDBID, false, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME));
			} catch (Exception e) {
				out.println(e.getMessage());
			}
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete:
			title = "Delete " + sPOShipViaObjectName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Inventory Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPOShipVias) 
		    		+ "\">Summary</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete.");
		    }else{
		    	sOutPut = "PO Ship Via was successfully deleted.";
			    try {
					Delete_Record(sEditCode, out, sDBID, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME));
				} catch (Exception e) {
					sOutPut = e.getMessage();
				}
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sPOShipViaObjectName));
	    	//User has chosen to add a new record:
			title = "Add " + sPOShipViaObjectName + ": " + sNewCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Inventory Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPOShipVias) 
		    		+ "\">Summary</A><BR><BR>");

		    if (sNewCode == ""){
		    	out.println ("You chose to add a new " + sPOShipViaObjectName + ", but you did not enter a new " + sPOShipViaObjectName + " code to add.");
		    }
		    else{
		    	try {
					Edit_Record(sNewCode, out, sDBID, true, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME));
				} catch (Exception e) {
					out.println(e.getMessage());
				}
		    }
	    }
	    if (sOutPut.compareToIgnoreCase("") != 0){
	    	out.println (sOutPut);
	    }
		out.println("</BODY></HTML>");
		return;
	}
	
	private void Edit_Record(
			String sCode, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew,
			String sUser) throws Exception{
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			try {
				Add_Record(sCode, sDBID, pwOut, sUser);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sICEditPOShipViasEditCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	  
		pwOut.println("<TABLE BORDER=2 CELLSPACING=2>");
        String sSQL = "SELECT * FROM"
	        	+ " " + SMTableicshipvias.TableName
	        	+ " WHERE" 
	        	+ " " + SMTableicshipvias.sshipviacode + " = '" + sCode + "'";
		try{
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        		sSQL, 
	        		getServletContext(), 
	        		sDBID, 
	        		"MySQL", 
	        		this.toString() + ".Edit_Record - user: " + sUser);
	        
	        rs.next();
	        //Display fields:
	        pwOut.println("<TR>"
					+ "<TD ALIGN=RIGHT><B>PO Ship Via Name:</B></TD>"
					+ "<TD >"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMTableicshipvias.sshipvianame + "\""
					+ " VALUE=\"" + rs.getString(SMTableicshipvias.sshipvianame).replace("\"", "&quot;") + "\""
					+ " SIZE=" + "50"
					+ " MAXLENGTH=" + Integer.toString(SMTableicshipvias.sshipvianameLength)
					+ ">"
					+ "</TD></TR>"
					);

	        rs.close();
		}catch (SQLException ex){
			throw new Exception("Error reading ship via '" + sCode + "' with SQL: " + sSQL + " - " + ex.getMessage());
		}
		
		pwOut.println("</TABLE>");
		pwOut.println("<BR>");
		pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sPOShipViaObjectName + "' STYLE='height: 0.24in'></P>");
		pwOut.println("</FORM>");
		
	}
	
	private void Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sConf,
			String sUser) throws Exception{
		
		//Include all the SQLs needed to delete a record:
		String SQL = "DELETE FROM " + SMTableicshipvias.TableName + 
						" WHERE" +
						" " + SMTableicshipvias.sshipviacode + " = '" + sCode + "'";
		try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				getServletContext(), 
				sConf, 
				"MySQL", 
				this.toString() + ".Delete_Record - user: " + sUser);
		}catch (SQLException ex){
			throw new Exception("Error deleting ship via with SQL: '" + SQL + "' - " + ex.getMessage());
		}		
	}
	
	private void Add_Record(String sCode,
	   String sConf, 
	   PrintWriter pwOut,
	   String sUser) throws Exception{
		
		//First, make sure there isn't a class by this name already:
		String sSQL = "SELECT" 
			+ " " + SMTableicshipvias.sshipviacode
			+ " FROM" 
			+ " " + SMTableicshipvias.TableName 
			+ " WHERE" 
			+ " " + SMTableicshipvias.sshipviacode + " = '" + sCode + "'";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sConf, 
					"MySQL", 
					this.toString() + ".Add_Record - user: " + sUser);
			if (rs.next()){
				//This record already exists, so we can't add it:
				rs.close();
				throw new Exception("Error: The " + sPOShipViaObjectName + " '" + sCode + "' already exists - it cannot be added.<BR>");
			}
			rs.close();
			
		}catch(SQLException ex){
			throw new Exception("Error checking for existing ship via with SQL: " + sSQL + " - " + ex.getMessage() + ".<BR>");
		}
		
		sSQL = "INSERT INTO"
			+ " " + SMTableicshipvias.TableName
			+ " (" 
			+ SMTableicshipvias.sshipviacode 
			+ "," + SMTableicshipvias.sshipvianame
			+ ") VALUES ("
			+ "'" + sCode + "'"
			+ ", ''"
			+ ")"
		; 
		try {
			clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sConf); 
		}catch (SQLException ex){
			throw new Exception("Error inserting new ship via with SQL: " + sSQL + " - " + ex.getMessage() + ".<BR>");
		}
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

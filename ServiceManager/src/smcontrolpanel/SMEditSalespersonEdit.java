package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditSalespersonEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Salesperson";
	private static String sCalledClassName = "SMEditSalespersonAction";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSalespersons))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sEditCode = clsStringFunctions.filter(request.getParameter(sObjectName));

		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit:
			title = "Edit " + sObjectName + ": " + sEditCode;
		    subtitle = "";
		   out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
		    Edit_Record(sEditCode, out, sDBID, false);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sEditCode;
		    subtitle = "";
		
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete.");
		    }
		    else{
			    if (Delete_Record(sEditCode, out, sDBID, sUserName) == false){
			    	out.println ("Error deleting " + sEditCode + ".");
			    }
			    else{
			    	out.println ("Successfully deleted " + sEditCode + ".");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
	    	//User has chosen to add a new user:
			title = "Add " + sObjectName + ": " + sNewCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
		    	+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	+ "\">Return to user login</A><BR><BR>");

		    if (sNewCode == ""){
		    	out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
		    }
		    else{
		    	Edit_Record(sNewCode, out, sDBID, true);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			String sCode, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			try {
				Add_Record (sCode, sDBID, pwOut);
			} catch (Exception e) {
				pwOut.println(e.getMessage() + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	    String sOutPut = "";
	  
        sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";
        String sSQL = "";
		try{
			//Get the record to edit:
	        sSQL = SMMySQLs.Get_Salesperson_By_Salescode(sCode);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	
	        rs.next();
	        //Display fields:
	        //Title:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesalesperson.sSalespersonTitle, 
	        		clsStringFunctions.filter(rs.getString(SMTablesalesperson.sSalespersonTitle)), 
	        		SMTablesalesperson.sSalespersonTitleLength, 
	        		"Title:", 
	        		"Salesperson's title.");
	        
	        //User ID:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesalesperson.lSalespersonUserID, 
	        		clsStringFunctions.filterZeroStringToEmptyString(Integer.toString(rs.getInt(SMTablesalesperson.lSalespersonUserID))), 
	        		SMTablesalesperson.lSalespersonUserIDLength, 
	        		"User ID:", 
	        		"The salesperson's system user ID, if he is set up as a user.");

	        //First name:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesalesperson.sSalespersonFirstName, 
	        		clsStringFunctions.filter(rs.getString(SMTablesalesperson.sSalespersonFirstName)), 
	        		SMTablesalesperson.sSalespersonFirstNameLength, 
	        		"First name:", 
	        		"Salesperson's first name.");

	        //Last name:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesalesperson.sSalespersonLastName, 
	        		clsStringFunctions.filter(rs.getString(SMTablesalesperson.sSalespersonLastName)), 
	        		SMTablesalesperson.sSalespersonLastNameLength, 
	        		"Last name:", 
	        		"Salesperson's last name.");
	        
	        //Show in report:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	        		SMTablesalesperson.iShowInSalesReport, 
	        		rs.getInt(SMTablesalesperson.iShowInSalesReport), 
	        		"Show in sales report:", 
	        		"Check to make this salesperson appear in sales report.");
	        
	        //Type of salesperson (Resi, Comm, or Neither)
	        ArrayList<String> sValues = new ArrayList<String>();
	        sValues.add("C");
	        sValues.add("R");
	        sValues.add("N");

	        ArrayList<String> sDescriptions = new ArrayList<String>();
	        sDescriptions.add("Commercial");
	        sDescriptions.add("Residential");
	        sDescriptions.add("N/A");

	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		SMTablesalesperson.sSalespersonType, 
	        		sValues, 
	        		rs.getString(SMTablesalesperson.sSalespersonType), 
	        		sDescriptions, 
	        		"Salesperson type:", 
	        		"For grouping in sales reports"
	        		);
	        
	        //Direct Dial:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesalesperson.sDirectDial, 
	        		clsStringFunctions.filter(rs.getString(SMTablesalesperson.sDirectDial)), 
	        		SMTablesalesperson.sDirectDialLength, 
	        		"Direct Dial:", 
	        		"Salesperson's direct dial.");
	        
	        //Email:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesalesperson.sSalespersonEmail, 
	        		clsStringFunctions.filter(rs.getString(SMTablesalesperson.sSalespersonEmail)), 
	        		SMTablesalesperson.sSalespersonEmailLength, 
	        		"Email:", 
	        		"Salesperson's email.");
	        
	        rs.close();
		}catch (SQLException ex){
			sOutPut += "<TR><TD><FONT=RED>" + "Error [[1391451569]] with SQL: " + sSQL + " - " + ex.getMessage() + "</FONT></TD></TR>";
		}
		
		//********************
        sOutPut += "</TABLE>";
        sOutPut += "<BR>";
        
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut += "</FORM>";
		pwOut.println(sOutPut);
		
	}
	
	private boolean Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sConf,
			String sUser){
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".Delete_Record - user: " + sUser));
		} catch (Exception e) {
			pwOut.println("<FONT COLOR=RED>Error [1422561428] getting connection to delete salesperson - " + e.getMessage() + "</FONT>");
				return false;
		}
		
		//Include all the SQLs needed to delete a record:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			pwOut.println("<FONT COLOR=RED>Error [1422561429] starting transaction to delete salesperson.</FONT>");
			return false;
		}
		//Delete the actual salesperson record:
		String SQL = "DELETE FROM " + SMTablesalesperson.TableName + " WHERE (" 
			+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + sCode + "')" 
		+ ")";
		try {
			Statement statement = conn.createStatement();
			statement.execute(SQL);
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080535]");
			pwOut.println("<FONT COLOR=RED>Error [1422561430] executing command (" + SQL + ") to delete salesperson - " 
				+ e.getMessage() + "</FONT>");
			return false;
		}
		//Delete default sales group saleperson records:
		SQL = "DELETE FROM " + SMTabledefaultsalesgroupsalesperson.TableName + " WHERE (" 
				+ "(" + SMTabledefaultsalesgroupsalesperson.ssalespersoncode + " = '" + sCode + "')" 
			+ ")";
		try {
			Statement statement = conn.createStatement();
			statement.execute(SQL);
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080536]");
			pwOut.println("<FONT COLOR=RED>Error [1422561431] executing command (" + SQL + ") to delete salesperson for customers - " 
				+ e.getMessage() + "</FONT>");
			return false;
		}

		//Remove this salesperson from the 'users' table records:
		SQL = "UPDATE " + SMTableusers.TableName + " SET " + SMTableusers.sDefaultSalespersonCode + " = '' WHERE ("
			+ "(" + SMTableusers.sDefaultSalespersonCode + " = '" + sCode + "')"
			+ ")";
		try {
			Statement statement = conn.createStatement();
			statement.execute(SQL);
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080537]");
			pwOut.println("<FONT COLOR=RED>Error [1422561432] executing command (" + SQL + ") to remove salesperson from user(s) - " 
				+ e.getMessage() + "</FONT>");
			return false;
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080538]");
			pwOut.println("<FONT COLOR=RED>Error [1422561432] committing transaction to delete salesperson.</FONT>");
			return false;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080539]");
		return true;
	}
	
	private void Add_Record(String sCode, String sConf, PrintWriter pwOut) throws Exception{
		
		//First, make sure there isn't a user by this name already:
		String sSQL = SMMySQLs.Get_Salesperson_By_Salescode(sCode);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				//This record already exists, so we can't add it:
				rs.close();
				throw new Exception("The " + sObjectName + " '" + sCode + "' already exists - it cannot be added.");
			}
			rs.close();
		}catch(SQLException ex){
			throw new Exception("Error [1391451569] checking if " + sObjectName + " '" + sCode + "' already exists - " + ex.getMessage());
		}
		sSQL = SMMySQLs.Add_New_Salesperson_SQL(sCode);
		try {
			if(!clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sConf)){
				throw new Exception("Could not add " + sObjectName + " '" + sCode + "' with SQL: " + sSQL);
			}
		}catch (SQLException ex){
			throw new Exception("Error [1391451570] adding new salesperson - " + sObjectName + " '" + sCode + "' already exists - " + ex.getMessage());
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
}

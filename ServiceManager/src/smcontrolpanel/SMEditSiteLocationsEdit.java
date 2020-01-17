package smcontrolpanel;

import SMDataDefinition.SMTablesitelocations;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ACCPACDataDefinition.ARCSP;
import SMClasses.MySQLs;

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

public class SMEditSiteLocationsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Site Location";
	private static String sCalledClassName = "SMEditSiteLocationsAction";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSiteLocations))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sEditCode = clsStringFunctions.filter(clsManageRequestParameters.get_Request_Parameter(sObjectName, request));
	    String sEditLabel = "";
	    String sEditCustomerNumber = "";
	    String sEditCustomerShipTo = "";
	    if(sEditCode.compareToIgnoreCase("") != 0){
		    sEditLabel = clsStringFunctions.StringRight(sEditCode,SMTablesitelocations.sLabelLength).trim();
		    sEditCustomerNumber = clsStringFunctions.StringLeft(sEditCode, ARCSP.sCustomerNumberLength).trim();
		    sEditCustomerShipTo = sEditCode.substring(
		    		SMTablesitelocations.sAcctLength, 
		    		SMTablesitelocations.sAcctLength + SMTablesitelocations.sShipToCodeLength).trim();
	    }
	    
		String title = "";
		String subtitle = "";
				
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit:
			title = "Edit " + sObjectName + ": " + sEditLabel;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
		    Edit_Record(sEditCustomerNumber.trim(), sEditCustomerShipTo.trim(), sEditLabel.trim(), out, sDBID, false);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sEditLabel;
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
			    if (Delete_Record(sEditCustomerNumber.trim(), sEditCustomerShipTo.trim(), sEditLabel.trim(), out, sDBID) == false){
			    	out.println ("Error deleting " + sEditLabel + ".");
			    }
			    else{
			    	out.println ("Successfully deleted " + sEditLabel + ".");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	//User has chosen to add a new record:
		    String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
		    String sNewCustomerNumber = 
		    	clsStringFunctions.StringLeft(
		    			request.getParameter("CustomerShipTos"),
		    			ARCSP.sCustomerNumberLength);
		    
		    String sNewCustomerShipTo = 
		    	clsStringFunctions.StringRight(
		    			request.getParameter("CustomerShipTos"),
		    			ARCSP.sShipViaCodeLength);
		    
		    title = "Add " + sObjectName + ": " + sNewCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		    if (sNewCode == ""){
		    	out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
		    }
		    else if (sNewCustomerNumber == ""){
		    	out.println ("You chose to add a new " + sObjectName + ", but you did not select a customer ship-to from the list.");
		    }
		    else if (sNewCustomerShipTo == ""){
		    	out.println ("You chose to add a new " + sObjectName + ", but you did not select a customer ship-to from the list.");
		    }
		    else{
		    	Edit_Record(sNewCustomerNumber.trim(), sNewCustomerShipTo.trim(), sNewCode.trim(), out, sDBID, true);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			String sCustomerNumber,
			String sCustomerShipTo,
			String sLabel,
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			if (Add_Record (sCustomerNumber, sCustomerShipTo, sLabel, sDBID, pwOut) == false){
				pwOut.println("ERROR - Could not add " + sLabel + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"CustomerNumber\" VALUE=\"" + sCustomerNumber + "\">");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"CustomerShipTo\" VALUE=\"" + sCustomerShipTo + "\">");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"Label\" VALUE=\"" + sLabel + "\">");
	    String sOutPut = "";
	  
        sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";
        
        //System.out.println("sCustomerNumber = " + sCustomerNumber);
        //System.out.println("sCustomerShipTo = " + sCustomerShipTo);
        //System.out.println("sLabel = " + sLabel);
		try{
			//Get the record to edit:
	        String sSQL = MySQLs.Get_SiteLocations_By_Code(sCustomerNumber, sCustomerShipTo, sLabel);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	
	        rs.next();
	        //Display fields:
	        //Comment
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablesitelocations.sComment, 
	        		clsStringFunctions.filter(rs.getString(SMTablesitelocations.sComment)), 
	        		SMTablesitelocations.sCommentLength, 
	        		"Comment:", 
	        		"Add a description of the site location here."
	        		);
	        rs.close();
	      
		}catch (SQLException ex){
	    	System.out.println("[1579271770] Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		//********************
        sOutPut += "</TABLE>";
        sOutPut += "<BR>";
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut += "</FORM>";
		pwOut.println(sOutPut);
		
	}
	
	private boolean Delete_Record(
			String sCustomerNumber,
			String sCustomerShipTo,
			String sLabel,
			PrintWriter pwOut,
			String sDBID){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//Include all the SQLs needed to delete a record:
		sSQLList.add((String) MySQLs.Delete_SiteLocation_SQL(sCustomerNumber, sCustomerShipTo, sLabel));
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBID);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("[1579271777] Error in " + this.toString()+ ".Delete_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private boolean Add_Record(
			String sCustomerNumber, 
			String sCustomerShipTo, 
			String sLabel, 
			String sDBID, 
			PrintWriter pwOut){
		
		//First, make sure there isn't a record already:
		String sSQL = MySQLs.Get_SiteLocations_By_Code(sCustomerNumber, sCustomerShipTo, sLabel);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			if (rs.next()){
				//This record already exists, so we can't add it:
				pwOut.println("The " + sObjectName + " '" + sLabel + "' already exists for this customer and ship to - it cannot be added.<BR>");
				return false;
			}
			rs.close();
		}catch(SQLException ex){
	    	System.out.println("[1579271786] Error in " + this.toString()+ ".Add_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		sSQL = MySQLs.Add_New_SiteLocation_SQL(sCustomerNumber, sCustomerShipTo, sLabel);
		try {
			
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("[1579271789] Error in " + this.toString()+ ".Add_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

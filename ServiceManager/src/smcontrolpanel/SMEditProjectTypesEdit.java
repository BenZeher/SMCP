package smcontrolpanel;

import SMDataDefinition.SMTableprojecttypes;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

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

import SMClasses.MySQLs;

public class SMEditProjectTypesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Project Type";
	private static String sCalledClassName = "SMEditProjectTypesAction";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditProjectTypes))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
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
			    if (Delete_Record(sEditCode, out, sDBID) == false){
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
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
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
			String sID, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			if (Add_Record (sID, sDBID, pwOut) == false){
				pwOut.println("ERROR - Could not add " + sID + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sID + "\">");
	    String sOutPut = "";
	  
        sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";
        
		try{
			//Get the record to edit:
	        String sSQL = MySQLs.Get_Project_Type_By_ID(sID);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        rs.next();
	        //Display fields:
	        //Job Code:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(SMTableprojecttypes.sTypeCode, 
													        	   clsStringFunctions.filter(rs.getString(SMTableprojecttypes.sTypeCode)), 
													        	   SMTableprojecttypes.iTypeCodeLength, 
													        	   "Project Type Code:", 
													        	   "A short 'code' for the project type, up to 8 characters.");

	        //Job Type Description:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(SMTableprojecttypes.sTypeDesc, 
															        		clsStringFunctions.filter(rs.getString(SMTableprojecttypes.sTypeDesc)), 
															        		"Project Type Description:", 
															        		"A full description of the project type.",
															        		4,
															        		75);

	        rs.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
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
	
	private boolean Delete_Record(String sID,
								  PrintWriter pwOut,
								  String sDBID){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//Include all the SQLs needed to delete a record:
		sSQLList.add(MySQLs.Delete_Project_Type_SQL(sID));
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBID);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ ".Delete_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private boolean Add_Record(String sID, String sDBID, PrintWriter pwOut){
		
		//First, make sure there isn't a user by this name already:
		String sSQL = MySQLs.Get_Project_Type_By_ID(sID);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			if (rs.next()){
				//This record already exists, so we can't add it:
				pwOut.println("The " + sObjectName + " '" + sID + "' already exists - it cannot be added.<BR>");
				return false;
			}
			rs.close();
			
		}catch(SQLException ex){
	    	System.out.println("Error in " + this.toString()+ ".Add_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		sSQL = MySQLs.Add_New_Project_Type_SQL(sID);
		try {
			
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ ".Add_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

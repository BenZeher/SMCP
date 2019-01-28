
package smfa;

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
import SMDataDefinition.SMTablefaclasses;
import SMDataDefinition.SMTablefamaster;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class FAEditClassEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sFAEditClassEditCalledClassName = "FAEditClassAction";
	private static final String sObjectClassName = "Class";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		 if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAEditClasses)){
		    	return;
		    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	   
	    String sEditCode = (String) clsStringFunctions.filter(request.getParameter(sObjectClassName));

		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit:
			title = "Edit " + sObjectClassName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Fixed Assets Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAEditClasses) 
		    		+ "\">Summary</A><BR><BR>");
			
		    Edit_Record(sEditCode, out, sDBID, false);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete:
			title = "Delete " + sObjectClassName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Fixed Assets Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItems) 
		    		+ "\">Summary</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete.");
		    }else{
			    if (Delete_Record(sEditCode, out, sDBID) == false){
			    	out.println ("Could not delete class '" + sEditCode + "'.");
			    }
			    else{
			    	out.println ("Successfully deleted classs '" + sEditCode + "'.");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectClassName));
	    	//User has chosen to add a new record:
			title = "Add " + sObjectClassName + ": " + sNewCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Fixed Assets Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItems) 
		    		+ "\">Summary</A><BR><BR>");

		    if (sNewCode == ""){
		    	out.println ("You chose to add a new " + sObjectClassName + ", but you did not enter a new " + sObjectClassName + " code to add.");
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
			if (Add_Record (sCode, sDBID, pwOut) == false){
				pwOut.println("ERROR - Could not add " + sCode + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sFAEditClassEditCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	    //String sOutPut = "";
	  
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
		try{
			//Get the record to edit:
	        String sSQL = "SELECT * FROM" +
	        				" " + SMTablefaclasses.TableName + 
	        			  " WHERE" +
	        				" " + SMTablefaclasses.sClass + " = '" + sCode + "'";
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        rs.next();
	        //Display fields:
	        pwOut.println("<TR>"
					+ "<TD ALIGN=RIGHT><B>Class description:</B></TD>"
					+ "<TD >"
					+ "<INPUT TYPE=TEXT NAME=\"" + SMTablefaclasses.sClassDescription + "\""
					+ " VALUE=\"" + rs.getString(SMTablefaclasses.sClassDescription).replace("\"", "&quot;") + "\""
					+ " SIZE=" + "50"
					+ " MAXLENGTH=" + Integer.toString(SMTablefaclasses.sClassDescriptionLength)
					+ ">"
					+ "</TD></TR>"
					);

		rs.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		pwOut.println("</TABLE>");
		pwOut.println("<BR>");
		pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectClassName + "' STYLE='height: 0.24in'></P>");
		pwOut.println("</FORM>");
		
	}
	
	private boolean Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sConf){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//First, make sure there are no assets using this class:
		String SQL = "SELECT"
				+ " " + SMTablefamaster.sClass
				+ " FROM " + SMTablefamaster.TableName
				+ " WHERE ("
					+ "(" + SMTablefamaster.sClass + " = '" + sCode + "')"
				+ ")"
				;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sConf, "MySQL", this.toString());
			if (rs.next()){
				pwOut.println("This class is used on some assets - it cannot be deleted.");
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			pwOut.println("Error checking for assets using this class - " + e.getMessage());
			return false;
		}
		
		//Include all the SQLs needed to delete a record:
		sSQLList.add("DELETE FROM " + SMTablefaclasses.TableName + 
						" WHERE" +
						" " + SMTablefaclasses.sClass + " = '" + sCode + "'");
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sConf);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ ".Delete_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private boolean Add_Record(String sCode,
							   String sConf, 
							   PrintWriter pwOut){
		
		//First, make sure there isn't a class by this name already:
		String sSQL = "SELECT" +
						" " + SMTablefaclasses.sClass + 
					  " FROM" +
					  	" " + SMTablefaclasses.TableName + 
					  " WHERE" +
					  	" " + SMTablefaclasses.sClass + " = '" + sCode + "'";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				//This record already exists, so we can't add it:
				pwOut.println("The " + sObjectClassName + " '" + sCode + "' already exists - it cannot be added.<BR>");
				rs.close();
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
		
		sSQL = "INSERT INTO" +
				" " + SMTablefaclasses.TableName + 
				"(" +
					" " + SMTablefaclasses.sClass + "," +
					" " + SMTablefaclasses.sClassDescription +
				") VALUES (" +
					" '" + sCode + "'" +
					", ''" +
				")"; 
		try {
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sConf); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ ".Add_Record class!!");
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

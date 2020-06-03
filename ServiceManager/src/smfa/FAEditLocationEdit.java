package smfa;

import SMDataDefinition.*;
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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class FAEditLocationEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sLocationObjectName = "Location";
	private static final String sFAEditLocationEditCalledClassName = "FAEditLocationAction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);

	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAEditLocation)){
	    	return;
	    }

	    String sEditCode = (String) clsStringFunctions.filter(request.getParameter(sLocationObjectName));

		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit:
			title = "Edit " + sLocationObjectName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Fixed Assets Main Menu</A><BR><BR>");
			
		    Edit_Record(sEditCode, out, sDBID, false, sDBID);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete:
			title = "Delete " + sLocationObjectName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Fixed Assets Main Menu</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete.");
		    }else{
			    if (Delete_Record(sEditCode, out, sDBID) == false){
			    	out.println ("Error deleting " + sEditCode + ".");
			    }
			    else{
			    	out.println ("Successfully deleted " + sEditCode + ".");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sLocationObjectName));
	    	//User has chosen to add a new record:
			title = "Add " + sLocationObjectName + ": " + sNewCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Fixed Assets Main Menu</A><BR><BR>");

		    if (sNewCode == ""){
		    	out.println ("You chose to add a new " + sLocationObjectName + ", but you did not enter a new " + sLocationObjectName + " to add.");
		    }
		    else{
		    	Edit_Record(sNewCode, out, sDBID, true, sDBID);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			String sCode, 
			PrintWriter pwOut, 
			String sDBIB,
			boolean bAddNew,
			String sDBID){
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			if (Add_Record (sCode, sDBIB, pwOut) == false){
				pwOut.println("ERROR - Could not add " + sCode + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sFAEditLocationEditCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	    //String sOutPut = "";
	  
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        String sSQL = "";
		try{
			//Get the record to edit:
	        sSQL = "SELECT * FROM" +
	        				" " + SMTablefalocations.TableName + 
	        			  " WHERE" +
	        				" " + SMTablefalocations.sLocLocation + " = '" + sCode + "'";
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBIB);
	        
	        rs.next();
	        //Display fields:
	        //Location code:
	        pwOut.println("<TR><TD>Location:</TD><TD>" + 
	        				rs.getString(SMTablefalocations.sLocLocation) +
	        				"</TD><TD>&nbsp;</TD></TR>");

			//Location Desc:
			pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMTablefalocations.sLocDescription, 
		        	rs.getString(SMTablefalocations.sLocDescription), 
		        	30, 
		        	"Location Desc:", 
		        	"Description for this location."));

		rs.close();
		}catch (SQLException ex){
	    	System.out.println("[1579189463] Error in " + this.toString()+ " class with SQL: '" + sSQL + "'");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		pwOut.println("</TABLE>");
		pwOut.println("<BR>");
		pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sLocationObjectName + "' STYLE='height: 0.24in'></P>");
		pwOut.println("</FORM>");
		
	}
	
	private boolean Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sDBIB){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//Include all the SQLs needed to delete a record:
		sSQLList.add("DELETE FROM " + SMTablefalocations.TableName + 
						" WHERE" +
						" " + SMTablefalocations.sLocLocation + " = '" + sCode + "'");
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBIB);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("[1579189470] Error in " + this.toString()+ ".Delete_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private boolean Add_Record(String sCode,
							   String sDBIB, 
							   PrintWriter pwOut){
		
		//First, make sure there isn't a user by this name already:
		String sSQL = "SELECT" +
						" " + SMTablefalocations.sLocLocation + 
					  " FROM" +
					  	" " + SMTablefalocations.TableName + 
					  " WHERE" +
					  	" " + SMTablefalocations.sLocLocation + " = '" + sCode + "'";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBIB);
			if (rs.next()){
				//This record already exists, so we can't add it:
				pwOut.println("The " + sLocationObjectName + " '" + sCode + "' already exists - it cannot be added.<BR>");
				rs.close();
				return false;
			}
			rs.close();
			
		}catch(SQLException ex){
	    	System.out.println("[1579189477] Error in " + this.toString()+ ".Add_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
		sSQL = "INSERT INTO" +
				" " + SMTablefalocations.TableName + 
				"(" +
					" " + SMTablefalocations.sLocLocation + "," +
					" " + SMTablefalocations.sLocDescription +
				") VALUES (" +
					" '" + sCode + "'," +
					" ''" +
				")"; 
		try {
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBIB); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("[1579189481] Error in " + this.toString()+ ".Add_Record class!!");
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

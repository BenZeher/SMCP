package smcontrolpanel;

import SMDataDefinition.SMTableworkperformedcodes;
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

public class SMEditWorkPerformedCodesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Work Performed Code";
	private static String sCalledClassName = "SMEditWorkPerformedCodesAction";
	private String sDBID = "";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditWorkPerformedCodes))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
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
		    String sNewServiceType = clsStringFunctions.filter(request.getParameter("NewServiceType"));
	    	//User has chosen to add a new record:
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
		    else if (sNewServiceType == ""){
		    	out.println ("You chose to add a new " + sObjectName + ", but you did not select a service type.");
		    }
		    else{
		    	//We need to concatenate the service type and the WP code to
		    	//match the way they are concatenated coming from the 
		    	//'SELECT' list in the calling form:
		    	String sConcatCode = clsStringFunctions.PadLeft(sNewServiceType, " ", SMTableworkperformedcodes.sCodeLength);
		    	sConcatCode += clsStringFunctions.PadLeft(sNewCode, " ", SMTableworkperformedcodes.sWorkPerformedCodeLength);
		    	Edit_Record(sConcatCode, out, sDBID, true);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			String sCode, 
			PrintWriter pwOut, 
			String sConf,
			boolean bAddNew){
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			if (Add_Record (sCode, sConf, pwOut) == false){
				pwOut.println("ERROR - Could not add " + sCode + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	    String sOutPut = "";
	  
        sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";
        String sWPCode = clsStringFunctions.StringRight(sCode, SMTableworkperformedcodes.sWorkPerformedCodeLength).trim();
        String sServiceType = clsStringFunctions.StringLeft(sCode, SMTableworkperformedcodes.sCodeLength).trim();
        
		try{
			//Get the record to edit:
	        String sSQL = SMMySQLs.Get_WorkPerformedCode_By_Code(sWPCode, sServiceType);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
        	
	        rs.next();
	        //Display fields:
	        //Sort order (integer):
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTableworkperformedcodes.iSortOrder, 
	        		Integer.toString(rs.getInt(SMTableworkperformedcodes.iSortOrder)), 
	        		4, 
	        		"Sort order:", 
	        		"Used to control where code appears in list - duplicates allowed."
	        		);
	      
	        //Work performed phrase:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
	        		SMTableworkperformedcodes.sWorkPerformedPhrase, 
	        		clsStringFunctions.filter(rs.getString(SMTableworkperformedcodes.sWorkPerformedPhrase)), 
	        		"Work performed phrase:", 
	        		"The full phrase, including punctuation.",
	        		4,
	        		60
	        		);
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
	
	private boolean Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sConf){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//Include all the SQLs needed to delete a record:
		String sWPCode = clsStringFunctions.StringRight(sCode, SMTableworkperformedcodes.sWorkPerformedCodeLength).trim();
        String sServiceType = clsStringFunctions.StringLeft(sCode, SMTableworkperformedcodes.sCodeLength).trim();
		sSQLList.add(SMMySQLs.Delete_WorkPerformedCode_SQL(sWPCode, sServiceType));
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
	
	private boolean Add_Record(String sCode, String sConf, PrintWriter pwOut){
		
		//First, make sure there isn't a record already:
		String sWPCode = clsStringFunctions.StringRight(sCode, SMTableworkperformedcodes.sWorkPerformedCodeLength).trim();
        String sServiceType = clsStringFunctions.StringLeft(sCode, SMTableworkperformedcodes.sCodeLength).trim();
		String sSQL = SMMySQLs.Get_WorkPerformedCode_By_Code(sWPCode, sServiceType);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				//This record already exists, so we can't add it:
				pwOut.println("The " + sObjectName + " '" + sCode + "' already exists - it cannot be added.<BR>");
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
		sSQL = SMMySQLs.Add_New_WorkPerformedCode_SQL(sWPCode, sServiceType);
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

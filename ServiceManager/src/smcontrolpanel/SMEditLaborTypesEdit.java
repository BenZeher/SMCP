package smcontrolpanel;

import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelabortypes;
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

public class SMEditLaborTypesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Labor Type";
	private static String sCalledClassName = "SMEditLaborTypesAction";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditLaborTypes))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sEditCode = (String) clsStringFunctions.filter(request.getParameter(sObjectName));

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
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	    //String sOutPut = "";
	  
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
		try{
			//Get the record to edit:
	        String sSQL = SMMySQLs.Get_LaborType_By_ID(sCode);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        rs.next();
	        //Display fields:
	        //Labor type name:
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelabortypes.sLaborName, 
	        		clsStringFunctions.filter(rs.getString(SMTablelabortypes.sLaborName)), 
	        		SMTablelabortypes.sLaborNameLength, 
	        		"Labor Code Name:", 
	        		"A short 'code' for the name type, up to " + SMTablelabortypes.sLaborNameLength + " characters."));

	        //Item number:
	        //Select from list of item numbers (with descriptions):
	        try{
				//Get the record to edit:
	        	sSQL = "SELECT "
	    			+ SMTableicitems.sItemNumber 
	    			+ ", " + SMTableicitems.sItemDescription
	    			+ " FROM " + SMTableicitems.TableName
	    			+ " ORDER BY " + SMTableicitems.sItemNumber
	    		;

		        ResultSet rsItems = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sDBID, 
		        	"MySQL", 
		        	"smcontrolpanel.SMEditLaborTypesEdit");
		        
		        pwOut.println("<TR>");
		        pwOut.println("<TD ALIGN=RIGHT><B>" + "Item number:" + " </B></TD>");
		        pwOut.println("<TD ALIGN=LEFT> <SELECT NAME = \"" + SMTablelabortypes.sItemNumber + "\">");
				
				//Print out directly so that we don't waste time appending to string buffers:
		        while (rsItems.next()){
					pwOut.println("<OPTION");
					//TBDL
					//if (sDatabaseType.compareToIgnoreCase("MySQL") == 0){
						if (rsItems.getString(SMTableicitems.sItemNumber).trim().compareToIgnoreCase(rs.getString(SMTablelabortypes.sItemNumber).trim()) == 0){
							pwOut.println( " selected=yes");
						}
						pwOut.println(" VALUE=\"" + rsItems.getString(SMTableicitems.sItemNumber).trim() + "\">");
						pwOut.println(rsItems.getString(SMTableicitems.sItemNumber).trim() + " - ");
						pwOut.println(rsItems.getString(SMTableicitems.sItemDescription).trim());
					//}else{
					//	if (rsItems.getString(ICITEM.sItemNumber).trim().compareToIgnoreCase(rs.getString(SMTablelabortypes.sItemNumber).trim()) == 0){
					//		pwOut.println( " selected=yes");
					//	}
					//	pwOut.println(" VALUE=\"" + rsItems.getString(ICITEM.sItemNumber).trim() + "\">");
					//	pwOut.println(rsItems.getString(ICITEM.sItemNumber).trim() + " - ");
					//	pwOut.println(rsItems.getString(ICITEM.sItemDesc).trim());
					//}
				}
		        rsItems.close();
				pwOut.println("</SELECT></TD>");
				  
				pwOut.println("<TD ALIGN=LEFT>" + "Select a labor item to be associated with this labor type." + "</TD>");
				pwOut.println("</TR>");
			}catch (SQLException ex){
		    	System.out.println("Error in " + this.toString()+ " class!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				//return false;
			}

			//Category:
	        //Select from list of categories (with descriptions):
	        try{
				//Get the record to edit:
	        	sSQL = "SELECT * FROM"
	        		+ " " + SMTableiccategories.TableName
	        		+ " ORDER BY " + SMTableiccategories.sCategoryCode
	        	;

	        	ResultSet rsCategories = clsDatabaseFunctions.openResultSet(
	        		sSQL, 
	        		getServletContext(), 
	        		sDBID,
	        		"MySQL",
	        		SMUtilities.getFullClassName(this.toString() + ".Edit_Record")
	        	);
		        pwOut.println("<TR>");
		        pwOut.println("<TD ALIGN=RIGHT><B>" + "Category:" + " </B></TD>");
		        pwOut.println("<TD ALIGN=LEFT> <SELECT NAME = \"" + SMTablelabortypes.sCategory + "\">");
				
				//Print out directly so that we don't waste time appending to string buffers:
		        while (rsCategories.next()){
					pwOut.println("<OPTION");
						if (rsCategories.getString(SMTableiccategories.sCategoryCode).trim()
								.compareToIgnoreCase(rs.getString(SMTablelabortypes.sCategory).trim()) == 0){
								pwOut.println( " selected=yes");
						}
							pwOut.println(" VALUE=\"" + rsCategories.getString(SMTableiccategories.sCategoryCode).trim() + "\">");
							pwOut.println(rsCategories.getString(SMTableiccategories.sCategoryCode).trim() + " - ");
							pwOut.println(rsCategories.getString(SMTableiccategories.sDescription).trim());
				}
		        rsCategories.close();
				pwOut.println("</SELECT></TD>");
				  
				pwOut.println("<TD ALIGN=LEFT>" + "Select a category to be associated with this labor type." + "</TD>");
				pwOut.println("</TR>");
			}catch (SQLException ex){
		    	System.out.println("Error in " + this.toString()+ " class!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				//return false;
			}

			//Mark up amount:
			//Start using the 'sOutPut' output buffer again:
			pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
		        		  SMTablelabortypes.dMarkupAmount, 
		        		  Double.toString(rs.getDouble(SMTablelabortypes.dMarkupAmount)), 
		        		  8, 
		        		  "Mark Up Amount:", 
		        			"Default amount of markup per labor unit for this labor type."));

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
		pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>");
		pwOut.println("</FORM>");
		
	}
	
	private boolean Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sDBID){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//Include all the SQLs needed to delete a record:
		sSQLList.add((String) SMMySQLs.Delete_LaborType_SQL(sCode));
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
	
	private boolean Add_Record(String sCode, String sDBID, PrintWriter pwOut){
		
		//First, make sure there isn't a user by this name already:
		String sSQL = SMMySQLs.Get_LaborType_By_ID(sCode);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
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
		sSQL = SMMySQLs.Add_New_LaborType_SQL(sCode);
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
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

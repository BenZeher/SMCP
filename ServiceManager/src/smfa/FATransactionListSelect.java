package smfa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class FATransactionListSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String LOCATION_PARAMETER = "LOCATION";
	private static final String CHECKBOX_LABEL = "CHECKBOXLABEL";
	//private static String sObjectName = "Asset";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCalledClassName = "FATransactionListGenerate";
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FATransactionReport)){
	    	return;
	    }
	    String title = "Transaction List for " + sCompanyName;
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FATransactionReport) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    try{
	    	List_Criteria(out, sDBID, sUserName, request);
	    }catch (Exception e){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu"
					+ "?Warning=" + clsServletUtilities.URLEncode("Error displaying period end criteria selecting screen : " + e.getMessage())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
		out.println("</FORM>");
    	
		out.println ("<NOSCRIPT>\n"
 			   + "    <font color=red>\n"
 			   + "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
 			   + "    </font>\n"
 			   + "</NOSCRIPT>\n"
				
 			   + "<script type=\"text/javascript\">\n"
 			   
			   + "function confirmation(){\n"
			   + " 	  var ProvisionalCheck = document.getElementById('PROVISIONALPOSTING');"
			   + " 	  if (ProvisionalCheck = false){"
			   + "    	if (confirm('This will create all of the depreciation transactions for the select period - are you sure you want to continue?')) {\n"
			   + "    	}else{\n"
			   + "		  //reset the check box;\n"
			   + " 		  document.getElementById('PROVISIONALPOSTING').checked = true;\n"
			   + " 	  	}\n"
			   + " 	  }\n"
			   + "}\n"
			   
			+ "</script>\n");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
	
	private void List_Criteria(PrintWriter pwOut, 
							   String sDBID,
							   String sUser,
							   HttpServletRequest req) throws Exception{

	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
        ArrayList<String> sYearValues = new ArrayList<String>();
        ArrayList<String> sYearDescriptions = new ArrayList<String>();
        ArrayList<String> sPeriodValues = new ArrayList<String>();
        ArrayList<String> sPeriodDescriptions = new ArrayList<String>();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        
	    try{
		    //Fiscal year:
	        sYearValues.clear();
	        sYearDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sYearValues.add("");
	        sYearDescriptions.add("--- Select a fiscal year ---");
	        for (int i=0;i<100;i++){
	        	sYearValues.add(String.valueOf(1970 + i));
	        	sYearDescriptions.add(String.valueOf(1970 + i));
	        }
		    //Fiscal period:
	        sPeriodValues.clear();
	        sPeriodDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sPeriodValues.add("");
	        sPeriodDescriptions.add("-- Select a fiscal period --");
	        for (int i=1;i<=13;i++){
	        	sPeriodValues.add(String.valueOf(i));
	        	sPeriodDescriptions.add(String.valueOf(i));
	        }	 
	        
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		"STARTFISCALYEAR", 
	        		sYearValues, 
	        		String.valueOf(c.get(Calendar.YEAR)), 
	        		sYearDescriptions, 
	        		"Transactions starting with fiscal year:", 
	        		"&nbsp;"
	        	)
	        ); 
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		"STARTFISCALPERIOD", 
	        		sPeriodValues, 
	        		String.valueOf(c.get(Calendar.MONTH) + 1), 
	        		sPeriodDescriptions, 
	        		"Transactions starting with fiscal period:", 
	        		"&nbsp;"
	        	)
	        ); 
	        
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		"ENDFISCALYEAR", 
	        		sYearValues, 
	        		String.valueOf(c.get(Calendar.YEAR)), 
	        		sYearDescriptions, 
	        		"Transactions ending with fiscal year:", 
	        		"&nbsp;"
	        	)
	        ); 
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		"ENDFISCALPERIOD", 
	        		sPeriodValues, 
	        		String.valueOf(c.get(Calendar.MONTH) + 1), 
	        		sPeriodDescriptions, 
	        		"Transactions ending with fiscal period:", 
	        		"&nbsp;"
	        	)
	        ); 
	    }catch (Exception e){
	    	throw new Exception ("Error: " + e.getMessage());
	    }

        //checkboxes
	    
	    
        //Print provisional posting?
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>Print provisional transactions </TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=CHECKBOX NAME=\"PRINTPROVISIONALTRANSACTION\""
        		+ clsServletUtilities.CHECKBOX_UNCHECKED_STRING
        		+ " STYLE=\"height: 0.25in\""
        		+ " onClick=\"confirmation()\"></TD>"
        		+ "<TD ALIGN=LEFT>&nbsp;</TD>"
        		+ "</TR>"
        		);

        //Print adjustment transactions?
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>Print adjustment transactions </TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=CHECKBOX NAME=\"PRINTADJUSTMENTTRANSACTION\""
        		+ clsServletUtilities.CHECKBOX_UNCHECKED_STRING
        		+ " STYLE=\"height: 0.25in\"></TD>"
        		+ "<TD ALIGN=LEFT>&nbsp;</TD>"
        		+ "</TR>"
        		);

        //Print actual transactions?
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>Print actual transactions </TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=CHECKBOX NAME=\"PRINTACTUALTRANSACTION\""
        		+ clsServletUtilities.CHECKBOX_UNCHECKED_STRING
        		+ " STYLE=\"height: 0.25in\"></TD>"
        		+ "<TD ALIGN=LEFT>&nbsp;</TD>"
        		+ "</TR>"
        		);
        
        //Include details?
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>Show details </TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=CHECKBOX NAME=\"SHOWDETAILS\""
        		+ clsServletUtilities.CHECKBOX_UNCHECKED_STRING
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>&nbsp;</TD>"
        		+ "</TR>"
        		);
        
        //Locations:
       
		try{
			//select location
			String sSQL = "SELECT"
				+ " " + SMTablelocations.sLocation
				+ ", " + SMTablelocations.sLocationDescription
				+ " FROM " + SMTablelocations.TableName
				//+ " WHERE ("
				//	+ "(" + SMTablelocations.ishowintruckschedule + " = 1)"
				//+ ")"
				+ " ORDER BY "  + SMTablelocations.sLocation
			;
			ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					"smfal.FATransactionListSelect");
			pwOut.println("\n  <TR>\n"
				+ "    <TD ALIGN=RIGHT VALIGN=TOP><H4>ONLY show assets assigned to these locations:&nbsp;</H4></TD>\n"
				+ "    <TD>\n");
			String sChecked = "";
			while(rsLocations.next()){
				String sLocation = rsLocations.getString(SMTablelocations.TableName + "." 
					+ SMTablelocations.sLocation).trim();
				pwOut.println(
					"<LABEL NAME=\"" + CHECKBOX_LABEL + sLocation + "\"" + ">"	
					+ "<INPUT TYPE=CHECKBOX NAME=\"" + LOCATION_PARAMETER 
					+ sLocation + "\""
				);
				if (
					(req.getParameter(LOCATION_PARAMETER + sLocation) != null)
				){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}else{
					sChecked = "";
				}
				pwOut.println(" " + sChecked + " "
					+ " width=0.25>" 
					+ sLocation + " - "
					+ rsLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription) + "<BR>" + "\n");
				pwOut.println("</LABEL>" + "\n");
			}
			rsLocations.close();
			pwOut.println("    <TD>&nbsp;</TD>\n");
			pwOut.println("</TR>");
		} catch(Exception e){
			pwOut.println("<BR><FONT COLOR=RED><B>Error [1549052143] reading locations - " + e.getMessage() + ".</B></FONT><BR>");
		}
        
        pwOut.println("</TABLE>");
        
        //pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitPrint' VALUE='Print' STYLE='height: 0.24in'></P>");
  
	}
}

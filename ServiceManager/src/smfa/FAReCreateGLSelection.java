package smfa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMRecreateExportAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smic.ICOption;
import ConnectionPool.WebContextParameters;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableglexportheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class FAReCreateGLSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sFAReCreateGLSelectionCalledClassName = "smcontrolpanel.SMRecreateExportAction";
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAReCreateGLSelection)){
	    	return;
	    }
	    String title = "Re-create Fixed Assets GL Export Batch.";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAPeriodEndProcessing) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    //TJR - 9/23/2016 - for now, we are using the 'IC Export Type' to determine the format of the FA exports:
		//Get the IC export type:
		ICOption icopt = new ICOption();
		try {
			icopt.load(sDBID, getServletContext(), sUserName);
		} catch (Exception e) {
			out.println("<FONT COLOR=RED><B><BR>Error [1474644840] reading IC Options to get export type - " 
				+ icopt.getErrorMessage()
				+ " <BR></B></FONT>");
		}
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + sFAReCreateGLSelectionCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMRecreateExportAction.BATCHLABEL_PARAM + "' VALUE='" + "Depreciation" + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMRecreateExportAction.SOURCELEDGER_PARAM + "' VALUE='" + SMModuleTypes.FA + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMRecreateExportAction.EXPORTTYPE_PARAM + "' VALUE='" + icopt.getExportTo() + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + "CallingClass" + "' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>");
	    
	    try{
	    	List_Criteria(out, sDBID, sUserID, sUserFullName, request);
	    }catch (Exception e){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu"
					+ "?Warning=" + clsServletUtilities.URLEncode("Error displaying fiscal year and period criteria to re-export transactions : " + e.getMessage())
			);
			return;
	    }
	    
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
	
	private void List_Criteria(PrintWriter pwOut, 
							   String sDBIB,
							   String sUserID,
							   String sUserFullName,
							   HttpServletRequest req) throws Exception{

	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
        ArrayList<String> sValues = new ArrayList<String>();
        ArrayList<String> sDescriptions = new ArrayList<String>();
        
	    //Fiscal year:
        sValues.clear();
        sDescriptions.clear();
        //First, add a blank to make sure the user selects one:
        sValues.add("");
        sDescriptions.add("--- Select a fiscal year and period ---");
        
        //Now try to load the list of available fiscal years and periods:
        String SQL = "SELECT DISTINCT "
        	+ SMTableglexportheaders.lbatchnumber
        	+ " FROM " + SMTableglexportheaders.TableName
        	+ " WHERE ("
        		+ "(" + SMTableglexportheaders.ssourceledger + " = '" + SMModuleTypes.FA + "')"
        	+ ") ORDER BY " + SMTableglexportheaders.lbatchnumber + " DESC"
        ;
        ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBIB, "MySQL", this.toString() + ".ListCriteria - user: " 
        												+ sUserID
        												+ " - "
        												+ sUserFullName);
        while (rs.next()){
        	long lBatchNumber = rs.getLong(SMTableglexportheaders.lbatchnumber);
        	long lFiscalYear = lBatchNumber / 100;
        	long lFiscalPeriod = lBatchNumber - (lFiscalYear * 100);
        	sValues.add(Long.toString(lBatchNumber));
        	sDescriptions.add("Fiscal year: " 
        	+ Long.toString(lFiscalYear)
        	+ ", fiscal period: "
        	+ Long.toString(lFiscalPeriod)
        	);
        }
        
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		SMRecreateExportAction.BATCHNUMBER_PARAM, 
        		sValues, 
        		"", 
        		sDescriptions, 
        		"Choose fiscal year and period you wish to re-export:", 
        		"&nbsp;"
        	)
        ); 

        pwOut.println("</TABLE>");
        pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Re-create export' STYLE='height: 0.24in'>");
	}
}

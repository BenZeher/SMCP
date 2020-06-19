package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.OHDirectFinderResults;
import SMClasses.SMFinderFunctions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class SMListCommonEstimateItems extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String FORM_NAME = "MAINFORM";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMEstimate.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditSMEstimateEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
		);   
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		SMEstimate estimate = new SMEstimate(request);
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smedit.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smedit.getFullUserName()
			);
		} catch (Exception e2) {
			smedit.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
			smedit.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e2.getMessage());
			smedit.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		);
			return;
		}
		
	    smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    smedit.getPWOut().println(SMUtilities.getShortcutJSIncludeString(getServletContext()));
		
	    try {
	    	createEditPage(
	    		getEditHTML(
	    			smedit, 
	    			estimate,
	    			request
	    		), 
	    		FORM_NAME,
				smedit.getPWOut(),
				smedit,
				estimate
			);
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTablesmestimatesummaries.lid + "=" + estimate.getslsummarylid()
				+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
				+ "&Warning=Could not load estimate ID: " + estimate.getslid() + " - " + SMUtilities.URLEncode(sError)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	}
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm,
			SMEstimate estimate
	) throws Exception{
		//Create HTML Form
		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
				
		//Create HTML Fields
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		//Add save button
		//pwOut.println("<BR>" + createSaveButton());
		pwOut.println("</FORM>");
	}
	private String getEditHTML(SMMasterEditEntry sm, SMEstimate estimate, HttpServletRequest req) throws Exception{
		String s = "";
		
		return s;
	}
	private String buildItemList(SMEstimate estimate, Connection conn) throws Exception{
		String s = "";
		
		return s;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ) throws Exception{
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			throw new Exception("Error [1395236124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		} catch (IllegalStateException e1) {
			throw new Exception("Error [1395236125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
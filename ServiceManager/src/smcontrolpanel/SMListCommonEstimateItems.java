package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablesmestimatelines;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;

public class SMListCommonEstimateItems extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String FORM_NAME = "MAINFORM";
	private static final int COMMON_ITEMS_LIMIT = 50;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMEstimate.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMListCommonEstimateItemsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
		);   
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		SMEstimate estimate = (SMEstimate)smedit.getCurrentSession().getAttribute(SMEstimate.OBJECT_NAME);
		//System.out.println("[202006194016] - dumpData = " + estimate.dumpData());
		
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
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSMEstimateEdit"
				+ "?" + SMTablesmestimates.lid + "=" + estimate.getslid()
				+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
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
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >" + "\n";
		
		//Make sure we load the summary first:
		try {
			estimate.loadSummary(getServletContext(), sm.getsDBID(), sm.getUserID());
		} catch (Exception e) {
			throw new Exception("Error [202006193335] - could not load summary for estimate number " + estimate.getslid() + " to get commonly used items - " + e.getMessage());
		}
		
		//Get the list of commonly used items:
		String SQL = "SELECT"
			+ " " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber
			+ ", " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.slinedescription
			+ ", " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sunitofmeasure
			+ ", COUNT(" + SMTablesmestimatelines.sitemnumber + ")"
			+ " FROM " + SMTablesmestimatelines.TableName
			+ " LEFT JOIN " + SMTablesmestimatesummaries.TableName
			+ " ON " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.lsummarylid + " = "
				+ SMTablesmestimatesummaries.TableName + "." + SMTablesmestimatesummaries.lid
			+ " WHERE ("
				+ "(" + SMTablesmestimatesummaries.TableName + "." + SMTablesmestimatesummaries.iordertype + " = " + estimate.getsummary().getsiordertype() + ")"
			+ ")"
			+ " GROUP BY " + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber
			+ " ORDER BY COUNT(" + SMTablesmestimatelines.TableName + "." + SMTablesmestimatelines.sitemnumber + ") DESC"
			+ " LIMIT " + COMMON_ITEMS_LIMIT
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sm.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString()));
			while (rs.next()) {
				//Build rows here:
				s += "  <TR> \n";
				
				s += "<TD>"
					+ "<INPUT TYPE = TEXT"
					+ " NAME = \"" + "" + "\""
					+ " ID = \"" + "" + "\""
					+ "</TD> \n"
				;
				
				s += "<TD>"
					+ rs.getString(SMTablesmestimatelines.sitemnumber)
					+ "</TD> \n"
				;
				
				s += "<TD>"
						+ rs.getString(SMTablesmestimatelines.slinedescription)
						+ "</TD> \n"
					;
				
				s += "<TD>"
						+ rs.getString(SMTablesmestimatelines.sunitofmeasure)
						+ "</TD> \n"
					;
				
				s += "  </TR> \n";
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [202006193605] - error reading most commonly used items with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		s += "</TABLE>" + "\n";
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
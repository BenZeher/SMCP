
package smic;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICDeleteInactiveItemsGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICDeleteInactiveItems
			)

		){
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    CurrentSession.removeAttribute(ICDeleteInactiveItemsSelection.DELETE_ERRORS_LIST);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
	    /**************Get Parameters**************/
	    java.sql.Date datDeleteDate = null;
	    String sDeleteDate = clsManageRequestParameters.get_Request_Parameter(ICDeleteInactiveItemsSelection.DELETE_DATE, request);
		try {
			datDeleteDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sDeleteDate);
		} catch (ParseException e1) {
			CurrentSession.setAttribute(
				ICDeleteInactiveItemsSelection.DELETE_ERRORS_LIST, 
				"Error:[1423661739] Invalid delete date: '" + datDeleteDate + "' - " + e1.getMessage());
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
    	//Customized title
    	String sTitle = "Delete Inactive Items";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sTitle + " - " 
	       + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) 
	       + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>"
		   + clsDateAndTimeConversions.nowStdFormat()
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" 
		   + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sTitle + "</B></FONT></TD></TR>"
		   );
		
    	//Delete inactives:
    	String sResult = "";
    	try {
    		sResult = deleteInactives(getServletContext(), sDBID, datDeleteDate, sUserID, sUserFullName);
		} catch (Exception e) {
			CurrentSession.setAttribute(ICDeleteInactiveItemsSelection.DELETE_ERRORS_LIST, URLEncoder.encode(e.getMessage(), "UTF-8"));
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
    	
    	CurrentSession.setAttribute(ICDeleteInactiveItemsSelection.DELETE_ERRORS_LIST, sResult);
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
	
	private String deleteInactives(
		ServletContext context, 
		String sDBID, 
		Date datDeleteDate, 
		String sUserID, 
		String sUserFullName) throws Exception{	
	
		String m_sWarning = "";
		long lSuccessfulDeletesCounter = 0;
		long lUnsuccessfulDeletesCounter = 0;
		//Delete all items with a last transaction date before the entered date
		String SQL = "SELECT "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", MAX("+ SMTableictransactions.datpostingdate +") AS 'TRANSDATE'"
			+ " FROM " + SMTableictransactions.TableName
			+ " LEFT JOIN " + SMTableicitems.TableName
			+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
			+ " = " + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
			+ " WHERE ("
			+ "(" + SMTableicitems.iActive + " = 0" + ")"
			+ ")"
			+ " GROUP BY " + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
			; 

		Connection conn = null;
		try{
			conn = clsDatabaseFunctions.getConnection(
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + ".deleteInactives - User: " + sUserID
					+ " - "
					+ sUserFullName
					
					);
				if (conn == null){
					throw new Exception("Error [20193111613495] " + "Could not get connection to delete inactives.");
				}
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	
			while (rs.next()){
				if (rs.getDate("TRANSDATE").before(datDeleteDate)){
					String sItemNum = rs.getString(SMTableicitems.sItemNumber);
					ICItem item = new ICItem(sItemNum);
					//If an item cannot be deleted, add it to the warning string, but go on:
					if (!item.delete(sItemNum, conn)){
							m_sWarning = m_sWarning + item.getErrorMessageString() + " ";
						lUnsuccessfulDeletesCounter++;
					}else{
						lSuccessfulDeletesCounter++;
					}
				}
			}
			rs.close();
			
			//Now delete all inactive items that do not have a last transaction date
			SQL = " SELECT " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
					+ " FROM " + SMTableicitems.TableName
					+ " LEFT JOIN " + SMTableictransactions.TableName 
					+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
					+ " = " + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
					+ " WHERE (( " + SMTableicitems.iActive + " = 0" + ") "
						+ "AND (" + SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate + " IS NULL))"			
					;
			ResultSet rs1 = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs1.next()){
				String sItemNum = rs1.getString(SMTableicitems.sItemNumber);
				ICItem item = new ICItem(sItemNum);
				if (!item.delete(sItemNum, conn)){
					m_sWarning = m_sWarning +  item.getErrorMessageString() + " ";
					lUnsuccessfulDeletesCounter++;
				}else{
					lSuccessfulDeletesCounter++;
				}
			}	
			rs1.close();
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080813]");
		}catch (Exception e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080913]");
			throw new Exception("Error [1573161124] deleting inactives - " + e.getMessage() + ".");
		}

		return Long.toString(lSuccessfulDeletesCounter) + " items WERE deleted, " + Long.toString(lUnsuccessfulDeletesCounter) + " could NOT be deleted."
			+ "<BR><BR>" + m_sWarning
		;
	}
}

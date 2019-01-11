package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.SMTablereminderusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMDisplayRemindersAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> arrScheduleCodes;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);	
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1)){return;}
		HttpSession CurrentSession = request.getSession(true);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
							+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		
		if(clsManageRequestParameters.get_Request_Parameter(SMDisplayReminders.Paramskipreminders, request).compareToIgnoreCase("1") == 0){
			CurrentSession.removeAttribute(SMUtilities.SMCP_SESSION_PARAM_CHECK_SCHEDULE);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)
				);
			return;
		}
		String warnings = (String) clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if(!warnings.isEmpty()){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayReminders"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)
					+ "&" + "Warning=" + warnings 
				);
			return;
		}
		boolean bErrors = false;
		Enumeration <String> e = request.getParameterNames();
		arrScheduleCodes = new ArrayList<String> (0);
		String sParam = "";
		arrScheduleCodes.clear();
		
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			String sScheduleCodesToUpdate = "";
			if (sParam.contains(SMDisplayReminders.SCHEDULE_CODE_ID_MARKER)){
				sScheduleCodesToUpdate = sParam.substring(SMDisplayReminders.SCHEDULE_CODE_ID_MARKER.length(), sParam.length());
			}		
			if (request.getParameter(sParam) != null && sScheduleCodesToUpdate.compareToIgnoreCase("") != 0){
				arrScheduleCodes.add(sScheduleCodesToUpdate);
			}	
		}
		
		//Get connection
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) 
				+ ":update schedule acknowledged date - user: " + sUserID
				+ " - "
				+ sUserFullName
				);
		if (conn == null){
			m_sErrorMessageArray.add("Error [149981852] getting data connection.");
			bErrors = true;
		}
				
		String sDateToUpdate = "";		
		for (int i = 0; i < arrScheduleCodes.size(); i++){
			//Get the date of the schedule from the parameters
			sDateToUpdate = clsManageRequestParameters.get_Request_Parameter(SMDisplayReminders.SCHEDULE_CODE_ID_MARKER + arrScheduleCodes.get(i), request);
			String SQL = "UPDATE " + SMTablereminderusers.TableName 
				+ " SET "
				+ SMTablereminderusers.datlastacknowledgedreminderdate + "=" 
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(sDateToUpdate) + "'"
				+ " ," + SMTablereminderusers.datlastacknowledgeddate + "=" + " NOW()"
				+ " WHERE ("
				+ "(" + SMTablereminderusers.sschedulecode + "='" + arrScheduleCodes.get(i) + "')"
				+ " AND "
				+ "(" + SMTablereminderusers.luserid + "='" + sUserID + "')"
				+ ")"
				;
			try{
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [149953852] updating acknowleged date in scheduledusers - "
							 + arrScheduleCodes.get(i) + " For: " + sUserName );
				bErrors = true;
				}
			}catch(Exception ex){
				m_sErrorMessageArray.add("Error [1452847846] updating acknowleged date in scheduledusers schedulecode: "
						 + arrScheduleCodes.get(i) + " User: " + sUserName + " - " + ex.getMessage());
				bErrors = true;			
			}			
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080469]");
		
		if(!bErrors){
		CurrentSession.removeAttribute(SMUtilities.SMCP_SESSION_PARAM_CHECK_SCHEDULE);
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)
			);
		return;
		}else{
			String sErrorList = "";
			for (String s : m_sErrorMessageArray){
				sErrorList += s + " ";
			}
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplaySchedule"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)
					+ "&" + "Warning=" + sErrorList.replace(" ", "%20")
				);
			return;
		}
	}
	
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
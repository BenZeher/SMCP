package smas;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmsequenceusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ASAuthorizeAlarmUsersAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sSessionTag = "";
	private String sCompanyName = "";
	private String sConfFile = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASAuthorizeAlarmSequenceUsers)){return;}

		String sAlarmSequenceID = clsManageRequestParameters.get_Request_Parameter(SMTablessalarmsequences.lid, request);
	    String title = "Updating authorized users for alarm '" + sAlarmSequenceID + "'";
	    String subtitle = "";
		
	    HttpSession CurrentSession = request.getSession(true);
	    sSessionTag = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sConfFile = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sConfFile), sCompanyName));
	    
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_SESSION_PARAM_SESSIONTAG + "=" + sSessionTag 
				+ "\">Return to user login</A><BR><BR>");

	    ArrayList<String> sSQLList = new ArrayList<String>(0);
	    //First, delete all the authorized users for this device:
	    sSQLList.add("DELETE FROM " + SMTablessalarmsequenceusers.TableName 
	    	+ " WHERE ("
			+ SMTablessalarmsequenceusers.lalarmsequenceid + " = '" + sAlarmSequenceID + "'"
	    	+ ")")
	    ;
	    
	    //Now add back in all the authorized users for this alarm:
		Enumeration<?> paramNames = request.getParameterNames();
	    String sUserMarker = ASAuthorizeAlarmUsersEdit.USER_CHECKBOX_PARAMETER_PREFIX;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
		  if (sParamName.contains(sUserMarker)){
			  String sUserID = (sParamName.substring(sParamName.indexOf(sUserMarker) + sUserMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(
				"INSERT INTO " + SMTablessalarmsequenceusers.TableName + "(" 
					+ SMTablessalarmsequenceusers.lalarmsequenceid
					+ ", " + SMTablessalarmsequenceusers.luserid
					+ ") VALUES ("
					+ "'" + sAlarmSequenceID + "'"
					+ ", " + sUserID + ""
					+ ")" 	  
			);
		  }
	    }

	    try{
	    	clsDatabaseFunctions.executeSQLsInTrans(sSQLList, getServletContext(), sConfFile);
	    }catch (SQLException ex){
	    	smaction.redirectAction(
	    		"Error updating alarm users - " + ex.getMessage(), 
	    		"", 
	    		SMTablessalarmsequences.lid + "=" + sAlarmSequenceID);
		    out.println("Error [1462387124] - could not update alarm users - " + ex.getMessage() + ".<BR>");
		}
	    
    	smaction.redirectAction(
	    		"", 
	    		"Alarm users were successfully updated", 
	    		SMTablessalarmsequences.lid + "=" + sAlarmSequenceID);
	    return;
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
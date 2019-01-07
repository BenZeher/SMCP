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
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessdeviceusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ASAuthorizeDeviceUsersAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASAuthorizeDeviceUsers)){return;}

		String sDeviceID = clsManageRequestParameters.get_Request_Parameter(SMTablessdevices.lid, request);
	    String title = "Updating authorized users for device '" + sDeviceID + "'";
	    String subtitle = "";
		
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    ArrayList<String> sSQLList = new ArrayList<String>(0);
	    //First, delete all the authorized users for this device:
	    sSQLList.add("DELETE FROM " + SMTablessdeviceusers.TableName 
	    	+ " WHERE ("
			+ SMTablessdeviceusers.ldeviceid + " = '" + sDeviceID + "'"
	    	+ ")")
	    ;
	    
	    //Now add back in all the authorized users for this device:
		Enumeration<?> paramNames = request.getParameterNames();
	    String sUserMarker = ASAuthorizeDeviceUsersEdit.USER_CHECKBOX_PARAMETER_PREFIX;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
		  if (sParamName.contains(sUserMarker)){
			  String sUserID = (sParamName.substring(sParamName.indexOf(sUserMarker) + sUserMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(
				"INSERT INTO " + SMTablessdeviceusers.TableName + "(" 
					+ SMTablessdeviceusers.ldeviceid
					+ ", " + SMTablessdeviceusers.luserid
					+ ") VALUES ('" 
					+ sDeviceID 
					+ "', " 
					+ sUserID + ")" 	  
			);
		  }
	    }

	    try{
	    	clsDatabaseFunctions.executeSQLsInTrans(sSQLList, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	smaction.redirectAction(
	    		"Error updating device users - " + ex.getMessage(), 
	    		"", 
	    		SMTablessdevices.lid + "=" + sDeviceID);
		    out.println("Error [1459171829] - could not update device - " + ex.getMessage() + ".<BR>");
		}
	    
    	smaction.redirectAction(
	    		"", 
	    		"Device users were successfully updated", 
	    		SMTablessdevices.lid + "=" + sDeviceID);
	    return;
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
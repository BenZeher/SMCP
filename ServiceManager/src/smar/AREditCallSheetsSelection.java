package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablecallsheets;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class AREditCallSheetsSelection extends HttpServlet {

	public static final String PARAM_SHOW_DROP_DOWN_LIST = "SHOWDROPDOWN";
	public static final String PARAM_DROP_DOWN_LIST = "CALLSHEETDROPDOWN";
	
	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smar.AREditCallSheetsEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		ARCallSheet entry = new ARCallSheet();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditBids
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.AREditCallSheets)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		smeditselect.showAddNewButton(true);
		
	    try {
	    	smeditselect.createEditForm(getEditHTML(smeditselect, request));
		} catch (SQLException e) {
    		smeditselect.getPrintWriter().println("Could not create edit form - " + e.getMessage());
    		smeditselect.getPrintWriter().println("</HTML>");
			return;
		}
	    return;

	}
	private String getEditHTML(SMMasterEditSelect smselect, HttpServletRequest req) throws SQLException{

		String s = "";
	    String sEditCode = "";
	    if (req.getParameter(ARCallSheet.ParamsID) != null){
	    	sEditCode = req.getParameter(ARCallSheet.ParamsID);
	    }
	    
		s+= 
			"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
			+ smselect.getsDBID() 
			+ "\">Return to Accounts Receivable Main Menu</A><BR>"
			+ "<P>Enter " + ARCallSheet.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ ARCallSheet.ParamsID + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ "8"
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=" + ARCallSheet.ParamObjectName
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&ReturnField=" + ARCallSheet.ParamsID
				+ "&SearchField1=" + ARCallSheet.ParamsAcct
				+ "&SearchFieldAlias1=Customer%10Account"
				+ "&SearchField2=" + ARCallSheet.ParamsCallSheetName
				+ "&SearchFieldAlias2=Call%20Sheet%20Name"
				+ "&SearchField3=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias3=Customer%20Name"
				+ "&SearchField4=" + ARCallSheet.ParamsPhone
				+ "&SearchFieldAlias4=Phone%20Number"
				+ "&SearchField5=" + ARCallSheet.ParamsOrderNumber
				+ "&SearchFieldAlias5=Order%20Number"
				+ "&ResultListField1=" + ARCallSheet.ParamsID
				+ "&ResultHeading1=ID"
				+ "&ResultListField2=" + ARCallSheet.ParamsAcct
				+ "&ResultHeading2=Account"
				+ "&ResultListField3=" + SMTablearcustomer.sCustomerName
				+ "&ResultHeading3=Customer%20Name"
				+ "&ResultListField4=" + ARCallSheet.ParamsCallSheetName
				+ "&ResultHeading4=Call%20Sheet%20Name"
				+ "&ResultListField5=" + ARCallSheet.ParamsJobPhone
				+ "&ResultHeading5=Phone"
				+ "&ResultListField6=" + ARCallSheet.ParamsCollector
				+ "&ResultHeading6=Collector"
				+ "&ResultListField7=" + ARCallSheet.ParamsOrderNumber
				+ "&ResultHeading7=Order"
				+ "&ResultListField8=" + ARCallSheet.ParamdatLastContact
				+ "&ResultHeading8=Last%20contact"
				+ "&ResultListField9=" + ARCallSheet.ParamdatNextContact
				+ "&ResultHeading9=Next%20Contact"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
				+ "\"> Find call sheet</A>";
		
		//If a drop-down is requested, show that:
		//if (req.getParameter(PARAM_SHOW_DROP_DOWN_LIST) != null){
			//show a drop down of call sheet names:
			String SQL = "SELECT"
				+ " " +SMTablecallsheets.sID
				+ ", " + SMTablecallsheets.sAcct
				+ ", " + SMTablecallsheets.sCallSheetName
				+ " FROM " + SMTablecallsheets.TableName
				+ " ORDER BY " + SMTablecallsheets.sAcct + ", " + SMTablecallsheets.sCallSheetName
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(),
					smselect.getsDBID(), 
					"MySQL",
					this.toString() + ".getEditHTML - user: " + smselect.getUserID()
					+ " - "
					+ smselect.getFullUserName()
						);
				s += "<BR><BR>Or choose from the list:&nbsp;"
					+ "<SELECT NAME=\"" + PARAM_DROP_DOWN_LIST + "\">"
				;
				
				String sSelectedItem = clsManageRequestParameters.get_Request_Parameter(PARAM_DROP_DOWN_LIST, req);
				if (sSelectedItem.compareToIgnoreCase("") == 0){
					s += "<OPTION SELECTED VALUE = " + "-1" + ">";
				}else{
					s += "<OPTION VALUE = " + "-1" + ">";
				}
				s += "*** SELECT A CALL SHEET ***";
				while (rs.next()){
					String sSelected = "";
					String sID = Long.toString(rs.getLong(SMTablecallsheets.sID));
					if (sSelectedItem.compareToIgnoreCase(sID) == 0){
						sSelected = "SELECTED ";
					}
					s += "<OPTION " + sSelected + "VALUE = " + sID + ">"
						+ sID + " "
						+ rs.getString(SMTablecallsheets.sAcct).trim() + " - " 
						+ rs.getString(SMTablecallsheets.sCallSheetName).trim()
						;
				}
				rs.close();
			} catch (SQLException e) {
				s += "<BR>Error reading call sheet list - " + e.getMessage();
			}
			s += "</SELECT>";
			
			s += "<BR>(<B>NOTE:</B> If you enter a call sheet number in the box above, that number will be used,"
				+ " to retrieve the call sheet regardless of what is"
				+ " selected in the drop down list.)";
		//}
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
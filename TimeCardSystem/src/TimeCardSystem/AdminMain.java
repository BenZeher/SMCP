package TimeCardSystem;

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

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTablecompanyprofile;

/** Servlet that reads pin number for validation.*/

public class AdminMain extends HttpServlet {

	static final long serialVersionUID = 0;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get a session for this user.
		HttpSession CurrentSession = request.getSession(true);
		//session will expire in one hour.
		CurrentSession.setMaxInactiveInterval(TimeCardUtilities.MAX_SESSION_INTERVAL);

		//check for valid pin number
		if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER) == null){
			//if there is no passwd passed in, check session for stored passwd
			if ((String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER) == null){
				//there is no valid passwd, go back to login screen
				CurrentSession.invalidate();
				out.println("<BR>No valid pin number was passed in, and the current session is void. Please login again.");
			}else{
				//a pin is already stored in session, do nothing.
			}
		}else{
			//store this pinnumber into session. overwrite any old one.
			CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER, request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER));
		}
		
		String sDatabaseServer = "";
		String sDB = "";
		
		
		if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB) == null){
			if((String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) == null){
				CurrentSession.invalidate();
				out.println("<BR>No valid db was passed in, and the current session is void. Please login again.");
				return;
			}else{
				sDB = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
				sDatabaseServer = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB_SERVER);
			}
		}else{
			try {
				sDB = (String)TimeCardUtilities.getDatabaseName(request, null, getServletContext());
				sDatabaseServer = TimeCardUtilities.getDatabaseServer(request, null, getServletContext());
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB, sDB);
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB_SERVER, sDatabaseServer);
			} catch (Exception e1) {
				out.println("<BR>Error [1541532821] "+e1.getMessage()+"");
				return;
			}
		}
		
		
		
		//check for valid conf name
		try {
			if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB) == null){
				//if there is no conf name passed in, check session for stored passwd
				if ((String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) == null){
					//there is no conf name, go back to login screen
					CurrentSession.invalidate();
					out.println("<BR>No valid db was passed in, and the current session is void. Please login again.");
					return;
				}else{
					//a conf name is already stored in session, do nothing.
				}
			}else{
				//store this conf name into session. overwrite any old one.
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB, request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB));
			}
		} catch (Exception e2) {
			out.println("<BR>Error with session attribute - " + e2.getMessage());
			return;
		}
		
		
		//Get the company information:
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;
		
		String sCompanyName = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDB, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				sCompanyName = rs.getString(TCSTablecompanyprofile.sCompanyName);
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, sCompanyName);
				rs.close();
			}else{
				out.println("<BR>Could not read company name.");
				out.println("</BODY></HTML>");
				rs.close();
				return;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading read company name: " + e.getMessage()+ ".");
			out.println("</BODY></HTML>");
			return;
		}
				
		String sTitle = "Time Card System";
		String sSubtitle = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();

		//out.println(TimeCardUtilities.BarTitleSubBGColor(bar, title, "Main menu", TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
		
		//out.println("<BR>PinNumber: " + sPinNumber + "<BR>");

		try{
			String sPinNumber = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER);
			sSQL = TimeCardSQLs.Get_Employee_Info_By_Pin_SQL(sPinNumber);
			//System.out.println("Get_Employee_Info_By_Pin_SQL = " + sSQL);
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
			if (rs.next()){
				String sFullName = rs.getString(Employees.TableName + "." + Employees.sEmployeeFirstName) + " " + rs.getString(Employees.TableName + "." + Employees.sEmployeeLastName);
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID, rs.getString(Employees.TableName + "." + Employees.sEmployeeID));
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EMPLOYEEFULNAME, sFullName);
				CurrentSession.setAttribute(clsServletUtilities.SESSION_PARAM_FULL_USER_NAME, sFullName + " - " + sCompanyName);
				
				out.print("<FONT SIZE=3>Hello, <B>" + sFullName + "</B>.</FONT> " +
				"	Welcome to the administrative section for <B>" 
						+ (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME) + "</B>.<BR>");//You are a member of ");
				out.println(
						ConnectionPool.WebContextParameters.getInitProgramTitle(getServletContext())
						+ " version " + TimeCardUtilities.sProgramVersion
						+ " last updated " + TimeCardUtilities.sLastUpdated
						+ " currently running on server <B> " + clsServletUtilities.getHostName()
						+ "</B>, using database server <B>" +sDatabaseServer+"</B>"
						+ ".<BR><BR>"
				);
						
				out.println("<TABLE BORDER=0 WIDTH=100%>");
				//out.println("<TR><TD><H1><span id=\"servertime\"></span></H1></TD></TR>");

				//save current user's pin number for future reference.
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER, sPinNumber);
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID, rs.getString(Employees.sEmployeeID));
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB, sDB);
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB_SERVER, sDatabaseServer);

				//get user access control information and save it in the session
				sSQL = TimeCardSQLs.Get_Employee_Access_Control_Info(CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString());
				ResultSet rsACLInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO, rsACLInfo);
				
				//list of report available for administrators:
				out.println("<TR><TD WIDTH=50% VALIGN=TOP><TABLE WIDTH=100% BORDER=1>");

				out.println("<TR><TD WIDTH=100%><H2>Manage Employees</H2></TD>");

				ArrayList <String>alPermissionsList = new ArrayList<String>(0);
				
				//Build out the menu links here:
				try {
					alPermissionsList.add(AccessControlFunctionList.EditEmployeeConfidentialInformation);
					alPermissionsList.add(AccessControlFunctionList.ViewEmployeeConfidentialInformation);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeeList?ShowInactive=false&InfoType=confidential", 
							"Edit Employee Confidential Information", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditEmployeeGeneralInformation);
					alPermissionsList.add(AccessControlFunctionList.ViewEmployeeGeneralInformation);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeeList?ShowInactive=false&InfoType=general", 
							"Edit Employee General Information", 
							alPermissionsList,
							out,
							response
						);
					
					// 2/7/2018 - TJR - removed per email today from Jaclyn:
//					alPermissionsList.clear();
//					alPermissionsList.add(AccessControlFunctionList.EmployeeLeaveBalanceSummary);
//					printMenuLink(
//							rsACLInfo, 
//							"TimeCardSystem.EmployeeLeaveSummaryCriteriaSelection?ShowInactive=false", 
//							"Employee Leave Balance Summary", 
//							alPermissionsList,
//							out,
//							response
//						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EmployeeLeaveManager);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeeLeaveManagerCriteriaSelection", 
							"Employee Leave Manager", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EmployeePerformanceReport);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeePerformanceListCriteriaSelection", 
							"Employee Performance Report", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.OnClockEmployeeList);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.OnClockEmployeeListCriteriaSelection", 
							"On-Clock Employee List", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.UNExcusedLateReport);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.LateReportCriteriaSelection", 
							"UN-Excused Late Report", 
							alPermissionsList,
							out,
							response
						);
					
					out.println("<TR><TD WIDTH=100%><H2>Payroll Process</H2></TD>");
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.ManagerReviewListTimeEditing);
					alPermissionsList.add(AccessControlFunctionList.ManagerReviewListNoteEditing);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.ManagerReviewListCriteriaSelection?ShowInactive=false", 
							"Manager Review List", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.PeriodTotalTimeReport);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.PeriodTotalDispCriteriaSelection", 
							"Period Total Time Report", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.PostPeriodTotalTime);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.PostPeriodTotalCriteriaSelection", 
							"Post Period Total Time", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.RawTimeEntryList);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.RawEntryListCriteriaSelection", 
							"Raw Time Entry List", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.SpecialNoteReport);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.SpecialNoteReportCriteriaSelection", 
							"Special Note Report", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.MADGICReport);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.MADGICReportCriteriaSelection", 
							"MADGIC Report", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.MANAGE_QUERIES);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TCQuerySelect", 
							"Manage Queries", 
							alPermissionsList,
							out,
							response
						);
					
					out.println("</TABLE></TD><TD WIDTH=50% VALIGN=TOP><TABLE WIDTH=100% BORDER=1>");
					out.println("<TR><TD WIDTH=100%><H2>General Reports</H2></TD>");
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.ViewEmployeeContactInformationList);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeeContactListCriteriaSelection", 
							"Employee Contact Information", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.ViewMilestoneReport);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.MilestonesReportCriteriaSelection", 
							"Milestones Report", 
							alPermissionsList,
							out,
							response
						);
					
					out.println("<TR><TD WIDTH=100%><H2>System Configuration</H2></TD>");
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditCompanyProfile);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TCSEditCompanyProfile", 
							"Edit Company Profile", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditDepartmentInformation);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.DepartmentList", 
							"Edit Department Information", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditEmployeeAuxiliaryInformationField);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeeAuxiFieldList", 
							"Edit Employee Auxiliary Information Fields", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditEmployeeStatus);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.EmployeeStatusList", 
							"Edit Employee Status", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditLeaveAdjustmentType);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.LeaveAdjustmentTypeList", 
							"Edit Leave Adjustment Type", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditPayType);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.PayTypeList", 
							"Edit Pay Type", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditTimeEntryType);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TimeEntryTypeList", 
							"Edit Time Entry Type", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditEmployeeTypes);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TCEditEmployeeTypeSelection", 
							"Edit Employee Types", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditMilestones);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TCEditMilestonesSelection", 
							"Edit Milestones", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.ManageUserAccessControl);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.ManageACGroups", 
							"Manage User Access Control", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.ManagerDepartmentDesignation);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.ManagerAccessControl", 
							"Manager-Department Designation", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditMADGICEventTypes);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TCEditMADGICEventTypesSelection", 
							"Edit MADGIC Event Types", 
							alPermissionsList,
							out,
							response
						);
					
					alPermissionsList.clear();
					alPermissionsList.add(AccessControlFunctionList.EditMADGICEvents);
					printMenuLink(
							rsACLInfo, 
							"TimeCardSystem.TCEditMADGICEventsSelection?lid=-1", 
							"Edit MADGIC Events", 
							alPermissionsList,
							out,
							response
						);
					
				} catch (Exception e) {
					out.println("<BR>Error constructing menus: " + e.getMessage()+ ".");
					out.println("</BODY></HTML>");
					return;
				}
				
				out.println("</TABLE></TD></TR></TABLE>");
	    	
			}else{
				out.println("The pin code is not valid. Please try again.");	
				out.println("<META http-equiv='Refresh' content='2;URL=" 
						+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext())
						+ MainLogin.CLASS_NAME
						+ "?" + TimeCardUtilities.REQUEST_PARAMETER_DB + "=" + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)
						+ "&" + TimeCardUtilities.REQUEST_PARAMETER_ADMIN_MODE + "=y"
						+ "'>");
			}
			rs.close();

		} catch (SQLException ex) {
			// handle any errors
			out.println("Error in AdminMain class - " + ex.getMessage());
		} 
		out.println("</BODY></HTML>");
	}

	private void printMenuLink(
		ResultSet rsACLInfo, 
		String sLink, 
		String sLinkName, 
		ArrayList<String>alPermissionList, 
		PrintWriter pwOut, 
		HttpServletResponse resp
		) throws Exception{
		
		boolean bIsPermitted = false;
		
		try {
			for(int i = 0; i < alPermissionList.size(); i++){
				if (TimeCardUtilities.IsAccessible(rsACLInfo, alPermissionList.get(i))){
					bIsPermitted = true;
				}
			}
			
			if (bIsPermitted){ 
				String EncodedURL = resp.encodeURL("" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + sLink);
				pwOut.println("<TR><TD><A HREF=\"" + EncodedURL + "\">" + sLinkName + " </TD></TR>");
			}
		} catch (Exception e) {
			throw new Exception("Error [1518014990] printing menu for '" + sLinkName + "' - " + e.getMessage());
		}		
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}


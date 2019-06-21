package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMSystemFunctions;
import SMClasses.SMAppointmentCalendarGroup;
import SMDataDefinition.SMTableappointmentgroups;
import ServletUtilities.clsDatabaseFunctions;

public class SMEditAppointmentGroupsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMEditAppointmentGroupsEdit";
	private static final String OBJECT_NAME = SMAppointmentCalendarGroup.ParamObjectName;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditAppointmentGroups
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditAppointmentGroups)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		smeditselect.printHeaderTable();

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
	    String sID = "";
	    if (req.getParameter(SMTableappointmentgroups.igroupid) != null){
	    	sID = req.getParameter(SMTableappointmentgroups.igroupid);
	    }
	    
	    s += 
	    	"<B>Appointment Group:<BR>"
	    	+ "\n<SELECT NAME=\"" + SMTableappointmentgroups.igroupid + "\">"
	    	+ "\n<OPTION VALUE=\"" + "" + "\">*** Select appointment group ***";
	    
	    //Drop down the list:
	    String SQL = "SELECT "
	    	+ " " + SMTableappointmentgroups.igroupid
	    	+ ", " + SMTableappointmentgroups.sappointmentgroupdesc
	    	+ ", " + SMTableappointmentgroups.sappointmentgroupname
	    	+ " FROM " + SMTableappointmentgroups.TableName
	    	+ " ORDER BY " + SMTableappointmentgroups.sappointmentgroupname
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					smselect.getsDBID(), "MySQL", SMUtilities
							.getFullClassName(this.toString())
							+ ".getEditHTML - user: " 
							+ smselect.getUserID()
							+ " - "
							+ smselect.getFullUserName()
					);
			while (rs.next()) {
				String sReadCode = Long.toString(rs.getLong(SMTableappointmentgroups.igroupid));
	
				s += "\n<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" 
					+ rs.getString(SMTableappointmentgroups.sappointmentgroupname) + " - "
					+ rs.getString(SMTableappointmentgroups.sappointmentgroupdesc)
					;
			}
			rs.close();
		} catch (SQLException e) {
			s += "</SELECT><BR><B>Error reading " + OBJECT_NAME + " data - " + e.getMessage();
		}
		s+= "</SELECT>";
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
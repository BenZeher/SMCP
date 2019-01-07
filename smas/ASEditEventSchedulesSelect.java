package smas;

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
import SMDataDefinition.SMTablesseventschedules;
import ServletUtilities.clsDatabaseFunctions;

public class ASEditEventSchedulesSelect extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smas.ASEditEventSchedulesEdit";
	private static String OBJECT_NAME = SSEventSchedule.ParamObjectName;
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
			SMSystemFunctions.ASEditEventSchedules
			);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ASEditEventSchedules)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" 
				+ smeditselect.getSessionTag() + "\">Return to Alarm Systems Main Menu</A><BR>");
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
	    if (req.getParameter(SMTablesseventschedules.lid) != null){
	    	sID = req.getParameter(SMTablesseventschedules.lid);
	    }
	    
	    s += 
	    	"<B>Event schedule:<BR>"
	    	+ "<SELECT NAME=\"" + SMTablesseventschedules.lid + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select event schedule ***";
	    
	    //Drop down the list:
	    String SQL = "SELECT *"
	    	+ " FROM " + SMTablesseventschedules.TableName
	    	+ " ORDER BY " + SMTablesseventschedules.sname 
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
				smselect.getConfFile(), "MySQL", SMUtilities
					.getFullClassName(this.toString())
					+ ".getEditHTML - user: " 
					+ smselect.getUserID()
					+ " - "
					+ smselect.getFullUserName()
					);
			while (rs.next()) {
				String sReadCode = Long.toString(rs.getLong(SMTablesseventschedules.TableName + "." + SMTablesseventschedules.lid));
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" + rs.getString(SMTablesseventschedules.sname) + " - "
					+ rs.getString(SMTablesseventschedules.sdescription)
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

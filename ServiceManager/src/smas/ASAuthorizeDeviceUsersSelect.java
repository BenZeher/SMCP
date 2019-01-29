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
import SMDataDefinition.SMTablessdevices;
import ServletUtilities.clsDatabaseFunctions;

public class ASAuthorizeDeviceUsersSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smas.ASAuthorizeDeviceUsersEdit";
	private static final String OBJECT_NAME = "authorized device users";
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
				SMSystemFunctions.ASAuthorizeDeviceUsers
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ASAuthorizeDeviceUsers)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		smeditselect.showAddNewButton(false);
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Alarm Systems Main Menu</A><BR>");
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
	    if (req.getParameter(SMTablessdevices.lid) != null){
	    	sID = req.getParameter(SMTablessdevices.lid);
	    }
	    
	    s += 
	    	"<B>Authorized users for device:<BR>"
	    	+ "<SELECT NAME=\"" + SMTablessdevices.lid + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select device ***";
	    
	    //Drop down the list:
	    String SQL = "SELECT "
	    	+ " " + SMTablessdevices.lid
	    	+ ", " + SMTablessdevices.sdescription
	    	+ ", " + SMTablessdevices.iactive
	    	+ " FROM " + SMTablessdevices.TableName
	    	+ " ORDER BY " + SMTablessdevices.sdescription 
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
					smselect.getsDBID(), "MySQL", SMUtilities
						.getFullClassName(this.toString())
						+ ".getEditHTML - user: " + smselect.getUserID()
						+ " - "
						+ smselect.getFullUserName()
					);
			while (rs.next()) {
				String sReadCode = Long.toString(rs.getLong(SMTablessdevices.TableName + "." + SMTablessdevices.lid));
				String sInactive = "";
				if (rs.getInt(SMTablessdevices.iactive) == 0){
					sInactive = " (INACTIVE)";
				}
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" + rs.getString(SMTablessdevices.sdescription)
					+ " - " + sReadCode + sInactive;
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
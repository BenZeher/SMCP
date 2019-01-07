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
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsDatabaseFunctions;

public class ASEditControllersSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smas.ASEditControllersEdit";
	private static String OBJECT_NAME = SSController.ParamObjectName;
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
				SMSystemFunctions.ASEditControllers
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ASEditControllers)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
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
	    if (req.getParameter(SMTablesscontrollers.lid) != null){
	    	sID = req.getParameter(SMTablesscontrollers.lid);
	    }
	    
	    s += 
	    	"<B>Controller:<BR>"
	    	+ "<SELECT NAME=\"" + SMTablesscontrollers.lid + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select controller ***";
	    
	    //Drop down the list:
	    String SQL = "SELECT "
	    	+ " " + SMTablesscontrollers.lid
	    	+ ", " + SMTablesscontrollers.iactive
	    	+ ", " + SMTablesscontrollers.scontrollername
	    	+ ", " + SMTablesscontrollers.sdescription
	    	+ " FROM " + SMTablesscontrollers.TableName
	    	+ " ORDER BY " + SMTablesscontrollers.scontrollername
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
				String sReadCode = Long.toString(rs.getLong(SMTablesscontrollers.lid));
				String sInactive = "";
				if (rs.getInt(SMTablesscontrollers.iactive) == 0){
					sInactive = " (INACTIVE)";
				}
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" 
					+ rs.getString(SMTablesscontrollers.scontrollername) + " - "
					+ rs.getString(SMTablesscontrollers.sdescription)
					+ sInactive;
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
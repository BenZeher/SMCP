package smbk;

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
import SMDataDefinition.SMTablebkbanks;
import ServletUtilities.clsDatabaseFunctions;

public class BKEditBanksSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smbk.BKEditBanksEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				BKBank.ParamObjectName,
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.BKEditBanks
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.BKEditBanks)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Bank Functions Main Menu</A><BR>");
	    try {
	    	smeditselect.getPrintWriter().println(javaScript());
	    	smeditselect.createEditForm(getEditHTML(smeditselect, request));
		} catch (SQLException e) {
    		smeditselect.getPrintWriter().println("Could not create edit form - " + e.getMessage());
    		smeditselect.getPrintWriter().println("</HTML>");
			return;
		}
	    return;

	}
	
	public String javaScript(){
		String s = "";
		s += " <script>\n"
		  +  "   window.addEventListener(\"beforeunload\",function(){\n"
		  +  "     doucment.getElementsByName(\""+SMMasterEditSelect.SUBMIT_EDIT_BUTTON_NAME+"\")[0].disabled = true;\n"
		  +  "     document.body.setAttribute(\"style\",\"pointer-events: none; color: black; cursor: not-allowed; display: inline-block; text-decoration: none;\");\n"
		  +  "     document.documentElement.style.cursor = \"wait\";\n"
		  +"      });\n"
//		  +  "   function Import(){\n"
//		  + "        alert(\"Hello\");\n"
////		  +  "     document.getElementById(id).setAttribute(\"style\",\"pointer-events: none; color: black; cursor: not-allowed; opacity: 0.5; display: inline-block; text-decoration: none; \");\n"
////		  +  "     window.location.href = link;\n"
//		  +  "      }\n"
		  +  " </script>\n";
		return s;
	}
	
	
	private String getEditHTML(SMMasterEditSelect smselect, HttpServletRequest req) throws SQLException{

		String s = "";
	    String sID = "";
	    if (req.getParameter(BKBank.Paramlid) != null){
	    	sID = req.getParameter(BKBank.Paramlid);
	    }
	    
	    s += 
	    	"<B>Bank:<BR>"
	    	+ "<SELECT NAME=\"" + BKBank.Paramlid + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select bank ***";
	    
	    //Drop down the banks:
	    String SQL = "SELECT "
	    	+ " " + SMTablebkbanks.lid
	    	+ ", " + SMTablebkbanks.sshortname
	    	+ " FROM " + SMTablebkbanks.TableName
	    	+ " ORDER BY " + SMTablebkbanks.sshortname
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
				String sReadCode = Long.toString(rs.getLong(SMTablebkbanks.lid));
				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sID) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" + sReadCode + " - "
						+ rs.getString(SMTablebkbanks.sshortname);
			}
			rs.close();
		} catch (SQLException e) {
			s += "</SELECT><BR><B>Error reading bank data - " + e.getMessage();
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
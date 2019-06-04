package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smap.APVendor;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsServletUtilities;

public class APEditVendorSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smap.APEditVendorsEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		APVendor entry = new APVendor();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditVendors
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.APEditVendors)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Accounts Payable Main Menu</A><BR>");
		
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
	    if (req.getParameter(APVendor.Paramsvendoracct) != null){
	    	sEditCode = req.getParameter(APVendor.Paramsvendoracct);
	    }
				
		s+= 
			"<P>Enter " + APVendor.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ APVendor.Paramsvendoracct + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" + Integer.toString(SMTableicvendors.svendoracctLength)
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s += "<A HREF=\""
			+ APVendor.getFindVendorLink(
				clsServletUtilities.getFullClassName(this.toString()), 
				APVendor.Paramsvendoracct, 
				"",
				getServletContext(),
				smselect.getsDBID()
			)
			+ "\"> Find vendor</A>"
			+ "</P>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
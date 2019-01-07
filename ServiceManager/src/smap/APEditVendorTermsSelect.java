package smap;

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
import SMDataDefinition.SMTableicvendorterms;
import ServletUtilities.clsDatabaseFunctions;

public class APEditVendorTermsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smap.APEditVendorTermsEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		APVendorTerms entry = new APVendorTerms();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditVendorTerms
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.APEditVendorTerms)){
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
	    if (req.getParameter(APVendorTerms.ParamsTermsCode) != null){
	    	sEditCode = req.getParameter(APVendorTerms.ParamsTermsCode);
	    }
	    
	    s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">";
	    
	    s += 
	    	"<B>Terms code:<BR>"
	    	+ "<SELECT NAME=\"" + APVendorTerms.ParamsTermsCode + "\">"
	    	+ "<OPTION VALUE=\"" + "" + "\">*** Select terms code ***";
	    
	    //Drop down the vendor terms:
	    String SQL = "SELECT "
	    	+ " " + SMTableicvendorterms.sTermsCode
	    	+ ", " + SMTableicvendorterms.sDescription
	    	+ " FROM " + SMTableicvendorterms.TableName
	    	+ " ORDER BY " + SMTableicvendorterms.sTermsCode
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
				String sReadCode = rs
						.getString(SMTableicvendorterms.sTermsCode);

				s += "<OPTION";
				if (sReadCode.compareToIgnoreCase(sEditCode) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + sReadCode + "\">" + sReadCode + " - "
						+ rs.getString(SMTableicvendorterms.sDescription);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<B>Error reading terms data - " + e.getMessage();
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
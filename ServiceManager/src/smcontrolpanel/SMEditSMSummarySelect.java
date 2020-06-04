package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsServletUtilities;

public class SMEditSMSummarySelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smcontrolpanel.SMEditSMSummaryEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				SMEstimateSummary.OBJECT_NAME,
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditSMEstimates
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		//Remove any leftover summary objects from the session:
		smeditselect.getSession().removeAttribute(SMEstimateSummary.OBJECT_NAME);
		
		smeditselect.printHeaderTable();
		smeditselect.getPrintWriter().println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smeditselect.getsDBID() 
				+ "\">Return to user login</A><BR>");
		
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
	    String sSummaryID = "";
	    if (req.getParameter(SMTablesmestimatesummaries.lid) != null){
	    	sSummaryID = req.getParameter(SMTablesmestimatesummaries.lid);
	    }
				
		s+= 
			"<P>Enter " + SMEstimateSummary.OBJECT_NAME + " Number: <INPUT TYPE=TEXT"
			+ " NAME=\"" + SMTablesmestimatesummaries.lid + "\""
			+ " ID=\"" + SMTablesmestimatesummaries.lid + "\""
			+ " VALUE = \"" + sSummaryID + "\""
			+ " SIZE=20 MAXLENGTH=13"
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s += "<A HREF=\""
			+ SMEstimateSummary.getFindSummaryLink(
				clsServletUtilities.getFullClassName(this.toString()), 
				SMTablesmestimatesummaries.lid, 
				"",
				getServletContext(),
				smselect.getsDBID()
			)
			+ "\"> Find summary</A>"
			+ "</P>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
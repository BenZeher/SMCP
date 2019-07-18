package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTableprojecttypes;

public class SMEditBidSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMEditBidEntry";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMBidEntry entry = new SMBidEntry();
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
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditBids)){
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
	    String sEditCode = "";
	    if (req.getParameter(SMBidEntry.ParamID) != null){
	    	sEditCode = req.getParameter(SMBidEntry.ParamID);
	    }
	    
		s+= 
			"<P>Enter " + SMBidEntry.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMBidEntry.ParamID + "\""
			+ " VALUE = \"" + sEditCode + "\""
			//+ " SIZE=32"
			+ " MAXLENGTH=" + "8"
			+ " STYLE=\"width: .75in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder" +
				"?ObjectName=" + SMBidEntry.ParamObjectName +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"&ResultClass=FinderResults" +
				"&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) +
				"&ReturnField=" + SMBidEntry.ParamID +
				"&SearchField1=" + SMTablebids.scustomername +
				"&SearchFieldAlias1=Customer%20Name" +
				"&SearchField2=" + SMTablebids.sprojectname +
				"&SearchFieldAlias2=Project%20Name" +
				"&SearchField3=" + FinderResults.COMPLETE_SHIP_TO_ADDRESS +
				"&SearchFieldAlias3=Complete%20Ship%20To%20Address" +
				"&SearchField4=" + SMTablebids.scontactname +
				"&SearchFieldAlias4=Contact%20Name" +
				"&SearchField5=" + SMTablebids.mdescription +
				"&SearchFieldAlias5=Description" +
				"&ResultListField1="  + SMTablebids.lid +
				"&ResultHeading1=ID" +
				"&ResultListField2="  + SMTablebids.scustomername +
				"&ResultHeading2=Customer%20Name" +
				"&ResultListField3="  + SMTablebids.sprojectname +
				"&ResultHeading3=Project%20Name" +
				"&ResultListField4="  + FinderResults.COMPLETE_SHIP_TO_ADDRESS +
				"&ResultHeading4=Complete%20Ship%20To%20Address" +
				"&ResultListField5="  + SMTablebids.ssalespersoncode +
				"&ResultHeading5=Salesperson" +
				"&ResultListField6="  + SMTableprojecttypes.sTypeCode +
				"&ResultHeading6=Project%20Type" +
				"&ResultListField7="  + SMTablebids.sstatus +
				"&ResultHeading7=Status" +
				//"&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"\"> Find " + SMBidEntry.ParamObjectName + "</A>";
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
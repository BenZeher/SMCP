package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMLaborBackCharge;
import SMDataDefinition.SMTablelaborbackcharges;

public class SMLaborBackchargeSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMLaborBackChargeEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMLaborBackCharge entry = new SMLaborBackCharge();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditLaborBackCharges
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditLaborBackCharges)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		smeditselect.showAddNewButton(
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditLaborBackCharges, 
				smeditselect.getUserID(), 
				getServletContext(), 
				smeditselect.getsDBID(),
				(String) smeditselect.getSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			)
		);
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
	    if (req.getParameter(SMLaborBackCharge.Paramlid) != null){
	    	sEditCode = req.getParameter(SMLaborBackCharge.Paramlid);
	    }
	    
		s+= 
			"<P>Enter " + SMLaborBackCharge.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMLaborBackCharge.Paramlid + "\""
			+ " VALUE = \"" + sEditCode + "\""
			//+ " SIZE=32"
			+ " MAXLENGTH=" + "8"
			+ " STYLE=\"width: .75in; height: 0.25in\">&nbsp;\n";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder" +
				"?ObjectName=" + SMLaborBackCharge.ParamObjectName +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"&ResultClass=FinderResults" +
				"&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) +
				"&ReturnField=" + SMLaborBackCharge.Paramlid +
				"&SearchField1=" + SMTablelaborbackcharges.scustomername +
				"&SearchFieldAlias1=Customer Name" +
				"&SearchField2=" + SMTablelaborbackcharges.strimmedordernumber +
				"&SearchFieldAlias2=Initiated by" +
				"&SearchField3=" + SMTablelaborbackcharges.strimmedordernumber +
				"&SearchFieldAlias3=Initiated by" +
				"&ResultListField1="  + SMTablelaborbackcharges.lid +
				"&ResultHeading1=ID" +
				"&ResultListField2="  + SMTablelaborbackcharges.scustomername +
				"&ResultHeading2=Description" +
				"&ResultListField3="  + SMTablelaborbackcharges.svendoracct +
				"&ResultHeading3=Initiated" +
				"&ResultListField4="  + SMTablelaborbackcharges.sinitiatedbyfullname +
				"&ResultHeading4=Initiated%20By" +
				//"&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"\"> Find " + SMLaborBackCharge.ParamObjectName + "</A>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

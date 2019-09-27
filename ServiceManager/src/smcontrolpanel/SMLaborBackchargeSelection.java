package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.SMLaborBackCharge;
import SMDataDefinition.SMTablelaborbackcharges;

public class SMLaborBackchargeSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMLaborBackChargeEdit";
	public static final String sTrimmedOrderNumberLaborBackCharge = "sTrimmedOrderNumberLaborBackCharge";
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
		 s+="<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder" +
			"?ObjectName=" + SMLaborBackCharge.ParamObjectName +
			"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
			"&ResultClass=FinderResults" +
			"&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) +
			"&ReturnField=" + SMLaborBackCharge.Paramlid +
			"&SearchField1=" + SMTablelaborbackcharges.scustomername +
			"&SearchFieldAlias1=Customer%20Name" +
			"&SearchField2=" +  SMTablelaborbackcharges.strimmedordernumber +
			"&SearchFieldAlias2=Order%20Number" +
			"&SearchField3=" + SMTablelaborbackcharges.svendoracct +
			"&SearchFieldAlias3=Vendor%20Acct" +
			"&SearchField4=" + SMTablelaborbackcharges.sinitiatedbyfullname +
			"&SearchFieldAlias4=Initiated%20By" +
			"&SearchField5=" + FinderResults.BILL_TO_NAME +
			"&SearchFieldAlias5=Bill-to%20Name" +
			"&SearchField6=" + FinderResults.SHIP_TO_NAME +
			"&SearchFieldAlias6=Ship-To%20Name" +
			"&ResultListField1="  + SMTablelaborbackcharges.lid +
			"&ResultHeading1=ID" +
			"&ResultListField2="  + SMTablelaborbackcharges.strimmedordernumber +
			"&ResultHeading2=Order%20Number" +
			"&ResultListField3="  + SMTablelaborbackcharges.scustomername +
			"&ResultHeading3=Customer%20Name" +
			"&ResultListField4="  + SMTablelaborbackcharges.svendoracct +
			"&ResultHeading4=Vendor%20Acct" +
			"&ResultListField5="  + SMTablelaborbackcharges.sinitiatedbyfullname +
			"&ResultHeading5=Initiated%20By" +
			"&ResultListField6="  + FinderResults.BILL_TO_NAME +
			"&ResultHeading6=Billed-To%20Name" +
			"&ResultListField7="  + FinderResults.SHIP_TO_NAME +
			"&ResultHeading7=Shipped-To%20Name" +
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

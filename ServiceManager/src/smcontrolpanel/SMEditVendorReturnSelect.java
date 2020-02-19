package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMVendorReturn;
import SMDataDefinition.SMTablevendorreturns;

public class SMEditVendorReturnSelect  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMEditVendorReturnEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMVendorReturn entry = new SMVendorReturn();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditVendorReturns
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditVendorReturns)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		smeditselect.showAddNewButton(
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditVendorReturns, 
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
	    if (req.getParameter(SMVendorReturn.Paramlid) != null){
	    	sEditCode = req.getParameter(SMVendorReturn.Paramlid);
	    }
	    
		s+= 
			"<P>Enter " + SMVendorReturn.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMVendorReturn.Paramlid + "\""
			+ " VALUE = \"" + sEditCode + "\""
			//+ " SIZE=32"
			+ " MAXLENGTH=" + "8"
			+ " STYLE=\"width: .75in; height: 0.25in\">&nbsp;\n";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder" +
				"?ObjectName=" + SMVendorReturn.ParamObjectName +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"&ResultClass=FinderResults" +
				"&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) +
				"&ReturnField=" + SMVendorReturn.Paramlid +
				"&SearchField1=" + SMTablevendorreturns.svendoracct +
				"&SearchFieldAlias1=Vendor Account" +
				"&SearchField2=" + SMTablevendorreturns.mVendorComments +
				"&SearchFieldAlias2=Vendor Comments" +
				"&SearchField3=" + SMTablevendorreturns.iponumber +
				"&SearchFieldAlias3=P.O. Number" +
				"&SearchField4=" + SMTablevendorreturns.screditmemonumber +
				"&SearchFieldAlias4=Credit Memo Number" +
				"&ResultListField1="  + SMTablevendorreturns.lid +
				"&ResultHeading1=ID" +
				"&ResultListField2="  + SMTablevendorreturns.iponumber +
				"&ResultHeading2=P.O. Number" +
				"&ResultListField3="  + SMTablevendorreturns.screditmemonumber +
				"&ResultHeading3=Credit Memo Number" +
				"&ResultListField4=" + SMTablevendorreturns.svendoracct +
				"&ResultHeading4=Vendor Account" +
				"&ResultListField5=" + SMTablevendorreturns.mVendorComments +
				"&ResultHeading5=Comments" +
				//"&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"\"> Find " + SMVendorReturn.ParamObjectName + "</A>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
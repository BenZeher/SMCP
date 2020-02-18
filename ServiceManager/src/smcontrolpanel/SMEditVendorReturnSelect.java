package smcontrolpanel;




import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMMaterialReturn;
import SMDataDefinition.SMTablematerialreturns;

public class SMEditVendorReturnSelect  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMEditVendorReturnSelect";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMMaterialReturn entry = new SMMaterialReturn();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditMaterialReturns
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditMaterialReturns)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		smeditselect.showAddNewButton(
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMInitiateMaterialReturns, 
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
	    if (req.getParameter(SMMaterialReturn.Paramlid) != null){
	    	sEditCode = req.getParameter(SMMaterialReturn.Paramlid);
	    }
	    
		s+= 
			"<P>Enter " + SMMaterialReturn.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMMaterialReturn.Paramlid + "\""
			+ " VALUE = \"" + sEditCode + "\""
			//+ " SIZE=32"
			+ " MAXLENGTH=" + "8"
			+ " STYLE=\"width: .75in; height: 0.25in\">&nbsp;\n";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder" +
				"?ObjectName=" + SMMaterialReturn.ParamObjectName +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"&ResultClass=FinderResults" +
				"&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) +
				"&ReturnField=" + SMMaterialReturn.Paramlid +
				"&SearchField1=" + SMTablematerialreturns.sdescription +
				"&SearchFieldAlias1=Description" +
				"&SearchField2=" + SMTablematerialreturns.sinitiatedbyfullname +
				"&SearchFieldAlias2=Initiated by" +
				"&SearchField3=" + SMTablematerialreturns.iponumber +
				"&SearchFieldAlias3=P.O. Number" +
				"&SearchField4=" + SMTablematerialreturns.strimmedordernumber +
				"&SearchFieldAlias4=Order Number" +
				"&ResultListField1="  + SMTablematerialreturns.lid +
				"&ResultHeading1=ID" +
				"&ResultListField2="  + SMTablematerialreturns.iponumber +
				"&ResultHeading2=P.O. Number" +
				"&ResultListField3="  + SMTablematerialreturns.strimmedordernumber +
				"&ResultHeading3=Order Number" +
				"&ResultListField4="  + SMTablematerialreturns.sdescription +
				"&ResultHeading4=Description" +
				"&ResultListField5="  + SMTablematerialreturns.datinitiated +
				"&ResultHeading5=Initiated" +
				"&ResultListField6="  + SMTablematerialreturns.sinitiatedbyfullname +
				"&ResultHeading6=Initiated%20By" +
				"&ResultListField7="  + SMTablematerialreturns.iresolved +
				"&ResultHeading7=Resolved?" +
				//"&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"\"> Find " + SMMaterialReturn.ParamObjectName + "</A>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
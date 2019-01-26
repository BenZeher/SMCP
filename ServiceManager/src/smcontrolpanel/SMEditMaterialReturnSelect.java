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

public class SMEditMaterialReturnSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smcontrolpanel.SMEditMaterialReturnEdit";
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
			+ " STYLE=\"width: .75in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder" +
				"?ObjectName=" + SMMaterialReturn.ParamObjectName +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"&ResultClass=FinderResults" +
				"&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) +
				"&ReturnField=" + SMMaterialReturn.Paramlid +
				"&SearchField1=" + SMTablematerialreturns.sdescription +
				"&SearchFieldAlias1=Description" +
				"&SearchField2=" + SMTablematerialreturns.sinitiatedbyfullname +
				"&SearchFieldAlias2=Initiated by" +
				"&ResultListField1="  + SMTablematerialreturns.lid +
				"&ResultHeading1=ID" +
				"&ResultListField2="  + SMTablematerialreturns.sdescription +
				"&ResultHeading2=Description" +
				"&ResultListField3="  + SMTablematerialreturns.datinitiated +
				"&ResultHeading3=Initiated" +
				"&ResultListField4="  + SMTablematerialreturns.sinitiatedbyfullname +
				"&ResultHeading4=Initiated%20By" +
				"&ResultListField5="  + SMTablematerialreturns.iresolved +
				"&ResultHeading5=Resolved?" +
				"&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
				"\"> Find " + SMMaterialReturn.ParamObjectName + "</A>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMTaxCertificate;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTabletaxcertificates;

public class SMEditTaxCertificatesSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "smcontrolpanel.SMEditTaxCertificatesEdit";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMTaxCertificate entry = new SMTaxCertificate();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditTaxCertificates
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.SMEditTaxCertificates)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		smeditselect.printHeaderTable();
		smeditselect.showAddNewButton(
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMEditTaxCertificates, 
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
	    if (req.getParameter(SMTaxCertificate.Paramlid) != null){
	    	sEditCode = req.getParameter(SMTaxCertificate.Paramlid);
	    }
	    
		s+= 
			"<P>Enter " + SMTaxCertificate.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMTaxCertificate.Paramlid + "\""
			+ " VALUE = \"" + sEditCode + "\""
			//+ " SIZE=32"
			+ " MAXLENGTH=" + "8"
			+ " STYLE=\"width: .75in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s+= "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder" 
			+ "?ObjectName=" + SMTaxCertificate.ParamObjectName 
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() 
			+ "&ResultClass=FinderResults" 
			+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString()) 
			+ "&ReturnField=" + SMTaxCertificate.Paramlid 
			+ "&SearchField1=" + SMTaxCertificate.Paramlid
			+ "&SearchFieldAlias1=ID"
			+ "&SearchField2=" + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.screatedbyfullname
			+ "&SearchFieldAlias2=Created%20By"
			+ "&SearchField3=" + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.scustomername
			+ "&SearchFieldAlias3=Customer%20Name"
			+ "&SearchField4=" + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.scustomernumber
			+ "&SearchFieldAlias4=Customer%20Number"
			+ "&SearchField5=" + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.sjobnumber
			+ "&SearchFieldAlias5=Job%20Number"
			+ "&ResultListField1="  + SMTabletaxcertificates.lid
			+ "&ResultHeading1=ID"
			+ "&ResultListField2="  + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.scustomernumber
			+ "&ResultHeading2=Customer%20Number"
			+ "&ResultListField3="  + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName
			+ "&ResultHeading3=Customer%20Name"
			+ "&ResultListField4="  + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.scustomername
			+ "&ResultHeading4=Entered%20Customer%20Name"
			+ "&ResultListField5="  + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.sjobnumber
			+ "&ResultHeading5=Job%20Number"
			+ "&ResultListField6="  + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
			+ "&ResultHeading6=Ship%20To%20Name"
			+ "&ResultListField7="  + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.sprojectlocation
			+ "&ResultHeading7=Project(s)%20Location"
			+ "&ResultListField8="  + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.staxjurisdiction
			+ "&ResultHeading8=Jurisdiction"
			+ "&ResultListField9="  + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.datexpired
			+ "&ResultHeading9=Date%20Expired"
			+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID() +
					"\"> Find " + SMTaxCertificate.ParamObjectName + "</A>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
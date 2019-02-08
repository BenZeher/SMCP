package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicpoinvoiceheaders;

public class ICEnterInvoiceSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCalledClassName = "smic.ICEnterInvoiceEdit";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		ICPOInvoice entry = new ICPOInvoice();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEnterInvoices
				);
		
		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ICEnterInvoices)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}
		
		//Remove any PO Invoice Entry object in the session to give us a blank starting place:
		smeditselect.getSession().removeAttribute(ICEnterInvoiceEdit.PO_INVOICE_ENTRY_OBJECT);
		
		smeditselect.printHeaderTable();
		
	    //If there is a warning from trying to input previously, print it here:
		
		smeditselect.getPrintWriter().println(
			"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smeditselect.getsDBID() + "\">Return to Inventory Control Main Menu</A><BR>");
		
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
	    if (req.getParameter(ICPOHeader.Paramlid) != null){
	    	sEditCode = req.getParameter(ICPOHeader.Paramlid);
	    }
	    
	    //If the id is -1, which is used to indicate that it's a NEW PO, set the code to a blank:
	    if (sEditCode.compareToIgnoreCase("-1") == 0){
	    	sEditCode = "";
	    }
	    
		s+= 
			"<P>Enter " + ICPOInvoice.ParamObjectName + " ID: <INPUT TYPE=TEXT NAME=\"" 
			+ ICPOInvoice.ParamlID + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ "10"
			+ " STYLE=\"width: .8in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		s += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
			+ "&ObjectName=" + ICPOInvoice.ParamObjectName
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
			+ "&ReturnField=" + ICPOInvoice.ParamlID
			+ "&SearchField1=" + SMTableicpoinvoiceheaders.sinvoicenumber
			+ "&SearchFieldAlias1=Vendor%20Invoice%20Number"
			+ "&SearchField2=" + SMTableicpoinvoiceheaders.lreceiptid
			+ "&SearchFieldAlias2=Receipt%20Number"
			+ "&SearchField3=" + SMTableicpoinvoiceheaders.lpoheaderid
			+ "&SearchFieldAlias3=PO%20Number"
			+ "&SearchField4=" + SMTableicpoinvoiceheaders.svendor
			+ "&SearchFieldAlias4=Vendor"
			+ "&ResultListField1="  + SMTableicpoinvoiceheaders.lid
			+ "&ResultHeading1=Invoice%20ID"
			+ "&ResultListField2="  + SMTableicpoinvoiceheaders.svendor
			+ "&ResultHeading2=Vendor"
			+ "&ResultListField3="  + SMTableicpoinvoiceheaders.sinvoicenumber
			+ "&ResultHeading3=Invoice%20Num."
			+ "&ResultListField4="  + SMTableicpoinvoiceheaders.lreceiptid
			+ "&ResultHeading4=Receipt%20ID"
			+ "&ResultListField5="  + SMTableicpoinvoiceheaders.lpoheaderid
			+ "&ResultHeading5=PO%20ID"
			+ "&ResultListField6="  + SMTableicpoinvoiceheaders.bdinvoicetotal
			+ "&ResultHeading6=Amount"
			+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
			+ "\"> Find invoice</A></P>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
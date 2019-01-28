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
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;

public class ICEditPOSelection extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sICEditPOSelectionCalledClassName = "smic.ICEditPOEdit";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		ICPOHeader entry = new ICPOHeader();
		SMMasterEditSelect smeditselect = new SMMasterEditSelect(
				request,
				response,
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				sICEditPOSelectionCalledClassName,
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditPurchaseOrders
		);

		if (!smeditselect.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){
			PrintWriter m_out = response.getWriter();
			m_out.println("Error in process session: " + smeditselect.getErrorMessages());
			return;
		}

		smeditselect.printHeaderTable();

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
			"<P>Enter " + ICPOHeader.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ ICPOHeader.Paramlid + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ "10"
			+ " STYLE=\"width: .8in; height: 0.25in\">&nbsp;";

	//Link to finders:
		//Search ALL Purchase Orders
		s += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
		+ "&ObjectName=" + ICPOHeader.ParamObjectName
		+ "&ResultClass=FinderResults"
		+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
		+ "&ReturnField=" + ICPOHeader.Paramlid
		+ "&SearchField1=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
		+ "&SearchFieldAlias1=Comment"
		+ "&SearchField2=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
		+ "&SearchFieldAlias2=Reference"
		+ "&SearchField3=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber
		+ "&SearchFieldAlias3=Old%20PO%20Number"
		+ "&SearchField4=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
		+ "&SearchFieldAlias4=Vendor%20Number"
		+ "&SearchField5=" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
		+ "&SearchFieldAlias5=Receipt%20Number"
		+ "&SearchField6=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.screatedbyfullname
		+ "&SearchFieldAlias6=Created%20By%20(Use%20real%20name)"
		+ "&ResultListField1="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
		+ "&ResultHeading1=Purchase%20Order%20No."
		+ "&ResultListField2="  + "STATUS"
		+ "&ResultHeading2=Status"
		+ "&ResultListField3="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
		+ "&ResultHeading3=Comment"
		+ "&ResultListField4="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
		+ "&ResultHeading4=Reference"
		+ "&ResultListField5="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
		+ "&ResultHeading5=Vendor%20Name"
		+ "&ResultListField6="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber
		+ "&ResultHeading6=Old%20PO%20Number"
		+ "&ResultListField7="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
		+ "&ResultHeading7=PO%20Date"
		+ "&ResultListField8="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.screatedbyfullname
		+ "&ResultHeading8=Created%20By"
		+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
		+ "\"> Search ALL purchase orders</A>";

		//Search OPEN Purchase Orders
		s += "&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
		+ "&ObjectName=" + "OPEN " + ICPOHeader.ParamObjectName
		+ "&ResultClass=FinderResults"
		+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
		+ "&ReturnField=" + ICPOHeader.Paramlid
		+ "&SearchField1=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
		+ "&SearchFieldAlias1=Comment"
		+ "&SearchField2=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
		+ "&SearchFieldAlias2=Reference"
		+ "&SearchField3=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber
		+ "&SearchFieldAlias3=Old%20PO%20Number"
		+ "&SearchField4=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
		+ "&SearchFieldAlias4=Vendor%20Number"
		+ "&SearchField5=" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
		+ "&SearchFieldAlias5=Receipt%20Number"
		+ "&SearchField6=" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.screatedbyfullname
		+ "&SearchFieldAlias6=Created%20By%20(Use%20real%20name)"
		+ "&ResultListField1="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
		+ "&ResultHeading1=Purchase%20Order%20No."
		+ "&ResultListField2="  + "STATUS"
		+ "&ResultHeading2=Status"
		+ "&ResultListField3="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
		+ "&ResultHeading3=Comment"
		+ "&ResultListField4="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
		+ "&ResultHeading4=Reference"
		+ "&ResultListField5="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
		+ "&ResultHeading5=Vendor%20Name"
		+ "&ResultListField6="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber
		+ "&ResultHeading6=Old%20PO%20Number"
		+ "&ResultListField7="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
		+ "&ResultHeading7=PO%20Date"
		+ "&ResultListField8="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.screatedbyfullname
		+ "&ResultHeading8=Created%20By"
		+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
		+ "\"> Search OPEN purchase orders</A>";
		
		//Search purchase order by Item.
		s += "&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
		+ "&ObjectName=" + ICPOHeader.ParamObjectName  + " by Item"
		+ "&ResultClass=FinderResults"
		+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
		+ "&ReturnField=" + ICPOHeader.Paramlid
		+ "&SearchField1=" + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber
		+ "&SearchFieldAlias1=Vendors%20Item%20Number"
		+ "&SearchField2=" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
		+ "&SearchFieldAlias2=Item%20Number"
		+ "&SearchField3=" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription
		+ "&SearchFieldAlias3=Item%20Description"
		+ "&ResultListField1="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
		+ "&ResultHeading1=Purchase%20Order%20No."
		+ "&ResultListField2="  + "STATUS"
		+ "&ResultHeading2=Status"
		+ "&ResultListField3=" + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
		+ "&ResultHeading3=Line%20No."
		+ "&ResultListField4=" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber  
		+ "&ResultHeading4=Item%20Number"
		+ "&ResultListField5=" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription
		+ "&ResultHeading5=Item%20Description"
		+ "&ResultListField6=" + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber
		+ "&ResultHeading6=Vendor%20Item%20Number"
		+ "&ResultListField7="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
		+ "&ResultHeading7=Reference"
		+ "&ResultListField8="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
		+ "&ResultHeading8=Vendor%20Name"
		+ "&ResultListField9="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
		+ "&ResultHeading9=PO%20Date"
		+ "&ResultListField10="  + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.screatedbyfullname
		+ "&ResultHeading10=Created%20By"
		+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smselect.getsDBID()
		+ "\"> Search purchase orders by Item</A></P>";

		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
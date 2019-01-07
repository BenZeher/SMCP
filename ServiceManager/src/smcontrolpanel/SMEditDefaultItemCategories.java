package smcontrolpanel;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditDefaultItemCategories extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_CATEGORY_MARKER = "DC";
	public static final String INITIAL_ITEM_MARKER = "II";
	public static final String UPDATE_BUTTON_NAME = "SubmitDIC";
	public static final String UPDATE_BUTTON_LABEL = "Update";
	//private static String sObjectName = "Default Item Category";
	private static String sCalledClassName = "SMEditDefaultItemCategoriesAction";
	private String sDBID = "";
	private String sCompanyName = "";
	private String sUserID = "";
	private String sUserFullName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditDefaultItemCategories))
		{
			return;
		}

		String sSQL = "";
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    long lstart = System.currentTimeMillis();
	    String title = "Manage Default Item Categories";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println ("<TABLE BORDER=12 CELLSPACING=2>");
	    //add headings
	    out.println ("<TR>" +
	    				"<TD><B>Location</B></TD>" + 
	    				"<TD><B>Service Type</B></TD>" + 
	    				"<TD><B>Default Item Category</B></TD>" +
	    				"<TD><B>Initial Item</B></TD>" + 
	    			"</TR>");
		sSQL = "SELECT"
				+ " LOCSERVICETYPEQUERY.LOCDESC"
				+ ", LOCSERVICETYPEQUERY.SERVICETYPE"
				+ ", LOCSERVICETYPEQUERY.LOC"
				+ ", LOCSERVICETYPEQUERY.SERVICECODE"
				+ ", " + SMTabledefaultitemcategories.TableName + "." + SMTabledefaultitemcategories.DefaultItemCategory
				+ ", " + SMTabledefaultitemcategories.TableName + "." + SMTabledefaultitemcategories.InitialItem
				+ " FROM ("
				+ "SELECT"
				+ " " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription + " AS LOCDESC"
				+ ", " + SMTablelocations.TableName + "." + SMTablelocations.sLocation + " AS LOC"
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode + " AS SERVICECODE"
				+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName  + " AS SERVICETYPE"
				+ " FROM " + SMTablelocations.TableName + ", " + SMTableservicetypes.TableName
				+ ") AS LOCSERVICETYPEQUERY"
				+ " LEFT JOIN " + SMTabledefaultitemcategories.TableName
				+ " ON (" + SMTabledefaultitemcategories.TableName + "." 
					+ SMTabledefaultitemcategories.ServiceTypeCode + "=LOCSERVICETYPEQUERY.SERVICECODE)"
				+ " AND (" + SMTabledefaultitemcategories.TableName + "." 
					+ SMTabledefaultitemcategories.LocationCode + "=LOCSERVICETYPEQUERY.LOC)"
				+ " ORDER BY LOCSERVICETYPEQUERY.LOCDESC"
				+ ", LOCSERVICETYPEQUERY.SERVICETYPE"
				+ ", " + SMTabledefaultitemcategories.TableName + "." 
				+ SMTabledefaultitemcategories.DefaultItemCategory
	    ;
	    try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".doPost - user: " + sUserID
					+ " - "
					+ sUserFullName
					);
			String sValue = "";
			while(rs.next()){
				out.println("<TR>");
				out.println("<TD>" + rs.getString("LOCDESC") + "</TD>");
				out.println("<TD>" + rs.getString("SERVICETYPE") + "</TD>");
				sValue = rs.getString(SMTabledefaultitemcategories.TableName + "." + SMTabledefaultitemcategories.DefaultItemCategory);
				if (sValue == null){
					sValue = "";
				}
				out.println("<TD>" + "<INPUT TYPE=TEXT " + "NAME=\"" + DEFAULT_CATEGORY_MARKER 
					+ rs.getString("LOC")
					+ rs.getString("SERVICECODE") 
			    	+ "\" id=\"" + DEFAULT_CATEGORY_MARKER 
					+ rs.getString("LOC")
					+ rs.getString("SERVICECODE") 
					+ "\""
					+ " VALUE=\"" + sValue + "\""
					+ " onblur=\"validateCategory('" 
					+ DEFAULT_CATEGORY_MARKER 
			    	+ rs.getString("LOC")
			    	+ rs.getString("SERVICECODE") 
					+ "');\""
					+ " MAXLENGTH=" + Integer.toString(SMTabledefaultitemcategories.DefaultItemCategoryLength)
					+ " STYLE=\"width: 2.41in; height: 0.25in\""
					+ "></TD>");
				sValue = rs.getString(SMTabledefaultitemcategories.TableName + "." + SMTabledefaultitemcategories.InitialItem);
				if (sValue == null){
					sValue = "";
				}
				out.println("<TD>" + "<INPUT TYPE=TEXT " + "NAME=\"" + INITIAL_ITEM_MARKER 
					+ rs.getString("LOC")
					+ rs.getString("SERVICECODE") 
			    	+ "\" id=\"" + INITIAL_ITEM_MARKER 
					+ rs.getString("LOC")
					+ rs.getString("SERVICECODE") 
					+ "\""
					+ " VALUE=\"" + sValue + "\""
					+ " onblur=\"validateItemNumber('" 
					+ INITIAL_ITEM_MARKER 
			    	+ rs.getString("LOC")
			    	+ rs.getString("SERVICECODE") 
					+ "');\""
					+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sItemNumberLength)
					+ " STYLE=\"width: 2.41in; height: 0.25in\""
					+ "></TD>");
				out.println("</TR>");
			}
			rs.close();
		} catch (SQLException e) {
			out.println("<BR>Error reading default item categories - " + e.getMessage() + "<BR");
		}

	    out.println("</TABLE>");
	 
	    out.println("<P><INPUT TYPE=SUBMIT NAME='" + UPDATE_BUTTON_NAME + "' VALUE='" + UPDATE_BUTTON_LABEL 
	    		+ "' STYLE='width: 2.50in; height: 0.24in'><BR>");
	    out.println("</FORM>");
	    
	    out.println("<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n")
		;
	    out.println("<script type=\"text/javascript\">\n");
		
	    out.println("var it=new Array(); \n");
	    out.println("var ct=new Array(); \n");

	    //get a list of item numbers
	    sSQL = "SELECT " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " FROM " + SMTableicitems.TableName;
	    //System.out.println("[1338842533]SQL = " + sSQL);
	    int i = 0;
	    try {
			ResultSet rsItems = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsItems.next()){
				out.println("it[" + i + "] = \"" + rsItems.getString(SMTableicitems.sItemNumber) + "\";");
				i++;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading items - " + e.getMessage() + "<BR");
		}

	    //get a list of item category
	    sSQL = "SELECT " + SMTableiccategories.TableName + "." + SMTableiccategories.sCategoryCode + " FROM " + SMTableiccategories.TableName;
	    i = 0;
	    try {
			ResultSet rsCategories = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsCategories.next()){
				out.println("ct[" + i + "] = \"" + rsCategories.getString(SMTableiccategories.sCategoryCode) + "\";");
				i++;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading categories - " + e.getMessage() + "<BR");
		}
	    
	    out.println("function validateItemNumber(s){\n"
    				//+ "alert('s = ' + s);\n"
	    			+ "    var txtBox = document.getElementById(s);\n"
	    			//+ "alert('txtBox.value = ' + txtBox.value);\n"
	    			+ "    if (txtBox.value != ''){"
		    		+ "        if (it.indexOf(txtBox.value) == -1){\n"
	    			+ "            alert('Please enter a valid item number!!');\n"
	    			+ "            txtBox.focus();\n"
					+ "        }\n" 
	    			+ "    }"
					+ "}\n");
	    
	    out.println("function validateCategory(s){\n"
    				//+ "alert('s = ' + s);\n"
    				+ "    var txtBox = document.getElementById(s);\n"
	    			//+ "alert('txtBox.value = ' + txtBox.value);\n"
	    			+ "    if (txtBox.value != ''){"
	    			+ "        if (ct.indexOf(txtBox.value) == -1){\n"
	    			+ "            alert('Please enter a valid category code!!');\n"
	    			+ "            txtBox.focus();\n"
					+ "        }\n" 
	    			+ "    }"
					+ "}\n");

	    out.println("</script>\n");
	       
		out.println("<FONT size=0>Loading time: " + (System.currentTimeMillis() - lstart) + " msec.</FONT></BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
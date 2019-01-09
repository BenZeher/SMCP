package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablecompanyprofile;

public class TCDisplayDataDefs  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String PARAM_COMMADELIMITED = "COMMADELIMITED";
	public static String PARAM_QUERYTITLE = "QUERYTITLE";
	public static String PARAM_QUERYSTRING = "QUERYSTRING";
	public static String PARAM_FONTSIZE = "FONTSIZE";
	public static String PARAM_INCLUDEBORDER = "INCLUDEBORDER";
	public static String PARAM_ALTERNATEROWCOLORS = "ALTERNATEROWCOLORS";

	private String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		
		try {
			sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sDBID = "";
		}
		if (sDBID == null){
			sDBID = "";
		}
		
		if (sDBID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No database name found.</FONT></B><BR>");
			return;
		}
		
		//Get the company information:
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;
		String sConfName;
		try {
			sConfName = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sConfName = "";
		}
		if (sConfName == null){
			sConfName = "";
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sConfName, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, rs.getString(TCSTablecompanyprofile.sCompanyName));
				rs.close();
			}else{
				out.println("<BR>Could not read company name.");
				out.println("</BODY></HTML>");
				rs.close();
				return;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading read company name: " + e.getMessage()+ ".");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sTitle = "Time Card System";
		String sSubtitle = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

		Connection conn = null;
		
		conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString()
		);
		if (conn == null){
			out.println("<B>Connection is null.<BR>");
			out.println("</BODY></HTML>");
			return;
		}
		ArrayList<String> arrTables = new ArrayList<String>(0);
		try {
		    // Gets the database metadata
		    DatabaseMetaData dbmd = conn.getMetaData();

		    // Specify the type of object; in this case we want tables
		    String[] types = {"TABLE"};
		    ResultSet resultSet = dbmd.getTables(null, null, "%", types);

		    // Get the table names
		    //TODO - get these into a collection, sort them, then print the field definitions for each
		    while (resultSet.next()) {
		    	arrTables.add(resultSet.getString(3));
		    }
		} catch (SQLException e) {
			out.println("<BR>Error listing tables - " + e.getMessage());
		}

		Collections.sort(arrTables, String.CASE_INSENSITIVE_ORDER);
		
		printFormatDefs(out);
		
		for (int i = 0; i < arrTables.size(); i++){
			try {
				printTableDefs(arrTables.get(i), conn, out);
			} catch (SQLException e) {
				out.println("<BR>Error getting table defs for table '" + arrTables.get(i) + " - " + e.getMessage());
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060118]");
				out.println("</BODY></HTML>");
				return;
			}
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060119]");
		
		out.println("<BR>Printed definitions for <B>" + arrTables.size() + "</B> tables.<BR>");
		out.println("</BODY></HTML>");
		return;
	}

	private void printFormatDefs(PrintWriter pwOut){
		
		String sBorderSize = "1";
		String sFontSize = "small";
		pwOut.println("<style type=\"text/css\">");
		pwOut.println(
				"table.main {"
				+ "border-width: " + sBorderSize + "px; "
				+ "border-spacing: 2px; "
				+ "border-style: pwOutset; "
				+ "border-color: gray; "
				+ "border-collapse: separate; "
				+ "font-size: " + sFontSize + "; "
				//+ "background-color: white; "
				+ "}"
		);

		pwOut.println(
				"table.main th {"
				+ "border-width: " + sBorderSize + "px; "
				+ "padding: 2px; "
				+ "border-style: inset; "
				+ "border-color: gray; "
				//+ "background-color: white; "
				+ "}"
		);


		pwOut.println(
				"table.main td {"
				+ "border-width: " + sBorderSize + "px; "
				+ "padding: 2px; "
				+ "border-style: inset; "
				+ "border-color: gray; "
				+ "vertical-align: text-top;"
				//+ "background-color: #EEEEEE; "
				+ "}"
		);

		pwOut.println("</style>");
		return;
	}
	private void printTableDefs (String sTable, Connection conn, PrintWriter pwOut) throws SQLException{
		
		pwOut.println("<B>" + sTable + "</B>");
		pwOut.println("<table class=\"main\">");
		pwOut.println("<TR>"
				+ "<th>NAME</th>"
				+ "<th>TYPE</th>"
				+ "<th>LENGTH</th>"
				+ "<th>SCALE</th>"
				+ "<th>PRECISION</th>"
				+ "<th>KEY</th>"
				+ "<th>DEFAULT VALUE</th>"
				+ "<th>COMMENTS</th>"
				+ "<th>ID</th>"
				+ "</TR>"
		);
		
		DatabaseMetaData dbmd = conn.getMetaData();
		
		String sSQL = "SELECT * FROM `" + sTable + "` LIMIT 1";
		String sSQL0 = "DESCRIBE " + sTable + "";
		
		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
		ResultSet rsDESCRIBE = clsDatabaseFunctions.openResultSet(sSQL0, conn);
		if (rs.next()){
			
			//Get the field comment
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++){

				ResultSet rsColumn = dbmd.getColumns(null, null, sTable, rs.getMetaData().getColumnName(i));
				String sColumnComment = "";
				String sColumnID = "";
				//String sFieldID = "";
				//String sKeyType = "";
				if(rsColumn.next() && rsDESCRIBE.next()){
					if(rsColumn.getString("REMARKS") != null){
						sColumnComment = rsColumn.getString("REMARKS");	
						//if an ID is present in the comments
						if(sColumnComment.contains("[") && sColumnComment.contains("]")) {
							sColumnID = sColumnComment.substring(sColumnComment.indexOf("["), sColumnComment.indexOf("]") + 1);
						}
					}
				}

				rsColumn.close();
				//Print a line for each field:
				//NOTE: The id is determined by starting and ending brackets '[ID]' at the START of the remarks.
				//The rest of the Remarks field is put in with the 'Extra' field of the DESCRIBE `TableName` query
				pwOut.println(
					"<TR>" 
					+ "<TD>"	
					+ rs.getMetaData().getColumnName(i)
					+ "</TD>"
					+ "<TD>"
					+ rs.getMetaData().getColumnTypeName(i)
					+ "</TD>"
					+ "<TD>"
					+ rs.getMetaData().getColumnDisplaySize(i)
					+ "</TD>"
					+ "<TD>"
					+ rs.getMetaData().getScale(i)
					+ "</TD>"
					+ "<TD>"
					+ rs.getMetaData().getPrecision(i)
					+ "</TD>"
					+ "<TD>"
					+ rsDESCRIBE.getString("Key")
					+ "</TD>"
					+ "<TD>"
					+ rsDESCRIBE.getString("Default")
					+ "</TD>"
					+ "<TD>"
					+ sColumnComment.substring(sColumnComment.indexOf("]") + 1, sColumnComment.length()) + " " + rsDESCRIBE.getString("Extra")
					+ "</TD>"
					+ "<TD>"
					+ sColumnID
					+ "</TD>"
					+ "</TR>"
					
				);
			}
			
		}
		rs.close();
		rsDESCRIBE.close();
		pwOut.println("</TABLE>");
		pwOut.println("<BR>");
		return;
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}


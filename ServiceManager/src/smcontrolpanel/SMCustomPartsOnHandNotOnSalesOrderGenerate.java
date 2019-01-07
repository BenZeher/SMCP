package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;

public class SMCustomPartsOnHandNotOnSalesOrderGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserID = "";
	private String sCompanyName = "";
	private static boolean bDebugMode = false;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCustomPartsonHandNotonSalesOrders))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Custom Parts On Hand Not On Sales Order";
		String subtitle = "";

		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		//log usage of this this report
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_CUSTOMITEMSONHANDNOTONSALESORDER, "REPORT", "SMCustomPartsOnHandNotOnSalesOrder", "[1376509317]");

		try{

			boolean bHasRecord = false;
			String sCurrentItemCategory = null;
			/*
			ArrayList<String> alItemCategories = new ArrayList<String>(0);
			if (request.getParameter("CheckItemCategories") != null){
				//create a list of 
				Enumeration<?> paramNames = request.getParameterNames();
				while(paramNames.hasMoreElements()) {
					String s = paramNames.nextElement().toString();
					//System.out.println("paramNames.nextElement() = " + s);
					if (s.substring(0, 2).compareTo("!!") == 0){
						alItemCategories.add(s.substring(2));
					}
				}
			}else{
				alItemCategories.add("ALLIC");
			}
			*/
			boolean bAllowItemViewing = 
					SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.ICDisplayItemInformation, 
							sUserID, 
							getServletContext(),
							sDBID,
							(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					);
		    String sSQL = 
	    		"SELECT"
		    	+ " 'Custom Parts On Hand Not On Sales Order'  as REPORTNAME"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " AS ITEM"
	    		+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription + " AS DESCRIPTION"
	    		+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCategoryCode + " AS CATEGORY"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " AS LOCATION"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " AS OH"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " AS TOTALCOST"
	    		+ " from " + SMTableicitemlocations.TableName + " LEFT JOIN " + SMTableicitems.TableName
	    		+ " on " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = " 
	    		+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber

				+ " LEFT JOIN"
				+ " (SELECT DISTINCT " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber 
				+ " as ITEM from " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName
				+ " ON " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " where ("
				+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " != 0.00)"
				+ " AND (NOT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " > '1900-01-01')"
				+ ")"
				+ ") AS DETAILQUERY"
				+ " on " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = DETAILQUERY.ITEM"

				+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sDedicatedToOrderNumber + " != '')"
				+ " AND ("
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " != 0.00)"
				+ " OR (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " != 0.00)"
				+ ")"
				+ " AND (DETAILQUERY.ITEM IS NULL)"
				+ ")"
				+ " ORDER BY CATEGORY, ITEM"
		    ;
		    if (bDebugMode){
		    	System.out.println("In " + this.toString() + " - main SQL = " + sSQL);
		    }
			ResultSet rsItemOnHandList = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					"smcontrolpanel.SMCustomPartsOnHandNotOnSalesOrderGenerate");
			out.println("<TABLE BORDER=0 WIDTH=100%>");

			boolean bFlipper = false;
			boolean bList = true;
			String sbgColor = "";
			while (rsItemOnHandList.next()){	
				if (bList){
					if (sCurrentItemCategory == null ||
							rsItemOnHandList.getString("CATEGORY").compareTo(sCurrentItemCategory) != 0){
						if (sCurrentItemCategory != null){
							out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
						}
						sCurrentItemCategory = rsItemOnHandList.getString("CATEGORY");
						out.println("<TR><TD COLSPAN=6><TABLE BORDER=1><TR><TD><FONT SIZE=3><B>Item Category:&nbsp;" + sCurrentItemCategory + "</B></FONT></TD></TR></TABLE></TD></TR>");
						out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
						out.println("<TR>" +
								//"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2><B>Order Number</B></FONT></TD>" +
								"<TD ALIGN=CENTER WIDTH=15%><FONT SIZE=2><B>Item Number</B></FONT></TD>" +
								"<TD ALIGN=LEFT WIDTH=35%><FONT SIZE=2><B>Item Desc</B></FONT></TD>" +
								"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2><B>Category</B></FONT></TD>" +
								"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2><B>Location</B></FONT></TD>" + 
								"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>Qty On Hand</B></FONT></TD>" + 
								"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>Total Cost</B></FONT></TD>" + 
								"</TR>");
						out.println("<TR><TD COLSPAN=6><HR></TD></TR>");

					}
					//System.out.println("6");
					bHasRecord = true;
					bFlipper = !bFlipper;
					if (bFlipper){
						sbgColor = "\"#FFFFFF\"";
					}else{
						sbgColor = "\"#DDDDDD\"";
					}
					String sItemNumber = rsItemOnHandList.getString("ITEM");
					String sItemNumberLink = sItemNumber;
					if (bAllowItemViewing){
						sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smic.ICDisplayItemInformation?ItemNumber=" 
						+ sItemNumber
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\">" + sItemNumber + "</A>";
					}						
					out.println("<TR BGCOLOR=" + sbgColor + ">" +
							//"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2>" + rsItem "</FONT></TD>" +
							"<TD ALIGN=CENTER WIDTH=15%><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>" +
							"<TD ALIGN=LEFT WIDTH=35%><FONT SIZE=2>" + rsItemOnHandList.getString("DESCRIPTION") + "</FONT></TD>" +
							"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2>" + rsItemOnHandList.getString("CATEGORY") + "</FONT></TD>" +
							"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2>" + rsItemOnHandList.getString("LOCATION") + "</FONT></TD>" + 
							"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=2>" + rsItemOnHandList.getString("OH") + "</FONT></TD>" + 
							"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=2>" + rsItemOnHandList.getString("TOTALCOST") + "</FONT></TD>" + 
							"</TR>");
				}
				bList = true;
			}
			if (!bHasRecord){
				out.println("<TR><TD ALIGN=CENTER COLSPAN=6><B>No Record Found</B></TD></TR>");

			}
			out.println("<TR><TD COLSPAN=6><HR></TD></TR>");
			out.println("</TABLE>");
			//out.println("End Time: " + USTimeOnlyformatter.format(new Date(System.currentTimeMillis())) + "<BR>");
			rsItemOnHandList.close();
		}catch (SQLException ex){
			System.out.println("Error in SMCustomPartsOnHandNotOnSalesOrderGenerate!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
		}

		out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
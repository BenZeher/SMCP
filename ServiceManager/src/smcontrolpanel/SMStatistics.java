package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTablearcustomershiptos;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablesalescontacts;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTablesystemlog;
import ServletUtilities.clsDatabaseFunctions;

public class SMStatistics extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMSystemstatistics))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "System Statistics";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    String sSQL = "";
	    Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMStatistics");
	    if (conn == null){
	    	out.println("<BR>Error opening database connection.<BR>");
	    	return;
	    }
		try{
	        sSQL = SMMySQLs.Statistics_OrderHeaders_Count_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	        long iOrderCount = rs.getLong("CNT");
	     	out.println ("Number of orders in system: " + iOrderCount + "; " );
	     	rs.close();
	     	
	        sSQL = SMMySQLs.Statistics_OrderDetails_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	        long iOrderLineCount = rs.getLong("CNT");
	     	out.println ("number of order lines in system: " + iOrderLineCount+ ", averaging " + iOrderLineCount / iOrderCount + " lines per order.<BR>");
	     	rs.close();
	     	
	        sSQL = SMMySQLs.Statistics_OrderHeadersOldest_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Date of oldest order in system: " + rs.getDate(SMDataDefinition.SMTableorderheaders.datOrderDate) + ".<BR>");
	     	rs.close();
	     	
	        sSQL = SMMySQLs.Statistics_InvoiceHeaders_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	        long iInvoiceCount = rs.getLong("CNT");
	     	out.println ("Number of invoices in system: " + iInvoiceCount+ "; " );
	     	rs.close();
	     	
	        sSQL = SMMySQLs.Statistics_InvoiceDetails_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	        long iInvoiceLineCount = rs.getLong("CNT");
	     	out.println ("number of invoice details in system: " + iInvoiceLineCount+ ", averaging " + iInvoiceLineCount / iInvoiceCount + " lines per invoice.<BR>" );
	     	rs.close();

	        sSQL = SMMySQLs.Statistics_CriticalDates_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	        long iCriticalDateCount = rs.getLong("CNT");
	     	out.println ("Number of critical dates in system: " + iCriticalDateCount + ".<BR>" );
	     	rs.close();

	        sSQL = SMMySQLs.Statistics_SpeedSearchHeaders_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Number of Speed Search records in system: " + rs.getLong("CNT") + "; " );
	     	rs.close();

	        sSQL = SMMySQLs.Statistics_SpeedSearchOldest_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("date of oldest Speed Search order in system: " + FormatLongDateAsStringYMD(rs.getLong(SMDataDefinition.SMTablessorderheaders.ORDDATE)) + ".<BR>" );
	     	rs.close();
	     	
	        sSQL = SMMySQLs.Statistics_SiteLocations_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Number of Site Locations (Door Labels) in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();

	        sSQL = SMMySQLs.Statistics_Users_Count_SQL();
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Number of users in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	        sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablearcustomer.TableName
	        	+ " WHERE " + SMTablearcustomer.iActive + " = 1";
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Number of ACTIVE customers in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablearcustomer.TableName
	     		+ " WHERE " + SMTablearcustomer.iActive + " = 0";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of INACTIVE customers in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	        sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableartransactions.TableName
	        	+ " WHERE " + SMTableartransactions.dcurrentamt + " != 0.00";
	        rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Number of OPEN AR transactions in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableicitems.TableName
	     		+ " WHERE " + SMTableicitems.iActive + " = 1";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        rs.first();
	     	out.println ("Number of ACTIVE items in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableicitems.TableName
     			+ " WHERE " + SMTableicitems.iActive + " = 0";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of INACTIVE items in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableicpoheaders.TableName
	     		+ " WHERE " + SMTableicpoheaders.lstatus + " IN (0,1)";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of OPEN/PARTIALLY COMPLETED purchase orders in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableicpoheaders.TableName
     			+ " WHERE " + SMTableicpoheaders.lstatus + " = 3";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of DELETED purchase orders in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableicpoheaders.TableName
 				+ " WHERE " + SMTableicpoheaders.lstatus + " = 2";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of COMPLETED purchase orders in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablearcustomershiptos.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of customer ship-to addresses in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablebids.TableName
				+ " WHERE " + SMTablebids.sstatus + " = '" + SMTablebids.STATUS_SUCCESSFUL + "'";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of SUCCESSFUL " + SMBidEntry.ParamObjectName + "s in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablebids.TableName
	     		+ " WHERE " + SMTablebids.sstatus + " = '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of UNSUCCESSFUL " + SMBidEntry.ParamObjectName + "s in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablebids.TableName
	     		+ " WHERE " + SMTablebids.sstatus + " = '" + SMTablebids.STATUS_PENDING + "'";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
     		rs.first();
     		out.println ("Number of PENDING " + SMBidEntry.ParamObjectName + "s in system: " + rs.getLong("CNT") + ".<BR>" );
     		rs.close();
     		
     		sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablebids.TableName
     			+ " WHERE " + SMTablebids.sstatus + " = '" + SMTablebids.STATUS_INACTIVE + "'";
     		rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
     		rs.first();
     		out.println ("Number of INACTIVE " + SMBidEntry.ParamObjectName + "s in system: " + rs.getLong("CNT") + ".<BR>" );
     		rs.close();
     		
     		sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablecallsheets.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of customer call sheets in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablefamaster.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of fixed assets in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableglaccounts.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of General Ledger Accounts in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTableicvendors.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of IC Vendors in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablemechanics.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of mechanics in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablesalescontacts.TableName
	     		+ " WHERE " + SMTablesalescontacts.binactive + " = 0";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of ACTIVE sales contacts in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablesalescontacts.TableName
     			+ " WHERE " + SMTablesalescontacts.binactive + " = 1";
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
     		rs.first();
     		out.println ("Number of INACTIVE sales contacts in system: " + rs.getLong("CNT") + ".<BR>" );
     		rs.close();
     		
     		sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablesalesperson.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of salespersons in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	sSQL = "SELECT COUNT(*) as CNT FROM " + SMTablesystemlog.TableName;
	     	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	     	rs.first();
	     	out.println ("Number of system log events in system: " + rs.getLong("CNT") + ".<BR>" );
	     	rs.close();
	     	
	     	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080672]");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080673]");
		out.println("</BODY></HTML>");
	}
	
	private String FormatLongDateAsStringYMD(long lDate){
		
		String sDate = Long.toString(lDate);
		
		return sDate.substring(0, 4) + "-" + sDate.substring(4, 6) + "-" + sDate.substring(6,8); 
		
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
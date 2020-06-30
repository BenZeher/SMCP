package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;

import SMDataDefinition.SMTablepricelistcodes;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;

public class SMPrintEstimateSummary extends java.lang.Object {

	private String m_sErrorMessage;
	public SMPrintEstimateSummary(
			){
		m_sErrorMessage = "";
	}

	public boolean processReport(
			Connection conn,
			SMEstimateSummary summary,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
		String s = "";
		s+= "<TABLE>";
		s+="<TR><TD> ";
		s+= "Summary ID: ";
		s+=  summary.getslid();
		s+= " Incorporated into order number: ";
		if(summary.getstrimmedordernumber().compareToIgnoreCase("") ==0) {
			s+="(none)";
		}else {
			s+= summary.getstrimmedordernumber();
		}
		s+= "</TD></TR>";
		s+="<TR><TD> ";
		s+="Created by: ";
		s+=summary.getscreatedbyfullname();
		s+=" on ";
		s+= summary.getsdatetimecreated();
		s+= " Last modified by: ";
		s+= summary.getslastmodifiedbyfullname();
		s+=" on ";
		s+= summary.getsdatetimeslastmodified();
		s+= "</TD></TR>";
		s+="<TR><TD> ";
		s+= "Ship-to: ";
		s+= summary.getsjobname();
		s+= "</TD><TD>";
		s+= "Tax Type: ";
		try {
			s+= summary.getstaxdescription();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			s+= e.getMessage();		
		}
		s+= "</TD></TR>";
		s+="<TR><TD> ";
		s+= "Sales Lead ID: ";
		s+= summary.getslsalesleadid();
		s+= "</TD><TD>";
		s+="Order Type: ";
		
		//Get the service types
		String sServiceName = "";
		String SQL = "SELECT"
			+ " " + SMTableservicetypes.id
			+ ", " + SMTableservicetypes.sName
			+ " FROM " + SMTableservicetypes.TableName
			+ " WHERE " + SMTableservicetypes.id + " = " + Integer.parseInt(summary.getsiordertype());
		;
		try {
			ResultSet rsServiceType = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsServiceType.next()) {
				sServiceName = (rsServiceType.getString(SMTableservicetypes.sName));
			}
			rsServiceType.close();
		} catch (Exception e1) {
			s += "<B>Error [1590530298] reading service types with SQL: '" + SQL + "' - " + e1.getMessage() + "</B><BR>";
		}
		s+=sServiceName;
		s+= "</TD></TR>";
		s+="<TR><TD> ";
		s+="Price list: ";
		
		String sPriceList = "";
		//GetPriceList
		SQL = "SELECT"
			+ " " + SMTablepricelistcodes.spricelistcode
			+ ", " + SMTablepricelistcodes.sdescription
			+ " FROM " + SMTablepricelistcodes.TableName
			+ " WHERE " + SMTablepricelistcodes.spricelistcode + " = " + summary.getspricelistcode();
		;
		try {
			ResultSet rsPriceListCodes = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsPriceListCodes.next()) {
				sPriceList=(rsPriceListCodes.getString(SMTablepricelistcodes.sdescription).trim());
			}
			rsPriceListCodes.close();
		} catch (SQLException e) {
			s += "<B>Error [1590535753] reading price list codes - " + e.getMessage() + "</B><BR>";
		}
		s+= sPriceList;
		s+= "</TD><TD>";
		s+= "Price level: ";
		s+= summary.getsipricelevel();
		s+= "</TD></TR>";
		s+= "</TABLE>";
		out.println(s);
		return false;
	}

}

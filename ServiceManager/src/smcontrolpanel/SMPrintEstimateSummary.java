package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMOHDirectFieldDefinitions;
import SMDataDefinition.SMTablepricelistcodes;
import SMDataDefinition.SMTablepricelistlevellabels;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;

public class SMPrintEstimateSummary extends java.lang.Object {

	public SMPrintEstimateSummary(
			){
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
		s+= "Summary ID: " +  summary.getslid();
		s+= " Incorporated into order number: ";
		if(summary.getstrimmedordernumber().compareToIgnoreCase("") ==0) {
			s+="(none)";
		}else {
			s+= summary.getstrimmedordernumber();
		}
		s+= "</TD></TR>";

		s+="<TR><TD> ";
		s+="Created by: " + summary.getscreatedbyfullname() + " on " + summary.getsdatetimecreated() +  " Last modified by: " + summary.getslastmodifiedbyfullname() + " on " + summary.getsdatetimeslastmodified();
		s+= "</TD></TR>";

		s+="<TR>"
				+ "<TD> ";
		s+= "Ship-to: " + summary.getsjobname();
		s+= "</TD>"
				+ "<TD>";
		s+= "Tax Type: ";
		try {
			s+= summary.getstaxdescription();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			s+= e.getMessage();		
		}
		s+= "</TD></TR>";
		s+="<TR>"
				+ "<TD> ";
		s+= "Sales Lead ID: " + summary.getslsalesleadid();
		s+= "</TD>"
				+ "<TD>";
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
		//Price level:
		ArrayList<String> arrPriceLevels = new ArrayList<String>(0);
		ArrayList<String> arrPriceLevelDescriptions = new ArrayList<String>(0);
		SQL = "SELECT"
				+ " * FROM " + SMTablepricelistlevellabels.TableName
				;
		//First, add a blank item so we can be sure the user chose one:
		for (int i = 0; i < SMTablepricelistlevellabels.NUMBER_OF_PRICE_LEVELS; i++) {
			arrPriceLevels.add(Integer.toString(i));
		}

		try {
			ResultSet rsPriceLevels = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsPriceLevels.next()) {
				arrPriceLevelDescriptions.add(rsPriceLevels.getString(SMTablepricelistlevellabels.sbasepricelabel));
				arrPriceLevelDescriptions.add(rsPriceLevels.getString(SMTablepricelistlevellabels.spricelevel1label));
				arrPriceLevelDescriptions.add(rsPriceLevels.getString(SMTablepricelistlevellabels.spricelevel2label));
				arrPriceLevelDescriptions.add(rsPriceLevels.getString(SMTablepricelistlevellabels.spricelevel3label));
				arrPriceLevelDescriptions.add(rsPriceLevels.getString(SMTablepricelistlevellabels.spricelevel4label));
				arrPriceLevelDescriptions.add(rsPriceLevels.getString(SMTablepricelistlevellabels.spricelevel5label));
			}
			rsPriceLevels.close();
		} catch (SQLException e) {
			s += "<B>Error [1590535953] reading price level labels - " + e.getMessage() + "</B><BR>";
		}
		s+= "Price level: ";
		s+=
				arrPriceLevelDescriptions.get(
						Integer.parseInt(summary.getsipricelevel())
						)
				;
		s+= "</TD></TR>";
		s+="<TR><TD> ";
		s+="Comments:	" + summary.getscomments();
		s+= "</TD></TR>";
		s+= "</TABLE>";
		
		s+= "<TABLE>";
		s+="<TR><TD> ";
		s+= "Estimates: ";
		s+= "</TD></TR>";
		for (int i = 0; i < summary.getEstimateArray().size(); i++) {
			s += "  <TR>" + "\n";
			
			//Line #:
			String sEstimateLink = "&nbsp;"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMEditSMEstimateEdit"
	    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    		+ "&" + SMTablesmestimates.lid + "=" + summary.getEstimateArray().get(i).getslid()
	    		+ "&" + SMTablesmestimates.lsummarylid + "=" + summary.getEstimateArray().get(i).getslsummarylid()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "&" + "CallingClass = " + SMUtilities.getFullClassName(this.toString())
	    		+ "\">Line " + summary.getEstimateArray().get(i).getslsummarylinenumber() + "</A>"
	    		+ "&nbsp;"
    		;
			s+= "    <TD >"
					+ sEstimateLink
					+ "</TD>" + "\n"
				;
			
			//Estimate ID:
			s+= "    <TD>"
					+ summary.getEstimateArray().get(i).getslid()
					+ "</TD>" + "\n"
				;		

			//Quantity:
			s+= "    <TD>"
					+ summary.getEstimateArray().get(i).getsbdquantity()
					+ "</TD>" + "\n"
				;
			
			//Vendor quote:
			String sVendorQuoteNumber = summary.getEstimateArray().get(i).getsvendorquotenumber()
					+ "/" + summary.getEstimateArray().get(i).getsivendorquotelinenumber();
			if (summary.getEstimateArray().get(i).getsvendorquotenumber().compareToIgnoreCase("") == 0) {
				sVendorQuoteNumber = "";
			}else {
				if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMOHDirectQuoteList,
					sUserID, 
					conn, 
					sLicenseModuleLevel)) {
					
					//Create a link to the vendor's quote line:
					sVendorQuoteNumber = 
				    	"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOHDirectQuote"
				    		+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
				    		+ "&" + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "=" + summary.getEstimateArray().get(i).getsvendorquotenumber()
				    		+ "&" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER + "=" + summary.getEstimateArray().get(i).getsivendorquotelinenumber()
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				    		+ "&" + SMDisplayOHDirectQuote.ADDITIONAL_PARAMETERS + "=" + SMTablesmestimatesummaries.lid + "=" + summary.getslid() 
				    		+ "\">" + sVendorQuoteNumber + "</A>"
					;
				}
			}
			s+= "    <TD>"
					+ "&nbsp;"  //Just for a little space...
					+ sVendorQuoteNumber
					+ "</TD>" + "\n"
				;
			
			//Product description:
			s+= "    <TD>"
					+ summary.getEstimateArray().get(i).getsproductdescription()
					+ "</TD>" + "\n"
				;
			
			//Price:
			try {
				s+= "    <TD>"
						+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(summary.getEstimateArray().get(i).getTotalPrice(conn))
						+ "</TD>" + "\n"
					;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			s += "  </TR>" + "\n";
		}

		s+= "</TABLE>";
		
		
		out.println(s);
		return false;
	}

}

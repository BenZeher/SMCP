package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMTax;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import SMDataDefinition.SMTablepricelistcodes;
import SMDataDefinition.SMTablepricelistlevellabels;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

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

		//TODO Calculated Totals

		int iNumberOfColumns = 6;
		//TOTALS
		s += "<TABLE style = \""
				+ " width:100%; "
				+ " \" >" + "\n";

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
				+ "<B>CALCULATED TOTALS</B>"
				+ "</TD>" + "\n"
				;

		s += "  </TR>" + "\n";

		//total material cost:
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdextendedcostScale, summary.getbdtotalmaterialcostonestimates())
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total freight
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT + "\""
			+ "width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdfreightScale, summary.getbdtotalfreightonestimates())
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total labor units:
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 3) + " >"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_UNITS_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_UNITS + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_UNITS + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + "; text-align:right; " + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborquantityScale, summary.getbdtotallaborunitsonestimates())
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;

		//total labor cost:
		s += "    <TD>"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD>"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborcostperunitScale, summary.getbdtotallaborcostonestimates())
				+ "</LABEL>"

			+ "</TD>" + "\n"
			;

		s += "  </TR>" + "\n";

		//total mark-up
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdmarkupamountScale, summary.getbdtotalmarkuponestimates())
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total tax
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_LABEL + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_LABEL + "\""
				+ ">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION
				+ "</LABEL>"
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";


		//ADDITIONAL COST NOT ELIGIBLE FOR USE TAX
		s += "  <TR>" + "\n";
		s += "    <TD"
				//+ " style = \" font-size: large; \""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, summary.getbdtotaladdlcostnoteligibleforusetax())
				+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total amount for summary
		String sSummaryID = SMEditSMSummaryEdit.UNSAVED_SUMMARY_LABEL;
		if (
				(summary.getslid().compareToIgnoreCase("-1") != 0)
				&& (summary.getslid().compareToIgnoreCase("0") != 0)
				&& (summary.getslid().compareToIgnoreCase("") != 0)			
				) {
			sSummaryID = summary.getslid();
		}
		s += "  <TR>" + "\n";
		s += "    <TD"
				//+ " style = \" font-size: large; \""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY_CAPTION + " " + sSummaryID + ":"
				+ "</TD>" + "\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(2, summary.getbdcalculatedtotalprice())
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		s += "  <TR>" + "\n";
		s += "    <TD"
				//+ " style = \" font-size: large; \""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX_LABEL + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX_LABEL + "\""
				+ ">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX_CAPTION
				+ "</LABEL>"
				+ "</TD>" + "\n"
				+ "    <TD>"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ "0.00"
				+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//space:
		s += "  <TR>" + "\n";
		s += "  </TR>" + "\n";

		//ADJUSTED VALUES:
		s += "  <TR>" + "\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
				+ "<B>ADJUSTED TOTALS</B>"
				+ "</TD>" + "\n"
				;

		//total adjusted material cost:
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdextendedcostScale, summary.getbdtotalmaterialcostonestimates())
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total adjusted freight
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_TOTAL_FREIGHT_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedfreight + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedfreight + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
			+ summary.getsbdadjustedfreight()
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Labor units
		s += "  <TR>" + "\n";
		s += "    <TD>"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_LABOR_UNITS_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL "
			+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
			+  summary.getsbdadjustedlaborunitqty()
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;

		//Total cost per labor unit
		s += "    <TD>"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_COST_PER_LABOR_UNIT_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ summary.getsbdadjustedlaborcostperunit()
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;

		//Total labor cost
		s += "    <TD>"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ "0.00"  // TODO - fill in this value with javascript
				+ "</LABEL>"
				+ "</TD>" + "\n"
				;
		s += "  </TR>" + "\n";

		//MU per labor unit
		s += "  <TR>" + "\n";
		s += "    <TD>"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION
				//+ "</TD>" + "\n"

				//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				//+ ">"
				+ "&nbsp;"
				+ "<LABEL "
				+ " NAME = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ "0.00"
				+ "</LABEL>"

				//MU Pctge
				+ "&nbsp;"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD>"
				+ "&nbsp;"
				+ "<LABEL "
				+ " NAME = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ "0.00"
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;

		//GP percentage
		s += "    <TD>"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ "0.00"
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;

		//Total MU
		s += "    <TD>"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MARKUP_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ summary.getsbdadjustedmarkupamt()
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;
		s += "  </TR>" + "\n";

		//Total tax on material
		s += "  <TR>" + "\n";
		s += "    <TD "
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ "0.00"
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Adjusted total
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY_CAPTION + " " + sSummaryID + ":"
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ "0.00"  // TODO - fill in this value with javascript
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Retail sales tax
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX_LABEL + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX_LABEL + "\""
				+ ">"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION
				+ "</LABEL>"
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ "0.00"  // TODO - fill in this value with javascript
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Additional cost AFTER retail sales tax:
		s += "  <TR> \n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " >"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_COST_AFTER_SALES_TAX_CAPTION
				+ " "
				+ "<LABEL "
				+ " NAME = \"" + SMTablesmestimatesummaries.sadditionalpostsalestaxcostlabel + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.sadditionalpostsalestaxcostlabel + "\""
				+ " style = \" text-align:right; width:200px;\">"
				+ summary.getsadditionalpostsalestaxcostlabel()
				+ "</LABEL>"

				+ "</TD>" + "\n"
				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadditionalpostsalestaxcostamt + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadditionalpostsalestaxcostamt + "\""
				+ " style = \" text-align:right;" + " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ summary.getsbdadditionalpostsalestaxcostamt()
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;
		s += "  </TR>" + "\n";


		s += "</TABLE>" + "\n";

	/*	try {
			s+=sCommandScripts(summary, sDBID, sUserFullName, context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		out.println(s);
		return false;
	}
	
	private String sCommandScripts(
			SMEstimateSummary summary,
			String sDBID,
			String sUserFullName,
			ServletContext context
			) throws Exception {
		String s = "";
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";
				
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";
		
		s += "window.onload = triggerinitiation;\n";

		//Build an array of taxes and rates to do the 'adjusted retail sales tax' calc on the fly:
		int iCounter = 0;
		String staxrates = "";
		String scalculateonpurchaseorsale = "";
		String scalculatetaxoncustomerinvoice = "";
		String staxjurisdiction = "";
		String staxtype = "";
		
		String SQL = "SELECT"
			+ " " + SMTabletax.lid
			+ ", " + SMTabletax.bdtaxrate
			+ ", " + SMTabletax.icalculateonpurchaseorsale
			+ ", " + SMTabletax.icalculatetaxoncustomerinvoice
			+ ", " + SMTabletax.staxjurisdiction
			+ ", " + SMTabletax.staxtype
			+ " FROM " + SMTabletax.TableName
			+ " ORDER BY " + SMTabletax.lid
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " [1591042478] SQL: " + SQL 
			);
			BigDecimal bdTaxRateAsPercentage = new BigDecimal("0.00");
			while (rs.next()){
				iCounter++;
				bdTaxRateAsPercentage = rs.getBigDecimal(SMTabletax.bdtaxrate);
				staxrates += "staxrates[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
					+ "\"] = \"" + clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTabletax.bdtaxratescale, bdTaxRateAsPercentage) + "\";\n";
				
				scalculateonpurchaseorsale += "scalculateonpurchaseorsale[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
				+ "\"] = \"" + Integer.toString(rs.getInt(SMTabletax.icalculateonpurchaseorsale)) + "\";\n";
				
				scalculatetaxoncustomerinvoice += "scalculatetaxoncustomerinvoice[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
				+ "\"] = \"" + Integer.toString(rs.getInt(SMTabletax.icalculatetaxoncustomerinvoice)) + "\";\n";
				
				staxjurisdiction += "staxjurisdiction[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
				+ "\"] = \"" + rs.getString(SMTabletax.staxjurisdiction) + "\";\n";
				
				staxtype += "staxtype[\"" + Long.toString(rs.getLong(SMTabletax.lid)) 
				+ "\"] = \"" + rs.getString(SMTabletax.staxtype) + "\";\n";
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1591112142] reading taxes for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "var staxrates = new Array(" + Integer.toString(iCounter) + ")\n";
			s += staxrates + "\n";
			s += "var scalculateonpurchaseorsale = new Array(" + Integer.toString(iCounter) + ")\n";
			s += scalculateonpurchaseorsale + "\n";
			s += "var scalculatetaxoncustomerinvoice = new Array(" + Integer.toString(iCounter) + ")\n";
			s += scalculatetaxoncustomerinvoice + "\n";
			s += "var staxjurisdiction = new Array(" + Integer.toString(iCounter) + ")\n";
			s += staxjurisdiction + "\n";
			s += "var staxtype = new Array(" + Integer.toString(iCounter) + ")\n";
			s += staxtype + "\n";
		}
		
		s += "\n";
		
	    //If this is an existing summary, and if the selected tax was re-configured since the summary was last saved,
	    //notify the user that the tax has changed, and they may have to click again in the tax to reset the tax values:
		String sTaxCheckAlert = "";

		    SMTax tax = new SMTax();
		    tax.set_slid(summary.getsitaxid());
		    try {
				tax.load(sDBID, context, sUserFullName);
				if (
					(tax.get_bdtaxrate().compareToIgnoreCase(summary.getsbdtaxrate()) != 0)
					|| (tax.get_scalculatetaxoncustomerinvoice().compareToIgnoreCase(summary.getsicalculatetaxoncustomerinvoice()) != 0)
					|| (tax.get_scalculateonpurchaseorsale().compareToIgnoreCase(summary.getsicalculatetaxonpurchaseorsale()) != 0)
				) {
					sTaxCheckAlert = "alert('The selected tax has been updated in the system, so the tax calculation may no longer be accurate.  '  \n" 
							+ "       + 'To update the tax information, click on the Tax drop down list, select a different tax, then select' \n" 
							+ " 	  + ' this tax again to trigger an update.')"
							+ "\n"
						;
				}
			} catch (Exception e) {
				throw new Exception("Error [202006021627] - Could not check tax with ID: '" + summary.getsitaxid() + "' - " + e.getMessage());
			}
	    
		
		s += "function checkfortaxupdates(){\n"	
				+ "    //This function has nothing in it unless the selected tax has been updated.\n"
				+ "    //In that case it will warn the user that the tax on the summary is not up to date.\n"
				+ "    " + sTaxCheckAlert
				+ "}\n\n"
			;
		
		s += "function triggerinitiation(){\n"		
			+ "    checkfortaxupdates();\n"
			+ "    // The 'taxChange' function will trigger recalculatelivetotals() automatically: \n"
			+ "    taxChange(document.getElementById(\"" + SMTablesmestimatesummaries.itaxid + "\")); \n"
			+ "    initShortcuts();\n"
			+ "    //Now reset the 'record changed' flag since the user hasn't done anything yet: \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.RECORDWASCHANGED_FLAG + "\").value = ''; \n" 
			+ "\n"
			+ "}\n\n"
		;
		
		s += "function taxChange(selectObj) {\n" 
				// get the index of the selected option 
				+ "    var idx = selectObj.selectedIndex;\n"
				// get the value of the selected option 
				+ "    var which = selectObj.options[idx].value;\n"
				//+ "alert(selectObj.options[idx].value);\n"
				// use the selected option value to retrieve the ship to fields from the ship to arrays:
				+ "    if (which != ''){\n"
				+ "        document.forms[\"MAINFORM\"].elements[\"" + SMTablesmestimatesummaries.bdtaxrate + "\"].value = staxrates[which];\n"
				+ "        document.forms[\"MAINFORM\"].elements[\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\"].value = scalculatetaxoncustomerinvoice[which];\n"
				+ "        document.forms[\"MAINFORM\"].elements[\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\"].value = scalculateonpurchaseorsale[which];\n"
				+ "        //Calculate the appropriate rates: \n"
				+ "        var icalculatetaxoncustomerinvoice = parseInt(\"0\");\n"
				+ "        var icalculatetaxonpurchaseorsale = parseInt(\"0\");\n"
				+ "        var materialtaxrate = '';\n"
				+ "        var retailsalestaxrate = '';\n"
				+ "        var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\").value);\n"
				+ "        if (temp == ''){\n"
				+ "            icalculatetaxoncustomerinvoice = parseInt(\"0\");\n"
				+ "        }else{\n"
				+ "            icalculatetaxoncustomerinvoice = parseInt(temp); \n"
				+ "        }\n"
				+ "        \n"
				
				+ "        var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\").value);\n"
				+ "        if (temp == ''){\n"
				+ "            icalculatetaxonpurchaseorsale = parseInt(\"0\");\n"
				+ "        }else{\n"
				+ "            icalculatetaxonpurchaseorsale = parseInt(temp); \n"
				+ "        }\n"
				+ "        \n"
				
				+ "        if((icalculatetaxoncustomerinvoice == 0) && ((icalculatetaxonpurchaseorsale == " 
					+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST) + "))){ \n"
				+ "            materialtaxrate = staxrates[which]; \n"
				+ "        }else{ \n"
				+ "            materialtaxrate = '0.0000'; \n"
				+ "        } \n"
				+ "        if((icalculatetaxoncustomerinvoice == 1) && ((icalculatetaxonpurchaseorsale == " 
				+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE) + "))){ \n"
				+ "            retailsalestaxrate = staxrates[which]; \n"
				+ "        }else{ \n"
				+ "            retailsalestaxrate = '0.0000'; \n"
				+ "        } \n"
				+ "        \n"
				+ "        //Add the tax type and rate to the material tax caption: \n"
				+ "        document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_LABEL + "\").innerText = \n"
				+ "            staxjurisdiction[which] + ' ' + staxtype[which] + ' ' + \n"
				+ "            materialtaxrate + '% ' + '" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL_CAPTION + "';\n"
				+ "        \n"		
				+ "        //Add the tax type and rate to the calculated retail sales tax caption: \n"
				+ "        document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX_LABEL + "\").innerText = \n"
				+ "            staxjurisdiction[which] + ' ' + staxtype[which] + ' ' + \n"
						+ "            retailsalestaxrate + '% ' + '" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX_CAPTION + "';\n"
				+ "        \n"
				+ "        //Add the tax type and rate to the calculated retail sales tax caption: \n"
				+ "        document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX_LABEL + "\").innerText = \n"
				+ "            staxjurisdiction[which] + ' ' + staxtype[which] + ' ' + \n"
						+ "            retailsalestaxrate + '% ' + '" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX_CAPTION + "';\n"

				+ "    } \n"
				+ "    //We'll set the 'dirty' flag, but not trigger a recursive racalculation here: \n"
				+ "    flagDirty(); \n"
				+ "}\n\n"; 
		
		s += "function promptToSave(){\n"		
			
			+ "    if (document.getElementById(\"" + SMEditSMSummaryEdit.RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ SMEditSMSummaryEdit.RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
			+ "        if (document.getElementById(\"" + SMEditSMSummaryEdit.COMMAND_FLAG + "\").value != \"" + SMEditSMSummaryEdit.SAVE_COMMAND_VALUE + "\""
					+ " && document.getElementById(\"" + SMEditSMSummaryEdit.COMMAND_FLAG + "\").value != \"" + SMEditSMSummaryEdit.DELETE_COMMAND_VALUE + "\"){\n"
			+ "        return 'You have unsaved changes!';\n"
			+ "        }\n"
			+ "    }\n"
			+ "}\n\n"
		;
		
	
		
		s += "function backintoprice(){\n"
				
			//+ "    alert('Back into'); \n"	
			+ "    var currentadjustedtotalprice = parseFloat(\"0.00\");\n"
			+ "    var desiredadjustedtotalprice = parseFloat(\"0.00\");\n"
			+ "    var currentadjustedmarkup = parseFloat(\"0.00\");\n"
			+ "    var requiredadjustedmarkup = parseFloat(\"0.00\");\n"
			+ "    var desireddifference = parseFloat(\"0.00\");\n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        currentadjustedtotalprice = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        currentadjustedtotalprice = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_BACK_INTO_DESIRED_PRICE + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        desiredadjustedtotalprice = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        desiredadjustedtotalprice = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    desireddifference = desiredadjustedtotalprice - currentadjustedtotalprice; \n"

			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        currentadjustedmarkup = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        currentadjustedmarkup = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    requiredadjustedmarkup = currentadjustedmarkup + desireddifference; \n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=formatNumber(requiredadjustedmarkup);\n"
			+ "    recalculatelivetotals(); \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_BACK_INTO_DESIRED_PRICE + "\").value = \"\"; \n"
			+ "}\n"
		;
		
		s += "function flagDirty() {\n"
				+ "    document.getElementById(\"" + SMEditSMSummaryEdit.RECORDWASCHANGED_FLAG + "\").value = \"" 
				+ SMEditSMSummaryEdit.RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "    recalculatelivetotals(); \n"
			+ "}\n";
		
		//Recalculate live totals:
		s += "function recalculatelivetotals(){\n"
			//+ "    alert('Recalculating');\n"
			+ "    formatnumberinputfields(); \n"
			
			// TJR - 6/2/2020 - we don't want the tax to update automatically when the page loads.
			// That should be done deliberately by the user if he WANTS to update the tax info.
			//+ "    //Set the retail sales tax rate, based on the current index of the tax drop down: \n"
			
			+ "    var adjustedlabortotalcost = parseFloat(\"0.00\");\n"
			+ "    var adjustedlaborunits = parseFloat(\"0.00\");\n"
			+ "    var adjustedlaborcostperunit = parseFloat(\"0.00\");\n"
			+ "    var adjustedtotalforsummary = parseFloat(\"0.00\");\n"
			+ "    var materialcosttotal = parseFloat(\"0.00\");\n"
			+ "    var adjustedtfreighttotal = parseFloat(\"0.00\");\n"
			+ "    var adjustedmarkuptotal = parseFloat(\"0.00\");\n"
			+ "    var taxonmaterial = parseFloat(\"0.00\");\n"
			+ "    var taxrateaspercentage = parseFloat(\"0.00\");\n"
			+ "    var icalculatetaxoncustomerinvoice = \"0\";\n"
			+ "    var icalculatetaxonpurchaseorsale = \"0\";\n"
			+ "    var totalcalculatedestimateprice = \"0\";\n"
			+ "    var totalfreightonestimates = \"0.00\"; \n"
			+ "    var totallaboronestimates = \"0.00\"; \n"
			+ "    var totalmarkuponestimates = \"0.00\"; \n"
			+ "    var calculatedretailsalestax = \"0.00\"; \n"
			+ "    var retailsalestaxrateaspercent = parseFloat(\"0.00\");\n"
			+ "    var retailsalestaxrateasdecimal = parseFloat(\"0.00\");\n"
			+ "    var adjustedretailsalestax = parseFloat(\"0.00\");\n"
			+ "    var adjustedretailsalestaxamount = parseFloat(\"0.00\");\n"
			+ "    var adjustedmarkupperlaborunit = parseFloat(\"0.00\");\n"
			+ "    var adjustedmarkuppercentage = parseFloat(\"0.00\");\n"
			+ "    var adjustedgppercentage = parseFloat(\"0.00\");\n"
			+ "    var addlcostnoteligibleforusetax = parseFloat(\"0.00\");\n"
			
			+ "    //Calculate the total adjusted sell price: \n"
			+ "    //Should equal totalmaterialcost + totalfreight + totallabor + totalmarkup + totalmaterialtax \n"
			+ "    \n"
			
			+ "    //Get the material total: \n"
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        materialcosttotal = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        materialcosttotal = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    //Get the tax on material: \n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdtaxrate + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        taxrateaspercentage = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        taxrateaspercentage = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.icalculatetaxoncustomerinvoice + "\").value);\n"
			+ "    if (temp == ''){\n"
			+ "        icalculatetaxoncustomerinvoice = parseInt(\"0\");\n"
			+ "    }else{\n"
			+ "        icalculatetaxoncustomerinvoice = parseInt(temp); \n"
			+ "    }\n"
			+ "    \n"
			
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.icalculatetaxonpurchaseorsale + "\").value);\n"
			+ "    if (temp == ''){\n"
			+ "        icalculatetaxonpurchaseorsale = parseInt(\"0\");\n"
			+ "    }else{\n"
			+ "        icalculatetaxonpurchaseorsale = parseInt(temp); \n"
			+ "    }\n"
			+ "    \n"
			
			+ "    if((icalculatetaxoncustomerinvoice == 0) && ((icalculatetaxonpurchaseorsale == " 
				+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST) + "))){ \n"
			+ "        taxonmaterial = parseFloat((materialcosttotal * (taxrateaspercentage / 100)).toFixed(2)); \n"
			+ "    }else{ \n"
			+ "        taxonmaterial = parseFloat(\"0.00\"); \n"
			+ "    } \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_TAX_ON_MATERIAL + "\").innerText=formatNumber(taxonmaterial);\n"
			+ "    \n"

			+ "    //Get the calculated total for the estimate summary: \n"
			+ "    //This should equal: totalmaterialcost + totalfreightonestimates + totallaborcost + totalmarkup + totaltax \n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        totalfreightonestimates = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        totalfreightonestimates = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        totallaboronestimates = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        totallaboronestimates = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        totalmarkuponestimates = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        totalmarkuponestimates = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        addlcostnoteligibleforusetax = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        addlcostnoteligibleforusetax = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    totalcalculatedestimateprice = \n"
				+ "        materialcosttotal + totalfreightonestimates + totallaboronestimates + totalmarkuponestimates + taxonmaterial + addlcostnoteligibleforusetax; \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\").innerText=formatNumber(totalcalculatedestimateprice);\n"
			+ "    \n"
				
			+ "    //Get the calculated retail sales tax for the estimate summary: \n"
			+ "    if((icalculatetaxoncustomerinvoice == 1) && ((icalculatetaxonpurchaseorsale == " 
			+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE) + "))){ \n"
			+ "        calculatedretailsalestax = parseFloat((totalcalculatedestimateprice * (taxrateaspercentage / 100)).toFixed(2)); \n"
			+ "    }else{ \n"
			+ "        calculatedretailsalestax = parseFloat(\"0.00\"); \n"
			+ "    } \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_RETAIL_SALES_TAX + "\").innerText=formatNumber(calculatedretailsalestax);\n"
			+ "    \n"
			
			+ "    //Get the adjusted freight amount: \n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedtfreighttotal = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedtfreighttotal = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    //Calculate the total adjusted labor cost: \n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedlaborunits = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedlaborunits = parseFloat(temp);\n"
			+ "    }\n"
			
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedlaborcostperunit = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedlaborcostperunit = parseFloat(temp);\n"
			+ "    }\n"
			
			+ "    adjustedlabortotalcost = adjustedlaborunits * adjustedlaborcostperunit;\n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\").innerText=formatNumber(adjustedlabortotalcost);\n"
			+ "    \n"
			
			+ "    //Get the adjusted markup amount: \n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedmarkuptotal = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedmarkuptotal = parseFloat(temp);\n"
			+ "    }\n"
			+ "    \n"
			
			+ "    //Set the tax on material for the adjusted section (same as tax on material above): \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\").innerText=formatNumber(taxonmaterial);\n"
			+ "    \n"
			
			+ "    adjustedtotalforsummary = materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost + adjustedmarkuptotal + taxonmaterial; \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\").innerText=formatNumber(adjustedtotalforsummary);\n"
			+ "    \n"
			
			+ "    //Calculate the MU per labor unit, as a percentage and as a GP percentage: \n"
			+ "    //adjustedmarkupperlaborunit = adjustedmarkuptotal / adjustedlaborunits \n"
			+ "    if(compare2DecimalPlaceFloats(adjustedlaborunits, parseFloat(\"0.00\"))){ \n"
			+ "        adjustedmarkupperlaborunit = adjustedmarkuptotal; \n"
			+ "    } else { \n"
			+ "        adjustedmarkupperlaborunit = adjustedmarkuptotal / adjustedlaborunits; \n"
			+ "    } \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\").value=formatNumber(adjustedmarkupperlaborunit);\n"
			+ "    \n\n"
			+ "    //adjustedmarkuppercentage = adjustedmarkuptotal / (materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost) \n"
			
			+ "    if ((materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost) > parseFloat(\"0\")){ \n"
			+ "        adjustedmarkuppercentage = adjustedmarkuptotal / (materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost); \n"
			+ "    } else {\n"
			+ "        adjustedmarkuppercentage = parseFloat(\"0\"); \n"
			+ "    } \n"
			+ "    adjustedmarkuppercentage = adjustedmarkuppercentage * 100; \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE + "\").value=formatNumber(adjustedmarkuppercentage);\n"
			+ "    \n"
			+ "    //adjustedgppercentage = adjustedmarkuptotal / adjustedtotalforsummary \n"
			+ "    if (adjustedtotalforsummary > parseFloat(\"0\")){ \n"
			+ "        adjustedgppercentage = adjustedmarkuptotal / adjustedtotalforsummary; \n"
			+ "    } else {\n"
			+ "        adjustedgppercentage = parseFloat(\"0\"); \n"
			+ "    } \n"
			+ "    adjustedgppercentage = adjustedgppercentage * 100; \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE + "\").value=formatNumber(adjustedgppercentage);\n"
			+ "    \n"
			
			+ "    //Get the adjusted retail sales tax for the estimate summary: \n"
			+ "    if((icalculatetaxoncustomerinvoice == 1) && ((icalculatetaxonpurchaseorsale == " 
						+ Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE) + "))){ \n"
			+ "        adjustedretailsalestaxamount = parseFloat((adjustedtotalforsummary * (taxrateaspercentage / 100)).toFixed(2)); \n"
			+ "    }else{ \n"
			+ "        adjustedretailsalestaxamount = parseFloat(\"0.00\"); \n"
			+ "    } \n"
			+ "    document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_RETAIL_SALES_TAX + "\").innerText=formatNumber(adjustedretailsalestaxamount);\n"
			+ "    \n"
		;	
		s += "}\n"
   		;
		
		//Compare floats with 2 decimal precision:
		s += "function compare2DecimalPlaceFloats(float1, float2){ \n"
			+ "    var firstfloatstring = (Math.round(parseFloat(float1)*100)/100).toFixed(2); \n"
			+ "    var secondfloatstring = (Math.round(parseFloat(float2)*100)/100).toFixed(2); \n"
			+ "    if(firstfloatstring.localeCompare(secondfloatstring) == 0){ \n"
			+ "        return true; \n"
			+ "    }else{ \n"
			+ "        return false; \n"
			+ "    }"
			+ "}\n\n"
		;
		
		//Format numbers to have commas as needed:
		s += "function formatNumber(num) {\n"
			+ "    return num.toFixed(2).replace(/(\\d)(?=(\\d{3})+(?!\\d))/g, '$1,') \n"
			+ "}\n\n"
		;
		
		//Format numbers to 4 decimal places and have commas as needed:
		s += "function formatNumberTo4Places(num) {\n"
			+ "    return num.toFixed(4); \n"
			+ "}\n\n"
		;
		
		s += "\n"
			+ "function isNumeric(value) {\n"
			+ "    if ((value == null) || (value == '')) return false;\n"
			+ "    var strippedstring = value.replace(/,/g, '');\n"
			//+ "    alert(strippedstring);\n"
			+ "    if (!strippedstring.toString().match(/^[-]?\\d*\\.?\\d*$/)) return false;\n"
			+ "    return true\n"
			+ "    }\n"
			+ "\n\n"
		;
		
		//Recalculate MU using MU percentage:
		s += "function calculateMUusingMUpercentage(){\n"
			+ "    var adjustedtotalmarkup = parseFloat(\"0.00\");\n"
			+ "    var adjustedMUpercentage = parseFloat(\"0.00\");\n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedMUpercentage = parseFloat(\"0.00\")\n;"
			+ "    }else{\n"
			+ "        adjustedMUpercentage = parseFloat(temp);\n"
			+ "    }\n"
			+ "    adjustedMUpercentage = adjustedMUpercentage / 100; \n"
			
			+ "    //Get the total cost before mark-up:\n"
			+ "    var materialcost = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        materialcost = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        materialcost = parseFloat(temp);\n"
			+ "    }\n"

			+ "    var adjustedfreightcost = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedfreightcost = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedfreightcost = parseFloat(temp);\n"
			+ "    }\n"
			+ "    var adjustedlaborcost = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedlaborcost = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedlaborcost = parseFloat(temp);\n"
			+ "    }\n"
			+ "    var adjustedpremarkupcost = materialcost + adjustedfreightcost + adjustedlaborcost;\n"
			
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=(adjustedpremarkupcost * adjustedMUpercentage).toFixed(2);\n"
			+ "    flagDirty();\n"
			
   			;
		s += "}\n"
   		;
		
		//Recalculate MU using MU per labor unit:
		s += "function calculateMUusingMUperlaborunit(){\n"
			+ "    // adjustedtotalmarkup = adjustedMUperlaborunit * adjustedlaborunits \n"
			+ "    var adjustedtotalmarkup = parseFloat(\"0.00\");\n"
			+ "    var adjustedMUperlaborunit = parseFloat(\"0.00\");\n"
			+ "    var adjustedlaborunits = parseFloat(\"0.00\");\n"
			
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedMUperlaborunit = parseFloat(\"0.00\")\n;"
			+ "    }else{\n"
			+ "        adjustedMUperlaborunit = parseFloat(temp);\n"
			+ "    }\n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedlaborunits = parseFloat(\"0.00\")\n;"
			+ "    }else{\n"
			+ "        adjustedlaborunits = parseFloat(temp);\n"
			+ "    }\n"
			+ "    adjustedtotalmarkup = adjustedMUperlaborunit * adjustedlaborunits; \n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=adjustedtotalmarkup.toFixed(2);\n"
			+ "    flagDirty(); \n"
   			;
		s += "}\n"
   		;
		
		//Recalculate MU using GP percentage:
		s += "function calculateMUusingGPpercentage(){\n"
			+ "    // adjustedtotalmarkup = (adjustedpremarkupcost / (1 - (adjustedGPpercentage/100))) - adjustedpremarkupcost \n"
			+ "    var adjustedtotalmarkup = parseFloat(\"0.00\");\n"
			+ "    var adjustedGPpercentage = parseFloat(\"0.00\");\n"
			+ "    var adjustedGPpercentageAsFraction = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedGPpercentage = parseFloat(\"0.00\")\n;"
			+ "    }else{\n"
			+ "        adjustedGPpercentage = parseFloat(temp);\n"
			+ "    }\n"
			+ "    adjustedGPpercentageAsFraction = adjustedGPpercentage / 100; \n"
			
			+ "    //Get the total cost before mark-up:\n"
			+ "    var materialcost = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        materialcost = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        materialcost = parseFloat(temp);\n"
			+ "    }\n"

			+ "    var adjustedfreightcost = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedfreightcost = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedfreightcost = parseFloat(temp);\n"
			+ "    }\n"
			+ "    var adjustedlaborcost = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\").innerText).replace(',','');\n"
			+ "    if (temp == ''){\n"
			+ "        adjustedlaborcost = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        adjustedlaborcost = parseFloat(temp);\n"
			+ "    }\n"
			+ "    var adjustedpremarkupcost = materialcost + adjustedfreightcost + adjustedlaborcost;\n"
			
			+ "    adjustedtotalmarkup = (adjustedpremarkupcost / (1 - (adjustedGPpercentageAsFraction))) - adjustedpremarkupcost; \n"
			
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=adjustedtotalmarkup.toFixed(2);\n"
			+ "    flagDirty();\n"
			
   			;
		s += "}\n"
   		;

		//Set all editable fields to their correct decimal formats:
		s += "function formatnumberinputfields(){ \n"
				
			+ "    var fieldvalue = parseFloat(\"0.00\");\n"
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value).replace(',','');\n"
			+ "    if (!isNumeric(temp)){ \n"
			+ "        temp = ''; \n"
			+ "    } \n"
			+ "    if (temp == ''){\n"
			+ "        fieldvalue = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        fieldvalue = parseFloat(temp);\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedfreight + "\").value=formatNumber(fieldvalue);\n"
			
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\").value).replace(',','');\n"
			+ "    if (!isNumeric(temp)){ \n"
			+ "        temp = ''; \n"
			+ "    } \n"
			+ "    if (temp == ''){\n"
			+ "        fieldvalue = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        fieldvalue = parseFloat(temp);\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\").value=formatNumber(fieldvalue);\n"
			
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value).replace(',','');\n"
			+ "    if (!isNumeric(temp)){ \n"
			+ "        temp = ''; \n"
			+ "    } \n"
			+ "    if (temp == ''){\n"
			+ "        fieldvalue = parseFloat(\"0.0000\");\n"
			+ "    }else{\n"
			+ "        fieldvalue = parseFloat(temp);\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\").value=formatNumberTo4Places(fieldvalue);\n"
		
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value).replace(',','');\n"
			+ "    if (!isNumeric(temp)){ \n"
			+ "        temp = ''; \n"
			+ "    } \n"
			+ "    if (temp == ''){\n"
			+ "        fieldvalue = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        fieldvalue = parseFloat(temp);\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\").value=formatNumber(fieldvalue);\n"
			
			+ "    var temp = (document.getElementById(\"" + SMTablesmestimatesummaries.bdadditionalpostsalestaxcostamt + "\").value).replace(',','');\n"
			+ "    if (!isNumeric(temp)){ \n"
			+ "        temp = ''; \n"
			+ "    } \n"
			+ "    if (temp == ''){\n"
			+ "        fieldvalue = parseFloat(\"0.00\");\n"
			+ "    }else{\n"
			+ "        fieldvalue = parseFloat(temp);\n"
			+ "    }\n"
			+ "    document.getElementById(\"" + SMTablesmestimatesummaries.bdadditionalpostsalestaxcostamt + "\").value=formatNumber(fieldvalue);\n"
			;
			s += "}\n\n"
		;
		
		s += "}\n";
			
		s += "</script>\n";
		return s;
	}

}

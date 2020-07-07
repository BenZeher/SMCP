package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
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
		if(summary.getsipricelevel().compareToIgnoreCase("")==0 || summary.getsipricelevel().isEmpty()) {
			summary.setsipricelevel("0");
		}
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
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + " >"
				+ "<B>CALCULATED TOTALS</B>"
				+ "</TD>" + "\n"
				;

		s += "  </TR>" + "\n";

		//total material cost:
		BigDecimal bdMaterialCost = new BigDecimal(0);
		bdMaterialCost = summary.getbdtotalmaterialcostonestimates();

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MATERIAL_COST + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdMaterialCost
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total freight
		BigDecimal bdFreightCost = new BigDecimal(0);
		bdFreightCost = summary.getbdtotalfreightonestimates();

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FREIGHT + "\""
			+ "width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdFreightCost
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total labor units:
		BigDecimal bdLaborUnits = new BigDecimal(0);
		bdLaborUnits = summary.getbdtotallaborunitsonestimates();

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 3) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_UNITS_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_UNITS + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_UNITS + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + "; text-align:right; " + "\""
			+ ">"
			+ bdLaborUnits
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;

		//total labor cost:
		BigDecimal bdLaborCost = new BigDecimal(0);
		bdLaborCost = summary.getbdtotallaborcostonestimates();

		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD>"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_LABOR_COST + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ bdLaborCost.setScale(2,BigDecimal.ROUND_HALF_UP)
				+ "</LABEL>"

			+ "</TD>" + "\n"
			;

		s += "  </TR>" + "\n";

		//total mark-up
		BigDecimal bdMarkUp = new BigDecimal(0);
		bdMarkUp = summary.getbdtotalmarkuponestimates();

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_MARKUP + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdMarkUp
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total tax
		//materialcosttotal * (taxrateaspercentage / 100
		BigDecimal bdTaxPercentage = new BigDecimal(0);
		bdTaxPercentage = BigDecimal.valueOf(Double.valueOf(summary.getsbdtaxrate()));

		BigDecimal bdTotalTax = new BigDecimal(0);
		bdTotalTax = bdMaterialCost.multiply(bdTaxPercentage.divide(BigDecimal.valueOf(100)));

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
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
			+ bdTotalTax.setScale(2, BigDecimal.ROUND_HALF_UP)
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";


		//ADDITIONAL COST NOT ELIGIBLE FOR USE TAX
		BigDecimal bdCostNotEligibleForUseTax = new BigDecimal(0);
		bdCostNotEligibleForUseTax = summary.getbdtotaladdlcostnoteligibleforusetax();

		s += "  <TR>" + "\n";
		s += "    <TD"
				//+ " style = \" font-size: large; \""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				+ ">"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_ADDITIONAL_COST_NOT_ELIGIBLE_FOR_USE_TAX + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ bdCostNotEligibleForUseTax
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
		BigDecimal bdCalculatedTotalPrice = new BigDecimal(0);
		bdCalculatedTotalPrice = summary.getbdcalculatedtotalprice();

		s += "  <TR>" + "\n";
		s += "    <TD"
				//+ " style = \" font-size: large; \""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY_CAPTION + " " + sSummaryID + ":"
				+ "</TD>" + "\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_CALCULATED_TOTAL_FOR_SUMMARY + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdCalculatedTotalPrice.setScale(2, BigDecimal.ROUND_HALF_UP)
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		s += "  <TR>" + "\n";
		s += "    <TD"
				//+ " style = \" font-size: large; \""
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
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
				+ "0.00" // TODO - fill in this value with java
				+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//space:
		s += "  <TR>" + "\n";
		s += "  </TR>" + "\n";

		//ADJUSTED VALUES:
		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
				+ "<B>ADJUSTED TOTALS</B>"
				+ "</TD>" + "\n"
				;

		//total adjusted material cost:
		BigDecimal bdTotalMaterialCost = new BigDecimal(0);
		bdTotalMaterialCost = summary.getbdtotalmaterialcostonestimates();

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MATERIAL_COST + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdTotalMaterialCost.setScale(SMTablesmestimates.bdextendedcostScale, BigDecimal.ROUND_HALF_UP)
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//total adjusted freight
		BigDecimal bdAdjustedFreight = new BigDecimal(0);
		bdAdjustedFreight = BigDecimal.valueOf(Double.valueOf(summary.getsbdadjustedfreight()));

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_TOTAL_FREIGHT_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedfreight + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedfreight + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
			+ bdAdjustedFreight
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Labor units
		BigDecimal bdAdjustedLaborUnitQty = new BigDecimal(0);
		bdAdjustedLaborUnitQty = BigDecimal.valueOf(Double.valueOf(summary.getsbdadjustedlaborunitqty()));

		s += "  <TR>" + "\n";
		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_LABOR_UNITS_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
			+ ">"
			+ "<LABEL "
			+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\""
			+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
			+  bdAdjustedLaborUnitQty
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;

		//Total cost per labor unit
		BigDecimal bdAdjustedLaborCostPerUnit = new BigDecimal(0);
		bdAdjustedLaborCostPerUnit = BigDecimal.valueOf(Double.valueOf(summary.getsbdadjustedlaborcostperunit()));

		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_COST_PER_LABOR_UNIT_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ bdAdjustedLaborCostPerUnit
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;

		BigDecimal bdAdjustLaborUnitQty = BigDecimal.valueOf(Double.valueOf(summary.getsbdadjustedlaborunitqty()));
		//Total labor cost
		BigDecimal bdTotalLaborCost = new BigDecimal(0);
		bdTotalLaborCost = bdAdjustedLaborUnitQty.multiply(bdAdjustedLaborCostPerUnit);


		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL"
				+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_LABOR_COST + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
				+ ">"
				+ bdTotalLaborCost
				+ "</LABEL>"
				+ "</TD>" + "\n"
				;
		s += "  </TR>" + "\n";

		BigDecimal bdAdjustedMarkUpAmt = new BigDecimal(0);
		bdAdjustedMarkUpAmt = BigDecimal.valueOf(Double.valueOf(summary.getsbdadjustedmarkupamt()));


		//Total tax on material
		//materialcosttotal * (taxrateaspercentage / 100)).toFixed(2))
		BigDecimal bdTaxOnMaterial = new BigDecimal(0);
		bdTaxOnMaterial = bdMaterialCost.multiply(bdTaxPercentage.divide(BigDecimal.valueOf(100))).setScale(2,BigDecimal.ROUND_HALF_UP);

		BigDecimal bdAdjustedTotalSummary = new BigDecimal(0);
		bdAdjustedTotalSummary = bdTotalMaterialCost.add(bdAdjustedFreight).add(bdTotalLaborCost).add(bdAdjustedMarkUpAmt).add(bdTaxOnMaterial);

		//MU per labor unit
		//adjustedmarkuptotal / adjustedlaborunits
		BigDecimal bdTemp = new BigDecimal(0);
		if(bdAdjustedMarkUpAmt.compareTo(BigDecimal.ZERO)==0 || bdAdjustLaborUnitQty.compareTo(BigDecimal.ZERO)==0) {
			bdTemp= BigDecimal.ZERO.setScale(2);
		}else {
			bdTemp=bdAdjustedMarkUpAmt.divide(bdAdjustLaborUnitQty, BigDecimal.ROUND_HALF_UP).setScale(2,BigDecimal.ROUND_HALF_UP);
		}

		s += "  <TR>" + "\n";
		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT_CAPTION
				//+ "</TD>" + "\n"

				//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\""
				//+ ">"
				+ "&nbsp;"
				+ "<LABEL "
				+ " NAME = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PER_LABOR_UNIT + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ bdTemp
				+ "</LABEL>";

		//MU Pctge
		//adjustedmarkuptotal / (materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost)
		BigDecimal bdMUPctgeDivisor = new BigDecimal(0);
		bdMUPctgeDivisor = bdMaterialCost.add(bdTotalLaborCost).add(bdAdjustedFreight);
		BigDecimal bdMUPctge = new BigDecimal(0);
		bdMUPctge=BigDecimal.valueOf(bdAdjustedMarkUpAmt.doubleValue()/bdMUPctgeDivisor.doubleValue());
		
		s+= "&nbsp;"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"
				+ "    <TD>"
				+ "&nbsp;"
				+ "<LABEL "
				+ " NAME = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_MU_PERCENTAGE + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ bdMUPctge.scaleByPowerOfTen(2).setScale(2, BigDecimal.ROUND_HALF_UP)
				+ "%</LABEL>"

				+ "</TD>" + "\n"
				;

		//GP percentage
		//adjustedgppercentage = adjustedmarkuptotal / adjustedtotalforsummary
		BigDecimal bdGPPercentage =  bdAdjustedMarkUpAmt.divide(bdAdjustedTotalSummary,BigDecimal.ROUND_HALF_UP,1).multiply(BigDecimal.valueOf(100));

		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE + "\""
				+ " ID = \"" + SMEditSMSummaryEdit.FIELD_ADJUSTED_GP_PERCENTAGE + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+bdGPPercentage.setScale(2, BigDecimal.ROUND_HALF_UP)
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;

		//Total MU

		s += "    <TD style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_MARKUP_CAPTION
				+ "</TD>" + "\n"

				+ "    <TD>"
				+ "<LABEL "
				+ " NAME = \"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\""
				+ " ID = \"" + SMTablesmestimatesummaries.bdadjustedmarkupamt + "\""
				+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_TEXT_INPUTS + ";" + "\">"
				+ bdAdjustedMarkUpAmt.setScale(2, BigDecimal.ROUND_HALF_UP)
				+ "</LABEL>"

				+ "</TD>" + "\n"
				;
		s += "  </TR>" + "\n";



		//Total tax on material
		s += "  <TR>" + "\n";
		s += "    <TD "
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL_CAPTION
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_TAX_ON_MATERIAL + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdTaxOnMaterial.setScale(2, BigDecimal.ROUND_HALF_UP)
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Adjusted total
		//adjustedtotalforsummary = materialcosttotal + adjustedtfreighttotal + adjustedlabortotalcost + adjustedmarkuptotal + taxonmaterial


		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
				+ SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY_CAPTION + " " + sSummaryID + ":"
				+ "</TD>" + "\n"

			+ "    <TD>"
			+ "<LABEL"
			+ " NAME = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\""
			+ " ID = \"" + SMEditSMSummaryEdit.LABEL_ADJUSTED_TOTAL_FOR_SUMMARY + "\""
			+ " width:" + SMEditSMSummaryEdit.TOTALS_FIELD_WIDTH_FOR_LABELS + ";" + "\""
			+ ">"
			+ bdAdjustedTotalSummary
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Retail sales tax
		//adjustedtotalforsummary * (taxrateaspercentage / 100)
		BigDecimal bdRetailSalesTax = new BigDecimal(0);
		if(bdTaxPercentage.compareTo(BigDecimal.valueOf(0))!=0) {
			bdRetailSalesTax = bdAdjustedTotalSummary.multiply(bdTaxPercentage.divide(BigDecimal.valueOf(100)));
		}

		s += "  <TR>" + "\n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\">"
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
			+ bdRetailSalesTax.setScale(2,BigDecimal.ROUND_HALF_UP)
			+ "</LABEL>"

			+ "</TD>" + "\n"
			;
		s += "  </TR>" + "\n";

		//Additional cost AFTER retail sales tax:
		s += "  <TR> \n";
		s += "    <TD"
				+ " COLSPAN = " + Integer.toString(iNumberOfColumns - 1) + " style=\"text-align:right\" >"
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


		ArrayList<SMEstimate> Estimates = summary.getEstimateArray();
		for (int i = 0; i < summary.getEstimateArray().size(); i++) {
			SMEstimate estimate = Estimates.get(i);
			
			s += "<TABLE style = \""
					+ " width:100%; "
					+ " \" >" + "\n";
			
			s += "  <TR> \n";
			s+="<TD>";
			s+="<B>Estimate ID:</B>" + estimate.getslid();
			s+="</TD>";
			s+="<TD>";
			s+="<B>Summary line #:</B>" + estimate.getslsummarylinenumber();
			s+="</TD>";
			s+="<TD>";
			s+="<B>Created:</B>" + estimate.getsdatetimecreated()
			+ " by " + estimate.getscreatedbyfullname() 
			+ " Last modified " + estimate.getsdatetimelastmodified()
			+ " by " + estimate.getslastmodifiedbyfullname();
			s+="</TD>";
			s += "  </TR>" + "\n";
			
			s += "  <TR> \n";
			s+="<TD>";
			s+="<B>Insert as prefix label using item #:</B>" + estimate.getsprefixlabelitem();
			s+="</TD>";
			s+="<TD colspan=\"2\">";
			s+="<B>Estimate description:</B>" + estimate.getsdescription();
			s+="</TD>";
			s += "  </TR>" + "\n";
			
			s += "  <TR> \n";
			s+="<TD colspan=\"3\">";
			if(estimate.getsvendorquotenumber().compareToIgnoreCase("")==0) {
				s+="<B>Vendor quote #:</B> (NONE)";
			}else {
				s+="<B>Vendor quote #:</B>" + estimate.getsvendorquotenumber();
			}
			s+="</TD>";
			s += "  </TR>" + "\n";
			s += "</TABLE>" + "\n";
			
			
			s += "<TABLE style = \""
					+ " width:100%; "
					+ " \" >" + "\n";
			//Heading
			s += "  <TR> \n";
			s+="<TD>";
			s+="<B>Quantity</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Item #</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Product description</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>U/M</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Multiplier</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Unit sell price</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Extended sell price</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Material Unit cost</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Material Extended cost</B>";
			s+="</TD>";
			s += "  </TR>" + "\n";
			
			//Line
			s += "  <TR> \n";
			s+="<TD>";
			s+="<B>Quantity</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Item #</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Product description</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>U/M</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Multiplier</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Unit sell price</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Extended sell price</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Material Unit cost</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Material Extended cost</B>";
			s+="</TD>";
			s += "  </TR>" + "\n";
			
			//Other
			s += "  <TR> \n";
			s+="<TD>";
			s+="<B>Quantity</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Item #</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Product description</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>U/M</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Multiplier</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Unit sell price</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Extended sell price</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Material Unit cost</B>";
			s+="</TD>";
			s+="<TD>";
			s+="<B>Material Extended cost</B>";
			s+="</TD>";
			s += "  </TR>" + "\n";
			
			s += "</TABLE>" + "\n";
			
			
		}

		out.println(s);
		return false;
	} 

}

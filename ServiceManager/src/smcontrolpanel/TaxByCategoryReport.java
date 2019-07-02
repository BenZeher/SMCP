package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class TaxByCategoryReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	public TaxByCategoryReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			boolean bShowInvoiceLines,
			PrintWriter out
			) throws Exception{

		//First, validate the dates:
		/*if(!SMUtilities.IsValidDateString("M/d/yyyy", sStartingDate)){
			m_sErrorMessage = "Invalid starting date: '" + sStartingDate + "'";
			return false;
		}
		if(!SMUtilities.IsValidDateString("M/d/yyyy", sEndingDate)){
			m_sErrorMessage = "Invalid ending date: '" + sEndingDate + "'";
			return false;
		}*/
    	//variables for total calculations
    	BigDecimal dCategoryCost = new BigDecimal(0);
    	BigDecimal dCategoryPrice = new BigDecimal(0);
    	BigDecimal dCategoryTax = new BigDecimal(0);

    	BigDecimal dTaxGroupCost = new BigDecimal(0);
    	BigDecimal dTaxGroupPrice = new BigDecimal(0);
    	BigDecimal dTaxGroupTax = new BigDecimal(0);
    	
    	BigDecimal dTaxClassCost = new BigDecimal(0);
    	BigDecimal dTaxClassPrice = new BigDecimal(0);
    	BigDecimal dTaxClassTax = new BigDecimal(0);
    	
    	//variables for grand total calculations
    	BigDecimal dGrandTotalCost = new BigDecimal(0);
    	BigDecimal dGrandTotalPrice = new BigDecimal(0);
    	BigDecimal dGrandTotalTax = new BigDecimal(0);
		out.println(SMUtilities.getMasterStyleSheetLink());
    	//print out the column headers.
    	out.println("<TABLE BORDER=0 WIDTH=100% class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
    	if (bShowInvoiceLines){
    		out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \">" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><FONT SIZE=2>Item Category</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><FONT SIZE=2>Tax Jurisdiction</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=2%><B><FONT SIZE=2>Tax Type</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=2%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><FONT SIZE=2>Invoice Date</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><FONT SIZE=2>Invoice #</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Qty</FONT></B></TD>" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10%><B><FONT SIZE=2>Item #</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=40%><B><FONT SIZE=2>Item Decription</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><FONT SIZE=2>Taxable</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Cost</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Price</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Tax</FONT></B></TD>" +
			"</TR>" + 
	   		"<TR><TD COLSPAN=13><HR></TD><TR>");
    	}else{
    		out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \">" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=2%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=40%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><FONT SIZE=2>&nbsp;</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Cost</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Price</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Tax</FONT></B></TD>" +
    			"</TR>" + 
    	   		"<TR><TD COLSPAN=13><HR></TD><TR>");
    		
    	}
    	String sCurrentCategory = "";
    	String sCurrentTaxGroup = "";
    	String sCurrentTaxType = "";
    	
    	BigDecimal bdCost = new BigDecimal(0);
    	BigDecimal bdPrice = new BigDecimal(0);
    	BigDecimal bdTax = new BigDecimal(0);
    	String SQLStartDate = null;
		try {
			SQLStartDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate), "yyyy-MM-dd");
		} catch (ParseException e1) {
			m_sErrorMessage = "Error:[1423845010] Invalid Starting date: '" + sStartingDate + "' - " + e1.getMessage();
			return false;
		}
    	String SQLEndDate = null;
		try {
			SQLEndDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate), "yyyy-MM-dd");
		} catch (ParseException e1) {
			m_sErrorMessage = "Error:[1423581180] Invalid Ending date: '" + sEndingDate + "' - " + e1.getMessage();
			return false;
		}
    	String SQL = "select" 
    		+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
    		+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxtype
    		+ ", "+ SMTableinvoiceheaders.TableName + "."+ SMTableinvoiceheaders.datInvoiceDate
    		+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
    		+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped
    		+ ", if(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable + " = 0,'N','Y') as Taxable"
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount
    		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.bdlinesalestaxamount
    		+ " from"
    		+ " " + SMTableinvoiceheaders.TableName + ", " + SMTableinvoicedetails.TableName
    		+ " where ("
    			+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber 
    				+ " = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + ")"
    			+ " and (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate 
    				+ " >= '" + SQLStartDate + "')"
    			+ " and (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + 
    				" <= '" + SQLEndDate + "')"
    		+ ")"
    		+ " ORDER BY"
    			+ " " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction
    			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory
    			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxtype
    			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
    		;
    	long lLinesInTable = 0;
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			out.println("</TABLE>");
			out.println("<TABLE BORDER=0 WIDTH=100% class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">");
			int alt = 0;
			while(rs.next()){
				
	    		if(lLinesInTable == 100){
	    			lLinesInTable = 0;
	    		}
				
				//Print the header for any new tax class OR tax group OR category:
				if (
					(rs.getString(SMTableinvoiceheaders.staxtype).compareToIgnoreCase(sCurrentTaxType) != 0)
					|| (rs.getString(SMTableinvoiceheaders.staxjurisdiction).compareToIgnoreCase(sCurrentTaxGroup) != 0)
	    			|| (rs.getString(SMTableinvoicedetails.sItemCategory).compareToIgnoreCase(sCurrentCategory) != 0)
				){
	    			//Print the footer, if the record is for a new tax class:
					if (sCurrentTaxType.compareToIgnoreCase("") != 0){
		    			printTaxClassFooter(
		    					sCurrentCategory, 
		    					sCurrentTaxGroup,
		    					sCurrentTaxType,
		    					dTaxClassCost, 
		    					dTaxClassPrice, 
		    					dTaxClassTax, 
		    					out
		    					);
		    			
		    			//Reset the tax class totals:
		    			dTaxClassCost = BigDecimal.ZERO;
		    			dTaxClassPrice = BigDecimal.ZERO;
		    			dTaxClassTax = BigDecimal.ZERO;
		    		}
	    		}
	    		
				//Print the footer for any new COMBINATION of category and tax group:
	    		if (
	    			(rs.getString(SMTableinvoiceheaders.staxjurisdiction).compareToIgnoreCase(sCurrentTaxGroup) != 0)
	    			|| (rs.getString(SMTableinvoicedetails.sItemCategory).compareToIgnoreCase(sCurrentCategory) != 0)
	    			){
	    			if (sCurrentCategory.compareToIgnoreCase("") != 0){
		    			printCategoryFooter(
			    				sCurrentTaxGroup, 
			    				sCurrentCategory, 
			    				dCategoryCost, 
			    				dCategoryPrice, 
			    				dCategoryTax, 
			    				out
			    			);
		    			
		    			//Reset the category totals:
		    			dCategoryCost = BigDecimal.ZERO;
		    			dCategoryPrice = BigDecimal.ZERO;
		    			dCategoryTax = BigDecimal.ZERO;
	    			}
	    		}
	    		//Print the footer for any new tax group:
	    		if (rs.getString(SMTableinvoiceheaders.staxjurisdiction).compareToIgnoreCase(sCurrentTaxGroup) != 0){
		    			//Print the footer, if the record is for a new tax group:
		    		if (sCurrentTaxGroup.compareToIgnoreCase("") != 0){
		    			printTaxGroupFooter(
		    					sCurrentTaxGroup, 
		    					dTaxGroupCost, 
		    					dTaxGroupPrice, 
		    					dTaxGroupTax, 
		    					out
		    					);
		    			
		    			//Reset the tax group totals:
		    			dTaxGroupCost = BigDecimal.ZERO;
		    			dTaxGroupPrice = BigDecimal.ZERO;
		    			dTaxGroupTax = BigDecimal.ZERO;
		    		}
	    		}
	    		bdCost = new BigDecimal(Double.toString(rs.getDouble(SMTableinvoicedetails.dExtendedCost)));
	    		bdPrice = new BigDecimal(Double.toString(rs.getDouble(SMTableinvoicedetails.dExtendedPriceAfterDiscount)));
	    		bdTax = rs.getBigDecimal(SMTableinvoicedetails.bdlinesalestaxamount);
		    	if (bShowInvoiceLines){
		    		if( alt%2 ==0) {
		        		out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + " \">");
		    		}else {
		        		out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + " \">");
		    		}
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoicedetails.sItemCategory) + "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoiceheaders.staxjurisdiction) + "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoiceheaders.staxtype) + "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>&nbsp;</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" 
	    					+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(
									rs.getString(SMTableinvoiceheaders.datInvoiceDate), 
									SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
									SMUtilities.EMPTY_DATE_VALUE)
	    					+ "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>"+rs.getString(SMTableinvoicedetails.sInvoiceNumber)+"</FONT></TD>");
	    			BigDecimal bdQtyShipped = new BigDecimal(Double.toString(rs.getDouble(SMTableinvoicedetails.dQtyShipped)));
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdQtyShipped) + "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoicedetails.sItemNumber) + "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableinvoicedetails.sDesc) + "</FONT></TD>");
	    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString("Taxable") + "</FONT></TD>");
	    			out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCost) + "</FONT></TD>");
	    			out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice) + "</FONT></TD>");
	    			out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTax) + "</FONT></TD>");
	    			out.println("</TR>");
	    			alt++;
		    	}
    			
    			//Set the totals:
    			dTaxClassCost = dTaxClassCost.add(bdCost);
    			dTaxClassPrice = dTaxClassPrice.add(bdPrice);
    			dTaxClassTax = dTaxClassTax.add(bdTax);

    			dTaxGroupCost = dTaxGroupCost.add(bdCost);
    			dTaxGroupPrice = dTaxGroupPrice.add(bdPrice);
    			dTaxGroupTax = dTaxGroupTax.add(bdTax);
		    	
    			dCategoryCost = dCategoryCost.add(bdCost);
    			dCategoryPrice = dCategoryPrice.add(bdPrice);
    			dCategoryTax = dCategoryTax.add(bdTax);
    	    	
    	    	//Accumulate the grand totals:
    	    	dGrandTotalCost = dGrandTotalCost.add(bdCost);
    	    	dGrandTotalPrice = dGrandTotalPrice.add(bdPrice);
    	    	dGrandTotalTax = dGrandTotalTax.add(bdTax);
    	    	
    			//Reset:
    	    	sCurrentTaxGroup = rs.getString(SMTableinvoiceheaders.staxjurisdiction);
    			sCurrentCategory = rs.getString(SMTableinvoicedetails.sItemCategory);
    			sCurrentTaxType = rs.getString(SMTableinvoiceheaders.staxtype);
    			
    			lLinesInTable++;
			}
			rs.close();
			
			//Print the last tax class totals, if at least one tax class was listed:
			if (sCurrentTaxType.compareToIgnoreCase("") != 0){
    			printTaxClassFooter(
					sCurrentCategory, 
					sCurrentTaxGroup, 
					sCurrentTaxType,
					dTaxClassCost, 
					dTaxClassPrice, 
					dTaxClassTax, 
					out
    			);
    		}

			//Print the last Category totals, if at least one Category was listed:
    		if (sCurrentCategory.compareToIgnoreCase("") != 0){
    			printCategoryFooter(
    				sCurrentTaxGroup, 
    				sCurrentCategory, 
    				dCategoryCost, 
    				dCategoryPrice, 
    				dCategoryTax, 
    				out
    			);
    		}

			//Print the last tax group totals, if at least one tax group was listed:
    		if (sCurrentTaxGroup.compareToIgnoreCase("") != 0){
    			printTaxGroupFooter(
					sCurrentTaxGroup, 
					dTaxGroupCost, 
					dTaxGroupPrice, 
					dTaxGroupTax, 
					out
    			);
    		}
    		
		    //Print the grand totals:
		    out.println("<TD colspan=\"11\">&nbsp;</TD>");
        	out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + " \">");
			out.println("<TD ALIGN=RIGHT colspan=\"10\"><B><FONT SIZE=2>Report totals:</FONT></B></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalCost) + "</FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalPrice) + "</FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalTax) + "</FONT></TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    
		}catch(SQLException e){
			//System.out.println("Error in " + this.toString() + ":processReport - " + e.getMessage());
			m_sErrorMessage = "Error [1423860326] in " + this.toString() + ":processReport - " + e.getMessage();
			return false;
		}
		return true;
	}
	private void printTaxClassFooter(
			String sCurrentCategory, 
			String sCurrentTaxGroup,
			String sCurrentTaxClass,
			BigDecimal dTaxClassCost,
			BigDecimal dTaxClassPrice, 
			BigDecimal dTaxClassTax,
			PrintWriter out
			){
    	out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTALS_HEADING + " \">");
		out.println("<TD ALIGN=RIGHT colspan=\"10\"><B><FONT SIZE=2>Total for category " + sCurrentCategory 
				+ ", Tax group " + sCurrentTaxGroup
				+ ", Tax class " + sCurrentTaxClass
				+ ":</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxClassCost) + "</FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxClassPrice) + "</FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxClassTax) + "</FONT></TD>");
		out.println("</TR>");
	}
	private void printTaxGroupFooter(
			String sCurrentTaxGroup,
			BigDecimal dTaxGroupCost,
			BigDecimal dTaxGroupPrice, 
			BigDecimal dTaxGroupTax,
			PrintWriter out
			){
    	out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTALS_HEADING + " \">");
		out.println("<TD ALIGN=RIGHT colspan=\"10\"><B><FONT SIZE=2>Total for tax group " + sCurrentTaxGroup + ":</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxGroupCost) + "</FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxGroupPrice) + "</FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxGroupTax) + "</FONT></TD>");
		out.println("</TR>");
	}
	private void printCategoryFooter(
			String sCurrentTaxGroup,
			String sCurrentCategory,
			BigDecimal dCategoryCost,
			BigDecimal dCategoryPrice, 
			BigDecimal dCategoryTax,
			PrintWriter out
			){
    	out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTALS_HEADING + " \">");
		out.println("<TD ALIGN=RIGHT colspan=\"10\"><B><FONT SIZE=2>Total for tax group " + sCurrentTaxGroup + ", category " + sCurrentCategory + ":</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCategoryCost) + "</FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCategoryPrice) + "</FONT></TD>");
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCategoryTax) + "</FONT></TD>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

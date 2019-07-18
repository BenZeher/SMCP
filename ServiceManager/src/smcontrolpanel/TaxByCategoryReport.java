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
    	out.println("<TABLE WIDTH=100% class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
    	if (bShowInvoiceLines){
    		out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \">" + 
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Item<BR>Category</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Tax<BR>Jurisdiction</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Tax Type</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>&nbsp;</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Invoice Date</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Invoice #</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Qty</B></TD>" + 
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Item #</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Item Decription</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>Taxable</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"  ><B>Cost</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"  ><B>Price</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"  ><B>Tax</B></TD>" +
			"</TR>");
    	}else{
    		out.println("<TR Class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \">" + 
			    "<TD COLSPAN = \"10\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" ><B>&nbsp;</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"  ><B>Cost</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"  ><B>Price</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"  ><B>Tax</B></TD>" +
    			"</TR>");
    		
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
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			int alt = 0;
			while(rs.next()){
				
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
		    			alt = 0;
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
		    			alt = 0;
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
		    			alt = 0;
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
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableinvoicedetails.sItemCategory) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableinvoiceheaders.staxjurisdiction) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableinvoiceheaders.staxtype) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">&nbsp;</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
	    					+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(
									rs.getString(SMTableinvoiceheaders.datInvoiceDate), 
									SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
									SMUtilities.EMPTY_DATE_VALUE)
	    					+ "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+rs.getString(SMTableinvoicedetails.sInvoiceNumber)+"</TD>");
	    			BigDecimal bdQtyShipped = new BigDecimal(Double.toString(rs.getDouble(SMTableinvoicedetails.dQtyShipped)));
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdQtyShipped) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableinvoicedetails.sItemNumber) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableinvoicedetails.sDesc) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString("Taxable") + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCost) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice) + "</TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTax) + "</TD>");
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
        	out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + " \">");
			out.println("<TD COLSPAN=\"13\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			out.println("</TR>");
			
        	out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + " \">");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" colspan=\"10\"><B>Report totals:</B></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalCost) + "</TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalPrice) + "</TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalTax) + "</TD>");
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
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" colspan=\"10\"><B>Total for category " + sCurrentCategory 
				+ ", Tax group " + sCurrentTaxGroup
				+ ", Tax class " + sCurrentTaxClass
				+ ":</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxClassCost) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxClassPrice) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxClassTax) + "</TD>");
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
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" colspan=\"10\"><B>Total for tax group " + sCurrentTaxGroup + ":</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxGroupCost) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxGroupPrice) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTaxGroupTax) + "</TD>");
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
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" colspan=\"10\"><B>Total for tax group " + sCurrentTaxGroup + ", category " + sCurrentCategory + ":</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCategoryCost) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCategoryPrice) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCategoryTax) + "</TD>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

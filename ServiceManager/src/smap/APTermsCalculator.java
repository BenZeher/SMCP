package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.ParseException;
import java.util.Calendar;
import javax.servlet.ServletContext;
import SMDataDefinition.SMTableapbatchentries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class APTermsCalculator {

	private String m_stermscode;
	private BigDecimal m_bdinvoicetotalamount;
	private BigDecimal m_bddiscountamt;
	private String m_sinvoicedate;
	private String m_sduedate;
	private String m_sdiscountdate;
	
	public APTermsCalculator(
			String sVendorTermsCode,
			String sInvoiceTotalAmount,
			String sInvoiceDateAsmmddyyyy
		) throws Exception{
		m_stermscode = sVendorTermsCode;
		try {
			m_bdinvoicetotalamount = new BigDecimal(sInvoiceTotalAmount.trim().replace(",", ""));
		} catch (Exception e) {
			throw new Exception("Error [1490648408] - total invoice amount '" + sInvoiceTotalAmount + "' is not valid.");
		}
		m_bddiscountamt = BigDecimal.ZERO;
		try {
			m_sinvoicedate = clsValidateFormFields.validateStandardDateField(sInvoiceDateAsmmddyyyy, "Invoice date", false);
		} catch (Exception e) {
			throw new Exception("Error [1490648409] - " + e.getMessage());
		}
		m_sduedate = SMUtilities.EMPTY_DATE_VALUE;
		m_sdiscountdate = SMUtilities.EMPTY_DATE_VALUE;
	}
	public void calculateTerms(ServletContext context, String sDBID, String sUserID) throws Exception{

		//First, validate the entry:
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + ".calculateDiscount - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1490647720] getting connection - " + e.getMessage());
		}

		APVendorTerms terms = new APVendorTerms();
		terms.setsTermsCode(m_stermscode);
		if (!terms.load(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059479]");
			throw new Exception("Error [1490647721] loading terms with code '" + m_stermscode + "' - " + terms.getErrorMessages());
		}
		BigDecimal bdDiscountPercent = new BigDecimal(terms.getsDiscountPercentage().replaceAll(",", ""));
		bdDiscountPercent = bdDiscountPercent.divide(
				new BigDecimal(100), 
				6, //Using the 'discountscale' was a problem for fractions of percents, so I increased the scale here - TJR - 11/10/13
				BigDecimal.ROUND_HALF_UP
				);
		int iDiscountDayOfTheMonth = Integer.parseInt(terms.getsDiscountDayOfTheMonth());
		int iDiscountNumberOfDays = Integer.parseInt(terms.getsDiscountNumberOfDays());
		int iDueDayOfTheMonth = Integer.parseInt(terms.getsDueDayOfTheMonth());
		int iDueNumberOfDays = Integer.parseInt(terms.getsDueNumberOfDays());
		int iMinimumDaysAllowedForDueDayOfMonth = Integer.parseInt(terms.getsMinimumDaysAllowedForDueDayOfMonth());
		int iMinimumDaysAllowedForDiscountDueDayOfMonth = Integer.parseInt(terms.getsMinimumDaysAllowedForDiscountDueDay());

		//Now calculate the discount amount, the discount date, and the due date:

		//Discount amount:
		m_bddiscountamt = m_bdinvoicetotalamount.multiply(bdDiscountPercent);

		//Calculate due date:
		//NOTE: The due day of the month takes precedence - if we have that, ignore the number of days due:
		java.sql.Date datInvoice = new java.sql.Date(0);
		try {
			datInvoice = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", m_sinvoicedate);
		} catch (ParseException e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059480]");
			throw new Exception("Error [1490647722] converting invoice date '" 
				+ m_sinvoicedate
				+ "' into java.sql.Date - " + e.getMessage()
				);
		}

		java.text.SimpleDateFormat sdf = 
				new java.text.SimpleDateFormat("M/d/yyyy");
		Calendar cDueDate = Calendar.getInstance(); 
		cDueDate.setTime(datInvoice);
		//If there is no 'Due date of the month', then just calculate using the number of days from the invoice date
		if (iDueDayOfTheMonth == 0){
			//Add the number of due days to the invoice date:
			cDueDate.add(Calendar.DATE, iDueNumberOfDays);
		}else{
			//Set the due date to the next occurrence of the due date of the month:

			//We may have to move the month of the due date forward - check that here:
			//If the due date of the month is <= the invoice date, add a month to the due date:
			if (iDueDayOfTheMonth <= cDueDate.get(Calendar.DAY_OF_MONTH)){
				cDueDate.add(Calendar.MONTH, 1);
				//If not then...
			}else{
				//...if the invoice date is only a few days before the due day of the month, see if it's in the 'grace period' and
				//kick it to the next month:
				if ((iDueDayOfTheMonth - cDueDate.get(Calendar.DAY_OF_MONTH)) < iMinimumDaysAllowedForDueDayOfMonth){
					cDueDate.add(Calendar.MONTH, 1);
				}
			}
			//Now set the due date to the correct day of the month:
			cDueDate.set(Calendar.DAY_OF_MONTH, iDueDayOfTheMonth);
		}
		m_sduedate = sdf.format(cDueDate.getTime());

		//Calculate the discount date:
		Calendar cDiscountDate = Calendar.getInstance(); 
		cDiscountDate.setTime(datInvoice);
		//If there is no 'Discount date of the month', then just calculate using the number of days from the invoice date
		if (iDiscountDayOfTheMonth == 0){
			//Add the number of discount days to the invoice date:
			cDiscountDate.add(Calendar.DATE, iDiscountNumberOfDays);
		}else{
			//Set the discount due date to the next occurrence of the discount due day of the month:

			//We may have to move the month of the due date forward - check that here:
			//If the discount due date of the month is <= the invoice date, add a month to the discount due date:
			if (iDiscountDayOfTheMonth <= cDiscountDate.get(Calendar.DAY_OF_MONTH)){
				cDiscountDate.add(Calendar.MONTH, 1);
				//If not then...
			}else{
				//...if the invoice date is only a few days before the discount day, see if it's in the 'grace period' and
				//kick it to the next month:
				if ((iDiscountDayOfTheMonth - cDiscountDate.get(Calendar.DAY_OF_MONTH)) < iMinimumDaysAllowedForDiscountDueDayOfMonth){
					cDiscountDate.add(Calendar.MONTH, 1);
				}
			}
			//Now set the discount date to the correct day of the month:
			cDiscountDate.set(Calendar.DAY_OF_MONTH, iDiscountDayOfTheMonth);
		}

		//IF the discount date is not later than the invoice date, then the discount date should be a zero date:
		Calendar cInvoiceDate = Calendar.getInstance();
		cInvoiceDate.setTime(datInvoice);
		if (cDiscountDate.after(cInvoiceDate)){
			m_sdiscountdate = sdf.format(cDiscountDate.getTime());
		}else{
			m_sdiscountdate = SMUtilities.EMPTY_DATE_VALUE;
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547059481]");
		return;
	}
	public String getDiscountAmountString(){
		return clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bddiscountScale, m_bddiscountamt);
	}
	public String getDiscountDateString(){
		return m_sdiscountdate;
	}
	public String getDueDateString(){
		return m_sduedate;
	}
}

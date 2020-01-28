package smcontrolpanel;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SMSalesOrderTaxCalculator extends java.lang.Object{

	private BigDecimal m_bdSalesTaxRate;
	private BigDecimal m_bdSalesTaxBase;
	private BigDecimal m_bdSalesTaxAmount;
	private BigDecimal m_bdDiscountAmt;
	private ArrayList<BigDecimal> m_arExtendedPriceBeforeDiscount;
	private ArrayList<Integer> m_arLineTaxable;
	private ArrayList<BigDecimal> m_arExtendedPriceAfterDiscount;
	private ArrayList<BigDecimal> m_arSalesTaxAmountPerLine;
	private ArrayList<BigDecimal> m_arQtyShipped;
	private ArrayList<String> m_arItemNumber;
	private boolean bCalculationProcessed;
	
	public SMSalesOrderTaxCalculator(
		BigDecimal bdSalesTaxRateAsWholeNumber,
		BigDecimal bdDiscountAmt
	    ) {
		m_bdSalesTaxRate = bdSalesTaxRateAsWholeNumber.setScale(4, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("100.00"));
		m_bdDiscountAmt = bdDiscountAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
		m_bdSalesTaxBase = new BigDecimal("0.00");
		m_bdSalesTaxAmount = new BigDecimal("0.00");
		m_arExtendedPriceBeforeDiscount = new ArrayList<BigDecimal>(0);
		m_arLineTaxable = new ArrayList<Integer>(0);
		m_arExtendedPriceAfterDiscount = new ArrayList<BigDecimal>(0);
		m_arSalesTaxAmountPerLine = new ArrayList<BigDecimal>(0);
		m_arQtyShipped = new ArrayList<BigDecimal>(0);
		m_arItemNumber = new ArrayList<String>(0);
		bCalculationProcessed = false;
	}
	
	public void addLine (BigDecimal bdExtendedPriceBeforeDiscount, 
			int iTaxable, 
			BigDecimal 
			bdQtyShipped, 
			String sItemNum) throws Exception{
		try {
			m_arExtendedPriceBeforeDiscount.add(bdExtendedPriceBeforeDiscount);
			m_arLineTaxable.add(iTaxable);
			m_arQtyShipped.add(bdQtyShipped);
			m_arItemNumber.add(sItemNum);
		} catch (Exception e) {
			throw new Exception("Error adding line to tax calculator - " + e.getMessage());
		}
		bCalculationProcessed = false;
	}
	public void calculateSalesTax() throws Exception{
		bCalculationProcessed = false;
		
		//First, calculate the extended price AFTER discount for each line:
		BigDecimal bdTotalBeforeDiscount = new BigDecimal("0.00");
		BigDecimal bdTotalExtendedPriceOfTaxableItemsBeforeDiscount = new BigDecimal("0.00");
		BigDecimal bdNetMultiplier = new BigDecimal("0.00");
		
		//Store the size of our arrays to make it easier to read:
		int iArraySize = m_arExtendedPriceBeforeDiscount.size();
		//Get the total before discount:
		for (int i = 0; i < iArraySize; i++){
			bdTotalBeforeDiscount = bdTotalBeforeDiscount.add(m_arExtendedPriceBeforeDiscount.get(i));
		}
		//If the total before the discount is less than the discount amount, then we have a problem:
		if (bdTotalBeforeDiscount.compareTo(BigDecimal.ZERO) >= 0){
			//We've got an invoice, so the discount amount has to be less than or equal to the total:
			if (bdTotalBeforeDiscount.compareTo(m_bdDiscountAmt) < 0){
				throw new Exception("Error calculating tax - the discount is more than the order total.");
			}
		}else{
			//We've got a credit note, so the discount amount has to be GREATER than or equal to the total:
			if (bdTotalBeforeDiscount.compareTo(m_bdDiscountAmt) > 0){
				throw new Exception("Error calculating tax - the discount is more than the order total.");
			}
		}
		//The discount percentage is the discount amount divided by the total before the discount:
		//Can't divide by zero, of course:
		if (bdTotalBeforeDiscount.compareTo(BigDecimal.ZERO) != 0){
			//The 'net percentage' is the 'multiplier', or what percent of the full price is the net price after discount
			bdNetMultiplier = 
				(bdTotalBeforeDiscount.subtract(m_bdDiscountAmt)).divide(bdTotalBeforeDiscount, 6, BigDecimal.ROUND_HALF_UP);
		}
		//Keep track of the total net amount in this variable:
		BigDecimal bdNetTotalAfterDiscount = new BigDecimal("0.00");
		
		//Determine which is the LAST LINE WITH A PRICE ON IT:
		int iLastPricedLineIndex = 0;
		for(int i = m_arExtendedPriceBeforeDiscount.size() - 1; i >= 0; i--){
			if (m_arExtendedPriceBeforeDiscount.get(i).compareTo(BigDecimal.ZERO) != 0){
				iLastPricedLineIndex = i;
				break;
			}
		}
		
		//Iterate through all but the LAST PRICED LINE - save that one to accumulate any rounding:
		for (int i = 0; i < iLastPricedLineIndex; i++){
			//The extended price after discount is equal to the price BEFORE discount, times the 'multiplier' factor:
			BigDecimal bdExtPriceAfterDiscount = (m_arExtendedPriceBeforeDiscount.get(i).multiply(bdNetMultiplier)).setScale(2, BigDecimal.ROUND_HALF_UP);
			m_arExtendedPriceAfterDiscount.add(bdExtPriceAfterDiscount);
			//Accumulate the price after discount into the 'net total':
			bdNetTotalAfterDiscount = bdNetTotalAfterDiscount.add(bdExtPriceAfterDiscount);
		}
		//Now use up the remaining discount on the last line so we accommodate rounding errors:
		BigDecimal bdDiscountAmtRemaining = new BigDecimal("0.00");
		//The discount amount remaining for the last line, is the total order amount BEFORE the discount - MINUS the discount amount 
		//calculated so far, MINUS the actual discount amount
		bdDiscountAmtRemaining =  (bdTotalBeforeDiscount.subtract(m_bdDiscountAmt)).subtract(bdNetTotalAfterDiscount);
		m_arExtendedPriceAfterDiscount.add(bdDiscountAmtRemaining);
		
		//Finish adding the last lines to the 'm_arExtendedPriceAfterDiscount' array:
		for (int i = iLastPricedLineIndex; i < iArraySize; i++){
			m_arExtendedPriceAfterDiscount.add(BigDecimal.ZERO);
		}
		
		//Next, calculate the tax on the total extended price AFTER discount (the 'taxable total'):
		//First, determine which is the LAST TAXABLE LINE: - we need this because when we calculate the tax for each line, we want
		//to put all the remaining tax on the last line to allow for rounding errors:
		int iLastTaxableLine = 0;
		for (int i = 0; i < iArraySize; i++){
			//If the line is taxable and IF it has a shipped qty AND it has a price, then add the price after discount to the tax base:
			if (
				(m_arLineTaxable.get(i) != 0) 
				&& (m_arQtyShipped.get(i).compareTo(BigDecimal.ZERO) != 0)
				&& (m_arExtendedPriceAfterDiscount.get(i).compareTo(BigDecimal.ZERO) != 0)
			){
				iLastTaxableLine = i;
				//If the line is taxable, then add the extended price after discount to the tax base:
				m_bdSalesTaxBase = m_bdSalesTaxBase.add(m_arExtendedPriceAfterDiscount.get(i));
				//System.out.println("Line " + i + 1 + " = " + m_arExtendedPriceAfterDiscount.get(i));
				//System.out.println("m_bdTaxBase = " + m_bdTaxBase);
				bdTotalExtendedPriceOfTaxableItemsBeforeDiscount = 
					bdTotalExtendedPriceOfTaxableItemsBeforeDiscount.add(m_arExtendedPriceBeforeDiscount.get(i));
			}
		}
		//Now that we have the tax base, calculate the tax amount:
		m_bdSalesTaxAmount = m_bdSalesTaxBase.multiply(m_bdSalesTaxRate).setScale(2, BigDecimal.ROUND_HALF_UP);
		
		//Next calculate the tax amount for each line:
		BigDecimal bdAccumulatedTaxAmt = new BigDecimal("0.00");
		for (int i = 0; i < iArraySize; i++){
			//If this is the last taxable line, then just place the remaining amount of tax in this line:
			if (i == iLastTaxableLine){
				m_arSalesTaxAmountPerLine.add(m_bdSalesTaxAmount.subtract(bdAccumulatedTaxAmt));
			//If it's NOT the last taxable line:
			}else{
				//If it's taxable AND has a qty shipped, then calculate the tax for the line
				if ((m_arLineTaxable.get(i) != 0) && (m_arQtyShipped.get(i).compareTo(BigDecimal.ZERO) != 0)){
					m_arSalesTaxAmountPerLine.add(m_arExtendedPriceAfterDiscount.get(i).multiply(m_bdSalesTaxRate).setScale(2, BigDecimal.ROUND_HALF_UP));
				//Otherwise, just add a zero tax amount for the line:
				}else{
					m_arSalesTaxAmountPerLine.add(BigDecimal.ZERO);
				}
				bdAccumulatedTaxAmt = bdAccumulatedTaxAmt.add(m_arSalesTaxAmountPerLine.get(i));
			}
		}
		bCalculationProcessed = true;
	}
	public BigDecimal getLineExtendedPriceAfterDiscount(int iIndex){
		return m_arExtendedPriceAfterDiscount.get(iIndex);
	}
	public BigDecimal getLineExtendedPriceBeforeDiscount(int iIndex){
		return m_arExtendedPriceBeforeDiscount.get(iIndex);
	}
	public BigDecimal getSalesTaxAmountPerLine(int iIndex){
		return m_arSalesTaxAmountPerLine.get(iIndex);
	}
	public BigDecimal getQtyShipped(int iIndex){
		return m_arQtyShipped.get(iIndex);
	}
	public String getItem(int iIndex){
		return m_arItemNumber.get(iIndex);
	}
	public int getIsLineTaxable(int iIndex){
		return m_arLineTaxable.get(iIndex);
	}
	public BigDecimal getTotalSalesTax(){
		return m_bdSalesTaxAmount;
	}
	public BigDecimal getTotalSalesTaxBase(){
		return m_bdSalesTaxBase;
	}
	public boolean isSalesTaxCalculationProcessed(){
		return bCalculationProcessed;
	}
	public int getLineCount(){
		return m_arExtendedPriceBeforeDiscount.size();
	}
	public BigDecimal getTotalExtendedPriceBeforeDiscount(){
		BigDecimal bdTotalExtendedPriceBeforeDiscount = new BigDecimal("0.00");
		for (int i = 0; i < m_arExtendedPriceBeforeDiscount.size(); i++){
			bdTotalExtendedPriceBeforeDiscount = bdTotalExtendedPriceBeforeDiscount.add(m_arExtendedPriceBeforeDiscount.get(i));
		}
		return bdTotalExtendedPriceBeforeDiscount;
	}
	public BigDecimal getTotalExtendedPriceAfterDiscount(){
		BigDecimal bdTotalExtendedPriceAfterDiscount = new BigDecimal("0.00");
		for (int i = 0; i < m_arExtendedPriceAfterDiscount.size(); i++){
			bdTotalExtendedPriceAfterDiscount = bdTotalExtendedPriceAfterDiscount.add(m_arExtendedPriceAfterDiscount.get(i));
		}
		return bdTotalExtendedPriceAfterDiscount;
	}
}

/*
 * Updated 05/05/2009 - TJR
 * BASIC TRANSACTIONAL TABLES:

BASIC TRANSACTIONAL TABLES:

1) ENTRY BATCHES:

2) ARTRANSACTIONS 

3) ARTRANSACTIONLINES

4) ARMATCHINGLINES


ARMATCHINGLINES:
This is a table of amounts, linked back to artransactionlines, that 'apply-to' documents and affect their current 

amount.  When a payment is applied to an invoice, an ARMATCHINGLINE is created for the payment, linked back to the 

original (parent) document, but also 'applying-to' the invoice.  It reduces the current amount of the invoice.  AT THE 

SAME TIME, another armatchingline is created linked back to the INVOICE as a parent, but also applying-to the payment.  

At any time, the total of the armatchinglines should be zero.  This is similar to the way ACCPAC currently does it.

ARTRANSACTIONS:
This table includes all of the documents that have been posted in the system.  Every invoice, credit, cash, retainage, 

prepay, and adjustment document is represented here.  Each record represents (in the 'current amount') an 

increase/decrease in a customer's liability.  These documents are divided into 'retainage' and 'non-retainage' 

documents.  

The original amount is just a record of the original amount of the document when it was first posted.

The current amount stores the current liability of the customer - the total of all the current amounts for each 

customer equals the customer's balance.  The liability of the document (current amt) equals the original amount less 

the sum of: ANY applied CHILD lines (no matter what they apply to) and any lines applied TO it.

When an entry is posted, the sum of any child lines that apply to another document are totalled, and subtracted from 

the original amount to get the current amount.  For example, when an invoice is posted, none of its lines apply to 

anything, so the current amount is equal to the original amount.  But when a payment is posted, if all of its lines are 

applied to some other documents, the current amount of the document is set to zero.

If a prepay is posted, and none of its lines are applied yet, the total amount of the prepay adds (a negative number, 

in this case) to the customer's liability.  When the lines are later applied, the current (negative) amount of the 

prepay is adjusted up to zero (when all the lines are applied).

If a credit is posted, all of the lines apply to an invoice, so the current amount of the document is zero.

If a retainage entry is posted, the single line (a positive amount) is applied to the original invoice, and the amount 

of the retainage entry is zero.  So the current amount would be equal to the total of the lines.


ARTRANSACTIONLINES:
This table includes all of the lines from the documents.  These lines can reduce or increase a customer's liability by 

'applying to' an ARTRANSACTION record - if they DO NOT apply to a transaction, they DO NOT affect the customer's 

liability, or the current amount of their 'parent' transaction.

IF they DO apply to a transaction, then they affect the customer's balance; they get an apply-to ID pointing to the 

apply-to transaction.  They increase/decrease the apply-to document by that much AND they get added to their parent 

document's amount to set the parent document's current amount.

IF they DO NOT apply to a transaction, they DO NOT affect a customer's balance, and they get a -1 in the apply-to id.  

They do not affect the parent document's current amount either - if the parent document has NO child lines that apply 

to anything AND has no lines applying to it, the parent document's current amt equals its original amount.


The logic for the creation for each type of transaction follows - no other transaction types are allowed:
INVOICE:
AROPENTRANSACTIONS:
CONTROL ACCT: Customer's AR, from ARAcctSet
ORIGINAL AMT: positive
CURRENT AMT: same as original amt
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: normally, revenue, tax, etc.
AMOUNT: negative, summing to total invoice
APPLIED TO: nothing (-1)

INVOICE ADJUSTMENT:

CREDIT:
AROPENTRANSACTIONS:
CONTROL ACCT: Same as control account of reversed document (AR)
ORIGINAL AMT: opposite of reversed document's original amount
CURRENT AMT: zero
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: same as reversed lines
AMOUNT: opposite of reversed lines
APPLIED TO: reversed AROPENTRANSACTION

CREDIT ADJUSTMENT:

RECEIPT (fully applied):
AROPENTRANSACTIONS:
CONTROL ACCT: Customer's Cash, from ARAcctSet
ORIGINAL AMT: negative
CURRENT AMT: zero
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: same as apply to doc control account (normally AR), or, for unapplied lines, misc. cash from 

customer's ar acct set
AMOUNT: positive
APPLIED TO: invoice(s)

PREPAY (BEFORE APPLICATION):
POSTING OF ORIGINAL PREPAY:
AROPENTRANSACTIONS:
CONTROL ACCT: Customer's Cash, from ARAcctSet
ORIGINAL AMT: negative
CURRENT AMT: same as original amt
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: AR from customer aracctset
AMOUNT: positive, summing to original amt
APPLIED TO: customer account, but no specific transaction, indicated by a -1

MISC RECEIPT:
AROPENTRANSACTIONS:
CONTROL ACCT: Misc cash account, from USER SELECTED aracctset
ORIGINAL AMT: negative
CURRENT AMT: same as original amount
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: misc income acct, from USER SELECTED aracctset
AMOUNT: positive
APPLIED TO: nothing (-1)

RECEIPT ADJUSTMENT:

ADJUSTMENT REVERSAL:
AROPENTRANSACTIONS:
CONTROL ACCT: Same as control acct of adjusted transaction
ORIGINAL AMT: opposite of adjusted document
CURRENT AMT: zero
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: same as adjusted lines
AMOUNT: opposite of adjusted lines
APPLIED TO: adjusted AROPENTRANSACTION

RETAINAGE - against invoices only:
AROPENTRANSACTIONS:
CONTROL ACCT: Customer's AR Retainage, from ARAcctSet
ORIGINAL AMT: zero
CURRENT AMT: same as current amount of adjusted document
ARTRANSACTIONLINES(S): (two lines):
LINE 1:
DISTRIBUTION ACCT: same as apply-to invoice control acct (normally customer's AR)
AMOUNT: amount of retention to be held - cannot be greater than the current amt of the original invoice
APPLIED TO: adjusted ARTRANSACTION
LINE 2:
DISTRIBUTION ACCT: retainage acct
AMOUNT: OPPOSTIE amount of retention to be held
APPLIED TO: nothing

APPLICATION OF PREPAY - the original prepay is modified like this:
AROPENTRANSACTIONS:
CONTROL ACCT: no change
ORIGINAL AMT: no change
CURRENT AMT: no change
ARTRANSACTIONLINES(S):
DISTRIBUTION ACCT: no change
AMOUNT: no change
APPLIED TO: the apply-to transaction (id of the apply-to is used, replacing the 'zero' used to indicate an anonymous 

line)

An adjustment reversal is an exact reverse of an adjustment transaction on the control and distribution sides, applying 

to the same accounts.
Samples of needed adjustments:
Invoice adjustment: tax account needs to be changed
Using an invoice 'reversal' (adjustment):
Adjustment Amount: zero
one line crediting the incorrect tax account
one line debiting the correct tax account 
apply-to's both go to the original invoice, but net to zero

Invoice adjustment: amount needs to be decreased (e.g., shouldn't be taxable)
Using an invoice 'reversal' (adjustment)
Amount: same as incorrect tax amount
one line crediting the tax account
apply-to's go to the original invoice, and change the AR amount

Cash adjustment: Customer overpaid
Using a receipt reversal (adjustment):
Amount: 10.00, to the cash account
one line debiting AR, applied to the original document

Credit adjustment: customer was overcredited
Using an adjustment adjustment:
Amount: 10.00, to the AR account
one line debiting a line for 10.00, applying to the original credit


?? How do we adjust a retainage transaction?  Can it just be reversed?

RULES:
1) The total of the current amounts for each customer, including any 'applied-to-the-customer' lines, equals his 

balance

2) The total of the NON-RETAINAGE amounts for each customer, including any 'applied-to-the-customer' lines, equals the 

non-retainage balance

3) The total of the RETAINAGE amounts equal the retainage balance

4) The total of any APPLIED matching lines PLUS the total of any applied child lines (regardless of what they apply to) 

equals the difference between the apply-to document's original amount and it's current amount.

5) The total of the 'child' transaction lines (applied OR unapplied) equals the total of the 'parent' transaction's 

original amounts.  THIS IS ONLY TRUE FOR TRANSACTIONS THAT HAVE BEEN ADDED THROUGH THE SYSTEM - TRANSACTIONS THAT COME 

FROM ACCPAC THROUGH THE INITIAL IMPORT MAY BE MISSING SOME OF THEIR 'CHILD' LINES BECAUSE ACCPAC DOES NOT KEEP THESE!!

6) Every original entry can be re-constructed from the open transactions and the transaction lines (EXCEPT FOR 

IMPORTED ACCPAC TRANSACTIONS - SEE ABOVE).

7) Retainage transactions can be created against invoices ONLY.

8) The regular aging is generated by listing all of the non-retainage aropentransactions and their apply-to documents, 

AND any 'applied-to-the-customer' (such as prepay) lines

9) Retainage - any transaction 'descended' (i.e. linked to, directly OR indirectly) from a retainage transaction is 

also a retainage transaction.

Rules for clearing transactions and lines:
1) If the aropentransaction has a current amount of zero, it can be cleared

2) If a transaction line's apply-to is cleared AND it's parent transaction is cleared, it can be cleared.


ACCPAC:
When converting from AROBP records, some records have no IDMEMOXREF value - these are 'aritificial' matches that never 

actually came from a transaction.  For example, if a check is entered, but then it bounces, the entry has to be 

canceled.  In that case, ACCPAC adds an 'applying' record to AROBP to cancel the original entry, but it has no 'parent' 

transaction . . . .

PREPAYS get entered in AROBL as soon as they are posted, but there are no AROBP records until they get applied.

 */

package SMDataDefinition;

public class SMTableartransactions {
	//Table Name
	public static final String TableName = "artransactions";
	
	//Field names:
	public static final String lid  = "id";
	public static final String loriginalbatchnumber = "loriginalbatchnumber";
	public static final String loriginalentrynumber = "loriginalentrynumber";
	public static final String spayeepayor = "spayeepayor";
	public static final String sdocnumber = "sdocnumber";
	//AR transaction doc types are:
	//	0: "Invoice";
	//	1: "Credit";
	//	2: "Receipt";
	//	3: "Prepayment";
	//	4: "Reversal";
	//	5: "Invoice Adjustment";
	//	6: "Misc Receipt";
	//	7: "Cash Adjustment";
	//	8: "Credit Adjustment";
	//	9: "Retainage";
	//	10: "Apply-To";
	public static final String idoctype = "idoctype";
	public static final String sterms = "sterms";
	public static final String datdocdate = "datdocdate";
	public static final String datduedate = "datduedate";
	public static final String doriginalamt = "doriginalamt";
	public static final String dcurrentamt = "dcurrentamt";
	public static final String sdocdescription = "sdocdescription";
	public static final String sordernumber = "sordernumber";
	public static final String scontrolacct = "scontrolacct";
	public static final String iretainage = "iretainage";
	public static final String sponumber = "sponumber";
	
	//Field lengths:
	public static final int spayeepayorlength = 12;
	public static final int sdocnumberlength = 75;
	public static final int stermslength = 6;
	public static final int sdocdescriptionlength = 128;
	public static final int sapplytoordernumberlength = 22;
	public static final int sordernumberlength = 22;
	public static final int scontrolacctlength = 75;
	public static final int sponumberlength = 40;
}

package smar;

public class ARClassNames {

	//Names of the classes used in the AR system:
	
	//Accounts Receivable main menu:
	public static final String AR_MAIN_MENU = "ARMainMenu";
	
	//'Finder' - used in conjunction with the Finder results class to find records in the system:
	public static final String FINDER_INPUT = "ObjectFinder";
	
	//Finder results - displays the list of results from the finder, and includes hyperlink to the original 
	//'calling' form:
	public static final String FINDER_RESULTS = "FinderResults";
	
	//Entries:
	//Used for editing cash entries:
	public static final String CASH_ENTRY_EDIT = "AREditCashEntry";
	
	//Used for editing invoice entries:
	public static final String INVOICE_ENTRY_EDIT = "AREditInvoiceEntry";
	
	//Used for editing credit entries:
	public static final String CREDIT_ENTRY_EDIT = "AREditCreditEntry";

	//Used for adding new entries - it gets the customer number, then calls the entry edit:
	public static final String ENTRY_ADD = "ARAddEntry";
	
	//Allow the user to edit an entry, add/remove lines, etc.
	public static final String ENTRY_EDIT = "AREditEntryEdit";
	
	//Process a 'save' or 'delete' command from the CASH_ENTRY_EDIT class:
	public static final String CASH_ENTRY_UPDATE = "AREditCashEntryUpdate";

	//Process a 'save' or 'delete' command from the INVOICE_ENTRY_EDIT class:
	public static final String INVOICE_ENTRY_UPDATE = "AREditInvoiceEntryUpdate";
	
	//Process a 'save' or 'delete' command from the CREDIT_ENTRY_EDIT class:
	public static final String CREDIT_ENTRY_UPDATE = "AREditCreditEntryUpdate";

	//Batches
	//Lists batches to be edited and lets the user choose one:
	public static final String BATCH_LIST = "AREditBatches";
	
	//Used for adding editing an individual batch:
	public static final String BATCH_EDIT = "AREditBatchesEdit";
	
	//Takes the user input from the edit batch routine, and does the action: delete, add, or update the batch:
	public static final String BATCH_UPDATE = "AREditBatchesUpdate";
	
	//Takes the customer number and triggers the display of customer activity:
	public static final String ACTIVITY_INQUIRY = "ARActivityInquiry";
	
	//Displays customer transactions on the screen, taking the customer number as input:
	public static final String ACTIVITY_DISPLAY = "ARActivityDisplay";

	//Displays matching customer transactions:
	public static final String MATCHING_ACTIVITY_DISPLAY = "ARDisplayMatchingTransactions";
}

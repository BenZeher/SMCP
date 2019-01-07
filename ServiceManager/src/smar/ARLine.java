package smar;

import SMClasses.*;

public class ARLine extends SMEntryLine{

	ARLine(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber,
    		String sEntryId
        ){
		
		super(sBatchNumber, sEntryNumber, sLineNumber, sEntryId);
	}
	
	ARLine(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sLineNumber
        ){
		
		super(sBatchNumber, sEntryNumber, sLineNumber);
	}
	
	ARLine(
    		String sBatchNumber,
    		String sEntryNumber
        ){
		
		super(sBatchNumber, sEntryNumber);
	}
	
	ARLine(
        ){
		
		super();
	}
	
	
	
}

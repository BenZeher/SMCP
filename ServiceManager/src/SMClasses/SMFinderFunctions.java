package SMClasses;

import SMDataDefinition.SMTableicitems;

public class SMFinderFunctions {

	public static String getStdITEMSearchAndResultString(){
		return 
			"&SearchField1=" + SMTableicitems.sItemDescription
			+ "&SearchFieldAlias1=Description"
			+ "&SearchField2=" + SMTableicitems.sItemNumber
			+ "&SearchFieldAlias2=Item%20No."
			+ "&SearchField3=" + SMTableicitems.sComment1
			+ "&SearchFieldAlias3=Comment%201"
			+ "&SearchField4=" + SMTableicitems.sComment2
			+ "&SearchFieldAlias4=Comment%202"
			+ "&SearchField5=" + SMTableicitems.sreportgroup1
			+ "&SearchFieldAlias5=Report%20Group%201"
			+ "&ResultListField1="  + SMTableicitems.sItemNumber
			+ "&ResultHeading1=Item%20No."
			+ "&ResultListField2="  + SMTableicitems.sItemDescription
			+ "&ResultHeading2=Description"
			+ "&ResultListField3="  + SMTableicitems.sCostUnitOfMeasure
			+ "&ResultHeading3=Cost%20Unit"
			+ "&ResultListField4="  + SMTableicitems.inonstockitem
			+ "&ResultHeading4=Non-stock?"
			+ "&ResultListField5="  + SMTableicitems.sPickingSequence
			+ "&ResultHeading5=Picking%20Sequence"
			+ "&ResultListField6="  + SMTableicitems.sreportgroup1
			+ "&ResultHeading6=Report%20Group%201"
			+ "&ResultListField7="  + SMTableicitems.sComment1
			+ "&ResultHeading7=Comment%201"
			+ "&ResultListField8="  + SMTableicitems.sComment2
			+ "&ResultHeading8=Comment%202"
		;
	}
}

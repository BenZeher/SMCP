package SMDataDefinition;

public class SMMasterStyleSheetDefinitions {

	//Master stylesheet definitions:
	
	//Table defs
	public static final String TABLE_BASIC_WITH_BORDER = "basic";
	public static final String TABLE_BASIC_WITH_BORDER_COLLAPSE = "collapseborder";
	public static final String TABLE_BASIC_WITHOUT_BORDER = "basicwithoutborder";
	
	//Table row defs
	public static final String TABLE_ROW_BACKGROUNDCOLOR_WHITE = "evennumberedtablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY = "oddnumberedtablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE = "lightbluetablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_BLACK = "blacktablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_ORANGE = "orangetablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK = "lightpinktablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREEN = "lightgreentablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_YELLOW = "yellowtablerow";
	public static final String TABLE_ROW_BACKGROUNDCOLOR_GREY = "greytablerow";
	public static final String TABLE_ROW_BREAK = "blackspace";
	
	//Table cell defs
	public static final String TABLE_CELL_COLLAPSE_BORDER = "collapsebordertabledata";
	public static final String TABLE_CELL_HEADING_LEFT_JUSTIFIED = "fieldleftheading";
	public static final String TABLE_CELL_HEADING_RIGHT_JUSTIFIED = "fieldrightheading";
	public static final String TABLE_CELL_HEADING_CENTER_JUSTIFIED = "fieldcenterheading";
	
	public static final String TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED = "fieldcontrolleft";
	public static final String TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED = "fieldcontrolright";
	public static final String TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED = "fieldcontrolcenter";
	
	public static final String TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED_WITH_BORDER = "fieldcontrolleftwithborder";
	public static final String TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED_WITH_BORDER = "fieldcontrolrightwithborder";
	public static final String TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED_WITH_BORDER = "fieldcontrolcenterwithborder";
	
	public static final String TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL = "leftjustifiedcell";
	public static final String TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL = "rightjustifiedcell";
	public static final String TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL = "centerjustifiedcell";
	
	public static final String TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER = "leftjustifiedcellnoborder";
	public static final String TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER = "rightjustifiedcellnoborder";
	public static final String TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER = "centerjustifiedcellnoborder";

	public static final String TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP = "leftjustifiedcellnoborderaligntop";
	public static final String TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP = "rightjustifiedcellnoborderaligntop";
	public static final String TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP = "centerjustifiedcellnoborderaligntop";

	public static final String TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD = "leftjustifiedcellnoborderbold";
	public static final String TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD = "rightjustifiedcellnoborderbold";
	public static final String TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD = "centerjustifiedcellnoborderbold";
	
	public static final String TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP = "leftjustifiedcellborderaligntop";
	public static final String TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP = "rightjustifiedcellborderaligntop";
	public static final String TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP = "centerjustifiedcellborderaligntop";
	
	
	//Table Row Condensed
	public static final String TABLE_HEADING = TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE;
	public static final String TABLE_FOOTER = TABLE_ROW_BACKGROUNDCOLOR_WHITE;
	public static final String TABLE_TOTALS_HEADING = TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE;
	public static final String TABLE_BREAK = TABLE_ROW_BACKGROUNDCOLOR_BLACK;
	public static final String TABLE_ROW = TABLE_ROW_BACKGROUNDCOLOR_WHITE;
	public static final String TABLE_ROW_EVEN = TABLE_ROW_BACKGROUNDCOLOR_WHITE;
	public static final String TABLE_ROW_ODD = TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY;
	public static final String TABLE_TOTAL = TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREEN;
	public static final String TABLE_ROW_HIGHLIGHT = TABLE_ROW_BACKGROUNDCOLOR_YELLOW;
	public static final String TABLE_NOTES = TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK;
	public static final String TABLE_SUB_BREAK = TABLE_ROW_BACKGROUNDCOLOR_GREY;
	
	
	//Notification defs
	public static final String SCREEN_OVERLAY = "overlay";
	public static final String NOTIFICATION = "notification";
	public static final String NOTIFICATION_CENTER = "notification-center";
	public static final String NOTIFICATION_SUCCESS_CENTER = "notification-success-center";
	public static final String NOTIFICATION_SUCCESS = "notification-success";
	public static final String NOTIFICATION_INFO_CENTER = "notification-info-center";
	public static final String NOTIFICATION_INFO = "notification-info";
	public static final String NOTIFICATION_WARNING_CENTER = "notification-warning-center";
	public static final String NOTIFICATION_WARNING = "notification-warning";
	
	public static final String SUCCESS_NOTIFICATION_TEXT_ID = "notification-success-text";
	public static final String INFO_NOTIFICATION_TEXT_ID = "notification-info-text";
	public static final String WARNING_NOTIFICATION_TEXT_ID = "notification-warning-text";
	
	//Text input and label color themes:
	public static final String LABEL_COLOR_THEME_YELLOW = "background-color: #f4f73b; border-style: inset; display:inline-block;";
	public static final String LABEL_COLOR_THEME_BLUE = "background-color: #99ebff; border-style: inset; display:inline-block;" ;
	
	//General Colors
	public static final String BACKGROUND_LIGHT_GREEN = "#CCFFB2";
	public static final String BACKGROUND_LIGHT_PEACH = "#FFBCA2";
	public static final String BACKGROUND_LIGHT_PEACH_ALT = "#FFC7B3";
	public static final String BACKGROUND_PEACH = "#FF8484";
	public static final String BACKGROUND_YELLOW = "#FFFF66";
	public static final String BACKGROUND_BLUE = "#99CCFF";
	public static final String BACKGROUND_LIGHT_PINK = "#F2C3FA";
	
	
	
	
}

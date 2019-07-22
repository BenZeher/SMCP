package ServletUtilities;

import SMDataDefinition.SMMasterStyleSheetDefinitions;


public class clsNotifications {

	private static int NOTIFICATION_FADE_OUT_TIME_MS = 3000;
	
	public static String success(String sNotificationText){
		String s = "";
		s +="<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION + " " 
							+SMMasterStyleSheetDefinitions.NOTIFICATION_SUCCESS_CENTER+ "\">\n" 
			  + " id=\"" + SMMasterStyleSheetDefinitions.SUCCESS_NOTIFICATION_TEXT_ID + "\">\n"
				+ sNotificationText + "\n"
			+ "</div>\n";
		return s;
	}
	
	public static String info(String sNotificationText){
		String s = "";
		s +="<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION + " " 
							+SMMasterStyleSheetDefinitions.NOTIFICATION_INFO_CENTER+ "\">\n" 
			  + " id=\"" + SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID + "\">\n"
				+ sNotificationText + "\n"
			+ "</div>\n";
		return s;
	}
	
	public static String warning(String sNotificationText){
		String s = "";
		s +="<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION + " " 
							+SMMasterStyleSheetDefinitions.NOTIFICATION_WARNING_CENTER+ "\">\n" 
			  + " id=\"" + SMMasterStyleSheetDefinitions.WARNING_NOTIFICATION_TEXT_ID + "\">\n"
				+ sNotificationText + "\n"
			+ "</div>\n";
		return s;
	}
	
	public static String success_center_fade(String sNotificationText){
		String s = "";
		s +="<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION_CENTER +"\">\n" 
				+ "<div class=\""+SMMasterStyleSheetDefinitions.NOTIFICATION_SUCCESS_CENTER+"\""
					+ " id=\"" + SMMasterStyleSheetDefinitions.SUCCESS_NOTIFICATION_TEXT_ID + "\">\n"
				+ sNotificationText + "\n"
				+ "</div>\n" 
			+ "</div>\n";
		
		s += "<script>\n"
				+ "$(function() {\n" 
				+ "    setTimeout(function() {\n" 
				+ "    $(\"." + SMMasterStyleSheetDefinitions.NOTIFICATION_SUCCESS_CENTER + "\")"
						+ ".fadeOut(" + Integer.toString(NOTIFICATION_FADE_OUT_TIME_MS/4)+ ");\n"
				+ "    }, " +Integer.toString(NOTIFICATION_FADE_OUT_TIME_MS)+ ");\n" 
				+ "});\n"
				+ ""
		+ "</script>\n";
		
		return s;
	}
	
	public static String info_center(String sNotificationText){
		String s = "";
		s += "<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION_CENTER +"\">\n"
				+ "<div class=\""+SMMasterStyleSheetDefinitions.NOTIFICATION_INFO_CENTER +"\""
			    	+ " id=\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\">\n"
				+ sNotificationText + "\n"
				+ "</div>\n" 
		+ "  </div>\n";
		return s;
	}
	
	public static String info_center_overlay(String sNotificationText){
		String s = "";
		s += "<div class=\""+ SMMasterStyleSheetDefinitions.SCREEN_OVERLAY +"\">\n"
				+ "<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION_CENTER +"\">\n"
				+ "<div class=\""+SMMasterStyleSheetDefinitions.NOTIFICATION_INFO_CENTER +"\""
			    	+ " id=\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\">\n"
				+ sNotificationText + "\n"
				+ "</div>\n" 
		+ "  </div>\n"
		+ "  </div>\n";
		return s;
	}
	
	public static String warning_center(String sNotificationText){
		String s = "";
		s += "<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION_CENTER +"\">\n"
				+ "<div class=\""+SMMasterStyleSheetDefinitions.NOTIFICATION_WARNING_CENTER +"\""
			    	+ " id=\""+SMMasterStyleSheetDefinitions.WARNING_NOTIFICATION_TEXT_ID+"\">\n"
				+ sNotificationText + "\n"
				+ "</div>\n" 
		+ "  </div>\n";
		return s;
	}
}

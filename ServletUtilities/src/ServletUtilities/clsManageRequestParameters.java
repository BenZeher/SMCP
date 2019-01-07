package ServletUtilities;

import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class clsManageRequestParameters {

	public static String get_Request_Parameter(String sParameterName, HttpServletRequest req){
		if (req.getParameter(sParameterName) == null){
			return "";
		}else{
			return req.getParameter(sParameterName);
		}
	}

	public static String getQueryStringFromPost(HttpServletRequest req){
		StringBuilder sb = new StringBuilder("?");
		for(Enumeration<String> e = req.getParameterNames();e.hasMoreElements();) {
			String param = e.nextElement().toString();
			sb.append(param)
			.append("=")
			.append(req.getParameter(param))
			.append("&");
		}
		return sb.toString().substring(0, sb.length() - 1);
	}

	public static String getAllRequestParameters(HttpServletRequest req){
	
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		String s = "";
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			s += ("\n" + sParam + " = '" + get_Request_Parameter(sParam, req) + "'");
		} 
		return s;
	}

	public static void printRequestParametersString(HttpServletRequest req){
	
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		//String sDisplay = "";
		System.out.println("Listing request parameters:\n");
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			System.out.println(sParam + " = '" + get_Request_Parameter(sParam, req) + "'");
		} 
	
	}

	public static void printRequestParameters(PrintWriter out, HttpServletRequest req){
	
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		//String sDisplay = "";
		out.println("Listing request parameters:");
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			out.println("\n" + sParam + " = '" + get_Request_Parameter(sParam, req) + "'");
		} 
	
	}

}

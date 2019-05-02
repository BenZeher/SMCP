package SMDataDefinition;

public class SMTablefaoptions {

	public static final String TableName = "faoptions";
	
	//Automatically feed SMCP GL?
	public static String ifeedgl = "ifeedgl";
	
	public static final int FEED_GL_EXTERNAL_GL_ONLY = 0;
	public static final int FEED_GL_SMCP_GL_ONLY = 1;
	public static final int FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL = 2;
	
}

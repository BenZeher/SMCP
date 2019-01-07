package ServletUtilities;
import java.io.IOException;
import java.io.OutputStream;

	/**Writes to nowhere*/
	public class clsNullOutputStream extends OutputStream {
	  @Override
	  public void write(int b) throws IOException {
	  }
	}
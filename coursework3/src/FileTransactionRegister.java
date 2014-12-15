import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Observable;
import java.util.Observer;

//file logger
public class FileTransactionRegister implements Observer {

	private File logFile;

	//truncate or don't in constructor
	public FileTransactionRegister(String path, boolean truncate) {
		logFile = new File(path);
		if (truncate) {
			try {
				FileOutputStream outStream = new FileOutputStream(logFile, true); 
				FileChannel outChan = outStream.getChannel();
				outChan.truncate(0L);
				outChan.close();
				outStream.close();
			} catch (FileNotFoundException e) {
				//shouldn't happen as file was already checked for this
				System.err.println("Couldn't find log file.");
			} catch (IOException e) {
				System.err.println("Couldn't truncate log file.");
				e.printStackTrace();
			}
		}
	}

	@Override
	//gets called on every notifObservers()
	public void update(Observable transaction, Object arg) {
		//needs to be an instance of Transaction
		if (transaction instanceof Transaction) {
			try {
				FileWriter writer = new FileWriter(logFile,true);
				//cast it to Transaction so I can use my methods
				writer.write(((Transaction) transaction).fileFormat() + "\n");
				writer.close();
			} catch (IOException e) {
				System.err.println("Failed to log to file.");
			}
		} else {
			System.err.println("Couldn't log this.");
		}
	}

}

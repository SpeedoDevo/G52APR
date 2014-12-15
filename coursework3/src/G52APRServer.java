import java.io.File;


public class G52APRServer {

	private static final int NO_LOGGING_ARGUMENT_LENGTH = 2;
	private static final int LOGGING_ARGUMENT_LENGTH = 4;

	private static final int CHECKPATH_FAILURE = -1;
	private static final int CHECKPATH_DIR = 0;
	private static final int CHECKPATH_FILE = 1;

	public static void main(String[] args) {
		//start listening on parsed port number
		new Network(checkArgs(args));
	}

	//straightforward method to parse arguments
	private static int checkArgs(String[] args) {
		//turn on logging and decide if truncating is needed
		if (args.length == LOGGING_ARGUMENT_LENGTH) {
			boolean truncate;
			if(args[2].equals("-r")) {
				truncate = false;
			} else if (args[2].equals("-R")) {
				truncate = true;
			} else {
				err();
				truncate = false;
			}
			if (checkPath(args[3]) == CHECKPATH_FILE) {
				Registers.addRegister(new FileTransactionRegister(args[3],truncate));
				new StartTransaction();
			} else {
				err();
			}
		}

		//required server setup
		if (args.length == NO_LOGGING_ARGUMENT_LENGTH || args.length == LOGGING_ARGUMENT_LENGTH) {
			int port = 0;
			//if argument is not an int throw an error
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				err();
			}
			//check if the second arg is an existing directory
			if (checkPath(args[1]) == CHECKPATH_DIR) {
				Serve.setBasePath(args[1]);
			} else {
				err();
			}
			return port;
		} else {
			err();
			return 0;
		}
	}

	//check a string based path if it's a file or directory
	private static int checkPath(String path) {
		File file = new File(path);
		int res;
		if (file.exists() && file.isDirectory()) {
			res = CHECKPATH_DIR; //it's a directory
		} else if (file.exists() && file.isFile()) {
			res = CHECKPATH_FILE; //it's a file
		} else {
			res = CHECKPATH_FAILURE; //it doesn't exist
		}
		return res;
	}

	//log error and exit
	private static void err() {
		System.err.println("Incorrect arguments");
		System.exit(-1);
	}

}

import java.io.File;


public class G52APRServer {

	private static final int ARGUMENT_NUMBER = 2;

	public static void main(String[] args) {
		//start listening on parsed port number
		new Network(checkArgs(args));
	}

	//straightforward method to parse arguments
	private static int checkArgs(String[] args) {
		if (args.length == ARGUMENT_NUMBER) {
			int port = 0;
			//if argument is not an int throw an error
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				err();
			}
			//check if the second arg is an existing directory
			File file = new File(args[1]);
			if (file.exists() && file.isDirectory()) {
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

	private static void err() {
		System.out.println("Incorrect arguments");
		System.exit(-1);
	}

}


public abstract class FullHttpTransaction extends Transaction {

	protected String file;
	private String firstLine;
	private String[] lines;
	private String[] reqTokens;
	private boolean error = false;
	protected byte[] rawData;
	//private Hashtable<String,String> headers = new Hashtable<String,String>(20);

	//method that produces the data required by a filelogger
	@Override
	protected String fileData() {
		return error ? ", Couldn't parse message." : file + ", " + firstLine;
	}

	//reparsing full message
	protected void parse() {
		//split req to headers and entity body
		String[] headAndBody = new String(rawData).split("\\\r\\\n\\\r\\\n");
		try {
			//try to split lines and req line tokens
			lines = headAndBody[0].split("\\\r\\\n");
			reqTokens = lines[0].split(" ");
			file = reqTokens[1].replaceFirst("^/", "");
			firstLine = lines[0];
			
		//TODO move catch upper
		} catch (ArrayIndexOutOfBoundsException e) {
			//if it didn't succeed return bad req
			error = true;
		} catch (NullPointerException npe) {
			error = true;
		}

		//left the header parsing here for possible later extension
		//save headers in a hashtable
		// for (String line : lines) {
		// 	//System.out.println(line);
		// 	String[] keyValuePair = line.split(": ");
		// 	if (keyValuePair.length == PAIR) {
		// 		headers.put(keyValuePair[0],keyValuePair[1]);
		// 	}
		// }

	}

	//logging happens asynchronously so logging wouldn't block server IO
	protected void doAsyncLog(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				parse();
				override();
				addAllRegisters();
				setChanged();
				notifyObservers();
			}
		}).start();
	}

	//if anything needs to be overriden before logging, required by response logging
	protected abstract void override();
}

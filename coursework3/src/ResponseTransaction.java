
public class ResponseTransaction extends FullHttpTransaction {
	private String override;

	public ResponseTransaction(byte[] rawData, String resFile) {
		//save raw data
		this.rawData = rawData;
		//setup marker
		marker = "<";
		//save request file for overiding
		override = resFile;
		//log asynchronously
		doAsyncLog();
	}

	//parse() doesn't work here so file needs overriding
	protected void override() {
		file = override;
	}
}

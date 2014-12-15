
public class RequestTransaction extends FullHttpTransaction {
	public RequestTransaction(byte[] rawData) {
		//save raw data
		this.rawData = rawData;
		//setup marker
		marker = ">";
		//log asynchronously
		doAsyncLog();
	}

	//no override necessary here
	protected void override() {
		return;
	}
}

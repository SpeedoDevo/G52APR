
public class SimpleResponseTransaction extends Transaction {

	private String file;

	public SimpleResponseTransaction(String file) {
		marker = "<";
		this.file = file;
		//async logging
		new Thread(new Runnable() {
			@Override
			public void run() {
				addAllRegisters();
				setChanged();
				notifyObservers();
			}
		}).start();
	}

	//only happens when simple request succeeds
	@Override
	protected String fileData() {
		return file + ", " + "HTTP/0.9 200 OK";
	}

}

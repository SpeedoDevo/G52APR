//simple logging start log
public class StartTransaction extends Transaction {

	public StartTransaction() {
		this.marker = "$";
		addAllRegisters();
		setChanged();
		notifyObservers();
	}
	
	@Override
	protected String fileData() {
		return "starting registering";
	}

}

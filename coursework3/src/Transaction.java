import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.client.utils.DateUtils;

//abstract for all transactions
public abstract class Transaction extends Observable {

	protected String marker = "";
	protected Date date = new Date();

	//inheritable file formatting
	public String fileFormat() {
		return (DateUtils.formatDate(date) + ", " + marker + ", " + fileData());
	}

	protected void addRegisters(ArrayList<Observer> registers) {
		for (Observer register : registers) {
			addObserver(register);
		}
	}

	//adding all observers with one method call
	protected void addAllRegisters() {
		addRegisters(Registers.getRegisters());
	}

	//anything else that needs to be logged in a file
	protected abstract String fileData();

}

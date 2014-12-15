import java.util.ArrayList;
import java.util.Observer;

//store all register in ArrayList so all Observables can add all Observers
//didn't implement single gets or removes as this implementation doesn't require it
public class Registers {
	private static ArrayList<Observer> registers = new ArrayList<Observer>();

	public static void addRegister(Observer register) {
		registers.add(register);
	}

	public static ArrayList<Observer> getRegisters() {
		return registers;
	}
}

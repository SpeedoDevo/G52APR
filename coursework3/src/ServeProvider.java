//singleton Serve provider
public class ServeProvider {
	private static Serve instance = null;

	public static Serve getInstance() {
		if(instance == null) {
			//thread safe instance generation
			synchronized(Serve.class) {
				if(instance == null) {
					instance = new Serve();
				}
			}
		}
		return instance;
	}
}

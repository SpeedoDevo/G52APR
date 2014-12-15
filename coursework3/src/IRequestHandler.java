
/**
 * The Interface IRequestHandler.
 */
public interface IRequestHandler {
	
	
	/**
	 * Process the request. This method takes in 
	 * a request, and returns the response. It does not
	 * respond directly to the client, but returns an
	 * array of bytes. The network section is responsible
	 * for sending this array of bytes back to the client.
	 *
	 * @param request the request received from the client.
	 * @return response as an array of bytes.
	 */
	public byte[] processRequest(byte[] request) ;

}

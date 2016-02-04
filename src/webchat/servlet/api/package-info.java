
/**
 * The servlets providing the HTTP API gateway to the chat server. <br />
 * 
 * Currently consists of two servlets: <br />
 * <ol>
 * <li>A stream servlet, which the agent connects with to fetch its messages (ie the client's input)</li>
 * <li>A command servlet, which the agent sends requests to (ie the client's output)</li>
 * </ol>

 * <p> 200 OK &ltResult&gt...error...&lt/Result&gt </p>
 * 
 * @author Nick
 *
 */
package webchat.servlet.api;
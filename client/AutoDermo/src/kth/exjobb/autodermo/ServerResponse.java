package kth.exjobb.autodermo;

/**
 * Simple class that encapsulates the server's response
 */
public class ServerResponse {
    public int code = 0;
    public String content = "";

    public ServerResponse(int code, String content) {
        this.code = code;
        this.content = content;
    }
}

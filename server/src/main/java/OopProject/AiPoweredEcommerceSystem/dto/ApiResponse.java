package OopProject.AiPoweredEcommerceSystem.dto;

/**
 * Generic API response envelope used by all controllers.
 *
 * <p>Every endpoint returns this wrapper so the frontend always gets a
 * consistent JSON shape:
 * <pre>
 * {
 *   "success": true,
 *   "message": "OK",
 *   "data": { ... }
 * }
 * </pre>
 *
 * @param <T> the payload type
 */
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;

    // ── Factory Methods ───────────────────────────────────────

    /** Wrap a successful payload. */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = "OK";
        r.data    = data;
        return r;
    }

    /** Wrap a successful payload with a custom message. */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data    = data;
        return r;
    }

    /** Return an error response (no data payload). */
    public static <T> ApiResponse<T> error(String message){
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        return r;
    }

    // ── Constructors ──────────────────────────────────────────

    public ApiResponse() {}

    // ── Getters & Setters ─────────────────────────────────────

    public boolean isSuccess()                { return success; }
    public void setSuccess(boolean success)   { this.success = success; }

    public String getMessage()                { return message; }
    public void setMessage(String message)    { this.message = message; }

    public T getData()                        { return data; }
    public void setData(T data)               { this.data = data; }
}


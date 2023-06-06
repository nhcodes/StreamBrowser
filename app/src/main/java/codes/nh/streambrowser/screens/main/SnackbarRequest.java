package codes.nh.streambrowser.screens.main;

public class SnackbarRequest {

    private final String message;

    private final SnackbarAction action;

    public SnackbarRequest(String message, SnackbarAction action) {
        this.message = message;
        this.action = action;
    }

    public SnackbarRequest(String message) {
        this(message, null);
    }

    public String getMessage() {
        return message;
    }

    public SnackbarAction getAction() {
        return action;
    }

    public static class SnackbarAction {

        private final String message;

        private final Runnable action;

        public SnackbarAction(String message, Runnable action) {
            this.message = message;
            this.action = action;
        }

        public String getMessage() {
            return message;
        }

        public Runnable getAction() {
            return action;
        }

    }
}

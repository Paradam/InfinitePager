package android.support.v4.view;

/**
 * An Interface to allow other classes to call methods to perform sliding in and out actions.
 */
public interface SlidablePagerTitle {

    /**
     * The Tab Layout is not hidable. Calling any method to hide or show the Tab Layout will be ignored.
     */
    public static final int HIDE_NONE    = 0;
    /**
     * The Tab Layout is hidable. The Tab Layout will appear and disappear automatically and also
     * when calling the appropriate methods to show or hide the Tab Layout.
     */
    public static final int HIDE_AUTO    = 1;
    /**
     * The Tab Layout is hidable, but will only change state when {@link #slideIn(long)} or {@link #slideOut(long)}
     * are called.
     */
    public static final int HIDE_PROGRAM = 2;

    /**
     * The delay in milliseconds after which the PagerTitleStrip will undergo hiding.
     *
     * Default long value is 5 seconds.
     */
    public static final long DISPLAY_TIME = 5000;

    /**
     * The delay in milliseconds after which the PagerTitleStrip will undergo hiding.
     *
     * Default short value is 1 second.
     */
    public static final long DISPLAY_TIME_SHORT = 1000;

    /**
     * The minimum amount the pointer needs to move in order to trigger the sliding menu to appear or
     * disappear.
     */
    public static final int MOVE_THRESHOLD = 50;

    /**
     * Show the title bar for the number of milliseconds provided before hiding again.
     * @param milliseconds The number of milliseconds to show the title for. A negative value to show
     *                     the title bar until another event occurs to close it again, or this method
     *                     is called again with a value equal to or larger than 0.
     */
    public void slideIn(long milliseconds);

    /**
     * Hide the title bar after the number of milliseconds provided before hiding again.
     * @param milliseconds The maximum number of milliseconds to wait before hiding the title.
     *                     A negative value or zero will hide the title bar now.
     */
    public void slideOut(long milliseconds);
}

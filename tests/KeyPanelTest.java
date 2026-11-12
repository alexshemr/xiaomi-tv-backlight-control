package local.ha.tvbacklight;

public final class KeyPanelTest {
    public static void main(String[] args) {
        KeyPanel panel = new KeyPanel();
        check(!panel.down(1, 19, false, 0, 100), "Closed arrows pass");
        check(!panel.up(1, 19), "Closed release passes");
        check(panel.down(1, 100, false, 0, 100), "Shortcut consumed");
        check(panel.visible, "Shortcut opens panel");
        check(panel.up(1, 100), "Shortcut release consumed");
        check(panel.down(1, 19, false, 10, 100) && panel.delta == 5, "Up increases");
        check(panel.down(1, 19, true, 20, 100) && panel.delta == 0, "Repeat throttled");
        check(panel.down(1, 19, true, 200, 100) && panel.delta == 5, "Repeat applied");
        panel.close();
        check(panel.down(1, 19, true, 400, 100) && panel.delta == 0, "Held after close consumed");
        check(panel.up(1, 19), "Held release after close consumed");
        check(!panel.down(1, 20, false, 500, 100), "Navigation restored");
        panel.up(1, 20);
        panel.down(1, 19, false, 600, 100);
        panel.down(1, 100, false, 610, 100);
        check(!panel.down(1, 19, true, 800, 100), "Previously passed press stays passed");
        check(!panel.up(1, 19), "Previously passed release stays passed");
        panel.up(1, 100);
        check(panel.down(1, 4, false, 900, 100) && !panel.visible, "Back closes");
        check(panel.up(1, 4), "Back release consumed");
        panel.reset();
        check(panel.down(1, 101, false, 1000, 100, 101) && panel.screenOffRequested,
            "Dedicated off button requested");
        check(!panel.visible && panel.up(1, 101), "Off button release consumed");
        check(!panel.down(1, 101, false, 1100, 100, 0), "Unassigned off button passes");
        panel.up(1, 101);
        panel.reset();
        check(panel.down(1, 19, false, 1200, 100, 101, true)
            && panel.wakeRequested && panel.delta == 5, "Up wakes at plus five");
        check(panel.up(1, 19), "Wake release consumed");
        panel.reset();
        check(panel.down(1, 21, false, 1300, 100, 101, true)
            && panel.wakeRequested, "Other key wakes");
        check(panel.down(1, 21, true, 1500, 100, 101, false)
            && !panel.wakeRequested, "Wake repeat consumed without replay");
        check(panel.up(1, 21), "Other wake release consumed");
        panel.reset();
        check(!panel.down(1, 26, false, 1600, 100, 101, true)
            && !panel.wakeRequested, "Power does not wake through app");
        panel.up(1, 26);
        check(panel.down(1, 101, false, 1700, 100, 101, true)
            && !panel.wakeRequested && panel.screenOffRequested, "Off key stays off");
        panel.up(1, 101);
        System.out.println("Key panel behavior passed");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

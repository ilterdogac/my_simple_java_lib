import javax.swing.*;
import java.awt.event.ActionEvent;

public class SwingWindow {
	
	private static final java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
	
	
	/** Plays the OS' default dialog/asterisk/foreground beep. <strong>Does not block the thread.</strong> */
	public static void beep(int beepBy, double waitEach) {
		fn.newThread(() -> {
			for (int i: fn.range(beepBy)) {
				fn.newThread(() -> {tk.beep();}).start();
				fn.sleep(beepBy == 1 ? 1 : waitEach);
			}
		}).start();
	}
	
	/** Creates a Swing window with the title and description, beeps (plays OS' default dialog/asterisk/foreground beep)
	 *  the specified times with the specified amount of pauses between and returns the window. <strong>Does not
	 *  block the thread.</strong>
	 *  <p><strong>Warning: The returned window should not be re-opened after closing it (because when closed its
	 *  native resources will be freed)</strong>
	 */
	public static javax.swing.JFrame alertWindow(String title, String message, int beepBy, double waitEach) {
		return alertWindow(title, message, beepBy, waitEach, 0);
	}
	
	/** Creates a Swing window with the title and description, beeps (plays OS' default dialog/asterisk/foreground beep)
	 *  the specified times with the specified amount of pauses between and returns the window. <strong>Does not
	 *  block the thread.</strong>
	 *  <p><strong>Warning: The returned window should not be re-opened after closing it (because when closed its
	 *  native resources will be freed)</strong>
	 */
	// TODO: Fix that (returned window can't be programmatically made hidden without disposing (prob. destroying) it)
	public static javax.swing.JFrame alertWindow(String title, String message, int beepBy, double waitEach, double delayBefore) {
		
		if (beepBy < 0) throw new IllegalArgumentException();
		
		beep(beepBy, waitEach);
		fn.sleep(delayBefore);
		
		javax.swing.JFrame frame = new javax.swing.JFrame(title);
		{ // Attach the window ESC key to close it.
			javax.swing.Action action = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					frame.setVisible(false);
				}
			};
			javax.swing.JComponent pnl = frame.getRootPane();
			
			javax.swing.KeyStroke keyStroke = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
			Object actionMapKey = "Escape to close the window";
			
			pnl.getActionMap().put(actionMapKey, action);
			pnl.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionMapKey);
		}
		
		fn.newThread(() -> {
			boolean multiline = str.split(message, "\\n").size() > 1;
			if (multiline) {
				java.awt.TextArea area = new java.awt.TextArea();
				area.append(message);
				area.setEditable(false);
				frame.getContentPane().add(area);
			} else {
				frame.getContentPane().add(new java.awt.Label(message, java.awt.Label.CENTER));
			}
			frame.pack();
			if (!multiline) frame.setSize(frame.getWidth() + 150, frame.getHeight() + 50);
			java.awt.Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
			int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
			frame.setLocation(x, y);
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
		}).start();
		
		// Wait until the frame is closed (indeed becomes invisible) then destroy it
		fn.newThread(() -> {
			while (!frame.isVisible()) fn.sleep(0.01); // For a fract. of second it is not visible
			while (frame.isVisible()) fn.sleep(0.1); // Wait until closed
			frame.dispose();
			fn.log("Note: The JFrame returned by SwingWindow.alertWindow has been dispose()'d "
			     + "since it was closed by user or code so that it no longer keeps the JVM "
			     + "like non-daemon threads.");
		}, true).start();
		
		return frame;
	}
}
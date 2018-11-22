/*
 * 07/29/2012
 *
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.demo;

import java.awt.Toolkit;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;


/**
 * Stand-alone version of the demo.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class DemoApp extends JFrame {

	private static final String ICON = "/org/fife/rsta/zscript/img/link.png";


	public DemoApp() {
		setRootPane(new DemoRootPane());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("ZScript Editor Demo");
		try {
			setIconImage(ImageIO.read(getClass().getResource(ICON)));
		} catch (IOException ioe) {
			ioe.printStackTrace(); // Never happens
		}
		pack();
	}


	public static void main(String[] args) {

		DemoRootPane.registerZScript();

		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.
										getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace(); // Never happens
			}
			Toolkit.getDefaultToolkit().setDynamicLayout(true);
			new DemoApp().setVisible(true);
		});
	}


}

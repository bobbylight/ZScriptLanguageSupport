/*
 * 08/19/2012
 *
 * ZScriptOptionPanel.java - Option panel for the plugin.
 * This library is distributed under a modified BSD license.  See the included
 * ZScriptLanguageSupport.License.txt file for details.
 */
package org.fife.rsta.zscript.rtext;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.zscript.ZScriptLanguageSupport;
import org.fife.rtext.AbstractMainView;
import org.fife.rtext.NumberDocumentFilter;
import org.fife.rtext.RText;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * Options panel for the ZScript plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ZScriptOptionPanel extends PluginOptionsDialogPanel {

	private Listener listener;
	private JCheckBox enabledCB;
	private JCheckBox paramAssistanceCB;
	private JCheckBox showDescWindowCB;
	private JCheckBox autoActivateCB;
	private JLabel aaDelayLabel;
	private JTextField aaDelayField;
	private JLabel aaKeysLabel;
	private JTextField aaKeysField;
	private JCheckBox foldingCB;
	private JButton rdButton;

	private static final String PROPERTY		= "Property";

	private static final String AA_DELAY_DEFAULT = "0";

	// NOTE: We're cheating here, we know the name of the main RSTALanguageSupport
	// plugin's properties.  It's worth it to re-use some of its properties.
	private static final String MSG = "org.fife.rtext.plugins.langsupport.Plugin";
	private static final ResourceBundle parentMsg = ResourceBundle.getBundle(MSG);


	public ZScriptOptionPanel(Plugin plugin) {

		super(plugin);
		setName(Messages.getString("OptionPanel.Name"));
		listener = new Listener();
		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());

		// Set up our border and layout.
		setBorder(UIUtil.getEmpty5Border());
		setLayout(new BorderLayout());
		Box cp = Box.createVerticalBox();

		cp.add(createGeneralPanel());
		cp.add(Box.createVerticalStrut(5));
		cp.add(createAutoActivationPanel());
		cp.add(Box.createVerticalStrut(5));
		cp.add(createFoldingPanel());
		cp.add(Box.createVerticalStrut(5));
		rdButton = new JButton(parentMsg.getString("Options.General.RestoreDefaults"));
		rdButton.addActionListener(listener);
		addLeftAligned(cp, rdButton, 5);

		// Put it all together!
		cp.add(Box.createVerticalGlue());
		add(cp, BorderLayout.NORTH);
		applyComponentOrientation(o);

	}


	/**
	 * Creates the options section dealing with auto-activation.
	 *
	 * @return The section of options.
	 */
	private Container createAutoActivationPanel() {

		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(
				parentMsg.getString("Options.General.AutoActivation")));

		autoActivateCB = createCB("Options.General.EnableAutoActivation");
		addLeftAligned(box, autoActivateCB, 5);

		Box box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		SpringLayout sl = new SpringLayout();
		JPanel temp = new JPanel(sl);
		aaDelayLabel = new JLabel(parentMsg.getString("Options.General.AutoActivationDelay"));
		aaDelayField = new JTextField(10);
		AbstractDocument doc = (AbstractDocument)aaDelayField.getDocument();
		doc.setDocumentFilter(new NumberDocumentFilter());
		doc.addDocumentListener(listener);
		aaKeysLabel = new JLabel(Messages.getString("Options.ZScript.AutoActivationKeys"));
		aaKeysLabel.setEnabled(false);
		aaKeysField = new JTextField(">", 10);
		aaKeysField.setEnabled(false);
		Dimension spacer = new Dimension(30, 5);
		if (o.isLeftToRight()) {
			temp.add(aaDelayLabel);				temp.add(aaDelayField);
			temp.add(Box.createRigidArea(spacer));
			temp.add(aaKeysLabel);			temp.add(aaKeysField);
		}
		else {
			temp.add(aaDelayField);			temp.add(aaDelayLabel);
			temp.add(Box.createRigidArea(spacer));
			temp.add(aaKeysField);		temp.add(aaKeysLabel);
		}
		UIUtil.makeSpringCompactGrid(temp, 1,5, 0,0, 5,5);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		box2.add(temp2);

		box2.add(Box.createVerticalGlue());

		return box;

	}


	private JCheckBox createCB(String key) {

		String text = null;

		if (key.indexOf('.')==-1) {
			text = Messages.getString("Options.ZScript." + key);
		}
		else { // Assume one of the "General" options.
			text = parentMsg.getString(key);
		}

		JCheckBox cb = new JCheckBox(text);
		cb.addActionListener(listener);
		return cb;

	}


	/**
	 * Creates the options section dealing with code folding.
	 *
	 * @return The section of options.
	 */
	private Container createFoldingPanel() {

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(parentMsg.
				getString("Options.General.Section.Folding")));

		foldingCB = createCB("Options.General.EnableCodeFolding");
		addLeftAligned(box, foldingCB, 5);

		return box;
	}


	/**
	 * Creates the options section dealing with general stuff.
	 *
	 * @return The section of options.
	 */
	private Container createGeneralPanel() {

		ComponentOrientation o = ComponentOrientation.getOrientation(getLocale());

		Box box = Box.createVerticalBox();
		box.setBorder(new OptionPanelBorder(parentMsg.
				getString("Options.General.Section.General")));

		enabledCB = createCB("Options.General.EnableCodeCompletion");
		addLeftAligned(box, enabledCB, 5);

		Box box2 = Box.createVerticalBox();
		if (o.isLeftToRight()) {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		}
		else {
			box2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
		}
		box.add(box2);

		showDescWindowCB = createCB("Options.General.ShowDescWindow");
		addLeftAligned(box2, showDescWindowCB, 5);

		paramAssistanceCB = createCB("Options.General.ParameterAssistance");
		addLeftAligned(box2, paramAssistanceCB, 5);

		box2.add(Box.createVerticalGlue());

		return box;

	}


	/**
	 * Applies the changes in this panel into the application.
	 *
	 * @see #setValuesImpl(Frame)
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls = lsf.getSupportFor(Plugin.SYNTAX_STYLE_ZSCRIPT);
		ZScriptLanguageSupport zls = (ZScriptLanguageSupport)ls;

		// Options dealing with code completion.
		zls.setAutoCompleteEnabled(enabledCB.isSelected());
		zls.setParameterAssistanceEnabled(paramAssistanceCB.isSelected());
		zls.setShowDescWindow(showDescWindowCB.isSelected());

		// Options dealing with auto-activation.
		zls.setAutoActivationEnabled(autoActivateCB.isSelected());
		int delay = Integer.parseInt(AA_DELAY_DEFAULT);
		String temp = aaDelayField.getText();
		if (temp.length()>0) {
			try {
				delay = Integer.parseInt(aaDelayField.getText());
			} catch (NumberFormatException nfe) { // Never happens
				nfe.printStackTrace();
			}
		}
		zls.setAutoActivationDelay(delay);

		// Options dealing with code folding.
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		boolean folding = foldingCB.isSelected();
		view.setCodeFoldingEnabledFor(Plugin.SYNTAX_STYLE_ZSCRIPT, folding);

	}


	/**
	 * Ensures all input from the user is valid.
	 */
	@Override
	protected OptionsPanelCheckResult ensureValidInputsImpl() {
		// User cannot enter bad data in this panel.
		return null;
	}


	@Override
	public JComponent getTopJComponent() {
		return enabledCB;
	}


	private void setAutoActivateCBSelected(boolean selected) {
		autoActivateCB.setSelected(selected);
		aaDelayLabel.setEnabled(selected);
		aaDelayField.setEnabled(selected);
	}


	private void setEnabledCBSelected(boolean selected) {
		enabledCB.setSelected(selected);
		paramAssistanceCB.setEnabled(selected);
		showDescWindowCB.setEnabled(selected);
	}


	/**
	 * Sets values in this panel to reflect those being used by the plugin.
	 *
	 * @param owner The application window.
	 * @see #doApplyImpl(Frame)
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport ls = lsf.getSupportFor(Plugin.SYNTAX_STYLE_ZSCRIPT);
		ZScriptLanguageSupport zls = (ZScriptLanguageSupport)ls;

		// Options dealing with code completion
		setEnabledCBSelected(zls.isAutoCompleteEnabled());
		paramAssistanceCB.setSelected(zls.isParameterAssistanceEnabled());
		showDescWindowCB.setSelected(zls.getShowDescWindow());

		// Options dealing with auto-activation
		setAutoActivateCBSelected(zls.isAutoActivationEnabled());
		aaDelayField.setText(Integer.toString(zls.getAutoActivationDelay()));

		// Options dealing with code folding
		RText rtext = (RText)owner;
		AbstractMainView view = rtext.getMainView();
		foldingCB.setSelected(view.isCodeFoldingEnabledFor(
				Plugin.SYNTAX_STYLE_ZSCRIPT));

	}


	/**
	 * Listens for events in this options panel.
	 */
	private class Listener implements ActionListener, DocumentListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			Object source = e.getSource();

			if (enabledCB==source) {
				// Trick related components to toggle enabled states
				setEnabledCBSelected(enabledCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (paramAssistanceCB==source ||
					showDescWindowCB==source ||
					foldingCB==source) {
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (autoActivateCB==source) {
				// Trick related components to toggle enabled states
				setAutoActivateCBSelected(autoActivateCB.isSelected());
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, null, null);
			}

			else if (rdButton==source) {

				if (!enabledCB.isSelected() ||
						!paramAssistanceCB.isSelected() ||
						!showDescWindowCB.isSelected() ||
						!autoActivateCB.isSelected() ||
						!AA_DELAY_DEFAULT.equals(aaDelayField.getText()) ||
						!foldingCB.isSelected()) {
					setEnabledCBSelected(true);
					paramAssistanceCB.setSelected(true);
					showDescWindowCB.setSelected(true);
					setAutoActivateCBSelected(true);
					aaDelayField.setText(AA_DELAY_DEFAULT);
					foldingCB.setSelected(true);
					hasUnsavedChanges = true;
					firePropertyChange(PROPERTY, null, null);
				}

			}

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		private void handleDocumentEvent(DocumentEvent e) {
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, null, null);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


}
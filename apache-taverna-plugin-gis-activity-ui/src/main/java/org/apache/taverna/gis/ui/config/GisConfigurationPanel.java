package org.apache.taverna.gis.ui.config;

import java.awt.GridLayout;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.apache.taverna.gis.GisActivity;
import org.apache.taverna.gis.GisActivityConfigurationBean;


@SuppressWarnings("serial")
public class GisConfigurationPanel
		extends
		ActivityConfigurationPanel<GisActivity, 
        GisActivityConfigurationBean> {

	private GisActivity activity;
	private GisActivityConfigurationBean configBean;
	
	private JTextField fieldProcessIdentifier;
	private JTextField fieldURI;

	public GisConfigurationPanel(GisActivity activity) {
		this.activity = activity;
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 2));

		// FIXME: Create GUI depending on activity configuration bean
		JLabel labelString = new JLabel("Example string:");
		add(labelString);
		fieldProcessIdentifier = new JTextField(20);
		add(fieldProcessIdentifier);
		labelString.setLabelFor(fieldProcessIdentifier);

		JLabel labelURI = new JLabel("Example URI:");
		add(labelURI);
		fieldURI = new JTextField(25);
		add(fieldURI);
		labelURI.setLabelFor(fieldURI);

		// Populate fields from activity configuration bean
		refreshConfiguration();
	}

	/**
	 * Check that user values in UI are valid
	 */
	@Override
	public boolean checkValues() {
		try {
			URI.create(fieldURI.getText());
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getCause().getMessage(),
					"Invalid URI", JOptionPane.ERROR_MESSAGE);
			// Not valid, return false
			return false;
		}
		// All valid, return true
		return true;
	}

	/**
	 * Return configuration bean generated from user interface last time
	 * noteConfiguration() was called.
	 */
	@Override
	public GisActivityConfigurationBean getConfiguration() {
		// Should already have been made by noteConfiguration()
		return configBean;
	}

	/**
	 * Check if the user has changed the configuration from the original
	 */
	@Override
	public boolean isConfigurationChanged() {
		String originalString = configBean.getProcessIdentifier();
		String originalUri = configBean.getOgcServiceUri().toASCIIString();
		// true (changed) unless all fields match the originals
		return ! (originalString.equals(fieldProcessIdentifier.getText())
				&& originalUri.equals(fieldURI.getText()));
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		configBean = new GisActivityConfigurationBean();
		
		// FIXME: Update bean fields from your UI elements
		configBean.setProcessIdentifier(fieldProcessIdentifier.getText());
		configBean.setOgcServiceUri(URI.create(fieldURI.getText()));
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
		configBean = activity.getConfiguration();
		
		// FIXME: Update UI elements from your bean fields
		fieldProcessIdentifier.setText(configBean.getProcessIdentifier());
		fieldURI.setText(configBean.getOgcServiceUri().toASCIIString());
	}
}

// $Id:$

package org.obfuscate4e.updater;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.obfuscate4e.BuildProperties;


/**
 * This class is the updater for build properties.
 * 
 * @author fwo
 * 
 */
public class BuildPropertiesUpdater extends AbstractUpdater {

	public static final String EXISTING_KEY_MESSAGE = "Key ''{0}'' exists in build.properties: \nOverwrite?";

	public static final String DESCRIPTION = "Update build.properties";

	/**
	 * the constructor
	 */
	public BuildPropertiesUpdater() {
		super(DESCRIPTION);
	}

	/**
	 * adds a the build entry to the build.properties.<br>
	 * usually this is:<br>
	 * "<i>customBuildCallbacks = customBuildCallbacks.xml</i>"<br>
	 * (but in fact it's generic)
	 * 
	 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		addBuildEntry(IUpdater.KEY_CUSTOM_BUILD_CALLBACKS,
				IUpdater.FILE_CUSTOM_BUILD_CALLBACKS);
	}

	private void addBuildEntry(final String key, final String value)
			throws CoreException {
		Assert.isNotNull(key);
		Assert.isNotNull(value);
		BuildProperties buildProperties = getProjectInfo().getBuildProperties();
		if (buildProperties.isCustomBuildCallbacksRegistered()) {
			return;
		}
		if (!isOverwriteConfirmationEnabled()) {
			if (!openConfirmDialog(key)) {
				return;
			}
		}
		buildProperties.registerCustomBuildCallbacks();
	}

	private boolean openConfirmDialog(final String key) {
		String message = MessageFormat.format(EXISTING_KEY_MESSAGE,
				new String[] { key });
		class MessageDialogWithPreference extends MessageDialog {
			private boolean rememberMyDecision = false;

			public MessageDialogWithPreference(Shell parentShell,
					String dialogTitle, Image dialogTitleImage,
					String dialogMessage, int dialogImageType,
					String[] dialogButtonLabels, int defaultIndex) {
				super(parentShell, dialogTitle, dialogTitleImage,
						dialogMessage, dialogImageType, dialogButtonLabels,
						defaultIndex);
			}

			protected Control createCustomArea(Composite parent) {
				Button rememberButton = new Button(parent, SWT.CHECK);
				rememberButton.setText("remember my decision");
				rememberButton.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						rememberMyDecision = ((Button) e.widget).getSelection();
					}

					public void widgetSelected(SelectionEvent e) {
						rememberMyDecision = ((Button) e.widget).getSelection();
					}
				});
				return rememberButton;
			}
		}
		;

		Shell activeShell = PlatformUI.getWorkbench().getDisplay()
				.getActiveShell();

		MessageDialogWithPreference dialog = new MessageDialogWithPreference(
				activeShell, getDescription(), null, message,
				MessageDialog.QUESTION, new String[] {
						IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);

		boolean confirmation = dialog.open() == 0;
		if (confirmation && dialog.rememberMyDecision) {
			rememberConfirmationDecision();
		}
		return confirmation;
	}

	protected boolean isOverwriteConfirmationEnabled() {
		return true;
	}

	protected void rememberConfirmationDecision() {
	}

}

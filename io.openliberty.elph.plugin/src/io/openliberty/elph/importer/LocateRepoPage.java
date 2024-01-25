package io.openliberty.elph.importer;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;

class LocateRepoPage extends WizardPage {
	LocateRepoPage() {
		super(LocateRepoPage.class.getSimpleName());
		setTitle("Locate the Open Liberty Repository");
		setDescription("Specify the directory containing the local Open Liberty git repository");
		setMessage("Please choose the directory containing your local Open Liberty git repository");
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite page = new Composite(parent, SWT.NONE);
		setControl(page);
		setPageComplete(false);

		page.setLayout(new GridLayout(2, false));
		String olPathTemplate = "Open Liberty repository: ";
		Label olLabel = new Label(page, SWT.NONE);
		olLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		olLabel.setText(olPathTemplate + "<unspecified>");
		    
		Button fileBrowser = new Button(page, SWT.PUSH);
		fileBrowser.setText("Browse");
		fileBrowser.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(page.getShell(), SWT.NULL);
				String dir = dialog.open();
				Optional<Path> repo = Optional.ofNullable(dir)
					.map(Controller::new)
					.map(Controller::getRepo);
					
				
				if (repo.isPresent()) {
					// save the setting
					new Config().saveOlRepoPath(repo.get());
					// display the setting
					Color black = new Color(new RGB(0, 0, 0));
					olLabel.setForeground(black);
					olLabel.setText(olPathTemplate + dir);
					// let the user continue
					setPageComplete(true);
				} else {
					setPageComplete(false);
					errorDialogue(parent, "Please choose a valid directory for your Open Liberty repository. "
							+ "It must contain a 'dev' folder.");
					olLabel.setText(olPathTemplate + "N/A");
					Color red = new Color(new RGB(255, 0, 0));
					olLabel.setForeground(red);
				}
			}
		});
	}	
	

	private void errorDialogue(Composite parent, String errMsg) { 
		MessageDialog.openError(parent.getShell(), getTitle(), errMsg);
	}
}
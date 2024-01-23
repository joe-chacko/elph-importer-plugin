package io.openliberty.elph.importer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportLibertyProjectsWizard extends Wizard implements IImportWizard {
	private final Config config = new Config();
	private Controller controller;
//	private ImportPage importPage;

	public ImportLibertyProjectsWizard() {
		this.controller = config.readOlRepoPath().map(Controller::new).orElse(null);
		setWindowTitle("Eclipse Liberty Project Helper");
	}

	@Override
	public void addPages() {
		addPage(new LocateRepoPage());
//		if (null == controller) addPage(new LocateRepoPage());
//		addPage(importPage = new ImportPage());
	}

	@Override
	public void init(IWorkbench arg0, IStructuredSelection arg1) {
		
	}
	
	@Override
	public boolean performFinish() {
		System.out.println("*** PERFORM FINISH ***");
//		importPage.importAllProjects();
		return true;
	}
}
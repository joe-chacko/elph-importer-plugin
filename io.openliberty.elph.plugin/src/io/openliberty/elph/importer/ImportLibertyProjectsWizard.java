package io.openliberty.elph.importer;

import java.util.stream.Stream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportLibertyProjectsWizard extends Wizard implements IImportWizard {	
	public ImportLibertyProjectsWizard() {		
		setWindowTitle("Import Liberty Projects");
	}

	@Override
	public void addPages() {
		if (!new Config().readOlRepoPath().isPresent()) addPage(new LocateRepoPage());
		addPage(new ImportPage());
	}

	@Override
	public void init(IWorkbench arg0, IStructuredSelection arg1) {}
	
	@Override
	public boolean performFinish() {
		System.out.println("*** PERFORM FINISH ***");
		Stream.of(getPages())
				.reduce((a,b) -> b)          // this gets the last element
				.map(ImportPage.class::cast) // which is the ImportPage
				.get()                       // and must be present!
				.importAllProjects();
		return true;
	}
}

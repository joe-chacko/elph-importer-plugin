package io.openliberty.elph.importer;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.core.runtime.Status.CANCEL_STATUS;
import static org.eclipse.core.runtime.Status.OK_STATUS;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

enum EclipseProjects {
	;

	static void importProject(final File baseDirectory, final String projectName) throws CoreException {
		IProjectDescription description = getWorkspace().loadProjectDescription(
				new org.eclipse.core.runtime.Path(baseDirectory.getAbsolutePath() + "/.project"));
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		project.create(description, null);
		project.open(null);
	}

	static Set<String> listProjects() {
		return Stream.of(getWorkspace().getRoot().getProjects())
			.map(p-> p.getName())
			// filter out any that aren't in the bnd workspace
			.peek(n -> System.out.println("Existing project: " + n))
			.collect(toSet());
	}

	static void importProjects(Collection<Path> paths) {
		Set<String> existingProjects = listProjects();
		Queue<Path> queue = paths.stream()
				.filter(p -> !existingProjects.contains(p.getFileName().toString()))
				.collect(Collectors.toCollection(LinkedList::new));
		Job job = new Job("Import Liberty projects") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {  
				// use a submonitor to specify the number of subtasks
				SubMonitor subMonitor = SubMonitor.convert(monitor, queue.size());
				for (Path p = queue.poll(); null != p; p = queue.poll()) {
					try {
						File file = new File(p.toString());
						String name = file.getName();
						subMonitor.setTaskName("Importing " + name);
						subMonitor.split(1);
						importProject(file, name);
						System.out.println("Successfully Imported " + file.getName());
					} catch (RuntimeException | CoreException  err) {
						System.err.println(err.getMessage());
						return CANCEL_STATUS;
					}
				}		
				return OK_STATUS;
			}
		};
		job.schedule();
		
	}
}
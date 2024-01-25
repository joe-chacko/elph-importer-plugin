package io.openliberty.elph.importer;

import static io.openliberty.elph.importer.EclipseProjects.listProjects;
import static java.util.stream.Collectors.toCollection;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.openliberty.elph.bnd.BndCatalog;
import io.openliberty.elph.bnd.ProjectPaths;
import io.openliberty.elph.util.IO;

class Controller {
	/** The Open Liberty repo location */
	private final IO io = new IO();
	private final Path repo;
	private final Path bndWorkspace;
	private final BndCatalog bndProjects;
	
	static Controller getController() {
		return new Controller(new Config().readOlRepoPath().get());
	}
	
	Controller(Path repo) {
		this.repo = validateRepo(repo);
		this.bndWorkspace = repo.resolve("dev");
		try {
			this.bndProjects = new BndCatalog(bndWorkspace, io, getRepoSettingsDir());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOError(e);
		}
	}

	Controller(String repo) {
		this(Paths.get(repo));
	}
	
	Path getRepo() {
		return repo;
	}
	
    Path getRepoSettingsDir() {
        Path dir = repo.resolve(".lct");
        if (!Files.isDirectory(dir)) {
            io.verifyOrCreateDir("LCT git repository settings directory", dir);
            // make sure the entire contents of the directory are ignored, including the .gitignore
            io.writeFile(".lct git ignore file", dir.resolve(".gitignore"), "*");
        }
        return dir;
    }

	List<String> listBndProjects(String pattern) {
		return bndProjects.findProjects(pattern).map(Controller::toName).collect(toCollection(ArrayList::new));
	}
	
	List<String> listUnimportedProjects(String pattern) {
		List<String> list = listBndProjects(pattern);
		list.removeAll(listProjects());
		return list;
	}

	void findProjectsAndDeps(Stream<String> names, Collection<? super Path> collection, boolean includeUsers) {
		Set<Path> set = bndProjects.findProjects(names).collect(Collectors.toSet());
		// add users first to pick up dependencies of users
		if (includeUsers) addUsers(set);
		addDeps(set);
		// remove already seen projects
		set.removeAll(collection);
		// add unique new projects to original collection
		collection.addAll(set);
	}

	private void addDeps(Set<Path> set) {
		bndProjects.getRequiredProjectPaths(toNames(set)).forEach(set::add);
	}

	private void addUsers(Set<Path> set) {
		bndProjects.getDependentProjectPaths(toNames(set)).forEach(set::add);
	}
	
	Queue<Path> inDependencyOrder(Collection<Path> projects) {
		return bndProjects.inTopologicalOrder(projects.stream()).collect(toCollection(LinkedList::new));
	}
	
    private static Path validateRepo(Path olRepo) throws RuntimeException {
        if (!Files.isDirectory(olRepo)) throw new RuntimeException("Open Liberty repository is not a valid directory: " + olRepo);
        else if (!Files.isDirectory(olRepo.resolve(".git"))) throw new RuntimeException("Open Liberty repository does not appear to be a git repository: " + olRepo);
        else if (!Files.isDirectory(olRepo.resolve("dev"))) throw new RuntimeException("Open Liberty repository does not contain an expected 'dev' subdirectory: " + olRepo);
        return olRepo;
	}
    
    public static Set<String> toNames(Collection<Path> projects) { return asNames(projects).collect(toCollection(TreeSet::new)); }

    public static Stream<String> asNames(Collection<Path> projects) { return projects.stream().map(Controller::toName); }
    
    public static String toName(Path project) { return project.getFileName().toString(); }
}
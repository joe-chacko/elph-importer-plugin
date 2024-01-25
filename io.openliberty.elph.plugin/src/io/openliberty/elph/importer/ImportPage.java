package io.openliberty.elph.importer;

import static io.openliberty.elph.importer.EclipseProjects.importProjects;
import static java.io.File.separator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;

import java.nio.file.Path;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

//import io.openliberty.elph.cmd.ElphCommand;

class ImportPage extends WizardPage {
	private static Table sourceTableVar = null;
	private static boolean draggedToDropTarget = false;
	private static Table projectsTable = null;
	private static Table depsTable = null;
	private static Table usersTable = null;
	Composite page = null;

	ImportPage() {
		super(ImportPage.class.getSimpleName());
		setTitle("Import Projects");
		setDescription("Specify the projects you want to import, "
				+ "and whether you want to import the users of a project as well as its dependencies");
	}

	@Override
	public void createControl(Composite parent) {
		page = new Composite(parent, SWT.NONE);
		setControl(page);
		setPageComplete(false);

		page.setLayout(new GridLayout(3, false));
		Label importLabel = new Label(page, SWT.WRAP);
		importLabel.setText("Please type below the project you would like to import");
		importLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 3, 1));

		Text projectFilterText = new Text(page, SWT.BORDER);
		projectFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		new Label(page, SWT.NONE);
		new Label(page, SWT.NONE);

		projectsTable = createTable(page, "All Projects");

		depsTable = createTable(page, "Import Deps");
		usersTable = createTable(page, "Import Deps+Users");

		projectFilterText.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event event) { filterProjects(projectFilterText.getText()); }
		});


		page.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e)
			{
				filterProjects("");
				page.removePaintListener(this);
			}
		});
	}

	void filterProjects(String pattern) {
		List<String> projects = Controller.getController().listUnimportedProjects("*" + pattern + "*");
		concat(stream(depsTable),stream(usersTable)).forEach(projects::remove);
		updateTableItems(projectsTable, projects);
	}

	public String getTitle() { return "Eclipse Liberty Project Helper"; }

	void importAllProjects() {
		Controller controller = Controller.getController();
		Set<Path> bndProjects = new HashSet<>();
		controller.findProjectsAndDeps(stream(depsTable), bndProjects, false);
		controller.findProjectsAndDeps(stream(usersTable), bndProjects, true);
		importProjects(controller.inDependencyOrder(bndProjects));
	}

	private Stream<String> stream(Table table) {
		return Arrays.stream(table.getItems()).map(TableItem::getText);
	}

	private Table createTable(Composite parent, String title) {
		Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(title);
		col.setResizable(false);

		table.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent arg0) {
				Rectangle rect = table.getClientArea();
				if(rect.width>0) col.setWidth((int) (rect.width*0.98));
			}

			public void controlMoved(ControlEvent arg0) {}
		});
		table.getColumn(0).pack();

		addDragSupport(table);
		addDropSupport(table);
		return table;
	}

	private void updateTableItems(Table table, List<String> projects) {
		table.removeAll();
		Collections.sort(projects);
		for (String project : projects) {   
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(new String[] {project});
		}
		//resize column to be either max table size or max size of text in column
		TableColumn col = table.getColumn(0);
		col.pack();
		Rectangle rect = table.getClientArea();
		if (rect.width > col.getWidth()) col.setWidth((int) (rect.width*0.98));
	}

	private void sortTable(Table table) {
		// record selection
		System.out.println("### table item count " + table.getItemCount());
		Set<String> selected = Stream.of(table.getSelection()).map(TableItem::getText).collect(Collectors.toSet());
		// clear selection
		table.deselectAll();
		// sort all the texts
		Comparator<String> ordering = Collator.getInstance(Locale.getDefault())::compare;
		Set<String> all = Stream.of(table.getItems())
				.map(TableItem::getText)
				.collect(toCollection(() -> new TreeSet<>(ordering)));
		// remove the old entries and re-add them in order
		System.out.println("### table item count " + table.getItemCount());
		Stream.of(table.getItems()).forEach(ti -> ti.dispose());
		System.out.println("### table item count " + table.getItemCount());
		all.forEach(txt -> new TableItem(table, SWT.NONE).setText(txt));
		System.out.println("### table item count " + table.getItemCount());
		// find the previously selected items
		int[] indices = Stream.of(table.getItems())
				.filter(item -> selected.contains(item.getText()))
				.mapToInt(table::indexOf)
				.toArray();
		// re-select the previously selected items
		table.select(indices);
	}

	private void addDragSupport(Table sourceTable) {
		// Allow data to be copied or moved from the drag source
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DragSource source = new DragSource(sourceTable, operations);

		// Provide data in Text format
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);

		source.addDragListener(new DragSourceListener() {
			TableItem[] selection = sourceTable.getSelection();

			public void dragStart(DragSourceEvent event) {
				sourceTableVar = sourceTable;
				//Reset bool which will be used to check if an item has been dropped to another valid drop target
				draggedToDropTarget = false;

				// Only start the drag if there is actually text in the label
				//This text will be what is dropped on the target.
				if (selection.length > 0) {
					if (selection[0].getText(0).length() == 0) event.doit = false;
				}
			}

			public void dragSetData(DragSourceEvent event) {
				// Provide the data of the requested type.
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					TableItem[] selection = sourceTable.getSelection();
					if (selection.length > 0) event.data = Stream.of(selection).map(TableItem::getText).collect(joining(separator));
				}
			}

			public void dragFinished(DragSourceEvent event) {
				// If data has been moved to another valid drop target, remove the data from the source 
				if (event.detail == DND.DROP_MOVE && draggedToDropTarget) {
					sourceTable.remove(sourceTable.getSelectionIndices());
					sourceTable.deselectAll();
				}
				if (depsTable.getItemCount()>0 || usersTable.getItemCount()>0) setPageComplete(true);
				else setPageComplete(false);
			}
		});
	}


	private void addDropSupport(Table targetTable) {
		// Allow data to be copied or moved to the drop target
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(targetTable, operations);

		// Receive data in Text format
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {textTransfer};
		target.setTransfer(types);

		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					//checking whether the DROP_MOVE operation is supported in the current drag-and-drop context
					//and you are not dragging an item to the same table it came from
					if ((event.operations & DND.DROP_MOVE) != 0 && sourceTableVar != targetTable) event.detail = DND.DROP_MOVE;
					else event.detail = DND.DROP_NONE;
				}
			}

			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
				if (textTransfer.isSupportedType(event.currentDataType)) {
					// NOTE: on unsupported platforms this will return null
					Object o = textTransfer.nativeToJava(event.currentDataType);
					String t = (String)o;
					if (t != null) System.out.println(t);
				}
			}

			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_MOVE) != 0) event.detail = DND.DROP_MOVE;
					else event.detail = DND.DROP_NONE;
				}
			}

			public void dragLeave(DropTargetEvent event) {}

			public void dropAccept(DropTargetEvent event) {}

			public void drop(DropTargetEvent event) {
				if (textTransfer.isSupportedType(event.currentDataType)) {
					System.out.println("Dropping data of type " + event.data.getClass());
					String data = (String)event.data;
					if (sourceTableVar != targetTable) {
						draggedToDropTarget = true;
						int[] indices = Stream.of(data.split(separator))
								.mapToInt(this::addItem)
								.toArray();
						targetTable.deselectAll();
						targetTable.select(indices);
						sortTable(targetTable);
					} else event.detail = DND.DROP_COPY;
				}
			}

			private int addItem(String text) {
				TableItem item = new TableItem(targetTable, SWT.NONE);
				item.setText(text);
				return targetTable.indexOf(item);
			}

		});
	}
}
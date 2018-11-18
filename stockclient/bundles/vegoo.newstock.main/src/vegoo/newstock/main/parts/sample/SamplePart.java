package vegoo.newstock.main.parts.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.nebula.widgets.xviewer.edit.DefaultXViewerControlFactory;
import org.eclipse.nebula.widgets.xviewer.edit.XViewerControlFactory;
import org.eclipse.nebula.widgets.xviewer.edit.XViewerConverter;
import org.eclipse.nebula.widgets.xviewer.edit.XViewerMultiEditAdapter;
import vegoo.newstock.main.parts.images.MyImageCache;
import vegoo.newstock.main.parts.model.ISomeTask;
import vegoo.newstock.main.parts.model.ISomeTask.RunDb;
import vegoo.newstock.main.parts.model.ISomeTask.TaskType;
import vegoo.newstock.main.parts.model.SomeTask;
import org.eclipse.nebula.widgets.xviewer.util.XViewerDisplay;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SamplePart {

	private Text txtInput;
	private TableViewer tableViewer;

	private static MyXViewer myXviewer;

	@Inject private MPart part;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		
		createXviewer(parent);
	}
	
	
	private void createXviewer(Composite parent) {
	      Composite toolBarComposite = new Composite(parent, SWT.NONE);
	      // bComp.setBackground(mainSComp.getDisplay().getSystemColor(SWT.COLOR_CYAN));
	      toolBarComposite.setLayout(new GridLayout(2, false));
	      toolBarComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		  myXviewer = new MyXViewer(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
	      myXviewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
	      myXviewer.setContentProvider(new MyXViewerContentProvider());
	      myXviewer.setLabelProvider(new MyXViewerLabelProvider(myXviewer));
	      // myXviewer.setLabelProvider(new MyXViewerStyledTextLabelProvider(myXviewer));

	      // XViewerEditAdapter
	      // XViewerControlFactory cFactory = new DefaultXViewerControlFactory();
	      // XViewerConverter converter = new MyXViewerConverter();
	      // myXviewer.setXViewerEditAdapter(new XViewerMultiEditAdapter(cFactory, converter));

	      // createTaskActionBar(toolBarComposite);

	      List<Object> tasks = new ArrayList<Object>();
	      for (int x = 0; x < 1; x++) {
	         tasks.addAll(getTestTasks());
	      }
	      
	      
	      /**
	       * Note: setInputXViewer must be called instead of setInput for XViewer to operate properly
	       marketManager.getStocks()
	       */
	      myXviewer.setInputXViewer(tasks);		
	}

	@Focus
	public void setFocus() {
		// tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		part.setDirty(false);
	}
	
	   public static void createTaskActionBar(Composite parent) {

		      Composite actionComp = new Composite(parent, SWT.NONE);
		      actionComp.setLayout(new GridLayout());
		      actionComp.setLayoutData(new GridData(GridData.END));

		      ToolBar toolBar = new ToolBar(actionComp, SWT.FLAT | SWT.RIGHT);
		      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		      toolBar.setLayoutData(gd);

		      ToolItem refreshItem = new ToolItem(toolBar, SWT.PUSH);
		      refreshItem.setImage(MyImageCache.getImage("refresh.gif"));
		      refreshItem.setToolTipText("Refresh");
		      refreshItem.addSelectionListener(new SelectionAdapter() {
		         @Override
		         public void widgetSelected(SelectionEvent e) {
		            List<Object> tasks = new ArrayList<Object>();
		            for (int x = 0; x < 1; x++) {
		               tasks.addAll(getTestTasks());
		            }
		            /**
		             * Note: setInputXViewer must be called instead of setInput for XViewer to operate properly
		             */
		            myXviewer.setInputXViewer(tasks);
		         }
		      });

		      Action dropDownAction = myXviewer.getCustomizeAction();
		      new ActionContributionItem(dropDownAction).fill(toolBar, 0);

		      ToolItem descriptionItem = new ToolItem(toolBar, SWT.PUSH);
		      descriptionItem.setImage(MyImageCache.getImage("descriptionView.gif"));
		      descriptionItem.setToolTipText("Show Description View");
		      descriptionItem.addSelectionListener(new SelectionAdapter() {
		         @Override
		         public void widgetSelected(SelectionEvent e) {
		            myXviewer.getCustomizeMgr().loadCustomization(MyDefaultCustomizations.getDescriptionCustomization());
		            myXviewer.refresh();
		         }
		      });

		      ToolItem completeItem = new ToolItem(toolBar, SWT.PUSH);
		      completeItem.setImage(MyImageCache.getImage("completionView.gif"));
		      completeItem.setToolTipText("Show Completion View");
		      completeItem.addSelectionListener(new SelectionAdapter() {
		         @Override
		         public void widgetSelected(SelectionEvent e) {
		            myXviewer.getCustomizeMgr().loadCustomization(MyDefaultCustomizations.getCompletionCustomization());
		            myXviewer.refresh();
		         }
		      });

		      ToolItem refreshSingleColumn = new ToolItem(toolBar, SWT.PUSH);
		      refreshSingleColumn.setImage(MyImageCache.getImage("columnRefresh.gif"));
		      refreshSingleColumn.setToolTipText("Example of Refreshing a Single Column");
		      refreshSingleColumn.addSelectionListener(new SelectionAdapter() {
		         @Override
		         public void widgetSelected(SelectionEvent e) {
		            @SuppressWarnings("unchecked")
		            List<Object> items = (List<Object>) myXviewer.getInput();
		            for (Object item : items) {
		               SomeTask task = (SomeTask) item;
		               task.setTaskType(TaskType.Refreshed);
		            }

		            String columnId = MyXViewerFactory.Task_Type.getId();
		            myXviewer.refreshColumn(columnId);
		         }
		      });

		   }	
	   private static Date date = new Date();

	   private static Date getDate() {
	      date = new Date(date.getTime() + (60000 * 60 * 2));
	      return date;
	   }

	   private static List<ISomeTask> getTestTasks() {
	      List<ISomeTask> tasks = new ArrayList<ISomeTask>();
	      SomeTask task =
	         new SomeTask(RunDb.Test_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test1", "10:03", "run to test this",
	            "Suite A", "mark@eclipse.com", 50, 50000);
	      tasks.add(task);

	      for (int x = 0; x < 5; x++) {
	         task.addChild(new SomeTask(RunDb.Test_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test33", "10:03",
	            "run to test isit this - child " + x, "Suite A", "mark@eclipse.com", 50, 9223336854775807L));
	      }

	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Data_Exchange, getDate(), "org.eclipse.osee.test2", "9:22",
	         "run to test that", "Suite B", "john@eclipse.com", 0, 50000L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test4", "8:23",
	         "in this world", "Suite A", "john@eclipse.com", 50, 50000L));
	      tasks.add(new SomeTask(RunDb.Test_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test3", "23:01",
	         "now is the time", "Suite B", "mike@eclipse.com", 30, 9223372036854775807L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Db_Health, getDate(), "org.eclipse.osee.test5", "7:32",
	         "may be never", "Suite A", "steve@eclipse.com", 10, 50000L));
	      tasks.add(new SomeTask(RunDb.Test_Db, TaskType.Data_Exchange, getDate(), "org.eclipse.osee.test14", "6:11", "",
	         "Suite A", "steve@eclipse.com", 95, 50000L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test6", "5:13",
	         "run to test this", "Suite B", "john@eclipse.com", 80, 50000L));
	      tasks.add(new SomeTask(RunDb.Test_Db, TaskType.Db_Health, getDate(), "org.eclipse.osee.test12", "23:15", "",
	         "Suite A", "mike@eclipse.com", 90, 50000L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test13", "4:01",
	         "run to test this", "Suite B", "steve@eclipse.com", 100, 50000L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Data_Exchange, getDate(), "org.eclipse.osee.test11", "3:16",
	         "run to test this", "Suite A", "steve@eclipse.com", 53, 50000000000L));
	      tasks.add(new SomeTask(RunDb.Test_Db, TaskType.Backup, getDate(), "org.eclipse.osee.test10", "5:01",
	         "run to test this", "Suite C", "mike@eclipse.com", 0, 50000L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Data_Exchange, getDate(), "org.eclipse.osee.test9", "4:27",
	         "run to test this", "Suite C", "steve@eclipse.com", 90, 50000L));
	      tasks.add(new SomeTask(RunDb.Production_Db, TaskType.Regression, getDate(), "org.eclipse.osee.test7", "2:37",
	         "run to test this", "Suite C", "john@eclipse.com", 20, 50000L));
	      int num = 10;
	      for(int j=0;j<200;++j) {
	      for (String str : Arrays.asList("Now", "Cat", "Dog", "Tree", "Bike", "Sun", "Moon", "Grass", "Can", "Car",
	         "Truck", "Block", "Earth", "Mars", "Venus", "Requirements visualization", "Requirements management",
	         "Feature management", "Modeling", "Design", "Project Management", "Change management",
	         "Configuration Management", "Software Information Management", "Build management", "Testing",
	         "Release Management", "Software Deployment", "Issue management", "Monitoring and reporting", "Workflow")) {
	         
	    	  tasks.add(new SomeTask(RunDb.Test_Db, TaskType.Db_Health, getDate(), "org.eclipse.osee." + str, "24:" + num++,
	            str + " will run to test this", "Suite C" + num++, str.toLowerCase().replaceAll(" ", ".") + "@eclipse.com",
	            20, 340000));
	      }
	      }
	      return tasks;
	   }	
	
	   
	   
}
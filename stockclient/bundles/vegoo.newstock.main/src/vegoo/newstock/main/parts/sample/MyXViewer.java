package vegoo.newstock.main.parts.sample;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.nebula.widgets.xviewer.XPromptChange;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import vegoo.newstock.main.parts.model.ISomeTask;
import vegoo.newstock.main.parts.model.SomeTask;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Example extension of XViewer.
 * 
 * @author Donald G. Dunne
 */
public class MyXViewer extends XViewer {
   private final Set<ISomeTask> runList = new HashSet<ISomeTask>();

   public MyXViewer(Tree tree) {
      super(tree, new MyXViewerFactory());
   }

   public MyXViewer(Shell shell_1, int i) {
      super(shell_1, i, new MyXViewerFactory());
   }

   public MyXViewer(Composite parent, int i) {
      super(parent, i, new MyXViewerFactory());
   }

   public boolean isScheduled(ISomeTask autoRunTask) {
      return true;
   }

   public boolean isRun(ISomeTask autoRunTask) {
      return runList.contains(autoRunTask);
   }

   public void setRun(ISomeTask autoRunTask, boolean run) {
      if (run) {
         runList.add(autoRunTask);
      } else {
         runList.remove(autoRunTask);
      }
   }

   @Override
   public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
      if (treeColumn.getData().equals(MyXViewerFactory.Run_Col)) {
         setRun((ISomeTask) treeItem.getData(), !isRun((ISomeTask) treeItem.getData()));
         update(treeItem.getData(), null);
         return true;
      } else {
         return super.handleLeftClickInIconArea(treeColumn, treeItem);
      }

   }

   @Override
   public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
      if (treeColumn.getData().equals(MyXViewerFactory.Last_Run_Date)) {
         Date promptChangeDate = XPromptChange.promptChangeDate(MyXViewerFactory.Last_Run_Date.getName(), new Date());
         System.out.println("promptChangeDate " + promptChangeDate);
         SomeTask task = (SomeTask) treeItem.getData();
         task.setLastRunDate(promptChangeDate);
         refresh();
      }
      return super.handleAltLeftClick(treeColumn, treeItem);
   }

}

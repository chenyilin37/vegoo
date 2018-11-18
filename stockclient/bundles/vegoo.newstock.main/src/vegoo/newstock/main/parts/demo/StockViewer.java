package vegoo.newstock.main.parts.demo;

import java.util.Date;

import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XPromptChange;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import vegoo.newstock.main.parts.model.ISomeTask;
import vegoo.newstock.main.parts.model.SomeTask;

public class StockViewer extends XViewer {

	private String dataCategory;

	public StockViewer(Composite parent, int style) {
		super(parent, style, new StockViewerFactory());
	}

	public void setDataCategory(String dataCategory) {
		this.dataCategory = dataCategory;
		this.refresh();
	}

	public String getDataCategory() {
		return dataCategory;
	}

	@Override
	public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
		/*
		 * if (treeColumn.getData().equals(MyXViewerFactory.Run_Col)) {
		 * setRun((ISomeTask) treeItem.getData(), !isRun((ISomeTask)
		 * treeItem.getData())); update(treeItem.getData(), null); return true; } else {
		 */
		return super.handleLeftClickInIconArea(treeColumn, treeItem);
//      }

	}

	@Override
	public boolean handleAltLeftClick(TreeColumn treeColumn, TreeItem treeItem) {
		/*
		 * if (treeColumn.getData().equals(MyXViewerFactory.Last_Run_Date)) { Date
		 * promptChangeDate =
		 * XPromptChange.promptChangeDate(MyXViewerFactory.Last_Run_Date.getName(), new
		 * Date()); System.out.println("promptChangeDate " + promptChangeDate); SomeTask
		 * task = (SomeTask) treeItem.getData(); task.setLastRunDate(promptChangeDate);
		 * refresh(); }
		 */ return super.handleAltLeftClick(treeColumn, treeItem);
	}

}

package vegoo.newstock.main.parts.demo;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.widgets.xviewer.IXViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerFactory;
import org.eclipse.nebula.widgets.xviewer.XViewerSorter;
import org.eclipse.nebula.widgets.xviewer.XViewerTreeReport;
import org.eclipse.nebula.widgets.xviewer.core.model.CustomizeData;
import org.eclipse.nebula.widgets.xviewer.core.model.SortDataType;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerAlign;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.nebula.widgets.xviewer.customize.IXViewerCustomizations;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;

public class StockViewerFactory extends XViewerFactory {
	public final static String COLUMN_NAMESPACE = "stockviewer";
	// String id, String name, int width, XViewerAlign align, boolean show, SortDataType sortDataType, boolean multiColumnEditable, String description
	public static XViewerColumn No_Col = new XViewerColumn(COLUMN_NAMESPACE + ".no", "No", 80, XViewerAlign.Right, true, SortDataType.Integer, false, null);
	public static XViewerColumn Code_Col = new XViewerColumn(COLUMN_NAMESPACE + ".code", "Code", 200, XViewerAlign.Left, true, SortDataType.String, false, null);
	public static XViewerColumn Name_Col = new XViewerColumn(COLUMN_NAMESPACE + ".name", "Name", 200, XViewerAlign.Left, true, SortDataType.String, false, null);
	public static XViewerColumn Market_Col = new XViewerColumn(COLUMN_NAMESPACE + ".market", "Market", 80, XViewerAlign.Center, true, SortDataType.Integer, false, null);
	public static XViewerColumn GDHS_Col = new XViewerColumn(COLUMN_NAMESPACE + ".gdhs", "GDHS", 80, XViewerAlign.Right, true, SortDataType.Integer, false, null);

	public StockViewerFactory() {
		super(COLUMN_NAMESPACE);
		
		registerColumns(Code_Col,Name_Col,Market_Col,GDHS_Col); //
	}

	@Override
	public boolean isAdmin() {
		
		return false;
	}

}

package vegoo.newstock.main.parts.demo;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.swt.graphics.Image;

import vegoo.newstock.core.bo.Stock;





public class StockViewerLabelProvider extends XViewerLabelProvider {
	private final StockViewer viewer;

	public StockViewerLabelProvider(StockViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
		if (element instanceof String) {
			if (columnIndex == 1) {
				return (String) element;
			} else {
				return "";
			}
		}

		Stock stock = ((Stock) element);
		if (stock == null) {
			return "";
		}
		
		String dataCategory =  viewer.getDataCategory();
		
		
		if (xCol.equals(StockViewerFactory.Code_Col)) {
			return stock.getCode();
		}
		if (xCol.equals(StockViewerFactory.Name_Col)) {
			return stock.getName();
		}
		if (xCol.equals(StockViewerFactory.Market_Col)) {
			return String.valueOf(stock.getMarketId());
		}

		return "unhandled column";
	}

}

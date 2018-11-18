package vegoo.newstock.main.parts.demo;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerStyledTextLabelProvider;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.main.parts.model.ISomeTask;



public class StockViewerStyledTextLabelProvider extends XViewerStyledTextLabelProvider {
	private final StockViewer viewer;
	
	public StockViewerStyledTextLabelProvider(StockViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	public Image getColumnImage(Object element, XViewerColumn xCol, int column) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StyledString getStyledText(Object element, XViewerColumn xCol, int column) throws Exception {
	      if (element instanceof String) {
	          if (column == 1) {
	             return new StyledString((String) element);
	          } else {
	             return new StyledString("");
	          }
	       }
	       Stock stock = ((Stock) element);
	       if (stock == null) {
	          return new StyledString("");
	       }

	       if (xCol.equals(StockViewerFactory.Code_Col)) {
		          return new StyledString(stock.getCode(), StyledString.DECORATIONS_STYLER);
		       }
	       if (xCol.equals(StockViewerFactory.Name_Col)) {
		          return new StyledString(stock.getName(), StyledString.DECORATIONS_STYLER);
		       }
	       if (xCol.equals(StockViewerFactory.Market_Col)) {
	          return new StyledString(String.valueOf(stock.getMarketId()), StyledString.DECORATIONS_STYLER);
	       }
	       
	       //if (xCol.equals(StockViewerFactory.GDHS_Col)) {
	       //   return new StyledString(stock.getDataOfGDHS(0), StyledString.DECORATIONS_STYLER);
	       //}
	       
	       return new StyledString("unhandled column");	}

	@Override
	public Color getBackground(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception {
	       Stock stock = ((Stock) element);
	       if (stock == null) {
	          return null;
	       }
		
	       int market = stock.getMarketId();
	       if(market==1) {
	    	  //return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	       }
	       
		return null;
	}

	@Override
	public Color getForeground(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
	       if (xCol.equals(StockViewerFactory.Code_Col)) {
		          return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
		       }
	       if (xCol.equals(StockViewerFactory.Name_Col)) {
		          return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		       }
	       if (xCol.equals(StockViewerFactory.Market_Col)) {
	          return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	       }
	       
		return null;
	}

	@Override
	public Font getFont(Object element, XViewerColumn viewerColumn, int columnIndex) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}

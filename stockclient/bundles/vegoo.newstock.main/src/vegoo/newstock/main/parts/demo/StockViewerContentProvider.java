package vegoo.newstock.main.parts.demo;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import vegoo.newstock.core.bo.Stock;





public class StockViewerContentProvider implements ITreeContentProvider {


	   protected Collection<Stock> rootSet = new HashSet<Stock>();
	   private final static Object[] EMPTY_ARRAY = new Object[0];

	   public StockViewerContentProvider() {
	      super();
	   }

	   @Override
	   @SuppressWarnings("rawtypes")
	   public Object[] getChildren(Object parentElement) {
	      if (parentElement instanceof Object[]) {
	         return (Object[]) parentElement;
	      }
	      if (parentElement instanceof Collection) {
	         return ((Collection) parentElement).toArray();
	      }
	      //if (parentElement instanceof Stock) {
	      //   return ((SomeTask) parentElement).getChildren().toArray();
	      //}
	      return EMPTY_ARRAY;
	   }

	   @Override
	   public Object getParent(Object element) {
	      return null;
	   }

	   @Override
	   public boolean hasChildren(Object element) {
	      //if (element instanceof Stock) {
	      //   return false;
	      //}
	      return false;
	   }

	   @Override
	   public Object[] getElements(Object inputElement) {
	      if (inputElement instanceof String) {
	         return new Object[] {inputElement};
	      }
	      return getChildren(inputElement);
	   }

	   @Override
	   public void dispose() {
	      // do nothing
	   }

	   @Override
	   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	      // do nothing
	   }

	   public Collection<Stock> getRootSet() {
	      return rootSet;
	   }

}

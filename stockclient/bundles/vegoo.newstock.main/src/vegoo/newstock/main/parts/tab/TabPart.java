package vegoo.newstock.main.parts.tab;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TabPart {
    private Table table;
    private Tree tree;
    Composite compsoite1;
    Group treeGroup;
    
	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		CTabFolder tabFolder = new CTabFolder(parent,SWT.BORDER);
	       tabFolder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				System.out.println("widgetSelected "+e.getSource());
				switchControl(tabFolder.getItem( tabFolder.getSelectionIndex()));
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("widgetDefaultSelected "+e.getSource());
				switchControl(tabFolder.getItem( tabFolder.getSelectionIndex()));
				
			}});
	        
	        CTabItem tabItem1 = new CTabItem(tabFolder,SWT.NONE);
	        tabItem1.setText("第一页");
	        
	        
	        CTabItem tabItem2 = new CTabItem(tabFolder,SWT.NONE);
	        tabItem2.setText("第二页");

	        createComposite(tabFolder);
	        
	        tabFolder.setSelection(tabItem2);
	        tabItem2.setControl(compsoite1);
	}

	private void switchControl(CTabItem cTabItem) {
		cTabItem.setControl(compsoite1);
		treeGroup.setText(cTabItem.getText());
	}
	
	private void createComposite(CTabFolder tabFolder) {
        compsoite1 = new Composite(tabFolder,SWT.NONE);
        
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        compsoite1.setLayout(layout);
         treeGroup = new Group(compsoite1, SWT.NONE);
        treeGroup.setText("Tree");
		
	}
	

}

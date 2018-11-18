package vegoo.newstock.main.parts.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vegoo.newstock.core.bo.Block;
import vegoo.newstock.core.bo.Stock;
import vegoo.newstock.core.services.StockDataService;



public class DemoPart {
	private static final Logger logger = LoggerFactory.getLogger(DemoPart.class);
	private static final String[] DATACATEGORIES   = {"GDHS","JGCG","ZH"};
	private static final String[] DATATITLES = {"股东户数","机构持股","大股东持股"};// "行情数据","财务数据","资金流","龙虎榜"	
	
	private Composite stockViewer_Composite;
	private CTabFolder tabsOfBlock;
	private CTabFolder tabsOfData;
	
	//private CLabel title;
	
	@Inject StockDataService dataService;
	
	private static StockViewer stockViewer;
	
	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		//title = new CLabel(stockViewer_Composite, SWT.NONE);
		//title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
        createTabsOfBlock(new String[] {"BK_HSAG", "BK_ZXB", "BK_CYB"}, parent);
        
        createTabsOfData(tabsOfBlock);
		
        createStockViewer(tabsOfData);
		
        //tabsOfBlock.setSelection(tabItem2);
        //tabItem2.setControl(stockViewer_Composite);		
	}
	
	private void createTabsOfData(Composite parent) {
		tabsOfData = new CTabFolder(parent,SWT.NONE);
		tabsOfData.setLayout(new GridLayout(1, false));
		tabsOfData.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabsOfData.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				switchControlOfDataTabs(tabsOfData.getItem( tabsOfData.getSelectionIndex()));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				switchControlOfDataTabs(tabsOfData.getItem( tabsOfData.getSelectionIndex()));
				
			}});
		
		for(int i=0;i<DATATITLES.length;++i) {
	        CTabItem tabItem1 = new CTabItem(tabsOfData, SWT.NONE);

	        tabItem1.setText(DATATITLES[i]);
	        tabItem1.setData(DATACATEGORIES[i]);	        
		}
	}

	private void switchControlOfDataTabs(CTabItem item) {
		stockViewer.setDataCategory((String)item.getData());
		item.setControl(stockViewer_Composite);
	}
	
	private void createTabsOfBlock(String[] blocks, Composite parent) {
		tabsOfBlock = new CTabFolder(parent,SWT.NONE);
		tabsOfBlock.setLayout(new GridLayout(1, false));
		tabsOfBlock.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabsOfBlock.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				switchControlOfBlockTabs(tabsOfBlock.getItem( tabsOfBlock.getSelectionIndex()));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				switchControlOfBlockTabs(tabsOfBlock.getItem( tabsOfBlock.getSelectionIndex()));
				
			}});
		
		for(String blk : blocks) {
			createTabItemForBlock(blk, tabsOfBlock);
		}
	}
	
	private void createTabItemForBlock(String blkCode, CTabFolder tabFolder) {
        CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
        Block blk = dataService.getBlock(blkCode);
        if(blk==null) {
        	logger.error("{} block no found!");
        	return;
        }
        tabItem1.setText(blk.getName());
        tabItem1.setData(blkCode);	        
	}

	private void switchControlOfBlockTabs(CTabItem cTabItem) {
		cTabItem.setControl(tabsOfData);
		//title.setText(cTabItem.getText());
		// refresh Data
		Collection<Stock> stocks = dataService.getStocksOfBlock((String)cTabItem.getData());
		stockViewer.setInputXViewer(stocks);		
	}

	private void createStockViewer(CTabFolder tabFolder) {
		stockViewer_Composite = new Composite(tabFolder, SWT.BORDER);
		stockViewer_Composite.setLayout(new GridLayout(1, false));
		stockViewer_Composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		stockViewer = new StockViewer(stockViewer_Composite, SWT.NONE);
		stockViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		// tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		stockViewer.setContentProvider(new StockViewerContentProvider());
		// stockViewer.setLabelProvider(new StockViewerLabelProvider(stockViewer));
		stockViewer.setLabelProvider(new StockViewerStyledTextLabelProvider(stockViewer));

	      // XViewerEditAdapter
	      // XViewerControlFactory cFactory = new DefaultXViewerControlFactory();
	      // XViewerConverter converter = new MyXViewerConverter();
	      // myXviewer.setXViewerEditAdapter(new XViewerMultiEditAdapter(cFactory, converter));

	      // createTaskActionBar(toolBarComposite);

	      /**
	       * Note: setInputXViewer must be called instead of setInput for XViewer to operate properly
	       */
	}

}

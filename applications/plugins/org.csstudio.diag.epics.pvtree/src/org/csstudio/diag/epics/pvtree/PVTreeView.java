package org.csstudio.diag.epics.pvtree;


import org.csstudio.platform.model.IProcessVariable;
import org.csstudio.platform.ui.internal.dataexchange.ProcessVariableDragSource;
import org.csstudio.platform.ui.internal.dataexchange.ProcessVariableDropTarget;
import org.csstudio.util.swt.ComboHistoryHelper;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/** Eclipse ViewPart for the EPICS PV Tree.
 */
public class PVTreeView extends ViewPart
{
    public static final String ID = PVTreeView.class.getName();

    // Memento tags
    private static final String PV_TAG = "pv"; //$NON-NLS-1$
    private static final String PV_LIST_TAG = "pv_list"; //$NON-NLS-1$
    
    private IMemento memento;
    
    /** The root PV name. */
    private Combo pv_name;
    
    private PVTreeModel model;

    private TreeViewer viewer;
    
    /** Allows 'zoom in' and then going back up via context menu. */
    private DrillDownAdapter drillDownAdapter;

    private ComboHistoryHelper pv_name_helper;
    
    /** ViewPart interface, keep the memento. */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException
    {
        super.init(site, memento);
        this.memento = memento;
    }
    
    /** ViewPart interface, persist state */
    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        memento.putString(PV_TAG, pv_name.getText());
    }

    /** Create the GUI. */
    public void createPartControl(Composite parent)
    {
        GridLayout gl = new GridLayout();
        gl.numColumns = 2;
        parent.setLayout(gl);
        GridData gd;
        
        Label l = new Label(parent, SWT.LEFT);
        l.setText("PV:");
        gd = new GridData();
        l.setLayoutData(gd);
        
        pv_name = new Combo(parent, SWT.LEFT);
        pv_name.setToolTipText("Enter PV name, press <RETURN>");
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        pv_name.setLayoutData(gd);
        pv_name_helper =
            new ComboHistoryHelper(Plugin.getDefault().getDialogSettings(),
                                   PV_LIST_TAG, pv_name)
        {
            public void newSelection(String new_pv_name)
            {   setPVName(new_pv_name); }
        };
        
        Tree tree = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        gd = new GridData();
        gd.horizontalSpan = gl.numColumns;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = SWT.FILL;
        tree.setLayoutData(gd);

        viewer = new TreeViewer(tree);
        drillDownAdapter = new DrillDownAdapter(viewer);
        model = new PVTreeModel(viewer);
        viewer.setContentProvider(model);
        viewer.setLabelProvider(new PVTreeLabelProvider());
        viewer.setInput(getViewSite());

        new ProcessVariableDragSource(viewer.getTree(), viewer);
        new ProcessVariableDropTarget(viewer.getTree())
        {
            /** @see org.csstudio.data.exchange.ProcessVariableDropTarget#handleDrop(java.lang.String) */
            @Override
            public void handleDrop(IProcessVariable name,
                                   DropTargetEvent event)
            {
                setPVName(name.getName());
            }
        };
        
        
        // Stop the press when we're no more
        pv_name.addDisposeListener(new DisposeListener()
        {
            public void widgetDisposed(DisposeEvent e)
            {
                model.dispose();
                pv_name_helper.saveSettings();
            }
        });
        hookContextMenu();
        
        // Populate PV list
        pv_name_helper.loadSettings();
        
        if (memento != null)
        {
            String pv_name = memento.getString(PV_TAG);
            if (pv_name.length() > 0)
                setPVName(pv_name);
        }
    }

    /** Set initial focus. */
    public void setFocus()
    {
        pv_name.setFocus();
    }
    
    /** Final cleanup. */
    @Override
    public void dispose()
    {
        model.dispose();
        super.dispose();
    }

    /** Update the tree with information for a newly entered PV name. */
    public void setPVName(String new_pv_name)
    {
        if (! pv_name.getText().equals(new_pv_name))
            pv_name.setText(new_pv_name);
        model.setRootPV(new_pv_name);
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                PVTreeView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void fillContextMenu(IMenuManager manager)
    {
        //manager.add(new Separator());        
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }
}

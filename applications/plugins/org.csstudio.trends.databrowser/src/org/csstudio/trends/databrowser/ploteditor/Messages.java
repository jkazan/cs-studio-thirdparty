package org.csstudio.trends.databrowser.ploteditor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.trends.databrowser.ploteditor.messages"; //$NON-NLS-1$

    public static String OpenArchiveView;

    public static String OpenAsView;
    public static String OpenConfigView;
    public static String OpenPerspective;
    public static String OpenSampleView;
    public static String OpenExportView;
    public static String RemoveMarkers;
    public static String RemoveMarkers_TT;
    public static String SaveBrowserConfig;
    static
    {   // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {}
}

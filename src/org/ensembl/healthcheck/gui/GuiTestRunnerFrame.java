/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.ensembl.healthcheck.CallbackTarget;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportLine;
import org.ensembl.healthcheck.TestRegistry;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.ConnectionPool;

/**
 * The main display frame for GuiTestRunner.
 */
public class GuiTestRunnerFrame extends JFrame implements CallbackTarget {

    private GuiTestRunner guiTestRunner;

    private static final Dimension PANEL_SIZE = new Dimension(600, 650);

    private JLabel statusLabel;

    private Map testButtonInfoWindows = new HashMap();

    private TestProgressDialog testProgressDialog;

    private int testsRun = 0;

    // -------------------------------------------------------------------------
    /**
     * Creates new form GuiTestRunnerFrame
     * 
     * @param gtr The GuiTestRunner that is associated with this Frame.
     * @param testRegistry The test registry to use.
     * @param databaseRegistry The database registry to use.
     */
    public GuiTestRunnerFrame(GuiTestRunner gtr, TestRegistry testRegistry, DatabaseRegistry databaseRegistry) {

        initComponents(testRegistry, databaseRegistry);
        this.guiTestRunner = gtr;

    }

    // -------------------------------------------------------------------------
    /**
     * Initialise the GUI
     */
    private void initComponents(TestRegistry testRegistry, DatabaseRegistry databaseRegistry) {

        // ----------------------------
        // Frame

        setSize(PANEL_SIZE);

        try {
            UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("EnsEMBL HealthCheck");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) {

                exit();

            }
        });

        //setIconImage(new ImageIcon(this.getClass().getResource("e-logo-small.gif")).getImage());

        // ----------------------------
        // Top panel - title

        JPanel topPanel = new JPanel();
        JLabel titleLabel = new JLabel();
        topPanel.setBackground(new Color(255, 255, 255));
        titleLabel.setFont(new Font("SansSerif", 1, 18));
        titleLabel.setText("HealthCheck");
        //titleLabel.setIcon(new ImageIcon(this.getClass().getResource("e-logo.gif")));
        topPanel.add(titleLabel);

        // ----------------------------
        // Centre panel - tests and databases
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        centrePanel.setBackground(Color.WHITE);

        // Panel to hold list of tests
        JPanel testPanel = new JPanel();
        testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.Y_AXIS));
        testPanel.setBackground(new Color(255, 255, 255));
        testPanel.setBorder(new TitledBorder("Select tests"));
        final TestTabbedPane testTabbedPane = new TestTabbedPane(testRegistry);
        testPanel.add(testTabbedPane);

        // Panel to hold database selection tabs
        JPanel databasePanel = new JPanel();
        databasePanel.setBackground(new Color(255, 255, 255));
        databasePanel.setBorder(new TitledBorder("Select databases"));
        final DatabaseTabbedPane databaseTabbedPane = new DatabaseTabbedPane(databaseRegistry);
        databasePanel.add(databaseTabbedPane);

        centrePanel.add(testPanel);
        centrePanel.add(databasePanel);

        // -----------------------------
        // Bottom panel - buttons & status bar
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.WHITE);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(153, 153, 255));
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Dialog", 0, 12));
        statusLabel.setText("Status ...");
        statusPanel.add(statusLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        final GuiTestRunnerFrame localGTRF = this;
        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                DatabaseRegistryEntry[] selectedDatabases = databaseTabbedPane.getSelectedDatabases();
                EnsTestCase[] selectedTests = testTabbedPane.getSelectedTests();
                guiTestRunner.runAllTests(selectedTests, selectedDatabases, localGTRF);

            }
        });

        JButton settingsButton = new JButton("Settings ...");
        settingsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                openSettingsDialog();
            }
        });

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                exit();

            }
        });

        buttonPanel.add(runButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(50, 0)));
        buttonPanel.add(settingsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(50, 0)));
        buttonPanel.add(quitButton);

        bottomPanel.add(buttonPanel);
        bottomPanel.add(statusPanel);

        // ----------------------------
        // Add basic panels to content pane
        Container contentPane = getContentPane();
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centrePanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        pack();

        // ----------------------------
        // Create progress window
        //testProgressDialog = new TestProgressDialog("Running tests", "", 0, 100);
        TestProgressDialogThread tpdThread = new TestProgressDialogThread();
        tpdThread.start();
        testProgressDialog = tpdThread.getDialog();

    }

    // -------------------------------------------------------------------------

    private void exit() {

        ConnectionPool.closeAll();
        testProgressDialog.dispose();
        dispose();
        System.exit(0);

    }

    // -------------------------------------------------------------------------
    /**
     * Update the status bar.
     * 
     * @param s The status.
     */
    public void setStatus(String s) {

        statusLabel.setText(s);

    } // setStatus

    // -------------------------------------------------------------------------
    /**
     * Implementation of CallbackTarget; update the relevant part of the GUI when a message is
     * received from the logger.
     * 
     * @param logRecord The log record to display.
     */
    public void callback(LogRecord logRecord) {

        setStatus(logRecord.getMessage());

    } // callback

    // -------------------------------------------------------------------------
    /**
     * Open the window that allows the user to set various parameters.
     */
    private void openSettingsDialog() {

        Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
        Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        setCursor(waitCursor);
        GuiTestRunnerSettings gtrs = new GuiTestRunnerSettings(this, guiTestRunner, true);
        gtrs.show();
        setCursor(defaultCursor);

    } // openSettingsDialog

    // -------------------------------------------------------------------------
    /**
     * Append a log record to the info window.
     * 
     * @param logRecord The record to append.
     */
    private void updateTestInfoWindow(LogRecord logRecord) {

        // try and figure out which window to update
        String loggingClass = logRecord.getSourceClassName();

        TestInfoWindow infoWindow = (TestInfoWindow) testButtonInfoWindows.get(loggingClass);
        if (infoWindow != null) {
            infoWindow.append(logRecord.getMessage() + "\n");
        }

    } // updateTestRunnerWindow

    // -------------------------------------------------------------------------
    /**
     * Set the text of a particular info window.
     * 
     * @param testClassName The name of the class sending the info. Used to decide which window to
     *            update.
     * @param report The String to add.
     */
    public void setTestInfoWindowText(String testClassName, String report) {

        TestInfoWindow infoWindow = (TestInfoWindow) testButtonInfoWindows.get(testClassName);
        if (infoWindow != null) {
            infoWindow.setText(report);
        }

    } // setTestInfoWindowText

    // -------------------------------------------------------------------------
    /**
     * Set the text of a particular info window.
     * 
     * @param testClassName The name of the class sending the info. Used to decide which window to
     *            update.
     * @param lines A List of Strings to add to the window.
     */
    public void setTestInfoWindowText(String testClassName, List lines) {

        TestInfoWindow infoWindow = (TestInfoWindow) testButtonInfoWindows.get(testClassName);
        if (infoWindow != null) {
            Iterator it = lines.iterator();
            while (it.hasNext()) {
                ReportLine line = (ReportLine) it.next();
                infoWindow.append(line.getDatabaseName() + ": " + (String) line.getMessage() + "\n");
            }
        }

    } // setTestInfoWindowText

    // -------------------------------------------------------------------------
    /**
     * Return the output level as set in the parent GuiTestRunner.
     * 
     * @return The output level.
     */
    public int getOutputLevel() {

        return guiTestRunner.getOutputLevel();

    } // getOutputLevel

    // -------------------------------------------------------------------------
    /**
     * Set the total number of tests that are to be run in this session so that the progress dialog
     * can update itself.
     * 
     * @param total The maximum number of tests that will be run.
     */
    public void setTotalToRun(int total) {

        testProgressDialog.setMaximum(total);

    }

    // -------------------------------------------------------------------------
    /**
     * Increase the number of tests that have been run so that the progress bar can be updated.
     * 
     * @param i The amount by which to increment the count.
     */
    public void incrementNumberRun(int i) {

        testsRun += i;

    }

    //  -------------------------------------------------------------------------
    /**
     * Update the test progress window with a new note.
     * 
     * @param s The string to display in the note area.
     */
    public void updateProgressDialog(String s) {

        if (!testProgressDialog.isVisible()) {
            testProgressDialog.setVisible(true);
            testProgressDialog.toFront();
        }

        testProgressDialog.setNote(s);

    } // updateProgressDialog

    //  -------------------------------------------------------------------------
    /**
     * Update the test progress window based on the internal count of tests run.
     */
    public void updateProgressDialog() {

        if (!testProgressDialog.isVisible()) {
            testProgressDialog.setVisible(true);
            testProgressDialog.toFront();
        }

        testProgressDialog.setProgress(testsRun);

    } // updateProgressDialog

    // -------------------------------------------------------------------------
    /**
     * Control whether the test progress dialog is displayed.
     * 
     * @param v Whether or not the progress dialog is displayed.
     */
    public void setTestProgressDialogVisibility(boolean v) {

        testProgressDialog.setVisible(v);
        testProgressDialog.repaint();

    }

    // -------------------------------------------------------------------------
    /**
     * Force the test progress dialog to show 100% completion.
     */
    public void setTestProgressDone() {

        testProgressDialog.setProgress(testProgressDialog.getMaximum());

    }

    // -------------------------------------------------------------------------
    /**
     * Repaint the test progress dialog.
     */
    public void repaintTestProgressDialog() {

        testProgressDialog.repaint();

    }

    // -------------------------------------------------------------------------

} // GuiTestRunnerFrame

// -------------------------------------------------------------------------
/**
 * ActionListener implementation to open a test info window.
 */

class TestInfoWindowOpener implements ActionListener {

    private TestInfoWindow infoWindow;

    public TestInfoWindowOpener(TestInfoWindow infoWindow) {

        this.infoWindow = infoWindow;

    } // TestInfoWindowOpener

    public void actionPerformed(ActionEvent e) {

        infoWindow.setVisible(!infoWindow.isVisible()); // toggle

    }

} // TestInfoWindowOpener

// -------------------------------------------------------------------------
/**
 * A class that extends JTabbedPane and provides a method for getting all the selected databases.
 * Also highlights currently-selected tab.
 */

class DatabaseTabbedPane extends JTabbedPane {

    public DatabaseTabbedPane(DatabaseRegistry databaseRegistry) {

        setBackground(Color.WHITE);

        DatabaseRegistryEntry[] entries = databaseRegistry.getAll();
        Map checkBoxMap = new HashMap();
        for (int i = 0; i < entries.length; i++) {
            DatabaseCheckBox dbcb = new DatabaseCheckBox(entries[i], false);
            checkBoxMap.put(entries[i], dbcb);
        }

        DatabaseType[] types = databaseRegistry.getTypes();
        Arrays.sort(types, new DatabaseTypeGUIComparator());
        for (int i = 0; i < types.length; i++) {
            List checkBoxesForTab = new ArrayList();
            for (int j = 0; j < entries.length; j++) {
                if (entries[j].getType() == types[i]) {
                    checkBoxesForTab.add((DatabaseCheckBox) checkBoxMap.get(entries[j]));
                }
            }
            addTab(types[i].toString(), new DatabaseListPanel(checkBoxesForTab));
        }

        setBackgroundAt(0, Color.LIGHT_GRAY);

        addChangeListener(new TabChangeListener());

    }

    // -------------------------------------------------------------------------

    public DatabaseRegistryEntry[] getSelectedDatabases() {

        List result = new ArrayList();

        // get all the selected databases for each tab in turn
        for (int i = 0; i < getTabCount(); i++) {

            DatabaseListPanel dblp = (DatabaseListPanel) getComponentAt(i);
            DatabaseRegistryEntry[] panelSelected = dblp.getSelected();
            for (int j = 0; j < panelSelected.length; j++) {
                result.add(panelSelected[j]);
            }
        }

        return (DatabaseRegistryEntry[]) result.toArray(new DatabaseRegistryEntry[result.size()]);

    } // getSelectedDatabases

    // -------------------------------------------------------------------------

} // DatabaseTabbedPane

// -------------------------------------------------------------------------
/**
 * Highlight the currently-selected tab of a JTabbedPane.
 */

class TabChangeListener implements ChangeListener {

    public void stateChanged(ChangeEvent e) {

        JTabbedPane jtp = (JTabbedPane) e.getSource();
        int sel = jtp.getSelectedIndex();
        for (int i = 0; i < jtp.getTabCount(); i++) {
            jtp.setBackgroundAt(i, (i == sel ? Color.LIGHT_GRAY : Color.WHITE));
            // TODO - set font?
        }

    }

} // tabChangeListener

// -------------------------------------------------------------------------
/**
 * A class that creates a panel (in a JScrollPane) containing a list of DatabseCheckBoxes, and
 * provides methods for accessing the selected ones.
 */

class DatabaseListPanel extends JScrollPane {

    private List checkBoxes;

    public DatabaseListPanel(List checkBoxes) {

        this.checkBoxes = checkBoxes;

        setPreferredSize(new Dimension(300, 500));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        Iterator it = checkBoxes.iterator();
        while (it.hasNext()) {
            JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            checkBoxPanel.setBackground(Color.WHITE);
            checkBoxPanel.add((DatabaseCheckBox) it.next());
            panel.add(checkBoxPanel);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton toggleAllButton = new JButton("Toggle all");
        final DatabaseListPanel localDBLP = this;
        toggleAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                localDBLP.toggleAll();

            }
        });

        buttonPanel.add(toggleAllButton);
        panel.add(Box.createVerticalGlue());
        panel.add(buttonPanel);

        setViewportView(panel);
    }

    // -------------------------------------------------------------------------
    /**
     * Get the databases that are selected on this panel.
     * 
     * @return the selected databases.
     */
    public DatabaseRegistryEntry[] getSelected() {

        List selected = new ArrayList();

        Iterator it = checkBoxes.iterator();
        while (it.hasNext()) {
            DatabaseCheckBox dbcb = (DatabaseCheckBox) it.next();
            if (dbcb.isSelected()) {
                selected.add(dbcb.getDatabase());
            }
        }

        return (DatabaseRegistryEntry[]) selected.toArray(new DatabaseRegistryEntry[selected.size()]);

    } // getSelected

    // -------------------------------------------------------------------------
    /**
     * Toggle the selections of the databases in this panel
     */
    public void toggleAll() {

        Iterator it = checkBoxes.iterator();
        while (it.hasNext()) {
            DatabaseCheckBox dbcb = (DatabaseCheckBox) it.next();
            dbcb.toggle();

        }

    } // toggleAll

    // -------------------------------------------------------------------------

} // DatabaseListPanel

// -------------------------------------------------------------------------
/**
 * A JCheckBox that stores a reference to a DatabaseRegistryEntry.
 */

class DatabaseCheckBox extends JCheckBox {

    private DatabaseRegistryEntry database;

    public DatabaseCheckBox(DatabaseRegistryEntry database, boolean selected) {

        super(database.getName(), selected);
        this.database = database;
        setBackground(Color.WHITE);

    }

    /**
     * @return Returns the database.
     */
    public DatabaseRegistryEntry getDatabase() {

        return database;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabase(DatabaseRegistryEntry database) {

        this.database = database;

    }

    /**
     * Toggle the selected state of this checkbox.
     */
    public void toggle() {

        setSelected(!isSelected());

    }

} // DatabaseCheckBox

//-------------------------------------------------------------------------
/**
 * A class that extends JTabbedPane to show test information. Also highlights currently-selected
 * tab.
 */

class TestTabbedPane extends JTabbedPane {

    public TestTabbedPane(TestRegistry testRegistry) {

        setBackground(Color.WHITE);

        DatabaseType[] types = testRegistry.getTypes();
        Arrays.sort(types, new DatabaseTypeGUIComparator());

        for (int i = 0; i < types.length; i++) {
            addTab(types[i].toString(), new TestListPanel(types[i], testRegistry));
        }

        if (getTabCount() > 0) {
            setBackgroundAt(0, Color.LIGHT_GRAY);
        }

        addChangeListener(new TabChangeListener());

    }

    // -------------------------------------------------------------------------

    public EnsTestCase[] getSelectedTests() {

        List result = new ArrayList();

        // get all the selected tests for each tab in turn
        for (int i = 0; i < getTabCount(); i++) {

            TestListPanel tlp = (TestListPanel) getComponentAt(i);
            EnsTestCase[] panelSelected = tlp.getSelected();
            for (int j = 0; j < panelSelected.length; j++) {
                result.add(panelSelected[j]);
            }
        }

        return (EnsTestCase[]) result.toArray(new EnsTestCase[result.size()]);

    } // getSelectedTests

    // -------------------------------------------------------------------------

} // TestTabbedPane

//-------------------------------------------------------------------------
/**
 * A class that creates a panel (in a JScrollPane) containing tests, and provides methods for
 * accessing the selected ones.
 */

class TestListPanel extends JScrollPane {

    private JTree tree;

    public TestListPanel(DatabaseType type, TestRegistry testRegistry) {

        setPreferredSize(new Dimension(300, 500));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        TestTreeNode top = new TestTreeNode("Select tests:");

        String[] groups = testRegistry.getGroups(type);
        for (int i = 0; i < groups.length; i++) {
            TestTreeNode groupNode = new TestTreeNode(groups[i]);
            EnsTestCase[] testCasesInGroup = testRegistry.getTestsInGroup(groups[i], type);
            for (int j = 0; j < testCasesInGroup.length; j++) {
                groupNode.add(new TestTreeNode(testCasesInGroup[j]));
            }
            top.add(groupNode);
        }

        tree = new JTree(top);
        tree.setRowHeight(0);
        tree.setCellRenderer(new TestTreeCellRenderer());
        tree.addMouseListener(new TestTreeNodeSelectionListener(tree));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        panel.add(tree);

        setViewportView(panel);
    }

    // -------------------------------------------------------------------------

    public EnsTestCase[] getSelected() {

        List selected = new ArrayList();

        TreeModel model = tree.getModel();
        TestTreeNode root = (TestTreeNode) model.getRoot();
        Enumeration children = root.breadthFirstEnumeration();
        while (children.hasMoreElements()) {
            TestTreeNode child = (TestTreeNode) children.nextElement();
            if (!child.isGroup() && child.isSelected()) {
                selected.add(child.getTest());
            }
        }

        return (EnsTestCase[]) selected.toArray(new EnsTestCase[selected.size()]);

    } // getSelected

    // -------------------------------------------------------------------------

} // TestListPanel

// -------------------------------------------------------------------------
/**
 * Custom cell renderer for a tree of groups and tests.
 */

class TestTreeCellRenderer extends JComponent implements TreeCellRenderer {

    private JLabel label;

    private JCheckBox checkBox;

    private Icon slowIcon;

    public TestTreeCellRenderer() {

        slowIcon = null;
        //slowIcon = new ImageIcon(this.getClass().getResource("hourglass-small.gif"));

        label = new JLabel();
        label.setBackground(Color.WHITE);
        label.setHorizontalTextPosition(SwingConstants.LEADING);
        checkBox = new JCheckBox();
        checkBox.setBackground(Color.WHITE);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(checkBox);
        add(label);

    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {

        TestTreeNode node = (TestTreeNode) value;
        if (node != null) {

            checkBox.setSelected(node.isSelected());
            String defaultFontName = label.getFont().getName();
            int defaultFontSize = label.getFont().getSize();

            if (node.isGroup()) {
                label.setText(node.getGroupName());
                label.setFont(new Font(defaultFontName, Font.BOLD, defaultFontSize));
            } else {
                EnsTestCase test = node.getTest();
                label.setText(test.getShortTestName());
                label.setFont(new Font(defaultFontName, Font.PLAIN, defaultFontSize));
                if (test.isLongRunning()) {
                    label.setIcon(slowIcon);
                } else {
                    label.setIcon(null);
                }
            }

        }

        return this;
    }

    public Dimension getPreferredSize() {

        Dimension dim = new Dimension(550, 25);
        return dim;
    }

}

/**
 * Subclass of DefaultMutableTreeNode that tracks whether it's selected or not.
 */

class TestTreeNode extends DefaultMutableTreeNode {

    private boolean selected;

    private EnsTestCase test;

    private String groupName;

    private boolean isGroup;

    public TestTreeNode(Object o) {

        if (o instanceof String) {
            this.groupName = (String) o;
            isGroup = true;
        } else if (o instanceof EnsTestCase) {
            this.test = (EnsTestCase) o;
            isGroup = false;
        }

    }

    public boolean isSelected() {

        return selected;
    }

    public void setSelected(boolean selected) {

        this.selected = selected;
    }

    public void toggle() {

        setSelected(!isSelected());
    }

    public boolean isGroup() {

        return isGroup;

    }

    public String getGroupName() {

        return groupName;
    }

    public EnsTestCase getTest() {

        return test;
    }

}

// -------------------------------------------------------------------------
/**
 * Listener that changes whether or not a node is selected when it is clicked.
 */

class TestTreeNodeSelectionListener extends MouseAdapter {

    private JTree tree;

    private int controlWidth = 20;

    TestTreeNodeSelectionListener(JTree tree) {

        this.tree = tree;
        // TODO - get width of control icons from BasicLookAndFeel and set controlWidth
        // appropriately
    }

    public void mouseClicked(MouseEvent me) {

        if (me.getSource().equals(tree) && me.getX() > controlWidth) { // try to avoid tree
            // expansion
            // controls

            // which node was clicked?
            TestTreeNode node = (TestTreeNode) tree.getLastSelectedPathComponent();
            if (node != null) {

                node.toggle();

                // select/deselect children
                if (!node.isLeaf()) {
                    Enumeration children = node.children();
                    while (children.hasMoreElements()) {
                        TestTreeNode child = (TestTreeNode) children.nextElement();
                        child.setSelected(node.isSelected());
                    }
                }

                // let the tree repaint itself
                tree.repaint();
            }
        }

    }

}

// -------------------------------------------------------------------------
/**
 * Progress dialog displayed when tests are being run.
 */

class TestProgressDialog extends JDialog {

    private JProgressBar progressBar;

    private JLabel messageLabel;

    private JLabel noteLabel;

    public TestProgressDialog(String message, String note, int min, int max) {

        setSize(new Dimension(300, 100));

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        noteLabel = new JLabel(message);
        noteLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        progressBar = new JProgressBar(min, max);

        progressPanel.add(messageLabel);
        progressPanel.add(noteLabel);
        progressPanel.add(progressBar);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(progressPanel);

        //pack();

    }

    public void setMaximum(int max) {

        progressBar.setMaximum(max);

    }

    public int getMaximum() {

        return progressBar.getMaximum();

    }

    public void setProgress(int p) {

        progressBar.setValue(p);

    }

    public void setNote(String s) {

        noteLabel.setText(s);

    }

    public void update(String s, int p) {

        setNote(s);
        setProgress(p);

    }
}
// -------------------------------------------------------------------------
/**
 * Fiddle things so that generic database types are moved to the front.
 */

class DatabaseTypeGUIComparator implements Comparator {

    public int compare(Object o1, Object o2) {

        if (!(o1 instanceof DatabaseType) || !(o2 instanceof DatabaseType)) { throw new RuntimeException(
                "Arguments to DatabaseTypeGUIComparator must be DatabaseType!"); }

        DatabaseType t1 = (DatabaseType) o1;
        DatabaseType t2 = (DatabaseType) o2;

        if (t1.isGeneric() && !t2.isGeneric()) {
            return -1;
        } else if (!t1.isGeneric() && t2.isGeneric()) {
            return 1;
        } else {
            return t1.toString().compareTo(t2.toString());
        }
    }

}
// -------------------------------------------------------------------------

/**
 * A thread to hold the test progress dialog so it doesn't block everything else.
 */

class TestProgressDialogThread extends Thread {

    private TestProgressDialog dialog;

    public TestProgressDialogThread() {

        dialog = new TestProgressDialog("Running tests", "", 0, 100);

    }

    public TestProgressDialog getDialog() {

        return dialog;

    }
}

// -------------------------------------------------------------------------

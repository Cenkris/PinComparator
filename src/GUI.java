import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class GUI extends JFrame {
    private JPanel originalExcelPanel;
    private JPanel excelToCompareToPanel;
    private final JFileChooser FILE_CHOOSER = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
    private JTextField resultsField;
    private JButton compareButton;
    private JPanel addPathsPanel;
    private File originalSelectedFile;
    private File compareToExcelFile;
    private JTextField excelToCompareToField;
    private JTextField originalExcelField;
    private final String EXCEL_TO_COMPARE_TO_STRING = "Compare with...";
    private final String ORIGINAL_STRING = "Please select Excel file";
    private Path createLogPath;

    GUI() {

        initOriginal();
        initExcelToCompareTo();
        initResultButton();
        initPathsPanel();

        JPanel addPathsPanelAndResult = new JPanel(new FlowLayout());
        addPathsPanelAndResult.add(addPathsPanel);
        addPathsPanelAndResult.add(compareButton);
        add(addPathsPanelAndResult);

        initResults();
        initDefaultProperties();
    }

    private void initResults() {
        resultsField = new JTextField("Results");
        resultsField.setEditable(false);
        resultsField.setHorizontalAlignment(SwingConstants.CENTER);
        add(resultsField, BorderLayout.SOUTH);
    }

    private void initResultButton() {
        compareButton = new JButton("Compare");
        compareButton.addActionListener(event -> {
            if (isValidPath()) {
                processFiles();
                resultsField.setText("Comparison finished, log file created!");
                int dialogButton = JOptionPane.showConfirmDialog(null, "Would you like to see the log file now?", "Log", JOptionPane.YES_NO_OPTION);
                if (dialogButton == JOptionPane.NO_OPTION) {
                    dispose();
                }

                if (dialogButton == JOptionPane.YES_OPTION) {
                    File logFile = new File(String.valueOf(createLogPath));
                    try {
                        Desktop.getDesktop().open(logFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dispose();
                }
            }

        });
    }

    private boolean isValidPath() {
        return !(originalExcelField.getText().equals(ORIGINAL_STRING) || excelToCompareToField.getText().equals(EXCEL_TO_COMPARE_TO_STRING));
    }

    private void processFiles() {
        String originalFilePath = originalSelectedFile.getAbsolutePath();
        String compareToFilePath = compareToExcelFile.getAbsolutePath();
        String parentPath = originalSelectedFile.getParent() + "/";

        String originalFileName = originalSelectedFile.getName().split("\\.")[0];
        String comparatorFileName = compareToExcelFile.getName().split("\\.")[0];

        List<String> originalPinNames = PinNames.getPinNames(originalFilePath);
        List<String> compareToPinNames = PinNames.getPinNames(compareToFilePath);

        HashMap<String, Boolean> comparedMap = PinNames.compare(originalPinNames, compareToPinNames);

        try {
            String logName = originalFileName + "_vs_" + comparatorFileName + ".log";
            createLogPath = Paths.get(parentPath + logName);
            Files.deleteIfExists(createLogPath);
            PrintStream printStream = new PrintStream(createLogPath.toString());
            int count = 0;
            for (String pinName : comparedMap.keySet()) {
                if (comparedMap.get(pinName)) {
                    printStream.print("MATCH: ");
                    printStream.println(pinName);
                    count++;
                }
            }

            for (String pinName : comparedMap.keySet()) {
                if (!comparedMap.get(pinName) && originalPinNames.contains(pinName)) {
                    printStream.print("MISSING: ");
                    printStream.println(pinName);
                } else if (!comparedMap.get(pinName) && !originalPinNames.contains(pinName)) {
                    printStream.print("UNMATCHED: ");
                    printStream.println(pinName);
                }
            }

            if(comparedMap.isEmpty()){
                printStream.println("Please input valid files!");
            }
            else if (count == comparedMap.size()) {
                printStream.println();
                printStream.println("All pins match!");
            } else {
                printStream.println();
                printStream.println("Not all pins match!");
            }
            printStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initPathsPanel() {
        addPathsPanel = new JPanel(new GridLayout(2, 1));


        addPathsPanel.add(originalExcelPanel);
        addPathsPanel.add(excelToCompareToPanel);
    }

    private void initExcelToCompareTo() {
        //Panel
        excelToCompareToPanel = new JPanel(new FlowLayout());

        //Field
        excelToCompareToField = new JTextField(EXCEL_TO_COMPARE_TO_STRING);
        excelToCompareToField.setPreferredSize(new Dimension(300, 30));
        excelToCompareToField.setEditable(false);

        //Button
        JButton excelToCompareToButton = new JButton("Add File");
        excelToCompareToButton.addActionListener(event -> compareToExcelFile = selectFileAndUpdateFields(excelToCompareToField));

        excelToCompareToPanel.add(excelToCompareToField);
        excelToCompareToPanel.add(excelToCompareToButton);
    }

    private void initOriginal() {
        //Panel
        originalExcelPanel = new JPanel(new FlowLayout());

        //Field
        originalExcelField = new JTextField(ORIGINAL_STRING);
        originalExcelField.setPreferredSize(new Dimension(300, 30));
        originalExcelField.setMaximumSize(new Dimension(600, 30));
        originalExcelField.setEditable(false);

        //Button
        JButton originalExcelButton = new JButton("Add File");
        originalExcelButton.addActionListener(event -> originalSelectedFile = selectFileAndUpdateFields(originalExcelField));

        originalExcelPanel.add(originalExcelButton);
        originalExcelPanel.add(originalExcelField);
    }

    private File selectFileAndUpdateFields(JTextField originalExcelField) {

        int returnValue = FILE_CHOOSER.showOpenDialog(null);
        File selectedFile = new File("");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = FILE_CHOOSER.getSelectedFile();
            Path selectedFilePath = Paths.get(selectedFile.getAbsolutePath());
            originalExcelField.setText(selectedFilePath.toString());
        }

        return selectedFile;
    }

    private void initDefaultProperties() {
        setResizable(false);
        setTitle("PinComparator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(600, 140);
        setVisible(true);
    }
}

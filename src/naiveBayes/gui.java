package naiveBayes;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.FlowLayout;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.JTextField;

import naiveBayes.NaiveBayes;

public class gui extends JFrame 
{

	private JPanel contentPane;
	private JTextField exportFileLocationTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run() {
				try 
				{
					gui frame = new gui();
					frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public gui() 
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //Set display theme to current system theme
			
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //Default windows theme
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //Linux desktop GTK based theme
		} 
		catch (Exception e) 
		{
			//Ignore
			System.out.println("Cant find built theme, using default");
		}
		
		
		JPanel inputFileBrowsePanel = new JPanel();

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 11, 764, 389);
		contentPane.add(tabbedPane);
		tabbedPane.addTab("Input File Location", null, inputFileBrowsePanel, null);
		inputFileBrowsePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JFormattedTextField importFileLocationTextField = new JFormattedTextField();
		inputFileBrowsePanel.add(importFileLocationTextField);
		importFileLocationTextField.setText(System.getProperty("user.dir") + "\\data\\chronic-kidney-disease.xls");
		importFileLocationTextField.setColumns(50);

		JButton importFileBrowserButton = new JButton("Browse");
		inputFileBrowsePanel.add(importFileBrowserButton);
		
				JButton doImportButton = new JButton("Import");
				inputFileBrowsePanel.add(doImportButton);

		JCheckBox chckbxKeepCopyOfImport = new JCheckBox("Copy file to project data folder?");
		chckbxKeepCopyOfImport.setSelected(true);
		inputFileBrowsePanel.add(chckbxKeepCopyOfImport);

		JPanel metadataViewerPanel = new JPanel();
		tabbedPane.addTab("Metadata", null, metadataViewerPanel, null);
		tabbedPane.setEnabledAt(1, false);
		metadataViewerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		metadataViewerPanel.setEnabled(false);

		JLabel fileStatusLabel = DefaultComponentFactory.getInstance().createLabel("No file has been imported yet");
		metadataViewerPanel.add(fileStatusLabel);
		
		JButton doClassify = new JButton("Classify");
		metadataViewerPanel.add(doClassify);

		JPanel outputFileBrowsePanel = new JPanel();
		tabbedPane.addTab("Output File Location", null, outputFileBrowsePanel, null);

		exportFileLocationTextField = new JTextField();
		outputFileBrowsePanel.add(exportFileLocationTextField);
		exportFileLocationTextField.setText(System.getProperty("user.dir") + "\\results\\chronic-kidney-disease-results.xls");
		exportFileLocationTextField.setColumns(50);

		JButton exportFileBrowserButton = new JButton("Browse");
		outputFileBrowsePanel.add(exportFileBrowserButton);

		JButton doExportButton = new JButton("Export");
		outputFileBrowsePanel.add(doExportButton);
		
		JCheckBox chckbxKeepCopyOfExport = new JCheckBox("Keep copy of file in project results folder?");
		chckbxKeepCopyOfExport.setSelected(true);
		outputFileBrowsePanel.add(chckbxKeepCopyOfExport);
		importFileBrowserButton.addActionListener(new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{ 
				JFileChooser fileBrowser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel 97-2003 Worksheet (*.xls)", "xls");

				File startDir = new File(importFileLocationTextField.getText());
				if(startDir.exists())
				{
					fileBrowser.setCurrentDirectory(startDir);
				}
				fileBrowser.setFileFilter(filter);
				int returnVal = fileBrowser.showOpenDialog(getParent());
				if(returnVal == JFileChooser.APPROVE_OPTION) 
				{
					String tempString = fileBrowser.getSelectedFile().getAbsolutePath();
					File tempFile = new File(tempString);
					if(tempFile.exists() && tempString.substring(tempString.lastIndexOf(".")+1).compareTo("xls") == 0)
					{
						importFileLocationTextField.setText(fileBrowser.getSelectedFile().getAbsolutePath());
						if(tempString.contains((System.getProperty("user.dir") + "\\data")))//If we are in the data folder
						{
							tempString = System.getProperty("user.dir") + "\\results" + tempString.substring(tempString.lastIndexOf("\\"));
						}
						else
						{
							tempString = importFileLocationTextField.getText() + tempString.substring(0, tempString.lastIndexOf(".")); 
						}
						exportFileLocationTextField.setText(tempString.substring(0, tempString.lastIndexOf(".")) + "-results.xls");
					}
				}
			}
		});
		exportFileBrowserButton.addActionListener(new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{ 
				JFileChooser fileBrowser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel 97-2003 Worksheet (*.xls)", "xls");

				File startDir = new File(exportFileLocationTextField.getText().substring(0, exportFileLocationTextField.getText().lastIndexOf("\\") + 1));
				if(startDir.exists())
				{
					fileBrowser.setCurrentDirectory(startDir);
				}
				fileBrowser.setFileFilter(filter);
				int returnVal = fileBrowser.showOpenDialog(getParent());
				if(returnVal == fileBrowser.APPROVE_OPTION) 
				{
					String tempString = fileBrowser.getSelectedFile().getAbsolutePath();
					if(tempString.substring(tempString.lastIndexOf(".")+1).compareTo("xls") != 0)
					{
						tempString += ".xls";
					}
				}
			}
		});
		
		doImportButton.addActionListener(new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				NaiveBayes.readExcelFile(importFileLocationTextField.getText());
				
				String fileLocation = importFileLocationTextField.getText();
				String newFileLocation = System.getProperty("user.dir") + "\\data\\" + fileLocation.substring(fileLocation.lastIndexOf("\\") + 1);
				
				if(chckbxKeepCopyOfImport.getSelectedObjects() != null && fileLocation.compareTo(newFileLocation) == 0); //If checkbox is checked and the location was not the data folder
				{
					File locationOfFileToCopy = new File(fileLocation);
					File locationToCopyFile = new File(newFileLocation);
					try 
					{
						Files.copy(locationOfFileToCopy.toPath(), locationToCopyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} 
					catch (IOException e1) 
					{
						System.out.println("Unable to copy file to data directory");
						e1.printStackTrace();
					}
				}
			}
		});
		
		doExportButton.addActionListener(new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				NaiveBayes.generateTrainingDataStride(NaiveBayes.dataLL.size()/2); //TODO Allow selection of training generator and size (as a fraction)
				NaiveBayes.generateClassifier();
				NaiveBayes.generateClassifications();
				NaiveBayes.writeExcelFile(exportFileLocationTextField.getText());
				

				String fileLocation = exportFileLocationTextField.getText();
				String newFileLocation = System.getProperty("user.dir") + "\\results\\" + fileLocation.substring(fileLocation.lastIndexOf("\\") + 1);
				if(chckbxKeepCopyOfExport.getSelectedObjects() != null && fileLocation.compareTo(newFileLocation) == 0); //If checkbox is checked and the location was not the data folder
				{
					File locationOfFileToCopy = new File(fileLocation);
					File locationToCopyFile = new File(newFileLocation);
					try 
					{
						Files.copy(locationOfFileToCopy.toPath(), locationToCopyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} 
					catch (IOException e1) 
					{
						System.out.println("Unable to copy file to data directory");
						e1.printStackTrace();
					}
				}
			}
		});
		
		doClassify.addActionListener(new ActionListener() 
		{ 
			public void actionPerformed(ActionEvent e) 
			{
			}
		});
	}
}

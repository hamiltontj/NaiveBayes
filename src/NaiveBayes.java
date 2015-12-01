import java.io.*;
import java.util.LinkedList;

import org.apache.poi.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

public class NaiveBayes 
{ 
	static LinkedList<String[]> testDataLL  = new LinkedList<String[]>();
	static LinkedList<String[]> trainingDataLL  = new LinkedList<String[]>();
	static LinkedList<String[]> dataLL = new LinkedList<String[]>();
	
	static LinkedList<String[]> metadataLL = new LinkedList<String[]>();
	static LinkedList<String> classificationsLL = new LinkedList<String>();
	
	//static int classCount = 0; //Made dynamic so it could potentially handle jagged data i.e. sentence classification
	static int classificationLocation = -1;
	static LinkedList<String>  classificationTypes = new LinkedList<String>();
	static LinkedList<Double>  classificationLikelihood = new LinkedList<Double>();

	static LinkedList<String> knownClassifications = new LinkedList<String>();
	static LinkedList<String> guessedClassifications = new LinkedList<String>();
	static LinkedList<String> actualClassifications = new LinkedList<String>();
	
	static LinkedList<LinkedList<String[]>> classifier = new LinkedList<LinkedList<String[]>>();
	
	static final int METADATASIZE = 3; //Size is defined by number of rows in data explanation sheet
	//Current metadata order is Data name, Data type (discrete or continuous), and a label for which is the classifier
	
	public static void readExcelFile(String fileName)
	{
		FileInputStream file;
		try 
		{
			file = new FileInputStream(new File(fileName));
			Workbook excelFile = new HSSFWorkbook(file);
			
			Sheet sheet1 = excelFile.getSheetAt(0);//Data sheet
			//Set just incase metadata is incomplete or malformed
			classificationLocation = sheet1.getRow(0).getPhysicalNumberOfCells() - 1; //Minus one since classificationLocation includes 0 and getPhysicalNumberOfCells does not
			
			Sheet sheet2 = excelFile.getSheetAt(1); //Metadata sheet
			//Loop based on number of attribute names
			for(int i = 0; i < sheet2.getRow(0).getPhysicalNumberOfCells(); i++)
			{
				String[] metadata = new String[METADATASIZE];
				
				//Construct metadata
				Row currRow = sheet2.getRow(0); //This should be a row of names
				metadata[0] = currRow.getCell(i).toString();
				currRow = sheet2.getRow(1); //This should be a row of data types (discrete or continuous) 
				metadata[1] = currRow.getCell(i).toString();
				currRow = sheet2.getRow(2); //This should say which one is the classifier
				if(currRow.getCell(i) == null || currRow.getCell(i).getCellType() == Cell.CELL_TYPE_BLANK )
				{
					metadata[2] = "attribute";					
				}
				else
				{
					metadata[2] = "classifier";
					classificationLocation = i;
				}
				
				metadataLL.add(metadata);
			}
			
			for(Row row : sheet1)
			{
				String data[] = new String[row.getPhysicalNumberOfCells() - 1];
				int offset = 0; //Used so that we can declare an array of the size of the attributes without the classification
				for(Cell cell: row)
				{
					int index = cell.getColumnIndex();
					if(classificationLocation != index)
					{
						data[index - offset] = cell.toString();
					}
					else
					{
						classificationsLL.add(cell.toString());
						
						//Moved to generate training data so that we do not get possible classifications from unknown data since some denote unknown by saying ?
						
//						//Check to see if we have seen it yet
//
//						int occurrences = 0;
//						for(int i = 0; i < classificationTypes.size(); i++)
//						{
//							if(classificationTypes.get(i).compareTo(cell.toString()) == 0)
//							{
//								occurrences++;
//							}
//						}
//						if(occurrences == 0)
//						{
//							classificationTypes.add(cell.toString());
//						}
						offset++;
					}
				}
				dataLL.add(data);
				//classCount = temp.length;
			}

		
			
			excelFile.close();
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("Error file not found");
			System.exit(0);
		}
		catch (IOException e) 
		{
			System.out.println("Unable to read file, disk drive may be failing");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void generateTrainingData(int locationIndex)
	{
		trainingDataLL.add(testDataLL.get(locationIndex));
		knownClassifications.add(actualClassifications.get(locationIndex));

		//Check to see if we have seen that classification yet
		int occurrences = 0;
		for(int i = 0; i < classificationTypes.size() && occurrences == 0; i++)
		{
			if(classificationTypes.get(i).compareTo(actualClassifications.get(locationIndex)) == 0)
			{
				occurrences = 1;
			}
		}
		if(occurrences == 0)
		{
			classificationTypes.add(actualClassifications.get(locationIndex));
		}
		
		testDataLL.remove(locationIndex);
		actualClassifications.remove(locationIndex);
	}
	
	public static void generateTrainingDataRandom(int trainingDataSize)
	{
		testDataLL = (LinkedList<String[]>) dataLL.clone();
		actualClassifications = (LinkedList<String>) classificationsLL.clone();
		
		for(int i = 0; i < trainingDataSize; i++)
		{
			int index = (int)(Math.random()*testDataLL.size());
			generateTrainingData(index);
		}
	}
	public static void generateTrainingDataFirst(int trainingDataSize)
	{
		testDataLL = (LinkedList<String[]>) dataLL.clone();
		actualClassifications = (LinkedList<String>) classificationsLL.clone();
		
		for(int i = 0; i < trainingDataSize; i++)
		{
			generateTrainingData(0);
		}
	}
	public static void generateTrainingDataStride(int trainingDataSize)
	{
		testDataLL = (LinkedList<String[]>) dataLL.clone();
		actualClassifications = (LinkedList<String>) classificationsLL.clone();

		int removalCount = 0;
		
		for(int i = 0; i < trainingDataSize; i++)
		{
			double index = i * ((double)dataLL.size()/(double)trainingDataSize);
			generateTrainingData((int)(Math.round(index)- removalCount));
			
			removalCount++;
		}
	}
	public static void generateTrainingDataFromFile(String fileLocation) //Requires that the original file had the metadata and requires that this file is formated the same in first sheet
	{
		testDataLL = (LinkedList<String[]>) dataLL.clone();
		actualClassifications = (LinkedList<String>) classificationsLL.clone();		
		
		FileInputStream file;
		try 
		{
			file = new FileInputStream(new File(fileLocation));
			Workbook excelFile = new HSSFWorkbook(file);
			Sheet sheet1 = excelFile.getSheetAt(0);//Data sheet
			for(Row row : sheet1)
			{
				String data[] = new String[row.getPhysicalNumberOfCells() - 1];
				String classification = "";
				
				int offset = 0; //Used so that we can declare an array of the size of the attributes without the classification
				for(Cell cell: row)
				{
					int index = cell.getColumnIndex();
					if(classificationLocation != index)
					{
						data[index - offset] = cell.toString();
					}
					else
					{
						classification = cell.toString();
						offset++;
					}
				}
				
				
				//Even though data and classifications are not really used add it onto the end so it is still complete for in the event they end up being used in a later version
				dataLL.add(data);
				classificationsLL.add(classification);
				
				trainingDataLL.add(data);
				knownClassifications.add(classification);
				
				//Check to see if we have seen that classification yet
				int occurrences = 0;
				for(int i = 0; i < classificationTypes.size() && occurrences == 0; i++)
				{
					if(classificationTypes.get(i).compareTo(classification) == 0)
					{
						occurrences = 1;
					}
				}
				if(occurrences == 0)
				{
					classificationTypes.add(classification);
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("Error file not found");
			System.exit(0);
		}
		catch (IOException e) 
		{
			System.out.println("Unable to read file, disk drive may be failing");
			e.printStackTrace();
			System.exit(0);
		}
	}	
	//TODO implement more trainingData generators if time allows
	
	public static void generateClassifierNaiveBayes(int attribute)
	{
		LinkedList<String> knownInstances = new LinkedList<String>();//Keep all seen attributes in here
		int totalFrequency[] = new int[classificationTypes.size()]; //Keep the total seen of each classification for use later but is more efficent to calculate inside the instance counting loop
		
		//Loop on nodes of each attribute
		for(int node = 0; node < trainingDataLL.size(); node++)
		{
			//Get value of current node
			String currNode = trainingDataLL.get(node)[attribute];				
			
			int loc = -1;
			for(int i = 0; i < knownInstances.size(); i++)
			{
				if(knownInstances.get(i).compareTo(currNode) == 0)
				{
					loc = i;
				}
			}
			
			//If no values of this attribute are known
			if(loc == -1)
			{
				String[] temp = new String[classificationTypes.size() + 1]; //Array to store frequency
				temp[0] = currNode; //Label array
				knownInstances.add(currNode); //Add label to the currently known instances
				
				//Build frequency counts since this is first pass values can be assume 0 if not equal to current classification and 1 if it is
				for(int i = 0; i < classificationTypes.size(); i++)
				{
					if(classificationTypes.get(i).compareTo(knownClassifications.get(node)) == 0)
					{
						temp[i+1] = "1";
						totalFrequency[i]++;
					}
					else
					{
						temp[i+1] = "0";
					}
				}
				classifier.get(attribute).add(temp); //Add built array to current attribute
			}
			else
			{
				for(int i = 0; i < knownInstances.size(); i++)
				{
					if(classifier.get(attribute).get(i)[0].compareTo(currNode) == 0)
					{
						for(int j = 0; j < classificationTypes.size(); j++)
						{
							if(classificationTypes.get(j).compareTo(knownClassifications.get(node)) == 0)
							{
								classifier.get(attribute).get(i)[j+1] = Integer.toString(1 + Integer.parseInt(classifier.get(attribute).get(i)[j+1]));
								totalFrequency[j]++;
							}
						}
					}
				}
			}
		}
		
		//Now more efficient just do one pass through the frequencies once everything is tallied to convert from counts to percents (helpful hint: Each attribute for each classification if you add up all occurrences percents it should equal 1 or roughly 1 since precision in computers)
		for(int classification = 0; classification < classificationTypes.size(); classification++) //Loop on 
		{			
			for(int occurrences = 0; occurrences < classifier.get(attribute).size(); occurrences++)
			{
				int currFrequency = Integer.parseInt(classifier.get(attribute).get(occurrences)[classification+1]); //Plus 1 to offset for attribute name value being in position 0
				classifier.get(attribute).get(occurrences)[classification + 1] = Double.toString((double)currFrequency/(double)totalFrequency[classification]); //Plus 1 to offset for attribute name value being in position 0
			}
		}
	}
	public static void generateClassifier()
	{
		//First linked list has a node for every attribute
		//Second linked list has a node for every instance of a value for that attribute
		//The String array is of the length of the number of possible classification outcomes plus 1 and 
			//contains a count of the number of times that attribute happened for each possible classification 
			//in all but the first row which contains labels
		
		//Set up the the top level of classifier to be the same size as the number of attributes
			//Note this will not accept jagged arrays as those would have a single total
		for(int i = 0; i < trainingDataLL.get(0).length; i++)
		{
			classifier.add(new LinkedList<String[]>());
		}
		
		//Loop on attribute even though looping on node would be more efficient due to less cache misses this project is not meant for big data anyways and it is much more readable and comprehensible this way anyways if you want a headache you can flip it around
		for(int attribute = 0; attribute < classifier.size(); attribute++)
		{
			generateClassifierNaiveBayes(attribute);
		}
		
		//This should work with either classifier type since classification types need to be seen before
		for(int classification = 0; classification < classificationTypes.size(); classification++) //Loop on 
		{
			int currClassificationCount = 0;
			for(int i = 0; i < knownClassifications.size(); i++)
			{
				if(classificationTypes.get(classification).compareTo(knownClassifications.get(i)) == 0)
				{
					currClassificationCount++;
				}
			}			
			classificationLikelihood.add((double)currClassificationCount/(double)knownClassifications.size()); //Stored for later use to decide which to use in the event of a tie (Which is unlikely unless an unknown attribute value if found in the data and then both will be 0)
		}
	}
	
	//TODO make this call a different class if it encounters a continuous data type attribute and split discrete to be its own function as well.
	public static String classify(String[] node)
	{
		double[] classificationScores = new double[classificationTypes.size()]; 
		
		for(int classification = 0; classification < classificationTypes.size(); classification++) //Loop on 
		{
			double classificationScore = 1;//Init to 1 since using a *=  (That would have saved me 15 minutes of debugging why my ints seemed to not be casting and why my outputs were always the first classification)		
			for(int attribute = 0; attribute < classifier.size(); attribute++)
			{
				for(int occurrences = 0; occurrences < classifier.get(attribute).size(); occurrences++)
				{				
					if(classifier.get(attribute).get(occurrences)[0].compareTo(node[attribute]) == 0)//Check if current occurrence string is the same as the curr attr string 
					{
						classificationScore *= Double.parseDouble(classifier.get(attribute).get(occurrences)[classification+1]);
					}
				}
			}
			
			classificationScores[classification] = classificationScore*classificationLikelihood.get(classification); //Before setting multiply by likelyhood of classification (cuurClassificationCount/totalClassifications) 	
		}

		int selectedClassificationLocation = -1;
		double largestClassificationScore = -1;
		for(int classification = 0; classification < classificationScores.length; classification++)
		{
			if(classificationScores[classification] > largestClassificationScore)
			{
				largestClassificationScore = classificationScores[classification];
				selectedClassificationLocation = classification;
			}
			else if(classificationScores[classification] == largestClassificationScore) //Unlikely but may happen if an unknown attribute value if found in the data and then both will be 0 (Now tested with that and does perform as expected)
			{
				if(classificationLikelihood.get(classification) > classificationLikelihood.get(selectedClassificationLocation))
				{
					selectedClassificationLocation = classification;
					//TODO flag that this happened
				}
			}
		}
		
		return classificationTypes.get(selectedClassificationLocation);
	}
	public static void generateClassifications()
	{
		//Compute all possible classifications
		for(int i = 0; i < testDataLL.size(); i++)
		{
			guessedClassifications.add(classify(testDataLL.get(i)));
		}
	}
	
	public static void printLinkedListData(LinkedList<String[]> data)
	{
		System.out.print("[");
		for(int i = 0; i < data.size(); i++)
		{
			String[] currNode = data.get(i);
			for(int j = 0; j < currNode.length; j++)
			{
				System.out.print(currNode[j] + ":");
			}
			System.out.print(", ");
		}
		System.out.print("]\n");
	}
	public static void printLinkedListDataWithClassification(LinkedList<String[]> data, LinkedList<String> classification)
	{
		System.out.print("[");
		for(int i = 0; i < data.size(); i++)
		{
			String[] currNode = data.get(i);
			for(int j = 0; j < currNode.length; j++)
			{
				System.out.print(currNode[j] + ":");
			}			
			System.out.print(":" + classification.get(i) + ", ");
		}
		System.out.print("]\n");
	}
	public static void printLinkedListClassifier(LinkedList<LinkedList<String[]>> classifier)
	{
		System.out.print("[\n");
		for(int i = 0; i < classifier.size(); i++)
		{
			System.out.print("[" + metadataLL.get(i)[0] + "::");
			for(int j = 0; j < classifier.get(i).size(); j++)
			{
				String[] currNode = classifier.get(i).get(j);
				for(int k = 0; k < currNode.length; k++)
				{
					System.out.print(currNode[k] + ":");
				}
				System.out.print(", ");
			}
			System.out.print("],\n");
		}
		System.out.print("]\n");
	}
	public static void printClassification(LinkedList<String> classification)
	{
		System.out.print("[");
		for(int i = 0; i < classification.size(); i++)
		{		
			System.out.print(classification.get(i) + ", ");
		}
		System.out.print("]\n");		
	}
	
	public static void printClassificationTypes()
	{
		System.out.print("[");
		for(int i = 0; i < classificationTypes.size(); i++)
		{		
			System.out.print(classificationTypes.get(i) + ", ");
		}
		System.out.print("]\n");		
	}
	
	public static void outputExcelFile(String fileLocation, String optionalSecondaryfileLocation)
	{
		outputExcelFile(fileLocation);
		outputExcelFile(optionalSecondaryfileLocation);
	}
	
	public static void outputExcelFile(String fileLocation)
	{
		try 
		{
			FileOutputStream fileOut = new FileOutputStream(fileLocation);
			
			HSSFWorkbook workbook = new HSSFWorkbook(); 
			Sheet worksheet = workbook.createSheet("Results");
			
		    Font bold = workbook.createFont();//Create font
		    bold.setBoldweight(Font.BOLDWEIGHT_BOLD);//Make font bold	
			
			CellStyle correctCell = workbook.createCellStyle();
			correctCell.setFillForegroundColor(HSSFColor.GREEN.index);	
			correctCell.setFillBackgroundColor(HSSFColor.GREEN.index);
			correctCell.setFillPattern(CellStyle.SOLID_FOREGROUND);
			
			CellStyle incorrectCell = workbook.createCellStyle();
			incorrectCell.setFillForegroundColor(HSSFColor.RED.index);
			incorrectCell.setFillBackgroundColor(HSSFColor.RED.index);
			incorrectCell.setFillPattern(CellStyle.SOLID_FOREGROUND);	

			CellStyle classificationCells = workbook.createCellStyle();
			classificationCells.setFillForegroundColor(HSSFColor.YELLOW.index);
			classificationCells.setFillBackgroundColor(HSSFColor.YELLOW.index);	
			classificationCells.setFillPattern(CellStyle.SOLID_FOREGROUND);
			
			CellStyle attributeNameCells = workbook.createCellStyle();
			attributeNameCells.setFont(bold);

			CellStyle classificationAttributeCell = workbook.createCellStyle();
			classificationAttributeCell.setFillForegroundColor(HSSFColor.YELLOW.index);
			classificationAttributeCell.setFillBackgroundColor(HSSFColor.YELLOW.index);
			classificationAttributeCell.setFillPattern(CellStyle.SOLID_FOREGROUND);
			classificationAttributeCell.setFont(bold);

			Row currRow = worksheet.createRow(0);
			for(int attribute = 0; attribute < metadataLL.size() + 1; attribute++)
			{
				Cell currCell = currRow.createCell(attribute);	
				if(attribute < metadataLL.size())
				{	
					currCell.setCellValue(metadataLL.get(attribute)[0]);
					if(metadataLL.get(attribute)[2].compareTo("classifier") == 0)
					{
						currCell.setCellStyle(classificationAttributeCell);
					}
					else
					{
						currCell.setCellStyle(attributeNameCells);
					}
				}
				else
				{
					currCell.setCellValue("Guessed Classification");
					currCell.setCellStyle(attributeNameCells);
				}
			}			
			
			for(int node = 0; node < testDataLL.size(); node++)
			{
				currRow = worksheet.createRow(node + 1); //Offset by one since first row is header data
				int classifierCompleted = 0; //Necessary for if data does not end in classifier
				for(int attribute = 0; attribute < metadataLL.size() + 1; attribute++)
				{
					Cell currCell = currRow.createCell(attribute);	
					
					
					if(attribute < metadataLL.size())
					{								
						if(metadataLL.get(attribute)[2].compareTo("classifier") == 0)
						{
							currCell.setCellValue(actualClassifications.get(node));
							currCell.setCellStyle(classificationCells);
							classifierCompleted++;
						}
						else
						{
							currCell.setCellValue(testDataLL.get(node)[attribute - classifierCompleted]);
						}
					}
					else
					{
						currCell.setCellValue(guessedClassifications.get(node));
						if(guessedClassifications.get(node).compareTo(actualClassifications.get(node)) == 0)
						{
							currCell.setCellStyle(correctCell);
						}
						else
						{
							currCell.setCellStyle(incorrectCell);
						}
					}
				}
			}
		
			worksheet = workbook.createSheet("Training Data");
			currRow = worksheet.createRow(0);
			for(int attribute = 0; attribute < metadataLL.size(); attribute++)
			{
				Cell currCell = currRow.createCell(attribute);	
				currCell.setCellValue(metadataLL.get(attribute)[0]);
				currCell.setCellStyle(attributeNameCells);
			}	
			
			for(int node = 0; node < trainingDataLL.size(); node++)
			{
				currRow = worksheet.createRow(node + 1); //Offset by one since first row is header data
				int classifierCompleted = 0; //Necessary for if data does not end in classifier
				for(int attribute = 0; attribute < metadataLL.size(); attribute++)
				{
					Cell currCell = currRow.createCell(attribute);	
								
					if(metadataLL.get(attribute)[2].compareTo("classifier") == 0)
					{
						currCell.setCellValue(knownClassifications.get(node));
						classifierCompleted++;
					}
					else
					{
						currCell.setCellValue(trainingDataLL.get(node)[attribute - classifierCompleted]);
					}
				}
			}
			
			worksheet = workbook.createSheet("Likelihood");
			currRow = worksheet.createRow(0);
			
			int largestAttributeSize = 0;
			
			for(int attribute = 0; attribute < classifier.size(); attribute++)
			{
				if(classifier.get(attribute).size() > largestAttributeSize)
				{
					largestAttributeSize = classifier.get(attribute).size();
				}
			}
			
			//Label attributes along the top
			for(int i = 0; i < metadataLL.size(); i++)
			{
				if(i == 0)
				{
					Cell currCell = currRow.createCell(i);	
					//currCell.setCellValue("Attributes");
					currCell.setCellStyle(attributeNameCells);
				}
				else
				{
					Cell currCell = currRow.createCell(i);
					currCell.setCellValue(metadataLL.get(i-1)[0]); //-1 since the first cell does not contain a attribute name
					currCell.setCellStyle(attributeNameCells);
				}
			}	
			
			
			for(int i = 0; i < (largestAttributeSize * (classificationTypes.size() + 1) + classificationTypes.size() + 1); i++)	//+1 since the first row of each stride lists each attributes string of what occurrence the likelihoods are displaying
			{																													//+classificationTypes.size() so we can list the classification types likelihood at the end
				currRow = worksheet.createRow(i + 1); //+1 since first row is attribute names
				Cell currCell = currRow.createCell(0);
				
				int currentClassificationType = i % (classificationTypes.size()+1); //+1 since the first row of each stride lists each attributes string of what occurrence the likelihoods are displaying
				
				//List the classification type of each row along the side
				if(i < largestAttributeSize * (classificationTypes.size() + 1)) //+1 since the first row of each stride lists each attributes string of what occurrence the likelihoods are displaying
				{
					for(int j = 0; j < classificationTypes.size() + 1; j++) //+1 since the first row of each stride lists each attributes string of what occurrence the likelihoods are displaying
					{
						if(currentClassificationType == 0)
						{
							//Do nothing for now may have it say value later
						}
						else if (currentClassificationType == j)
						{
							currCell.setCellValue(classificationTypes.get(j-1)); //-1 since the first cell does not contain a classification type
						}
					}
				}
				else //List the classification likelihood of each row along the side
				{
					
					for(int j = 0; j < classificationTypes.size() + 1; j++) //+1 since the first row of each stride lists each attributes string of what occurrence the likelihoods are displaying
					{
						if(currentClassificationType == 0)
						{
							//Do nothing for now may have it say value later
						}
						else if (currentClassificationType == j)
						{
							currCell.setCellValue("Likelihood of: " + classificationTypes.get(j-1) + " is " + classificationLikelihood.get(j-1)); //-1 since the first cell does not contain a classification type
						}
					}
				}
				currCell.setCellStyle(attributeNameCells);
			}
			
			//List the data
			for(int i = 0; i < classifier.size(); i++)
			{
				for(int j = 0; j < classifier.get(i).size(); j++)
				{
					String[] currNode = classifier.get(i).get(j);
					for(int k = 0; k < currNode.length; k++)
					{
						currRow = worksheet.getRow((j*largestAttributeSize + k)+1); //+1 since first row is attribute names
						Cell currCell = currRow.createCell((i)+1);
						currCell.setCellValue(currNode[k]);
					}
				}
			}
			
			workbook.write(fileOut);
			workbook.close();
			workbook.close();
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("Error file not found");
			e.printStackTrace();
			System.exit(0);
		} 
		catch (IOException e) 
		{
			System.out.println("Unable to output file, is the output destination writelocked?");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) 
	{ 
		String intputFileName = "./data/golfWeather.xls";
		String outputFileName = "./results/golfWeather-results.xls";
		
		
		System.out.println("Importing data from: " + intputFileName);		
		readExcelFile(intputFileName);
		System.out.println("Imported");
		
		//classificationLocation = 5 -1; //Now autogened these from data or gui
		//TODO autogen row 2 from data if missing? Make row 1 not required? Assume last column if row 3 is missing
		
		
		
		//generateTrainingDataFromFile("./data/golfWeather-training.xls");
		generateTrainingDataFirst(dataLL.size() - 3);
	

		System.out.println("\nThe Data Is As Follows::");
		//System.out.print("All Data: "); printLinkedListDataWithClassification(dataLL, classificationsLL);
		System.out.print("Training Data: "); printLinkedListDataWithClassification(trainingDataLL, knownClassifications);
		System.out.print("Testing Data: "); printLinkedListDataWithClassification(testDataLL, actualClassifications);
		//printLinkedDataList(dataLL);
		//printLinkedDataList(trainingDataLL);
		//printLinkedDataList(testDataLL);
		
		
		
		generateClassifier();
		generateClassifications();
		
		System.out.print("\nClassification Types: "); printClassificationTypes(); //TODO make this its own function and print out all classification types	
		System.out.print("Each data occurence in training data followed by likelihood of it for each possible classification (The model or classifier)::\n");printLinkedListClassifier(classifier);		
		
		System.out.print("Guessed Classifications: "); printClassification(guessedClassifications);
		System.out.print("Actual Classifications: "); printClassification(actualClassifications);	
		


		
		
		
		System.out.println("Exporting results to: " + outputFileName);	
		outputExcelFile(outputFileName);
		System.out.println("Exported");
		

		
		
		
		//System.out.println(testDataLL);
	}
}
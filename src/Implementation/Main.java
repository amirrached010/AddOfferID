/*
    This is the main class for AddOfferID.
    the main method is where the execution begins
 */
package Implementation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import org.apache.log4j.Logger;


/**
 *
 * @author Amir.Rashed
 */

public class Main {
    
    static Logger logger;
    static Properties properties;
    
    static int counter;
    
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void main(String args[]) {
        
        
        logger = Logger.getLogger(Main.class);
        Util.intializeLogger(logger);
        
        if(!initiatePropertiesFile())
            return;
        Globals.NUMBER_OF_THREADS = Integer.parseInt(properties.getProperty("NumberOfThreads"));
        Globals.DIRECTORY_PATH =properties.getProperty("HOME_DIRECTORY");
        Util.setGlobals();
        logger.debug("Connect Revamp's Input Folder : " + Globals.WATCHED_DIRECTORY );
        ArrayList<File> BulkFiles = new ArrayList<File>(Globals.NUMBER_OF_THREADS);
        Globals.IS_OSWIN = Util.getOSType();
        
        
        File file = new File(Globals.WATCHED_DIRECTORY);
        int waitInactive = 0;
        while(true){
            try{
                Util.checkLogCapacity(logger);
            } catch(Exception ex){
                logger.error("Error in checking for log file size");
            }
            File[] directoryListing = file.listFiles();
            
            if(directoryListing != null && directoryListing.length > 0){
                logger.debug("Number of files in the Watched folder is : "+ directoryListing.length);
                counter =0;
                waitInactive=0;
                for(int i=0; i< directoryListing.length;i++){
                    try {
                        Util.archiveWatchDoLogFile(logger);
                        
                    } catch(Exception ex){
                        logger.error("Error in accumulating Watch Dog logs : " + ex);
                    }
                    try {
                        Util.accumulateLogs();
                    }catch(Exception ex){
                        logger.error("Error in accumulating logs : " + ex);
                    }
                    
                    try{
                        logger.debug("File being processed : "+ directoryListing[i].getName());
                        HandleFile(directoryListing[i]);
                        while(Thread.activeCount()!=1){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                logger.error("Thread sleeping fails");
                            }
                            
                        }
                        logger.debug("Done Processing file : "+ directoryListing[i].getName());
                    } catch(Exception ex){
                        logger.error("Error in Handling file : "+ directoryListing[i].getAbsolutePath());
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    logger.error("Thread sleeping fails");
                }
            }
            else {
                try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        logger.error("Thread sleeping fails");
                    }
                waitInactive ++;
                if(waitInactive == Integer.parseInt(properties.getProperty("waitInActive"))){
                    logger.debug("Execution is terminating after wait for : "+ properties.getProperty("waitInActive"));
                    logger.debug("Execution ended at : "+sdf.format(new Date()));
                    System.exit(0);
                }
            }
            
            
                
        }
        
        
    }
    
    private static void HandleFile(File toFile) {
        
        File currentFile = toFile;
        try {
            if(currentFile.getAbsolutePath().toLowerCase().contains("tmp")){
                return;
            }
        }catch(Exception ex){
            logger.error("A Temp File check caused an Exception : " + ex);
        }
        try{
            
            ArrayList<ArrayList<String>> twoDimensionalArrayOfStrings = BreakFile(toFile);
            
            for(int i=0; i<twoDimensionalArrayOfStrings.size();i++){
                SendThread a = new SendThread(twoDimensionalArrayOfStrings.get(i),properties,i);
                new Thread(a).start();
            }
            archiveFile(currentFile);
            counter++;
        } catch(Exception ex){
            logger.error("Error in initializing the Thread : " + ex);
        }
        counter++;
    }

    public static boolean initiatePropertiesFile(){
        properties = new Properties();
        properties.clear();
        try {
            File file = new File(System.getProperty("user.dir")+"/Resources/config.properties");
            logger.debug("Resource File Path : "+ file.getAbsolutePath());
            FileInputStream fileInput = new FileInputStream(file);
            properties.load(fileInput);
            fileInput.close();
            logger.setLevel(Util.getLogLevel(properties.getProperty("LogLevel")));
            return true;
        } catch (FileNotFoundException ex) {
            logger.error("Config File Not Found : " + ex.getMessage());
            
        } catch (IOException ex) {
            logger.error("Config File Parsing Error: " + ex.getMessage());
        }
        return false;
    }
    
    public static boolean CheckBulkFiles(ArrayList<File> BulkFiles){
        
        for(int i=0; i<BulkFiles.size();i++){
            File newFile = null;
            if(Globals.IS_OSWIN){
                newFile = new File(Globals.WORK_DIRECTORY+"\\"+BulkFiles.get(i).getName());
            } else {
                newFile = new File(Globals.WORK_DIRECTORY+"/"+BulkFiles.get(i).getName());
            }
            if(newFile.isFile())
                return false;
        }
        return true;
    }

    public static ArrayList<ArrayList<String>> BreakFile(File file){
        ArrayList<ArrayList<String>> resultArray = new ArrayList<ArrayList<String>>();
        if(!file.exists())
        {
                System.out.println("Input file not found."); 
                System.exit(0);
        }
        
        FileInputStream inputStream = null;
        Scanner sc = null;
        
        int numberOfThreads = Integer.parseInt(properties.getProperty("NumberOfThreads"));
        
        int counter =0;
        
        int linesPerThreadNumber = UpdateBalance.Util.getLinesNumberInAFile(file)/numberOfThreads;
        
        try {
            inputStream = new FileInputStream(file.getAbsolutePath());
            sc = new Scanner(inputStream, "UTF-8");
            ArrayList<String> linesPerThread = new ArrayList<String>();
            int linecounter = 1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                try{

                String msisdn= line.trim();
                
                
                if(linesPerThreadNumber > 0){
                    if(counter >= linesPerThreadNumber){
                        resultArray.add(linesPerThread);
                        
                        linesPerThread = new ArrayList<String>();
                        counter = 0;
                    }
                    linesPerThread.add(msisdn);
                    counter ++;
                }   else {
                    linesPerThread = new ArrayList<String>();
                    linesPerThread.add(msisdn);
                    counter ++;
                    resultArray.add(linesPerThread);
                    
                }
                }catch(Exception ex){
                    logger.error("Processing line failed for line : " + line);
                }
                linecounter++;
            }
            resultArray.add(linesPerThread);
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } 
        catch (FileNotFoundException ex) {
            logger.error("Method : processFile");
            logger.error("File not found exception for file : " + file.getAbsolutePath());
        }
        catch(IOException ex){
            logger.error("Method : processFile");
            logger.error("Error in parsing file : " + file.getAbsolutePath());
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    logger.error("Method : processFile");
                    logger.error("Error in closing input stream while parsing file : " + file.getAbsolutePath());
                }
            }
            if (sc != null) {
                sc.close();
                logger.debug("Scanner is closing for file : "+file.getName());
                
            }
        }
        
        return resultArray;
    }
    
    public  static void archiveFile(File currentFile){
        File newFile = null;
        
        SimpleDateFormat  sdf1 = new SimpleDateFormat("yyyyMMdd");
        try {
            if(Globals.IS_OSWIN){
                File file = new File (Globals.ARCHIVE_DIRECTORY+sdf1.format(new Date()));
                if (!file.exists()) {
                    if (file.mkdir()) {
                            logger.debug("Directory "+file.getAbsolutePath()+" is created!");
                            newFile = new File(file.getAbsolutePath()+"\\"+currentFile.getName());
                    } else {
                            logger.error("Failed to create directory "+ file.getAbsolutePath());
                    }
                }
                else {
                    logger.debug("Directory "+file.getAbsolutePath()+" already exists");
                    newFile = new File(file.getAbsolutePath()+"\\"+currentFile.getName());
                }
            }
            else {
                File file = new File (Globals.ARCHIVE_DIRECTORY+sdf1.format(new Date()));
                if (!file.exists()) {
                    if (file.mkdir()) {
                            logger.debug("Directory "+file.getAbsolutePath()+" is created!");
                            
                    } else {
                            logger.debug("Failed to create directory "+ file.getAbsolutePath());
                    }
                    
                }
                else {
                    logger.debug("Directory "+file.getAbsolutePath()+" already exists");
                    
                }
                newFile = new File(file.getAbsolutePath()+"/"+currentFile.getName());
            }
        }catch(Exception e){
            logger.error("Exception in creating the archive date folder or setting the path of the file in the archive");
            logger.error(e);
        }
        try{
            Files.move(Paths.get(currentFile.getAbsolutePath()), Paths.get(newFile.getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
            logger.debug("File : " + currentFile.getAbsolutePath() + " is moved to the archive directory ");
//            newFile.delete();
//            if(currentFile.renameTo(newFile)){
//
//                logger.debug("File "+ currentFile.getAbsolutePath() + " is moved to " + newFile.getAbsolutePath());
//                logger.debug("work file "+newFile.getName()+" is archived");
//            }
//            else {
//                logger.error("Failed to move "+ currentFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
//                logger.debug("work file "+newFile.getName()+" was not archived");
//            }
        }catch(Exception e){
            logger.error("Error in moving the file : "+ currentFile.getAbsolutePath() + " To the archive folder : " + newFile.getAbsolutePath());
            logger.error(e);
        }
     
    }
}

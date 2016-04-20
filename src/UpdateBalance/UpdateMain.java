/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UpdateBalance;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import org.apache.log4j.Logger;


/**
 *
 * @author omar.alfar
 */

public class UpdateMain {

    static Logger logger;
    static Properties properties;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static ArrayList<ArrayList<String>> great_List = new ArrayList<ArrayList<String>>();
    public static void main1(String [] args)
    {
        logger = Logger.getLogger(UpdateMain.class);
        Util.intializeLogger(logger,"Connect-Recharge-Monitoring-Corrective-Action");
        
        if(!initiatePropertiesFile())
            return;
        
        Util.setGlobals();
        logger.debug("Connect-Recharge-Monitoring-Corrective-Action's Input Folder : " + Globals.WATCHED_DIRECTORY);
        File file = new File(Globals.WATCHED_DIRECTORY);
        
        
        while(true){
            RoundRobin <Air> AirList= new RoundRobin <Air>();
            //initializeAirs(AirList);
            try{
                Util.checkLogCapacity(logger,"Connect-Recharge-Monitoring-Corrective-Action");
            } catch(Exception ex){
                logger.error("Error in checking for log file size");
            }
            File[] directoryListing = file.listFiles();
            
            if(directoryListing != null && directoryListing.length > 0){
                logger.debug("Number of files in the Watched folder is : "+ directoryListing.length);
                for(int i=0; i< directoryListing.length;i++){
                    try {
                        Util.archiveWatchDoLogFile(logger,"Connect-Recharge-Monitoring-Corrective-Actiono");
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
                        HandleFile(directoryListing[i],AirList);
                        logger.debug("Done Processing file : "+ directoryListing[i].getName());
                    } catch(Exception ex){
                        logger.error("Error in Handling file : "+ directoryListing[i].getAbsolutePath());
                    }
                }
                
            }
            
            try{
                 for(int i=0;i<AirList.size();i++)
                    AirList.next().shutDown();
            }catch(Exception e){
                logger.error("Error in shutting down Airs");
            }
        }
        
     
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
    
    public static void initializeAirs(RoundRobin <Air> AirList){
        for(Entry<Object, Object> e : properties.entrySet()) {
            if(((String) e.getKey()).startsWith("AIR_")){
                String value = (String) e.getValue();
                
                AirList.enqueue(new Air(value.split(",")[0],Integer.parseInt(value.split(",")[1]),value.split(",")[3]));
            }
        }
        
    }

    public static void HandleFile(File file,RoundRobin <Air> AirList){
        
        if(!file.exists())
        {
                System.out.println("Input file not found."); 
                System.exit(0);
        }
        
        FileInputStream inputStream = null;
        Scanner sc = null;
        
        int numberOfThreads = Integer.parseInt(properties.getProperty("NumberOfThreads"));
        
        int counter =0;
        
        int linesPerThreadNumber = Util.getLinesNumberInAFile(file)/numberOfThreads;
        
        try {
            inputStream = new FileInputStream(file.getAbsolutePath());
            sc = new Scanner(inputStream, "UTF-8");
            String text = null;
            ArrayList<String> linesPerThread = new ArrayList<String>();
            while (sc.hasNextLine()) {
                text = sc.nextLine();
                text=text.trim();
                
                String msisdn= text;
                
                
                if(linesPerThreadNumber > 0){
                    if(counter >= linesPerThreadNumber){
                        great_List.add(linesPerThread);
                        
                        linesPerThread = new ArrayList<String>();
                        counter = 0;
                    }
                    linesPerThread.add(msisdn);
                    counter ++;
                }   else {
                    linesPerThread = new ArrayList<String>();
                    linesPerThread.add(msisdn);
                    counter ++;
                    great_List.add(linesPerThread);
                    
                }
            }
            great_List.add(linesPerThread);
//            for(int i=0; i<great_List.size(); i++){
//                Air a = AirList.next();
//                a.addRequest(great_List.get(i),properties,i);
//            }
            for(int i=0; i<great_List.size(); i++){
                SendThread a = new SendThread(properties.getProperty("AIR_1_URL"),great_List.get(i),properties.getProperty("AIR_1_PASSWORD"),properties,i);
                a.run();
            }
            
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
        try {
            logger.debug("File : "+ file.getAbsolutePath() + " is getting archived");
            archiveFile(file);
           logger.debug("File : "+ file.getAbsolutePath() + " has been archived");
        }catch (Exception e) {
            logger.error("Error in archiving file : " + file.getAbsolutePath());
            logger.error(e);
        }
        
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
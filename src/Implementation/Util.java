package Implementation;


import static Implementation.Globals.DIRECTORY_PATH;
import static Implementation.Main.logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Amir.Rashed
 */
public class Util {
    
    public static void intializeLogger(Logger logger){

        logger.removeAllAppenders();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        RollingFileAppender appender = new RollingFileAppender();
        appender.setName("GlobalAppender");
        appender.setAppend(true);
        appender.setMaxFileSize("50MB");
        appender.setMaxBackupIndex(10);
        String fileName="Corrective_Action_" +dateFormat.format(new Date());
        appender.setFile(fileName + ".log");
        PatternLayout layOut = new PatternLayout();
        layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
        appender.setLayout(layOut);
        appender.activateOptions();
        logger.addAppender(appender);
        logger.debug("Starting log file for WatchDirectory Tool - "+sdf.format(new Date()));
        
    }

    public static org.apache.log4j.Level getLogLevel(String level){
        if(level.equalsIgnoreCase("DEBUG"))
            return org.apache.log4j.Level.DEBUG;
        else
            if(level.equalsIgnoreCase("ERROR"))
            return org.apache.log4j.Level.ERROR;
        
        return org.apache.log4j.Level.DEBUG;
    }
    
    public static boolean getOSType(){
        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.contains("win"))
            return Globals.OS_WIN;
        else
            return Globals.OS_UNIX;
    }
    
    public static void accumulateLogs(){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        File file =null;
        if(getOSType() == Globals.OS_WIN){
            file =new File (Globals.DIRECTORY_PATH+"ArchiveLog\\"+sdf1.format(new Date())+"\\");
        }
        else {
            file =new File (Globals.DIRECTORY_PATH+"ArchiveLog/"+sdf1.format(new Date())+"/");
        }
        
        if (!file.exists()) {
            if (file.mkdir()) {
                    logger.debug("Directory "+file.getAbsolutePath()+" is created!");
            } else {
                    logger.error("Failed to create directory "+ file.getAbsolutePath());
            }
        }
        
        
        accumulateLogHelper();
    }

    public static void accumulateLogHelper(){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        
        Date currentDate = new Date();
        
        String myDirectoryPath;
        String logFileName;
        if(Globals.IS_OSWIN){
            myDirectoryPath = Globals.INSTANT_LOG_PATH;
            logFileName =Globals.ARCHIVE_LOG_DIRECTORY+sdf2.format(currentDate)+"\\IN_SMSTool_AccumulativeLog_"+sdf1.format(currentDate)+".log";
        }
        else
        {
            myDirectoryPath = Globals.INSTANT_LOG_PATH;
            logFileName =Globals.ARCHIVE_LOG_DIRECTORY+sdf2.format(currentDate)+"/IN_SMSTool_AccumulativeLog_"+sdf1.format(currentDate)+".log";
        }
        
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        StringBuilder all = new StringBuilder();
        if(directoryListing.length < 5){
            return;
        }
        if (directoryListing != null) {
          for (File child : directoryListing) {
                
                if(!child.getName().contains(sdf1.format(currentDate))) {
                    String childContent = Util.readFromFile(child);
                    if(childContent.contains("--------")){
                        all.append(childContent);
                        child.delete();
                        all.append((System.lineSeparator()));
                        Util.WriteHourlyLog(all, logFileName);
                        all = new StringBuilder(); 
                    }
                    
                }
          }
          Util.WriteHourlyLog(all, logFileName);
        } else {
          logger.error(" No files to be accumulated");
        }
    }
   
    public static String readFromFile(File dialsFile){
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(dialsFile));
        } catch(FileNotFoundException e){
            
        }
        
        try {
            
            String line = br.readLine();

            while (line != null) {
                
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            
            br.close();
        } catch (IOException ex) {
         
        } 
        return sb.toString();
    }
    
    public static void WriteHourlyLog(StringBuilder result,String currentFileName){
        
        File resultFile = new File(currentFileName);
        if(!resultFile.exists())
            try {
                resultFile.createNewFile();
        } catch (IOException ex) {
            
        }
        try {
            FileWriter fw = new FileWriter(resultFile,true);
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(result.toString());   
            bw.close();
        } catch (UnsupportedEncodingException ex) {
           logger.error("WriteHourlyLog : UnsupportedEncodingException : " + ex);
        } catch (FileNotFoundException ex) {
            logger.error("WriteHourlyLog : FileNotFoundException :" + ex);
        } catch (IOException ex) {
            logger.error("WriteHourlyLog : IOException :" + ex);
        }
    }
    
    public static void checkLogCapacity(Logger logger){
        RollingFileAppender rollingAppender = (RollingFileAppender) logger.getAppender("GlobalAppender");
        File logFile = new File(rollingAppender.getFile());
        Long fileSize = Long.parseLong("52428800");
        if(logFile.length()> fileSize)
            Util.intializeLogger(logger);
    }
    
    public static void archiveWatchDoLogFile(Logger logger){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateString = sdf.format(new Date());
        File file = new File(Globals.DIRECTORY_PATH);
        File[] directoryListing = file.listFiles();
        boolean currentLogExists = false;
        for(int i=0; i< directoryListing.length;i++){
            if(directoryListing[i].getName().contains(".log") && !directoryListing[i].getName().contains(dateString)){
                File logDirectory = null;
                File newFile = null;
                String fileName ="";
                try {
                    
                    fileName = directoryListing[i].getName().substring(12, 16)+
                        directoryListing[i].getName().substring(16, 18)+
                        directoryListing[i].getName().substring(18, 20);
                
                }catch(Exception e){
                    logger.error("The fileName construction of the Watch Dog Log is in correct");
                    logger.error("Error : "+ e);
                }
                if(Globals.IS_OSWIN){
                    logDirectory = new File(Globals.ARCHIVE_LOG_DIRECTORY+fileName+"\\");
                    newFile = new File(logDirectory.getAbsolutePath()+"\\"+directoryListing[i].getName());
                }   
                else {
                    logDirectory = new File(Globals.ARCHIVE_LOG_DIRECTORY+fileName+"/");
                    newFile = new File(logDirectory.getAbsolutePath()+"/"+directoryListing[i].getName());    
                }
                if(!logDirectory.exists()){
                    try {
                        logDirectory.delete();
                        logDirectory.mkdir();
                    }catch(Exception ex){
                        logger.error("cannot create directory : " + logDirectory.getAbsolutePath());
                    }
                }
                newFile.delete();
                if(directoryListing[i].renameTo(newFile)){
                    logger.debug("File archived : " + newFile.getAbsolutePath());
                }
                else {
                    logger.error("Error in Archiving File :  " + newFile.getAbsolutePath());
                }
                
            }
            
            if(directoryListing[i].getName().contains(".log") && directoryListing[i].getName().contains(dateString)){
                currentLogExists = true;
            }
        }
        if(!currentLogExists){
            intializeLogger(logger);
        }
    }
    
    public static void setGlobals(){
//        Globals.WATCHED_DIRECTORY=Globals.DIRECTORY_PATH+"INPUT\\Ready\\";
//        Globals.WORK_DIRECTORY=Globals.DIRECTORY_PATH+"INPUT\\Work\\";
//        Globals.SMS_PREPARATION_DIRECTORY=Globals.DIRECTORY_PATH+"SMSPreparation\\";    
//        Globals.INSTANT_LOG_PATH= Globals.DIRECTORY_PATH+"logs\\";   
//        Globals.ARCHIVE_LOG_DIRECTORY = Globals.DIRECTORY_PATH+"ArchiveLog\\";
//        Globals.ARCHIVE_DIRECTORY = Globals.DIRECTORY_PATH+"Archive\\";
        
        Globals.WATCHED_DIRECTORY=Globals.DIRECTORY_PATH+"INPUT/Ready/";
        Globals.WORK_DIRECTORY=Globals.DIRECTORY_PATH+"INPUT/Work/";
        Globals.SMS_PREPARATION_DIRECTORY=Globals.DIRECTORY_PATH+"SMSPreparation/";    
        Globals.INSTANT_LOG_PATH= Globals.DIRECTORY_PATH+"logs/";   
        Globals.ARCHIVE_LOG_DIRECTORY = Globals.DIRECTORY_PATH+"ArchiveLog/";
        Globals.ARCHIVE_DIRECTORY = Globals.DIRECTORY_PATH+"Archive/";
    }
}
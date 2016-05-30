/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Implementation;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import javax.xml.xpath.XPath;
import javax.xml.xpath.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/**
 *
 * @author Omar.AlFar
 */
public class SendThread implements Runnable {

    // String request;
    String serverUrl;
    ArrayList<String> inputList;
    String password;
    String msisdn;
    Globals.UCIPRequest currentRequest;
    Logger logger;
    String appenderName;
    Properties properties;
    int counter;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hh:mm:ss+0200");
    public SendThread( ArrayList<String> inputList, 
            Properties properties,int counter) {
        // this.request = request;
        this.serverUrl = properties.getProperty("AIR_"+((counter%(Integer.parseInt(properties.getProperty("NumberOfAirs"))))+1)+"_URL");
        this.inputList = inputList;
        this.password = properties.getProperty("AIR_"+((counter%(Integer.parseInt(properties.getProperty("NumberOfAirs"))))+1)+"_PASSWORD");;
        this.properties = properties;
        this.counter= counter;
        intializeLogger();
        logger.debug("Thread is Using Air "+((counter%(Integer.parseInt(properties.getProperty("NumberOfAirs"))))+1)+" whose URL is : "+ serverUrl);
    }

    public void run() {
        
        logger.debug("Starting the run method for Thread : "+appenderName);
        for(int i=0; i<inputList.size(); i++){
            HashMap<String,String> ucip_inputs1 = new HashMap<String,String>();
            parseInputs(ucip_inputs1,inputList.get(i));
            logger.debug("Handling the MSISDN : " + this.msisdn);
            logger.debug("current UCIP Request : "+ currentRequest.name());
            
            String request1 = formatRequestV1(currentRequest,ucip_inputs1); 
            String response1 = sendRequest(request1);
            String parsedResponse = parseResponse(response1);
            if(!parsedResponse.trim().equals("0") && !parsedResponse.trim().equals("190") ){
                logger.error("The request for the current UCIP Request :  "+ request1);
                logger.error("The response for the current UCIP Request : "+ response1);
            } else{
                logger.debug("Request sent successfully with Response Code : "+ parsedResponse);
            }
        
        }
        
        stopThread();
}

    
//    public void start(){
//        run();
//    }
    public String sendRequest(String request) {

        OutputStreamWriter streamWriter = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String response = "";
        //tring password = "Z3NkYzpnc2Rj";
        // String password = "YWlydXNlcjpsYXVuY2gwNQ==";
        URLConnection urlConnection= null;
        HttpURLConnection httpConnection = null;
        try {
            //   System.out.println("in 1");
            urlConnection = getConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Method", "POST");
            urlConnection.setRequestProperty("User-Agent", "UGw Server/5.0/1.0");
            urlConnection.setRequestProperty("Content-Type", "text/xml");
            urlConnection.setRequestProperty("Authorization",password);
            urlConnection.setRequestProperty("Host", "Air");
            String currentRequest = request.replace("$msisdn", msisdn);
            urlConnection.setRequestProperty("Content-Length", "" + currentRequest.length());
            
            streamWriter = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            
            streamWriter.write(currentRequest);
            streamWriter.flush();
            
            inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            String responseLine = "";
            while ((responseLine = bufferedReader.readLine()) != null) {
                response = response + responseLine;
            }
            
            // System.out.println(response);
        } catch (Exception exception) {
            logger.error("Exception in sendRequest ");
            logger.error(exception);
        } finally {
            if (streamWriter != null) {
                try {
                    streamWriter.close();
                } catch (IOException ioException) {
                    logger.error("Exception in sendRequest ");
                    logger.error(ioException);
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ioException) {
                    logger.error("Exception in sendRequest ");
                    logger.error(ioException);
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    logger.error("Exception in sendRequest ");
                    logger.error(ioException);
                }
            }
            if (urlConnection != null) {
                 httpConnection = (HttpURLConnection) urlConnection;
                 httpConnection.disconnect();
                 logger.debug("The connection to "+serverUrl+" is closed");
            }
        }
        //   System.out.println("in 5");
        return response;
    }

    private URLConnection getConnection() throws IOException {

        URL url = new URL(serverUrl);

        URLConnection urlConnection = url.openConnection();

        return urlConnection;
    }

    private String formatRequest() {
        Random randomGenerator = new Random();
        String id = randomGenerator.nextInt(10000000) + "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'hh:mm:ss+2000");

        String date = dateFormat.format(new Date()).toString();
        //String date = "20130910T00:00:00+2000";



        String xml = "<?xml version=\"1.0\"?><methodCall><methodName>UpdateBalanceAndDate</methodName><params><param><value><struct>";

        //THIS VALUE EXT TO BE REPLACE IF NEEDED 
        //THE ORIGIN NODE NAME
        xml += "<member><name>originNodeType</name><value><string>EXT</string></value></member>";

        xml += "<member><name>originOperatorID</name><value><string>Etisalat</string></value></member>";

        xml += "<member><name>originHostName</name><value><string>Air</string></value></member>";

        xml += "<member><name>subscriberNumberNAI</name><value><int>2</int></value></member>";

        xml += String.format("<member><name>originTransactionID</name><value><string>%s</string></value></member>", id);

        xml += String.format("<member><name>originTimeStamp</name><value><dateTime.iso8601>%s</dateTime.iso8601></value></member>", date);

        xml += String.format("<member><name>transactionCurrency</name><value><string>EGP</string></value></member>");

        xml += String.format("<member><name>externalData1</name><value><string>CADEINSr</string></value></member>");

        xml += String.format("<member><name>externalData2</name><value><string>CADEINSr</string></value></member>");

        //xml += String.format("<member><name>adjustmentAmountRelative</name><value><string>%s</string></value></member>", offerId);


        if (msisdn.startsWith("20")) {
            msisdn = msisdn.replaceFirst("20", "");
        }

        xml += String.format("<member><name>subscriberNumber</name><value><string>%s</string></value></member>", msisdn);
        xml += "</struct></value></param></params></methodCall>";

        return xml;

    }

    private String formatRequestV1(Globals.UCIPRequest ucipRequest,HashMap<String,String> inputs){
        String updateBalanceAndDateRequest ="";
        updateBalanceAndDateRequest = readFromFile(new File(System.getProperty("user.dir")+"/Requests/"+ucipRequest+".txt")).toString();
        updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$originTimeStamp",sdf.format(new Date()));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 6);
        switch(ucipRequest){
            case UpdateOffer: 
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                break;
            case UpdateOfferWithExpiry: 
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$expiryDate", inputs.get("$expiryDate"));
                break;
            case AddPam: 
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$PAMServiceID", inputs.get("$PAMServiceID"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$PAMCLASSID", inputs.get("$PAMCLASSID"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$PAMSCHEDULEID", inputs.get("$PAMSCHEDULEID"));
                break;
            case RunPam: 
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$pamServiceID", inputs.get("$PAMServiceID"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$pamIndicator",inputs.get("$pamIndicator"));
                break;
            case ResetFiveAccumulator:
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_1", properties.getProperty("AccumulatorID_1"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_2", properties.getProperty("AccumulatorID_2"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_3", properties.getProperty("AccumulatorID_3"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_4", properties.getProperty("AccumulatorID_4"));
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_5", properties.getProperty("AccumulatorID_5"));
                break;
            case DeleteOffer: 
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                break;
            case ChangeSC: 
                updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$SC", inputs.get("$SC"));
                break;
        };
        return updateBalanceAndDateRequest;        
    }
    
    private String parseResponse(String response){
        String status = "";
        if (response.length() > 0) {

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            InputSource source = new InputSource(new StringReader(
                    response));

            try {
                status = xpath.evaluate("/methodResponse/params/param/value/struct/member[name='responseCode']/value/i4", source);
                
            } catch (XPathExpressionException ex) {
               logger.error("Cannot Parse Response : " +ex);
            }

            xpath.reset();
        } else {
            status = "-1";
        }
        return status;
    }
    
    private String parseGerOffers(String response){
        if (response.length() > 0) {
            DocumentBuilderFactory builderFactory =DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = builderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  
            }
            try {
                org.w3c.dom.Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));
                XPath xPath =  XPathFactory.newInstance().newXPath();
                String expression = "/methodResponse/params/param/value/struct/member[name='offerInformation']/value/array/data/value/struct/member[name='offerID']/value/i4";
                NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
                String asm = "";
                for (int i = 0; i < nodeList.getLength(); i++) {
                    asm+=(nodeList.item(i).getFirstChild().getNodeValue())+";"; 
                }
                return asm;

            } catch (SAXException ex) {
                java.util.logging.Logger.getLogger(SendThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SendThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                java.util.logging.Logger.getLogger(SendThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        } 
        return null;
    }
    
    private String parseSC(String response){
        String status = "";
        if (response.length() > 0) {

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            InputSource source = new InputSource(new StringReader(
                    response));

            try {
                status = xpath.evaluate("/methodResponse/params/param/value/struct/member[name='serviceClassCurrent']/value/i4", source);
                
            } catch (XPathExpressionException ex) {
               logger.error("Cannot Parse Response : " +ex);
            }

            xpath.reset();
        } else {
            status = "-1";
        }
        return status;
    }
    
    public  StringBuilder readFromFile(File dialsFile){
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
        } catch (Exception ex) {
            logger.error("Exception in reading file : "+ dialsFile.getName());
            logger.error(ex);
        } 
        return sb;
    }
    
    public  void intializeLogger(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        RollingFileAppender appender = new RollingFileAppender();
        appender.setAppend(true);
        appender.setMaxFileSize("1MB");
        appender.setMaxBackupIndex(1);
        String fileName="logs/ThreadLog_"+counter+"_"+dateFormat.format(new Date());
        appenderName = fileName;
        appender.setName(appenderName);
        logger = Logger.getLogger(fileName);    
        appender.setFile(fileName + ".log");
        PatternLayout layOut = new PatternLayout();
        layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p :%L - %m%n");
        appender.setLayout(layOut);
        appender.activateOptions();
        logger.removeAllAppenders();
        logger.addAppender(appender);
        logger.debug("Log appended : " + fileName);
        
    }
    
    public  void sendSMS(String toString,int lineCounter) {
        Writer writer = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentFileName ="";
        File resultFile = null;
        File newFile  = null;
        if(Util.getOSType() == Globals.OS_UNIX){
            currentFileName = Globals.SMS_PREPARATION_DIRECTORY+"AirTool_"+sdf.format(new Date())+"_L"+lineCounter+"_V"+this.inputList+".txt";
            resultFile = new File(currentFileName);
            newFile = new File(Globals.SMS_DIRECTORY+resultFile.getName());
                    
        }
        else{
            currentFileName = Globals.SMS_PREPARATION_DIRECTORY+"AirTool_"+sdf.format(new Date())+"_L"+lineCounter+"_V"+this.inputList+".txt";
            resultFile = new File(currentFileName);
            newFile = new File(Globals.SMS_DIRECTORY+resultFile.getName());
        }
        
        
        if(!resultFile.exists())
            try {
                resultFile.createNewFile();
        } catch (IOException ex) {
           logger.error("Cannot create the SMS file in the SMS Preparation Directory: "+ currentFileName);
        }
        try {
            FileWriter fw = new FileWriter(resultFile,true);
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(toString);
            bw.close();
            // Send the file to the SMS tool
            //logger.info("Deleting moved file : "+ newFile.delete());
            try{
            Files.move(Paths.get(resultFile.getAbsolutePath()), Paths.get(newFile.getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
            logger.info("File "+ newFile.getAbsolutePath()+" is moved to the SMS Directory");
            }catch(Exception e){
                logger.error("Failed to move File "+ resultFile.getAbsolutePath()+" to the SMS Directory : " + Globals.SMS_DIRECTORY + " under name : " + newFile.getName());
                logger.error("Exception : "+ e);
            }    
//            if(resultFile.renameTo(newFile)){
//              logger.info("File "+ newFile.getAbsolutePath()+" is moved to the SMS Directory");
//            }
//            else {
//               logger.error("Failed to move File "+ resultFile.getAbsolutePath()+" to the SMS Directory : " + Globals.SMS_DIRECTORY + " under name : " + newFile.getName());
//            }
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error in writing in file : "+ currentFileName);
        } catch (FileNotFoundException ex) {
            logger.error("File not found : "+ currentFileName);
        } catch (IOException ex) {
            logger.error("IO Exception: "+ currentFileName);;
        }
    }
 
    public void stopThread(){
        logger.debug("Thread Stopped ");
        logger.debug("------------------------------------------------------------------------------------------------");
        logger.getAppender(appenderName).close();
        //LogManager.shutdown();
    }
    
    public void parseInputs(HashMap<String,String> ucip_inputs1,String s){
        this.msisdn = s.split(",")[0];
        if(s.split(",")[1].equals(Globals.UCIPRequest.AddPam.toString())){
            this.currentRequest = Globals.UCIPRequest.AddPam;
            ucip_inputs1.put("$PAMServiceID",s.split(",")[2]);
            ucip_inputs1.put("$PAMCLASSID",s.split(",")[3]);
            ucip_inputs1.put("$PAMSCHEDULEID",s.split(",")[4]);
        }
        if(s.split(",")[1].equals(Globals.UCIPRequest.UpdateOffer.toString())){
            this.currentRequest = Globals.UCIPRequest.UpdateOffer;
            ucip_inputs1.put("$offerid",s.split(",")[2]);
        }
        if(s.split(",")[1].equals(Globals.UCIPRequest.GetAccountDetails.toString()))
            this.currentRequest = Globals.UCIPRequest.GetAccountDetails;
        if(s.split(",")[1].equals(Globals.UCIPRequest.ResetFiveAccumulator.toString()))
            this.currentRequest = Globals.UCIPRequest.ResetFiveAccumulator;
        if(s.split(",")[1].equals(Globals.UCIPRequest.GetOffers.toString()))
            this.currentRequest = Globals.UCIPRequest.GetOffers;
        if(s.split(",")[1].equals(Globals.UCIPRequest.UpdateOfferWithExpiry.toString())){
            this.currentRequest = Globals.UCIPRequest.UpdateOfferWithExpiry;
            ucip_inputs1.put("$offerid",s.split(",")[2]);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH,Integer.parseInt(s.split(",")[3]));
            java.util.Date dt = cal.getTime();
            String currentDate = sdf.format(dt);
            ucip_inputs1.put("$expiryDate",currentDate);
        }
        if(s.split(",")[1].equals(Globals.UCIPRequest.DeleteOffer.toString())){
            this.currentRequest = Globals.UCIPRequest.DeleteOffer;
            ucip_inputs1.put("$offerid",s.split(",")[2]);
        }
        if(s.split(",")[1].equals(Globals.UCIPRequest.ChangeSC.toString())){
            this.currentRequest = Globals.UCIPRequest.ChangeSC;
            ucip_inputs1.put("$SC",s.split(",")[2]);
        }
    }
}

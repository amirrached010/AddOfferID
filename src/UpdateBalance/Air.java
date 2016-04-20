/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UpdateBalance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * @author Omar.AlFar
 */
public class Air {
    
    BlockingQueue<Runnable> worksQueue=null;
    ThreadPoolExecutor executor=null;
    String url;
    String password;
    
    
    public Air(String url,int threads,String password)
    {
        worksQueue = new LinkedBlockingQueue<Runnable>();

        executor = new ThreadPoolExecutor(threads, threads, 200,TimeUnit.SECONDS, worksQueue);
        
        
       // executor.(true);
        
        this.password=password;
        this.url=url;
    }
    
    public void shutDown()
    {
        executor.shutdown();
    }
    
    public void addRequest(ArrayList<String> inputList,Properties properties, int counter)
    {
       
       // System.out.println(xml);
        
        executor.execute(new SendThread(url, inputList,password, 
             properties,counter));
        
    }
      
}

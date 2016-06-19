/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Implementation;

import UpdateBalance.*;
import java.util.ArrayList;

/**
 *
 * @author Amir.Rashed
 */
public class Globals {
    
    public static  String DIRECTORY_PATH;
    public static  String WATCHED_DIRECTORY;
    public static  String WORK_DIRECTORY;
    public static  String SMS_PREPARATION_DIRECTORY;
    public static  String INSTANT_LOG_PATH;
    public static  String ARCHIVE_LOG_DIRECTORY;
    public static  String ARCHIVE_DIRECTORY;
    public static  String SMS_DIRECTORY="/export/home/etisalatSMS/input/";
    public static final boolean OS_WIN = true;
    public static final boolean OS_UNIX = false;
    public static boolean IS_OSWIN;
    public static int NUMBER_OF_THREADS;
    
    public static enum UCIPRequest {
        UpdateOffer,
        AddPam,
        GetAccountDetails,
        ResetFiveAccumulator,
        GetOffers,
        RunPam,
        UpdateOfferWithExpiry,
        DeleteOffer,
        ChangeSC,
        AddSob,
        AddTwoSob,
        AddThreeSob,
        SendSMS
    }
}

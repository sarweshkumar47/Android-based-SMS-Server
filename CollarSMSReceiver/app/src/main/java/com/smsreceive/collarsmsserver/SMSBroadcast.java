package com.smsreceive.collarsmsserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.b2msolutions.reyna.Header;
import com.b2msolutions.reyna.Message;
import com.b2msolutions.reyna.services.StoreService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;


public class SMSBroadcast extends BroadcastReceiver
{
    private String tag = "SMSReceiver";
    public static String ElephantURL = "http://elephanttracking.appspot.com/trackingdata"; //"http://elephanttracking.appspot.com/odata";
    private Context context;

    @Override
    public void onReceive(Context c, Intent intent)
    {
        this.context = c;
        Toast.makeText(context, "SMS Received", Toast.LENGTH_SHORT).show();
        Bundle bundle = intent.getExtras();

		/*
		 * if bundle has data call AsyncTask and pass received bundle
		 */
        if (bundle != null)
        {
            new pduBundleparse().execute(bundle);
        }
    }

	/*
	 * desc: Background AsyncTask to extract the raw pdu format from the bundle
	 * @param: Bundle - Bundle what we get in onReceive function
	 */

    private class pduBundleparse extends AsyncTask<Bundle, Void, Void>
    {

        @Override
        protected Void doInBackground(Bundle... params)
        {
            SMSData smsdata = new SMSData();
            try
            {
                Bundle b = params[0];

                Object[] messages = (Object[]) b.get("pdus");
                for (int i = 0; i < messages.length; i++)
                {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[])messages[i]);

                    byte[] pduData = msg.getPdu();
                    Log.d(tag, "SMSReceiver byte[]: "+Arrays.toString(pduData));

		/*
	     * pduDataLength - Total length of the pduData(including header)
	     * Header = 27B(including extra padding bytes 2222), Type = 1B, FixData = 20B, Type = 1B, StatusData = 12B
	     * Total 61Bytes
	     */
                    int pduDataLength = pduData.length;
                    Log.d(tag, "SMSReceiver data length: "+pduDataLength);

                    // kollarmobileno - mobile no. of the eCollar module

                    String kollarmobileno = msg.getDisplayOriginatingAddress();
                    Log.d(tag, "SMSReceiver mob no: "+kollarmobileno);

                    if((pduDataLength == 61) && (pduData[0] == 0x07))
                    {
                        Log.d(tag, "SMS has been received from one of the collar module");
                        smsdata.setPhoneNumber(kollarmobileno);

                        //==============================Message time parsing==================================================

                        if((pduData[27] == 0x01) && (pduData[48] == 0x02))
                        {
                            Log.d(tag, "SMS, Parsing the fix data");

                            long temptime = (long)((pduData[31] & 0xFF) << 24);
                            temptime = (temptime | ((pduData[30] & 0xFF) << 16));
                            temptime = (temptime | ((pduData[29] & 0xFF) << 8));
                            temptime = (temptime | (pduData[28] & 0xFF));

                            int hour = (int) (temptime/10000);
                            int min =(int) ((temptime%10000)/100);
                            int sec =(int) (temptime%100);

                            //================================Message date parsing==================================================
                            long tempdate = (long)((pduData[33] & 0xFF) << 8);

                            tempdate = (tempdate | (pduData[32] & 0xFF));
                            int date = (int) (tempdate & 0x1F);
                            int month = (int) ((tempdate & (0x0f << 5)) >> 5);
                            int year = (int) ((tempdate >> 9) + 2000);

                            String smsSentTimeInfo = convertUTCtoISTDateTime(year, month, date, hour, min, sec);
                            Log.d(tag, "SMS, Sent info: "+smsSentTimeInfo);

                            // Splitting the date and time sepatarely to put into Json object

                            String[] istDateTime =  smsSentTimeInfo.split("T");
                            String[] dateSeparate = istDateTime[0].split("-");
                            String[] timeSeparate = istDateTime[1].split(":");
                            smsdata.setYear(Integer.valueOf(dateSeparate[0]));
                            smsdata.setMonth(Integer.valueOf(dateSeparate[1]));
                            smsdata.setDate(Integer.valueOf(dateSeparate[2]));

                            smsdata.setHours(Integer.valueOf(timeSeparate[0]));
                            smsdata.setMin(Integer.valueOf(timeSeparate[1]));
                            smsdata.setSec(Integer.valueOf(timeSeparate[2]));


                            //=================================Latitude info Extraction==================================================
                            long templatitude = (long)((pduData[37] & 0xFF) << 24);
                            templatitude = (templatitude | ((pduData[36] & 0xFF) << 16));
                            templatitude = (templatitude | ((pduData[35] & 0xFF) << 8));
                            templatitude = (templatitude | (pduData[34] & 0xFF));

                            int lat_temp = (int)templatitude / 10000000;
                            double lati = ((double)((templatitude % 10000000L) / 6000000.0D) + lat_temp);
                            smsdata.setLatitude(lati);
                            //=================================Longitude info Extraction==================================================
                            long templongitutde = (long)((pduData[41] & 0xFF) << 24);
                            templongitutde = (templongitutde | ((pduData[40] & 0xFF) << 16));
                            templongitutde = (templongitutde | ((pduData[39] & 0xFF) << 8));
                            templongitutde = (templongitutde | (pduData[38] & 0xFF));


                            int long_temp = (int)templongitutde / 10000000;
                            double longi = ((double)((templongitutde % 10000000L) / 6000000.0D) + long_temp);
                            smsdata.setLongitude(longi);

                            //=============================== Altitude info Extraction ===========================================
                            int tempaltitude = (int)((pduData[43] & 0xFF) << 8);
                            tempaltitude = (tempaltitude | (pduData[42] & 0xFF));
                            smsdata.setAltitude(tempaltitude);

                            //==================================Status info Extraction======================================

                            Log.d(tag, "SMS, Parsing the status data");
                            //=================================== Voltage info extraction =====================================

                            int tempvol1 = (int)((pduData[50] & 0xFF) << 8);
                            tempvol1 = (tempvol1 | (pduData[49] & 0xFF));
                            float vol = ((float)tempvol1 / 1000);
                            smsdata.setVoltage(vol);
                            //============================== GSM info extraction ====================================

                            int tempsmsfailcount = (int) (pduData[53] & 0xFF);
                            smsdata.setGsmFailureCount(tempsmsfailcount);

                            int templastsmsfailtype = (int) (pduData[54] & 0xFF);
                            smsdata.setLastGsmFailureType(templastsmsfailtype);


                            JSONObject finalJsonData = putIntoJSONObject(smsdata);
                            Log.d(tag, "SMS final data: "+finalJsonData);


                            SendToServer(finalJsonData);
                            writeData(finalJsonData.toString());


                        }
                        else
                            Log.d(tag, "SMS, Error has been occured while parsing sms");

                    }

                }
            }
            catch(Exception e)
            {
                Log.e(tag, "SMSReceiver General exception1");
            }

            return null;
        }
    }
	 /* Desc: Creates a JSON Object to post it to Googleapp engine
	  * @param: s - SMSData object
	  */

    private JSONObject putIntoJSONObject(SMSData s)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("altitude", s.getAltitude());
            jsonObject.put("date", s.getDate());
            jsonObject.put("gsmFailureCount", s.getGsmFailureCount());
            jsonObject.put("hour", s.getHours());
            jsonObject.put("lastSmsFailureType", s.getLastGsmFailureType());
            jsonObject.put("latitude", s.getLatitude());
            jsonObject.put("longitude", s.getLongitude());
            jsonObject.put("minute", s.getMin());
            jsonObject.put("month", s.getMonth());
            jsonObject.put("collarphoneno", s.getPhoneNumber());
            jsonObject.put("second", s.getSec());
            jsonObject.put("voltage", s.getVoltage());
            jsonObject.put("year", s.getYear());
            jsonObject.put("URL", ElephantURL);

        }
        catch (JSONException e)
        {
            Log.e(tag, "SMSReceiver JSONexception");
        }
        catch (Exception e)
        {
            Log.e(tag, "SMSReceiver General exception2");
        }


        return jsonObject;
    }

	 /*
	  * desc: Convert UTC time to IST time
	  * @param: takes year, month, date, hour, minute and second(UTC time values)
	  */

    private String convertUTCtoISTDateTime(int y, int m, int d, int h, int mn, int s)
    {
        StringBuilder sb = new StringBuilder();

        String sy = String.valueOf(y);
        String sm = String.valueOf(m);
        String sd = String.valueOf(d);
        String sh = String.valueOf(h);
        String smin = String.valueOf(mn);
        String ss = String.valueOf(s);

        String dateformat = sb.append(sy).append("-").append(sm).append("-").append(sd)
                .append("T").append(sh).append(":").append(smin).append(":")
                .append(ss).toString();

        System.out.println("SMSReceiver, datetime in stringformat : "+dateformat);


        SimpleDateFormat sdfgmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdfgmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat sdfmad = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdfmad.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        String convdatetime = null;
        try
        {
            Date inptdate = sdfgmt.parse(dateformat);
            convdatetime = sdfmad.format(inptdate);
        }
        catch (ParseException e)
        {
            Log.e(tag, "SMSReceiver Parse Exception");
        }
        catch (Exception e)
        {
            Log.e(tag, "SMSReceiver General Exception");
        }
        Log.d(tag, "Message received in IST Time: " + convdatetime);

        return convdatetime;
    }

    public void writeData(String data)
    {
        try
        {

            File direct = new File(Environment.getExternalStorageDirectory()+ "/ElephantDataFiles");

            if(!direct.exists())
            {
                direct.mkdir(); //directory is created;
            }

            File file = new File(direct,"ElephantData.txt");
            if(!file.exists()){
                file = new File(direct,"ElephantData.txt");
            }

            FileWriter fw = new FileWriter(file, true);
            fw.write(data);
            fw.append("\n");
            fw.flush();
            fw.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Otter", "Otter file write error");
        }

    }

    public void SendToServer(JSONObject message_json)
    {

        // Add any headers if required
        Header[] headers = new Header[] {
                new Header("Content-Type", "application/json"),
        };


        // Create the message to send
        Message message = null;
        try {
                message = new Message(
                    new URI(ElephantURL),
                    message_json.toString(), headers);
        }
        catch (Exception e)
        {
            Log.e("Otter", "Exception");
        }

        // Send the message to Reyna
        StoreService.start(context, message);


        // set Reyna logging level, same constant values as android.util.log (ERROR, WARN, INFO, DEBUG, VERBOSE)
        StoreService.setLogLevel(0);
    }
}

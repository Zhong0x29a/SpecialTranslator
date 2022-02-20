package cc0x29a.specialtranslator;

import org.json.JSONObject;

import java.net.URL;

public class EveryDayAPI {
    public static class EverydaySentence {
        public static String[] fetchEverydaySentence(){
            try{
                NetworkTools.MyHTTP myHTTP = new NetworkTools.MyHTTP();
                myHTTP.url=new URL("https://sentence.iciba.com/index.php?c=dailysentence&m=getTodaySentence");
                JSONObject dataJson=new JSONObject(myHTTP.GetHttpsURL());

                String content=dataJson.getString("content");
                String note=dataJson.getString("note");
                return new String[]{content,note};

            }catch (Exception e){
                System.err.println(e.getMessage());
            }
            return new String[]{"Network err?","网络错误？"};
        }
    }
}

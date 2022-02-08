package cc0x29a.specialtranslator;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class EveryDayAPI {
    public static class EverydaySentence {
        public static String[] fetchEverydaySentence(){
            try{
                String urlStr="https://sentence.iciba.com/index.php?c=dailysentence&m=getTodaySentence";
                URL url=new URL(urlStr);

                HttpsURLConnection conn=(HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                if(conn.getResponseCode()==200){
                    InputStream is=conn.getInputStream();
                    byte[] data=TranslateAPI.StreamTool.read(is);

                    is.close();
                    conn.disconnect();

                    JSONObject dataJSON=new JSONObject(new String(data, StandardCharsets.UTF_8));

                    String content=dataJSON.getString("content");
                    String note=dataJSON.getString("note");
                    return new String[]{content,note};
                }
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
            return new String[]{"Network err?","网络错误？"};
        }
    }
}

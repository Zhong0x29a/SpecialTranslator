package cc0x29a.specialtranslator;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class TranslateAPI {

    // Baidu 's API
    public static class BaiduAPI{
        public static String translate(String fromLang,String toLang,String text){
            try {
                String URL="https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token=";

                // todo: api token will expire in 30 days!
                String APIToken="24.ebbfc44b1c5316fdea6ec0179efe7fb4.2592000.1645786137.282335-25560885";

                // pack the data.
                JSONObject data=new JSONObject();
                data.put("from",fromLang);
                data.put("to",toLang);
                data.put("q",text);

                java.net.URL url = new URL(URL+APIToken);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);

                //设置运行输入,输出:
                conn.setDoOutput(true);
                conn.setDoInput(true);
                //Post method cant use caches.
                conn.setUseCaches(false);

                OutputStream out=conn.getOutputStream();
                out.write(( data.toString() ).getBytes());
                out.flush();

                if (conn.getResponseCode() == 200) {
                    System.out.println("ok!");

                    InputStream in = conn.getInputStream();
                    byte[] dataByte = StreamTool.read(in);

                    JSONObject jsonObject = new JSONObject(new String(dataByte, StandardCharsets.UTF_8));

                    StringBuffer sb = new StringBuffer();
                    for(int i=0;!jsonObject.getJSONObject("result").getJSONArray("trans_result")
                                .isNull(i);i++){
                        sb.append(jsonObject.getJSONObject("result").getJSONArray("trans_result")
                                .getJSONObject(i).getString("dst"));
                        sb.append("\n");

                        System.out.println(jsonObject.getJSONObject("result").getJSONArray("trans_result")
                                .getJSONObject(i).getString("dst"));
                    }
                    return sb.toString();
                }
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
            return ":<..";
        }
    }

    // YouDao 's API
    public static class YouDaoAPI{
        public static String translate(String fromLang,String toLang,String text){
            try{
                // http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=计算
                String TType=getTType(fromLang,toLang); // Translate type
                String URL_String="https://fanyi.youdao.com/translate?&doctype=json&type="+TType+"&i="+text;

                java.net.URL url=new URL(URL_String);
                HttpsURLConnection conn=(HttpsURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                conn.setDoOutput(true);
                conn.setDoInput(true);

                if(conn.getResponseCode()==200){
                    System.out.println("ok!");
                    InputStream inputStream= conn.getInputStream();
                    byte[] dataByte=StreamTool.read(inputStream);

                    JSONObject jsonObject=new JSONObject(new String(dataByte, StandardCharsets.UTF_8));

                    return jsonObject.getJSONArray("translateResult").getJSONArray(0)
                            .getJSONObject(0).getString("tgt");

                }

            }catch (Exception e){
                System.err.println(e.getMessage());
            }
            return ":(..";
        }

        /**
         * Adapt for the API
         * @param fromLang  from Language
         * @param toLang    to Language
         * @return          fit the API translation "type"
         */
        static String getTType(@NonNull String fromLang, String toLang){
            if( !(fromLang.equals(toLang)) ) {
                String TType= "AUTO"; // Translate type
                switch (fromLang) {
                    case "zh":
                        TType = "zh_cn2";
                        break;
                    case "en":
                        TType = "en2";
                        break;
                    default:
                        return "AUTO";
                }
                switch (toLang) {
                    case "zh":
                        TType += "zh_cn";
                        break;
                    case "en":
                        TType += "en";
                        break;
                }
                return TType;
            }else{
                return "AUTO";
            }
        }
    }

    public static class StreamTool {
        // Read all data from Stream.
        public static byte[] read(InputStream inStream) throws Exception{
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while((len = inStream.read(buffer)) != -1)
            {
                outStream.write(buffer,0,len);
            }
            inStream.close();
            return outStream.toByteArray();
        }
    }
}

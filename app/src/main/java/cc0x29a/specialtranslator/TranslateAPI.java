package cc0x29a.specialtranslator;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

public class TranslateAPI {

    // 360 's API
    public static class _360API{
        public static JSONObject lookupDictionary(String fromLang, String toLang, String text){
            // https://fanyi.so.com/index/search?eng=1&validate=&ignore_trans=0&query=apple
            try{
                int eng=getEng(fromLang);
                String urlStr="https://fanyi.so.com/index/search?eng="+eng+"&validate=&ignore_trans=0&query="+text;
                URL url=new URL(urlStr);

                HttpsURLConnection conn=(HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
//                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
//                conn.setUseCaches(false);
                conn.addRequestProperty("pro","fanyi");

                if(conn.getResponseCode()==200) {
                    InputStream is=conn.getInputStream();
                    byte[] data = StreamTool.read(is);
                    is.close();
                    conn.disconnect();

                    JSONObject dataJSON = new JSONObject(new String(data, StandardCharsets.UTF_8));

                    // phrase the json.
                    dataJSON=dataJSON.getJSONObject("data");

                    System.out.println(dataJSON);
                    return dataJSON;
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

            return null;
        }

        private static int getEng(String fromLang){
            if(fromLang.equals("en")){
                return 1;
            }else{
                return 0;
            }
        }
    }

    // Baidu 's API
    public static class BaiduAPI{
        // todo: api token will expire in 30 days!

//        final static String APIToken="24.ebbfc44b1c5316fdea6ec0179efe7fb4.2592000.1645786137.282335-25560885";

        public static String fetchNewToken(){
            try{
                HttpURLConnection conn=(HttpURLConnection) (new URL("http://0x29a.cc/rc/script/special_translator_fetch_new_token.php")).openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                InputStream is=conn.getInputStream();
                if(conn.getResponseCode()==200) {
                    String dataStr;
                    dataStr = new String(StreamTool.read(is),StandardCharsets.UTF_8);

                    is.close();
                    conn.disconnect();
                    return dataStr;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        public static String translateSentence(String fromLang, String toLang, String text, String APIToken){
            try {
                // pack the data.
                JSONObject data=new JSONObject();
                data.put("from",fromLang);
                data.put("to",toLang);
                data.put("q",text);

                java.net.URL url = new URL("https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token="+APIToken);
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
                out.close();

                if (conn.getResponseCode() == 200) {
                    System.out.println("ok!");

                    InputStream in = conn.getInputStream();
                    byte[] dataByte = StreamTool.read(in);

                    in.close();
                    conn.disconnect();

                    JSONObject jsonObject = new JSONObject(new String(dataByte, StandardCharsets.UTF_8));

                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<jsonObject.getJSONObject("result").getJSONArray("trans_result")
                                .length();i++){
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

        public static String[] lookUpDictionary(String fromLang,String toLang,String text){
            try{
                // pack the data.
                JSONObject dataSend=new JSONObject();
                dataSend.put("from",fromLang);
                dataSend.put("to",toLang);
                dataSend.put("q",text);

                URL url=new URL("https://aip.baidubce.com/rpc/2.0/mt/texttrans-with-dict/v1?access_token="+"APIToken"); //todo err token!
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
                out.write(( dataSend.toString() ).getBytes());
                out.flush();

                if(conn.getResponseCode()==200){
                    // do sth.
                    byte[] dataBytes = StreamTool.read(conn.getInputStream());

                    String testData="{\"lang\":\"1\",\"word_result\":{\"edict\":{\"item\":[{\"tr_group\":[{\"tr\":[\"fruit with red or yellow or green skin and sweet to tart crisp whitish flesh\"],\"example\":[],\"\n" +
                            "similar_word\":[]},{\"tr\":[\"native Eurasian tree widely cultivated in many varieties for its firm rounded edible fruits\"],\"example\":[],\"similar_word\":[\"orchard apple\n" +
                            "tree\",\"Malus pumila\"]}],\"pos\":\"noun\"}],\"word\":\"apple\"},\"zdict\":\"\",\"simple_means\":{\"word_name\":\"apple\",\"from\":\"original\",\"word_means\":[\"苹果\"],\"exchange\":\n" +
                            "{\"word_pl\":[\"apples\"]},\"tags\":{\"core\":[\"高考\",\"考研\"],\"other\":[\"\"]},\"symbols\":[{\"ph_en\":\"pl\",\"ph_am\":\"pl\",\"parts\":[{\"part\":\"n.\",\"means\":[\"苹果\"]}],\"\n" +
                            "ph_other\":\"\"}]},\"general_knowledge\":{\"similar_words\":[{\"en\":\"pear\",\"zh\":\"梨\"},{\"en\":\"peach\",\"zh\":\"桃子\"},{\"en\":\"apple\",\"zh\":\"苹果\"},{\"en\":\"grape\",\"zh\":\"葡\n" +
                            "萄\"},{\"en\":\"banana\",\"zh\":\"香蕉\"},{\"en\":\"cherry\",\"zh\":\"樱桃\"},{\"en\":\"mulberry\",\"zh\":\"桑椹\"},{\"en\":\"persimmon\",\"zh\":\"柿子\"},{\"en\":\"hippophae\",\"zh\":\"沙\n" +
                            "棘\"},{\"en\":\"strawberry\",\"zh\":\"草莓\"},{\"en\":\"watermelon\",\"zh\":\"西瓜\"},{\"en\":\"pomegranate\",\"zh\":\"石榴\"}],\"word_name\":\"apple\",\"word_type\":\"水果\",\"\n" +
                            "word_lang\":\"en\"}}}";

                    JSONObject dataJson=new JSONObject(testData);
                    JSONObject word_result=dataJson.getJSONObject("word_result");

                    System.out.println(word_result.getJSONObject("edict").getJSONArray("item").length());
                }

            }catch (Exception e){
                System.err.println(e.getMessage());
            }

            return null;
        }
    }

    // YouDao 's API
    public static class YouDaoAPI{
        public static String translate(String fromLang,String toLang,String text){
            try{
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

                    inputStream.close();
                    conn.disconnect();

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
                String TType; // Translate type
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

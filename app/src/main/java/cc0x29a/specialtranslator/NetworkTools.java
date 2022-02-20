package cc0x29a.specialtranslator;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class NetworkTools {

    public static class MyHTTP {
        public URL url;

        public String GetHttpURL() throws Exception{
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(5000);
            httpConn.setDoInput(true);

            if(httpConn.getResponseCode()==200){
                byte[] result= TranslateAPI.StreamTool.read(httpConn.getInputStream());

                httpConn.disconnect();
                return new String(result, StandardCharsets.UTF_8);
            }else{
                return null;
            }
        }

        public String GetHttpsURL() throws Exception{
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();

            httpsConn.setRequestMethod("GET");
            httpsConn.setConnectTimeout(5000);
            httpsConn.setReadTimeout(5000);
            httpsConn.setDoInput(true);

            if(httpsConn.getResponseCode()==200){
                byte[] result= TranslateAPI.StreamTool.read(httpsConn.getInputStream());

                httpsConn.disconnect();
                return new String(result,StandardCharsets.UTF_8);
            }else{
                return null;
            }
        }

        public String PostHttpURL(String data) throws Exception{
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(5000);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);

            OutputStream optStream = httpConn.getOutputStream();
            optStream.write(data.getBytes(StandardCharsets.UTF_8));
            optStream.flush();
            optStream.close();

            if(httpConn.getResponseCode()==200){
                byte[] result = TranslateAPI.StreamTool.read(httpConn.getInputStream());
                httpConn.disconnect();
                return new String(result,StandardCharsets.UTF_8);
            }else {
                return null;
            }
        }
        public String PostHttpsURL(String data) throws Exception{
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();

            httpsConn.setRequestMethod("POST");
            httpsConn.setConnectTimeout(5000);
            httpsConn.setReadTimeout(5000);
            httpsConn.setDoOutput(true);
            httpsConn.setDoInput(true);
            httpsConn.setUseCaches(false);

            OutputStream optStream = httpsConn.getOutputStream();
            optStream.write(data.getBytes(StandardCharsets.UTF_8));
            optStream.flush();
            optStream.close();

            if(httpsConn.getResponseCode()==200){
                byte[] result = TranslateAPI.StreamTool.read(httpsConn.getInputStream());
                httpsConn.disconnect();
                return new String(result,StandardCharsets.UTF_8);
            }else {
                return null;
            }
        }
    }

}

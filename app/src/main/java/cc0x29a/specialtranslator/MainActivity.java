package cc0x29a.specialtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText etTextToTrans;
    Button btnStartTrans;
    TextView tvTextOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTextToTrans=findViewById(R.id.mainAct_TextToTrans);
        btnStartTrans=findViewById(R.id.mainAct_StartTrans);
        tvTextOutput=findViewById(R.id.mainAct_TextOutput);

        btnStartTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String textToTrans=etTextToTrans.getText().toString();
                        String textOutput=transText(textToTrans);
                        setTvTextOutputCont(textOutput);
                    }
                }).start();
            }
        });

    }

    // Set the Output view new content
    public void setTvTextOutputCont(String text){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTextOutput.setText(text);
            }
        });
    }

    // Use baidu API translate the text.
    public String transText(String text){
        try {
            String URL="https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token=";
            String APIToken="24.ebbfc44b1c5316fdea6ec0179efe7fb4.2592000.1645786137.282335-25560885";

            // pack the data.
            JSONObject data=new JSONObject();
            data.put("from","en");
            data.put("to","zh");
            data.put("q",text);

            URL url = new URL(URL+APIToken);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            System.out.println("fine78");
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            //设置运行输入,输出:
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //Post method cant use caches.
            conn.setUseCaches(false);
//            String data = "passwd="+ URLEncoder.encode(passwd, "UTF-8")+ "&number="+ URLEncoder.encode(number, "UTF-8");

            OutputStream out = conn.getOutputStream();
            out.write(( data.toString() ).getBytes());
            out.flush();

            System.out.println("flushed!");

            if (conn.getResponseCode() == 200) {
                System.out.println("ok!");

                InputStream in = conn.getInputStream();
                byte[] dataByte = StreamTool.read(in);

                JSONObject jsonObject = new JSONObject(new String(dataByte, StandardCharsets.UTF_8));

                String textOutput=jsonObject.getJSONObject("result").getJSONArray("trans_result")
                        .getJSONObject(0).getString("dst");
                return textOutput;
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            System.out.println("err!!");
        }
        return "Null! Error(s) occurred!!";
    }

    public static class StreamTool {
        // Read all data from Stream.
        public static byte[] read(InputStream inStream) throws Exception{
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = inStream.read(buffer)) != -1)
            {
                outStream.write(buffer,0,len);
            }
            inStream.close();
            return outStream.toByteArray();
        }
    }
}
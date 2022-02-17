package cc0x29a.specialtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class About extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        try {
            TextView tv=findViewById(R.id.aboutAct_app_info);
            tv.setText("Special Translator - "+getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new Thread(()->{
            try{
                URL url=new URL("https://raw.githubusercontent.com/Galaxy-cube/SpecialTranslator/master/version");
                HttpsURLConnection con=(HttpsURLConnection) url.openConnection();

                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setReadTimeout(10000);
                con.setConnectTimeout(10000);

                if(con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();
                    byte[] data=TranslateAPI.StreamTool.read(is);
                    is.close();

                    int version_code=Integer.parseInt(new String(data, StandardCharsets.UTF_8));
                    System.out.println(version_code);

                }
                con.disconnect();

            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
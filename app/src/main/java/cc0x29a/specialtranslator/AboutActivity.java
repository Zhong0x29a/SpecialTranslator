package cc0x29a.specialtranslator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class AboutActivity extends AppCompatActivity {

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

        findViewById(R.id.aboutAct_upgradeBtn).setOnClickListener((view) -> {
            Intent intent= new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://github.com/Galaxy-cube/SpecialTranslator/raw/master/app/release/app-release.apk");
            intent.setData(content_url);
            startActivity(intent);
        });

        // fetch new version code.
        new Thread(()->{
            try{
                // todo: 封装http方法，以简化代码！
                URL url=new URL("https://gitee.com/galaxy-cube/SpecialTranslator/raw/master/version");
                HttpsURLConnection con=(HttpsURLConnection) url.openConnection();

                con.setRequestMethod("GET");
                con.setDoInput(true);

                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);

                if(con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();
                    byte[] data=TranslateAPI.StreamTool.read(is);
                    is.close();
                    con.disconnect();

                    final int newVersionCode=Integer.parseInt(new String(data, StandardCharsets.UTF_8));

                    AboutActivity.this.runOnUiThread(()->{
                        TextView t=findViewById(R.id.aboutAct_app_newVer);
                        try {
                            t.setText("Current ver: "+getPackageManager().getPackageInfo(getPackageName(),0).versionCode+"\nLatest ver: "+newVersionCode);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    if(newVersionCode > getPackageManager().getPackageInfo(getPackageName(),0).versionCode){
                        url=new URL("https://gitee.com/galaxy-cube/SpecialTranslator/raw/master/new_feature");
                        con=(HttpsURLConnection) url.openConnection();

                        con.setRequestMethod("GET");
                        con.setDoInput(true);

                        con.setReadTimeout(5000);
                        con.setConnectTimeout(5000);

                        if(con.getResponseCode()==200){
                            is = con.getInputStream();
                            final byte[] data2=TranslateAPI.StreamTool.read(is);
                            is.close();
                            con.disconnect();
                            AboutActivity.this.runOnUiThread(()->{
                                TextView t=findViewById(R.id.aboutAct_app_newVer_info);
                                t.setText("New features"+(new String(data2,StandardCharsets.UTF_8)) );
                                Toast.makeText(AboutActivity.this,"Has new version! \nClick the button to upgrade",Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                }else{
                    AboutActivity.this.runOnUiThread(()->{
                        Toast.makeText(AboutActivity.this,"Something Error!!",Toast.LENGTH_LONG).show();
                    });
                    con.disconnect();
                }
            }catch (Exception e){
                AboutActivity.this.runOnUiThread(()->{
                    Toast.makeText(AboutActivity.this,"Network Error!!",Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        }).start();

    }
}
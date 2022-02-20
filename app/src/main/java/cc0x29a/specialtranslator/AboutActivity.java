package cc0x29a.specialtranslator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URL;

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
                NetworkTools.MyHTTP myHTTP=new NetworkTools.MyHTTP();
                myHTTP.url= new URL("https://gitee.com/galaxy-cube/SpecialTranslator/raw/master/version");
                String data=myHTTP.GetHttpsURL();

                if(data != null){
                    final int newVersionCode=Integer.parseInt(data);

                    AboutActivity.this.runOnUiThread(()->{
                        TextView t=findViewById(R.id.aboutAct_app_newVer);
                        try {
                            t.setText("Current ver: "+getPackageManager().getPackageInfo(getPackageName(),0).versionCode+"\nLatest ver: "+newVersionCode);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    if(newVersionCode > getPackageManager().getPackageInfo(getPackageName(),0).versionCode){
                        NetworkTools.MyHTTP myHTTP2= new NetworkTools.MyHTTP();
                        myHTTP2.url = new URL("https://gitee.com/galaxy-cube/SpecialTranslator/raw/master/new_feature");

                        String data2=myHTTP2.GetHttpsURL();

                        if(data2 != null) {
                            AboutActivity.this.runOnUiThread(() -> {
                                TextView t = findViewById(R.id.aboutAct_app_newVer_info);
                                t.setText("New features:\n" + data2);
                                Toast.makeText(AboutActivity.this, "Has new version! \nClick the button to upgrade", Toast.LENGTH_LONG).show();
                            });
                        }
                    }else{
                        AboutActivity.this.runOnUiThread(()->{
                            findViewById(R.id.aboutAct_upgradeBtn).setClickable(false);
                        });
                    }
                }else{
                    AboutActivity.this.runOnUiThread(()->{
                        Toast.makeText(AboutActivity.this,"Network Error!!",Toast.LENGTH_LONG).show();
                    });
                }
            }catch(Exception e){
                AboutActivity.this.runOnUiThread(()->{
                    Toast.makeText(AboutActivity.this,"Network Error!!",Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        }).start();

    }
}
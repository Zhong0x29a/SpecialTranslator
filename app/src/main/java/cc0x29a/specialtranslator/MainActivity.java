package cc0x29a.specialtranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // todo: TTS 
    ScrollView svSelectLang;
    RadioGroup rgLangGroup;
    RadioButton rbSelectAuto,rbSelectEn,rbSelectCn;

    Button btnSelectSouLang,btnSelectDesLang;

    EditText etTextToTrans;
    TextView tvTextOutput;

    String fromLang,toLang;
    String API;
    String BaiduAPIToken;

    // languages set
    HashMap<Integer,String> id2lang=new HashMap<>();
    HashMap<String,Integer> lang2id=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Select Language section.
        id2lang.put(R.id.mainAct_Select_Auto,"auto");
        id2lang.put(R.id.mainAct_Select_English,"en");
        id2lang.put(R.id.mainAct_Select_Chinese,"zh");
        lang2id.put("auto",R.id.mainAct_Select_Auto);
        lang2id.put("en",R.id.mainAct_Select_English);
        lang2id.put("zh",R.id.mainAct_Select_Chinese);

        svSelectLang=findViewById(R.id.mainAct_SelectLang);
        rgLangGroup=findViewById(R.id.mainAct_LangRadioGroup);
        rbSelectAuto=findViewById(R.id.mainAct_Select_Auto);
        rbSelectEn=findViewById(R.id.mainAct_Select_English);
        rbSelectCn=findViewById(R.id.mainAct_Select_Chinese);

        // Click to chang language section.
        btnSelectSouLang=findViewById(R.id.mainAct_SouLang);
        btnSelectDesLang=findViewById(R.id.mainAct_DesLang);

        // Inout and output section.
        etTextToTrans=findViewById(R.id.mainAct_TextToTrans);
        tvTextOutput=findViewById(R.id.mainAct_TextOutput_tv);

        // Bind Listener.
        // Languages Radio group.
        rgLangGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            // set the lang.
            try {
                if (svSelectLang.getTag().toString().equals("SourceLang")) {
                    fromLang = Objects.requireNonNull(id2lang.get(i));
                    btnSelectSouLang.setText(fromLang);
                } else if (svSelectLang.getTag().toString().equals("DestinyLang")) {
                    toLang = Objects.requireNonNull(id2lang.get(i));
                    btnSelectDesLang.setText(toLang);
                }
            }catch (NullPointerException e){
                System.err.println(e.getMessage());
            }

            // hide the scroll view
            showOrHideRadioGroupAuto("");
        });

        // click to select another language.
        btnSelectSouLang.setOnClickListener(cliListenerChangeLangBTN);
        findViewById(R.id.mainAct_SwpSDLang).setOnClickListener(cliListenerChangeLangBTN);
        btnSelectDesLang.setOnClickListener(cliListenerChangeLangBTN);

        // open/close menu
        findViewById(R.id.mainAct_MenuBtn).setOnClickListener(view -> openOrCloseTopBarMenu());
        findViewById(R.id.mainAct_TopBarMenu).setOnClickListener(view -> openOrCloseTopBarMenu());

        // Top bar menu items
        findViewById(R.id.mainAct_MenuUseBaiduAPI).setOnClickListener(menuOnCliListener);
        findViewById(R.id.mainAct_MenuUseYoudaoAPI).setOnClickListener(menuOnCliListener);
        findViewById(R.id.mainAct_MenuShowStcEvrDay).setOnClickListener(menuOnCliListener);
        findViewById(R.id.mainAct_openAbout).setOnClickListener(menuOnCliListener);

        // Start Translation.
        findViewById(R.id.mainAct_StartTrans).setOnClickListener(view -> new Thread( () -> {
            String textToTrans=etTextToTrans.getText().toString();

            // if search for a word, look up the dictionary.
            if(isAWord(textToTrans)){
                // hide the sentence opt view & display the word opt view.
                showAndHideOptView(true);

                // display the content.
                textToTrans=textToTrans.toLowerCase(Locale.ROOT); // to avoid the api return sentence translation.
                displayWordLookupDetail(Objects.requireNonNull(TranslateAPI._360API.lookupDictionary(fromLang, toLang, textToTrans)));
            }else{
                // hide the word opt view & display the sentence opt view.
                showAndHideOptView(false);
                // if search sentence
                String textOutput;
                if ("youdao".equals(API)) {
                    textOutput = TranslateAPI.YouDaoAPI.translate(fromLang, toLang, textToTrans);
                } else {
                    textOutput = TranslateAPI.BaiduAPI.translateSentence(fromLang, toLang, textToTrans, BaiduAPIToken);
                }
/*
                switch (API) {
                    case "baidu":
                        textOutput = TranslateAPI.BaiduAPI.translateSentence(fromLang, toLang, textToTrans,BaiduAPIToken);
                        break;
                    case "youdao":
                        textOutput = TranslateAPI.YouDaoAPI.translate(fromLang, toLang, textToTrans);
                        break;
                    default:
                        textOutput = TranslateAPI.BaiduAPI.translateSentence(fromLang, toLang, textToTrans, BaiduAPIToken);
                }
*/
                setTvTextOutputCont(textOutput);
            }
        }).start());

        // get "last status"
        // to refresh Baidu api token.
        new Thread(()->{
            long timeCurr=System.currentTimeMillis();
            SharedPreferences sharedPreferences=getSharedPreferences("info",MODE_PRIVATE);
            long BDTokenExpDate=Long.parseLong(sharedPreferences.getString("BaiduToken_expDate","0"));
            if(timeCurr >= BDTokenExpDate){
                String token=TranslateAPI.BaiduAPI.fetchNewToken();
                sharedPreferences.edit().putString("BaiduToken",token).apply();
                sharedPreferences.edit().putString("BaiduToken_expDate",(timeCurr+7*24*60*60*1000)+"").apply();
                BaiduAPIToken=token;
            }
        }).start();

        // fetch baidu api token from local
        BaiduAPIToken=getSharedPreferences("info",MODE_PRIVATE).getString("BaiduToken","");

        // set default sourceLang & DestinyLang
        new Thread(()-> {
            SharedPreferences sharedPreferences=getSharedPreferences("info",MODE_PRIVATE);
            fromLang=sharedPreferences.getString("fromLang","en");
            toLang=sharedPreferences.getString("toLang","zh");
            API=sharedPreferences.getString("API","baidu");

            MainActivity.this.runOnUiThread(()->{
                btnSelectSouLang.setText(fromLang);
                btnSelectDesLang.setText(toLang);
            });
        }).start();

        // todo: set it fold able
        // hide the everyday sentence.
        findViewById(R.id.mainAct_StcEvrDay_tv).setOnLongClickListener((view) -> {
            findViewById(R.id.mainAct_StcEvrDay_tv).setVisibility(View.GONE);
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // fetch the everyday sentence.
        new Thread(()->{
            String[] strArr=EveryDayAPI.EverydaySentence.fetchEverydaySentence();
            setTvTextStcEvrDay(strArr[0],strArr[1]);
        }).start();
    }

    // Change language
    @SuppressLint("NonConstantResourceId")
    View.OnClickListener cliListenerChangeLangBTN = view -> {
        switch (view.getId()){
            case R.id.mainAct_SouLang:
                // edit the tag
                svSelectLang.setTag("SourceLang");
                showOrHideRadioGroupAuto("from");
                break;
            case R.id.mainAct_SwpSDLang:
                if( !(fromLang.equals("auto")) ){
                    svSelectLang.setVisibility(View.GONE);
                    svSelectLang.setTag(null);
                    String temp = fromLang;
                    fromLang = toLang;
                    toLang = temp;
                }
                break;
            case R.id.mainAct_DesLang:
                svSelectLang.setTag("DestinyLang");
                showOrHideRadioGroupAuto("to");
                break;
        }
        btnSelectSouLang.setText(fromLang);
        btnSelectDesLang.setText(toLang);

        getSharedPreferences("info",MODE_PRIVATE).edit().putString("fromLang",fromLang).apply();
        getSharedPreferences("info",MODE_PRIVATE).edit().putString("toLang",toLang).apply();
    };

    // onClickListener when menu item selected.
    @SuppressLint("NonConstantResourceId")
    View.OnClickListener menuOnCliListener = view -> {

        switch (view.getId()){
            case R.id.mainAct_MenuUseBaiduAPI:
                API="baidu";
                getSharedPreferences("info",MODE_PRIVATE).edit().putString("API","baidu").apply();
                break;
            case R.id.mainAct_MenuUseYoudaoAPI:
                API="youdao";
                getSharedPreferences("info",MODE_PRIVATE).edit().putString("API","youdao").apply();
                break;
            case R.id.mainAct_MenuShowStcEvrDay:
                findViewById(R.id.mainAct_StcEvrDay_tv).setVisibility(View.VISIBLE);
                break;
            case R.id.mainAct_openAbout:
                startActivity(new Intent(this,About.class));
                break;
        }


        openOrCloseTopBarMenu();
    };

//    @SuppressLint("NullPointerException")
    public void showOrHideRadioGroupAuto(String fromOrTo){
        if(svSelectLang.getVisibility()==View.VISIBLE){
            svSelectLang.setVisibility(View.GONE);
        }else{
            if(fromOrTo.equals("from")) {
                rbSelectAuto.setClickable(true);
                RadioButton rb = findViewById(lang2id.get(fromLang));
                rb.setChecked(true);
            }else if(fromOrTo.equals("to")){
                rbSelectAuto.setClickable(false);
                RadioButton rb = findViewById(lang2id.get(toLang));
                rb.setChecked(true);
            }
            svSelectLang.setVisibility(View.VISIBLE);
        }
    }

    public void openOrCloseTopBarMenu(){
//        mainAct_mainContainLayout
        LinearLayout l=findViewById(R.id.mainAct_TopBarMenu);
        if(l.getVisibility()==View.VISIBLE){
            l.setVisibility(View.GONE);
        }else{
            l.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Change the output view visibility.
     * @param whichOne true -> word view
     *                 false -> sentence view
     */
    public void showAndHideOptView(boolean whichOne){
        if (whichOne){
            // hide the sentence opt view & display the word opt view.
            MainActivity.this.runOnUiThread(()->{
                findViewById(R.id.mainAct_TextOutput_tv).setVisibility(View.GONE);
                findViewById(R.id.mainAct_wordOpt_sv).setVisibility(View.VISIBLE);
            });
        }else{
            // hide the word opt view & display the sentence opt view.
            MainActivity.this.runOnUiThread(()->{
                findViewById(R.id.mainAct_TextOutput_tv).setVisibility(View.VISIBLE);
                findViewById(R.id.mainAct_wordOpt_sv).setVisibility(View.GONE);
            });
        }
    }

    // Set the Output view new content
    @SuppressLint("SetTextI18n")
    public void setTvTextOutputCont(String text){
        MainActivity.this.runOnUiThread(() -> tvTextOutput.setText(text+"\n\n--by "+API));
    }

    // display the word lookup details.
    @SuppressLint("SetTextI18n")
    public void displayWordLookupDetail(JSONObject data){
        // init?
        try {
            String[] expArr = fetchAllJsonArrayContent(data.getJSONObject("explain").getJSONArray("translation"));
            String[] expArr2 = fetchJsonArrItem(data.getJSONObject("explain").getJSONArray("exsentence"),"Title","Body");
            String[] expArrAll = new String[expArr.length+ expArr2.length];
            System.arraycopy(expArr,0, expArrAll,0,expArr.length);
            System.arraycopy(expArr2,0,expArrAll,expArr.length,expArr2.length);

            TextView tvFanyi= findViewById(R.id.mainAct_wordOpt_fanyi);
            ListView lvExplainTrans= findViewById(R.id.mainAct_wordOpt_explainTranslation);
            ArrayAdapter<String> adtExplainTrans= new ArrayAdapter<>(this, R.layout.listview_explain_word, expArrAll);


            MainActivity.this.runOnUiThread(()->{
                try {

                    if (data.getJSONObject("explain").getJSONObject("phonetic").isNull("英")) {
                        tvFanyi.setText(data.getString("fanyi"));
                    }else {
                        tvFanyi.setText(data.getString("fanyi") + " " + data.getJSONObject("explain").getJSONObject("phonetic").getString("英"));
                    }
                    lvExplainTrans.setDivider(null);
                    lvExplainTrans.setAdapter(adtExplainTrans);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });

//            tvExplainTrans.setText(data.getJSONObject("explain").getString("translation"));
        } catch (JSONException e) {
            e.printStackTrace();
            MainActivity.this.runOnUiThread(()->{
                try {
                    TextView tvFanyi= findViewById(R.id.mainAct_wordOpt_fanyi);
                    ListView lvExplainTrans= findViewById(R.id.mainAct_wordOpt_explainTranslation);
                    ArrayAdapter<String> adtExplainTrans= new ArrayAdapter<>(this, R.layout.listview_explain_word);
                    tvFanyi.setText(data.getString("fanyi"));
                    lvExplainTrans.setDivider(null);
                    lvExplainTrans.setAdapter(adtExplainTrans);
                }catch (Exception ee){
                    ee.printStackTrace();
                }
            });
        }
    }

    // Display the Everyday Sentence.
    @SuppressLint("SetTextI18n")
    public void setTvTextStcEvrDay(String textEng, String textZh){
        MainActivity.this.runOnUiThread(() -> {
            TextView tv=findViewById(R.id.mainAct_StcEvrDay_tv);
            tv.setText("Everyday sentence: \n\n    "+textEng+"\n    "+textZh+"\n\n-by iciba");
        });
    }

    public boolean isAWord(String text){
        // [a-zA-z]+
        return text.split("[a-zA-z]+").length==0;
    }

    public String[] fetchAllJsonArrayContent(JSONArray jsonArray){
        try {
            String[] strArr = new String[jsonArray.length()+1];
            strArr[0]="Word Explanation: ";
            for (int i = 0; i < jsonArray.length(); i++) {
                strArr[i+1]="   "+jsonArray.getString(i);
            }
            return strArr;
        }catch (Exception e){
            e.printStackTrace();
            return new String[0];
        }
    }

    public String[] fetchJsonArrItem(JSONArray jsonArray,String name1,String name2){
        try{
            String[] strArr= new String[jsonArray.length()+1];
            strArr[0]="\nExample Sentences: ";
            for(int i=0; i < jsonArray.length(); i++){
                strArr[i+1]=(i+1)+".  "+jsonArray.getJSONObject(i).getString(name1).replace("<b>","").replace("</b>","");
                strArr[i+1]+="\n    "+jsonArray.getJSONObject(i).getString(name2);
//                strArr[i]="----------";
            }
            return strArr;
        }catch (Exception e){
            e.printStackTrace();
            return new String[0];
        }
    }
}

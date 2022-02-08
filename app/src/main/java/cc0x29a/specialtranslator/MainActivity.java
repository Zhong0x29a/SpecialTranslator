package cc0x29a.specialtranslator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;

import android.annotation.SuppressLint;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // todo: clean the codes!!!!
    ScrollView svSelectLang;
    RadioGroup rgLangGroup;
    RadioButton rbSelectAuto,rbSelectEn,rbSelectCn;

    Button btnSelectSouLang,btnSelectDesLang;
    Button btnSwapSDLang;

    EditText etTextToTrans;
    Button btnStartTrans;
    TextView tvTextOutput;

//    Button btnMenu;
    Button btnUseBaidu,btnUseYoudao;

    String fromLang,toLang;
    String API;
    String BaiduAPIToken;

    // languages set
    HashMap id2lang=new HashMap<Integer,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // todo: save "last status"
        SharedPreferences sharedPreferences=getSharedPreferences("token",MODE_PRIVATE);

        new Thread(()->{
            long timeCurr=System.currentTimeMillis();
            long BDTokenExpDate=Long.parseLong(sharedPreferences.getString("BaiduToken_expDate","0"));
            if(timeCurr >= BDTokenExpDate){
                String token=TranslateAPI.BaiduAPI.fetchNewToken();
                sharedPreferences.edit().putString("BaiduToken",token).apply();
                sharedPreferences.edit().putString("BaiduToken_expDate",(timeCurr+7*24*60*60*1000)+"").apply();
                BaiduAPIToken=token;
            }
        }).start();

        // set default sourceLang & DestinyLang
        fromLang="en";
        toLang="zh";
        API="baidu";

        // fetch baidu api token
        BaiduAPIToken=sharedPreferences.getString("BaiduToken","");

        // Select Language section.
        id2lang.put(R.id.mainAct_Select_Auto,"auto");
        id2lang.put(R.id.mainAct_Select_English,"en");
        id2lang.put(R.id.mainAct_Select_Chinese,"zh");

        svSelectLang=findViewById(R.id.mainAct_SelectLang);
        rgLangGroup=findViewById(R.id.mainAct_LangRadioGroup);
        rbSelectAuto=findViewById(R.id.mainAct_Select_Auto);
        rbSelectEn=findViewById(R.id.mainAct_Select_English);
        rbSelectCn=findViewById(R.id.mainAct_Select_Chinese);

        // Click to chang language section.
        btnSelectSouLang=findViewById(R.id.mainAct_SouLang);
        btnSelectDesLang=findViewById(R.id.mainAct_DesLang);
        btnSwapSDLang=findViewById(R.id.mainAct_SwpSDLang);

        // Inout and output section.
        etTextToTrans=findViewById(R.id.mainAct_TextToTrans);
        btnStartTrans=findViewById(R.id.mainAct_StartTrans);
        tvTextOutput=findViewById(R.id.mainAct_TextOutput_tv);

        // TopBar section.
//        btnMenu=findViewById(R.id.mainAct_MenuBtn);
        btnUseBaidu=findViewById(R.id.mainAct_MenuUseBaiduAPI);
        btnUseYoudao=findViewById(R.id.mainAct_MenuUseYoudaoAPI);

        // Bind Listener.

        rgLangGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            // set the lang.
            try {
                if (svSelectLang.getTag().toString().equals("SourceLang")) {
                    fromLang = Objects.requireNonNull(id2lang.get(i)).toString();
                    btnSelectSouLang.setText(fromLang);
                } else if (svSelectLang.getTag().toString().equals("DestinyLang")) {
                    toLang = Objects.requireNonNull(id2lang.get(i)).toString();
                    btnSelectDesLang.setText(toLang);
                }
            }catch (NullPointerException e){
                System.err.println(e.getMessage());
            }

            // hide the scroll view
            showOrHideRadioGroupAuto("no");
//            setRadioGroupChecked(false,fromLang);
        });

        // click to select another language.
        btnSelectSouLang.setOnClickListener(cliListenerChangeLang);
        btnSwapSDLang.setOnClickListener(cliListenerChangeLang);
        btnSelectDesLang.setOnClickListener(cliListenerChangeLang);

        // open/close menu
        findViewById(R.id.mainAct_MenuBtn).setOnClickListener(view -> openOrCloseMenu());
        findViewById(R.id.mainAct_TopBarMenu).setOnClickListener(view -> openOrCloseMenu());

        // select API
        btnUseBaidu.setOnClickListener(cliListenerChangeAPI);
        btnUseYoudao.setOnClickListener(cliListenerChangeAPI);

        // Start Translation.
        btnStartTrans.setOnClickListener(view -> new Thread( () -> {
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
                setTvTextOutputCont(textOutput);
            }
        }).start());

        // fetch the everyday sentence.
        new Thread(()->{
            String[] strArr=EveryDayAPI.EverydaySentence.fetchEverydaySentence();
            setTvTextStcEvrDay(strArr[0],strArr[1]);
        }).start();

        // todo: set it fold able
        // hide the everyday sentence.
        findViewById(R.id.mainAct_StcEvrDay).setOnLongClickListener((view) -> {
            findViewById(R.id.mainAct_StcEvrDay).setVisibility(View.GONE);
            return true;
        });
    }

    // Change language
    @SuppressLint("NonConstantResourceId")
    View.OnClickListener cliListenerChangeLang = view -> {
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
    };

    // Change API
    View.OnClickListener cliListenerChangeAPI = view -> {
        switch (view.getId()){
            case R.id.mainAct_MenuUseBaiduAPI:
                API="baidu";
                break;
            case R.id.mainAct_MenuUseYoudaoAPI:
                API="youdao";
                break;
            default:
                API="baidu";
        }
        openOrCloseMenu();
    };

    // Show/hide Checkbox.
    //todo: may no use? or simplify?
    /**
     * use to set the visibility of the radio group.
     * @param isDestLang    if true, set "auto" uncheckable.
     * @param checkedOne    set which "were" selected.
     */
    public void setRadioGroupChecked(boolean isDestLang,String checkedOne){
        rbSelectAuto.setClickable(!isDestLang);
        switch (checkedOne){
            case "auto":
                rbSelectAuto.setChecked(true);
                break;
            case "en":
                rbSelectEn.setChecked(true);
                break;
            case "zh":
                rbSelectCn.setChecked(true);
                break;
        }
    }

    public void showOrHideRadioGroupAuto(String fromOrTo){
        if(svSelectLang.getVisibility()==View.VISIBLE){
            svSelectLang.setVisibility(View.GONE);
        }else{
            if(fromOrTo.equals("from")) {
                setRadioGroupChecked(false, fromLang);
            }else if(fromOrTo.equals("to")){
                setRadioGroupChecked(true,toLang);
            }
            svSelectLang.setVisibility(View.VISIBLE);
        }
    }

    public void openOrCloseMenu(){
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
    public void setTvTextOutputCont(String text){
        MainActivity.this.runOnUiThread(() -> tvTextOutput.setText(text+"\n\n--by "+API));
    }

    // display the word lookup details.
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
    public void setTvTextStcEvrDay(String textEng,String textZh){
        MainActivity.this.runOnUiThread(() -> {
            TextView tv=findViewById(R.id.mainAct_StcEvrDay);
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

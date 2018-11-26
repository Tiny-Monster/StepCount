package com.tinymonster.stepcount;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.droi.sdk.DroiError;
import com.droi.sdk.core.DroiCondition;
import com.droi.sdk.core.DroiQuery;
import com.droi.sdk.core.DroiUser;

import java.util.List;

public class LogActivity extends AppCompatActivity {
    private TextView login_register;
    private EditText Login_username;
    private EditText Login_password;
    private Button login;
    private String name="";
    private String pwd="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        initView();
    }
    private void initView(){
        Login_username=(EditText)findViewById(R.id.Login_username);
        Login_password=(EditText)findViewById(R.id.Login_password);
        login=(Button)findViewById(R.id.login);
        login_register=(TextView)findViewById(R.id.login_register);
        login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LogActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=Login_username.getText().toString();
                pwd=Login_password.getText().toString();
                if(name.equals("")||pwd.equals("")||name==null||pwd==null){
                    Toast.makeText(LogActivity.this,"请输入账号密码!",Toast.LENGTH_SHORT).show();
                }else {
                    DroiError error=new DroiError();
                    DroiUser droiUser=DroiUser.login(name,pwd,DroiUser.class,error);
//                    if(error.isOk()&&droiUser!=null&&droiUser.isAuthorized()){
                        SharedPreferences.Editor editor=getSharedPreferences("USER",MODE_PRIVATE).edit();
                        editor.putString("name",name);
                        editor.apply();
                        Intent intent=new Intent(LogActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
//                    }else {
//                        Toast.makeText(LogActivity.this,"账号密码错误",Toast.LENGTH_SHORT).show();
//                    }
//                    DroiError error = new DroiError();
//                    DroiQuery query = DroiQuery
//                            .Builder.newBuilder()
//                            .query(user.class)
//                            .where(DroiCondition.eq("name",name).and(DroiCondition.eq("pwd",pwd)))
//                            .build();
//                    List<user> result_list=query.runQuery(error);
//                    Log.e("LogActivity","result_list大小:"+result_list.size());
//                    if(error.isOk()&&result_list.size()>0){
//                        SharedPreferences.Editor editor=getSharedPreferences("USER",MODE_PRIVATE).edit();
//                        editor.putString("name",name);
//                        editor.apply();
//                        Intent intent=new Intent(LogActivity.this,MainActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }else {
//                        Toast.makeText(LogActivity.this,"账号密码错误",Toast.LENGTH_SHORT).show();
//                    }
                }
            }
        });
    }
}

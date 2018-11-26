package com.tinymonster.stepcount;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.droi.sdk.DroiCallback;
import com.droi.sdk.DroiError;
import com.droi.sdk.core.DroiUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText register_Edt_phone;
    private EditText register_Edt_password;
    private Button register;
    private String name="";
    private String pwd="";
    private ImageView register_img_return;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }
    private void initView(){
        register_img_return=(ImageView)findViewById(R.id.register_img_return);
        register_Edt_phone=(EditText)findViewById(R.id.register_Edt_phone);
        register_Edt_password=(EditText)findViewById(R.id.register_Edt_password);
        register=(Button)findViewById(R.id.register);
        register_img_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=register_Edt_phone.getText().toString();
                pwd=register_Edt_password.getText().toString();
//                if(name.equals("")||pwd.equals("")||name==null||pwd==null){
//                    Toast.makeText(RegisterActivity.this,"请输入账号和密码！",Toast.LENGTH_SHORT).show();
//                }else {
//                    user user=new user(name,pwd);
//                    user.saveInBackground(new DroiCallback<Boolean>() {
//                        @Override
//                        public void result(Boolean aBoolean, DroiError droiError) {
//                            if(aBoolean){
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
//                                        Intent intent=new Intent(RegisterActivity.this,LogActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                });
//                            }else {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(RegisterActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//                        }
//                    });
//                }
                //系统注册
                DroiUser droiUser=DroiUser.getCurrentUser();
                if (droiUser != null && droiUser.isAuthorized() && !droiUser.isAnonymous()) {
                    // 已经有注册用户登录，不允许注册；必须先将当前用户登出
                    droiUser.logout();
                    Toast.makeText(RegisterActivity.this,"您已登陆!",Toast.LENGTH_SHORT).show();
                }else {
                    if(droiUser==null){
                        droiUser=new DroiUser();
                    }
                    droiUser.setUserId(name);
                    droiUser.setPassword(pwd);
                    DroiError droiError=droiUser.signUp();
                    if(droiError.isOk()){
                        Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(RegisterActivity.this,LogActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(RegisterActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}

package com.example.myaidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.myaidlserver.IMyAidlInterface;
import com.example.myaidlserver.Person;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private IMyAidlInterface iMy;
    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iMy=IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService();
    }

    private void bindService() {
        Intent intent =new Intent();
        intent.setComponent(new ComponentName(
                "com.example.myaidlserver",
                "com.example.myaidlserver.AidlService"));
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
    }

    public void click(View view) {
        try {
            iMy.addPerson(new Person("何大侠",100));
            List<Person> list=iMy.getPersonList();
            Toast.makeText(this,list.toString(),Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            System.out.println("AIDL-------------------错误");
            e.printStackTrace();
        }
    }
}

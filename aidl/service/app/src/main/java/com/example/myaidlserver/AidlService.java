package com.example.myaidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hi on 2019.10.30.
 */

//注意要在andriodManifest中注册
public class AidlService extends Service {
    private ArrayList<Person> personArrayList;

    @Override
    public IBinder onBind(Intent intent) {
        personArrayList=new ArrayList<>();
        return iBinder;
    }

    private IBinder iBinder=new IMyAidlInterface.Stub() {
        @Override
        public void addPerson(Person person) throws RemoteException {
            personArrayList.add(person);
        }

        @Override
        public List<Person> getPersonList() throws RemoteException {
            return personArrayList;
        }
    };

}

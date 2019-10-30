// IMyAidlInterface.aidl
package com.example.myaidlserver;

// Declare any non-default types here with import statements
import com.example.myaidlserver.Person; //指的是Person.java

interface IMyAidlInterface {
   void addPerson(in Person person);
    List<Person> getPersonList();
}

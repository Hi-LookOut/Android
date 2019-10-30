// IMyAidlInterface.aidl
package com.example.myaidlserver;

// Declare any non-default types here with import statements
import com.example.myaidlserver.Person;

interface IMyAidlInterface {
    void addPerson(in Person person);
    List<Person> getPersonList();
}

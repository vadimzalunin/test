package grizzlyexample;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.org.apache.xml.internal.security.encryption.Serializer;

import java.util.Arrays;

/**
 * Created by vadim on 01/02/2016.
 */
public class TestBook {
    public static void main (final String[] args) throws InvalidProtocolBufferException {
        System.out.println("Hello!");
       long sum = 0;
        for (int i=0; i<1000; i++)
            sum+=i;

        System.out.println(sum);

//           System.out.println((i+1) + ": Hello!");



//        AddressBookProtos.Person.PhoneNumber phoneNumber = AddressBookProtos.Person.PhoneNumber.newBuilder().setNumber("123").build();
//        AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder().addPhone(phoneNumber).setId(22).setName("first").setPosition(111).build();
//        AddressBookProtos.AddressBook addressBook = AddressBookProtos.AddressBook.newBuilder().addPerson(person).build();
//        int size = addressBook.getSerializedSize();
//        System.out.printf("size");
//        byte[] bytes = addressBook.toByteArray();
//        System.out.println(Arrays.toString(bytes));
//
//        AddressBookProtos.AddressBook addressBook1 = AddressBookProtos.AddressBook.parseFrom(bytes);
//        System.out.println("phone: "+addressBook1.getPerson(0).getPhoneList().get(0)+"]");
    }
}

package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class Tests {

    @Test
    void errorName() {
        String actual = TestMethods.errorName("Tommaso", "localhost");
        Assertions.assertEquals("PASSED", actual);

        System.out.print("1) Test passed: input values are valid");
    }

    @Test
    void firstElement() {
        ArrayList<String> users = new ArrayList<>();
        boolean first = TestMethods.firstElementTest(users);

        Assertions.assertTrue(first);

        System.out.print("2) Test passed: size of array was zero");
    }

    @Test
    void sendMessageIfMoreThanOne() {
        ArrayList<String> users = new ArrayList<>();
        users.add("Tommaso");
        users.add("Dieman");
        String first = TestMethods.sendMessageIfSizeIsMoreThanOne(users);

        Assertions.assertEquals("MESSAGE SENT TO THE OTHERS", first);

        System.out.print("3) Test passed: size of array was greater than one");
    }

    @Test
    void removeIfNotRepond() {
        ArrayList<String> actualUsers = new ArrayList<>();
        ArrayList<String> online = new ArrayList<>();
        ArrayList<String> finalArray;

        actualUsers.add("Tommaso");
        online.add("Tommaso");
        online.add("Dieman");

        String[] strings = new String[]{"Tommaso"};
        finalArray = TestMethods.testNotRespond(actualUsers, online);

        Assertions.assertArrayEquals(strings, finalArray.toArray());

        System.out.print("4) Test passed: first element of array is different");
    }

    @Test
    void changeAdmin() {
        ArrayList<String> actualUsers = new ArrayList<>();
        ArrayList<String> online = new ArrayList<>();

        actualUsers.add("Tommaso");
        actualUsers.add("Dieman");
        //online.add("Tommaso");
        online.add("Dieman");

        String actual = TestMethods.changeAdmin(actualUsers, online);

        Assertions.assertEquals("New admin = Dieman", actual);

        System.out.print("5) Test passed: first element of array is the new admin");
    }
}
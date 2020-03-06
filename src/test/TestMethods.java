package test;

import java.util.ArrayList;

public class TestMethods {

    /////////////// TEST ///////////////////
    public static boolean firstElementTest(ArrayList<String> users) {
        boolean first;
        if (users.size() > 0)
            first = false;
        else first = true;
        return first;
    }

    public static String sendMessageIfSizeIsMoreThanOne(ArrayList<String> users) {
        String result;
        if (users.size() > 1)
            result = "MESSAGE SENT TO THE OTHERS";
        else result = "MESSAGE NOT SENT";

        return result;
    }

    public static ArrayList<String> testNotRespond(ArrayList<String> actualUsers, ArrayList<String> onlineUsers) {
        ArrayList<String> finalArray;

        if (actualUsers.equals(onlineUsers)) {
            finalArray = actualUsers;
        } else {
            actualUsers.removeIf(x -> (!onlineUsers.contains(x)));
            finalArray = actualUsers;
        }
        return finalArray;
    }

    public static String changeAdmin(ArrayList<String> actualUsers, ArrayList<String> onlineUsers) {
        String result;

        String adminBefore = actualUsers.get(0);
        actualUsers.removeIf(x -> (!onlineUsers.contains(x)));
        String adminAfter = actualUsers.get(0);

        if (!adminAfter.equals(adminBefore))
            result = "New admin = " + adminAfter;
        else result = "Admin not change = " + adminBefore;
        return result;
    }

    public static String errorName(String user, String ip) {
        String error;
        if (!ip.trim().equals("") || !user.trim().equals("")) {
            if (user.matches("^[a-zA-Z0-9]+$") && (ip.matches("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")
                    || ip.matches("^[a-zA-Z0-9]+$")))
                error = "PASSED";
            else error = "FAILED";
        } else error = "FAILED";

        return error;
    }
/////////////// TEST ///////////////////
}

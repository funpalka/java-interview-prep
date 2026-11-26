package ru.yandex.strings;

public class PhoneMasker {

    public static String maskPhone(String phone) {
        if (phone == null) {
            return null;
        }

        if (phone.length() < 9) {
            return phone;
        }


        StringBuilder sb = new StringBuilder();
        sb.append(phone, 0, 5);
        sb.append("***");
        sb.append(phone,(phone.length() - 4), phone.length());

        return sb.toString();
    }
}
package ru.bendricks.piris.model;

public enum Disability {

    NO("Отсутствует"), FIRST_LEVEL("Первой стадии"), SECOND_LEVEL("Второй стадии"), THIRD_LEVEL("Третьей стадии");

    Disability(String rus) {
        ruValue = rus;
    }

    private final String ruValue;

    public String getRuValue() {
        return ruValue;
    }

}

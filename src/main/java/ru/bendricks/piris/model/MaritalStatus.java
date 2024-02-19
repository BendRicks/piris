package ru.bendricks.piris.model;

public enum MaritalStatus {

    SINGLE("Одинок/Одинока"), MARRIED("Женат/Замужем");

    MaritalStatus(String rus) {
        ruValue = rus;
    }

    private final String ruValue;

    public String getRuValue() {
        return ruValue;
    }

}

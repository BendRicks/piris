package ru.bendricks.piris.model;

public enum Citizenship {

    REPUBLIC_OF_BELARUS("Республика Беларусь"), RUSSIAN_FEDERATION("Российская Федерация"), REPUBLIC_OF_POLAND("Республика Польша");

    Citizenship(String rus) {
        ruValue = rus;
    }

    private final String ruValue;

    public String getRuValue() {
        return ruValue;
    }

}

package ru.bendricks.piris.model;

public enum City {

    MINSK("Минск"), GOMEL("Гомель"), GRODNO("Гродно"), BREST("Брест"), VITEBSK("Витебск"), MOGILEV("Могилёв");

    City(String rus) {
        ruValue = rus;
    }

    private final String ruValue;

    public String getRuValue() {
        return ruValue;
    }

}

package ru.bendricks.piris.model;

public enum Sex {

    MALE("Мужчина"), FEMALE("Женщина"), ATTACK_HELICOPTER("Attack helicopter"), CROISSANT("Croissant");

    Sex(String rus) {
        ruValue = rus;
    }

    private final String ruValue;

    public String getRuValue() {
        return ruValue;
    }
}

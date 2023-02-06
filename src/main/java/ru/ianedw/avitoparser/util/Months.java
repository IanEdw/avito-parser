package ru.ianedw.avitoparser.util;

public enum Months {
    января("1"),
    февраля("2"),
    марта("3"),
    апреля("4"),
    мая("5"),
    июня("6"),
    июля("7"),
    августа("8"),
    сентября("9"),
    октября("10"),
    ноября("11"),
    декабря("12");

    private final String number;
    Months(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
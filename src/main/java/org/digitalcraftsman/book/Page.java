package org.digitalcraftsman.book;

public class Page {

    private final int number;
    private final int size;

    public Page(int number, int size) {
        this.number = number;
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }
}

package org.digitalcraftsman.book;

public class Page {

    private final long number;
    private final long size;

    public Page(int number, int size) {
        this.number = number;
        this.size = size;
    }

    public Page(long number, long size) {
        this.number = number;
        this.size = size;
    }

    public long getNumber() {
        return number;
    }

    public long getSize() {
        return size;
    }
}

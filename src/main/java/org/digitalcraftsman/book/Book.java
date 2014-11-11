package org.digitalcraftsman.book;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Book<T> implements Iterable<T>{

    private final Function<Page, Iterable<T>> turnPage;
    private final ConcurrentHashMap<Page, Iterator<T>> contents;

    private Page currentPage;

    public Book(Function<Page, Iterable<T>> turnPage) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");

        this.turnPage = turnPage;
        this.contents = new ConcurrentHashMap<>();
        this.currentPage = new Page(1, 10);
        startReading();
    }

    private void startReading() {
        contents.put(currentPage, turnPage.apply(currentPage).iterator());
    }

    @Override
    public Iterator<T> iterator() {

        return new BookIterator();
    }

    private class BookIterator implements Iterator<T> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            return null;
        }
    }
}

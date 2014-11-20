package org.digitalcraftsman.book;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class Book<T> implements Iterable<T>{

    private final Function<Page, Iterable<T>> turnPage;

    public Book(Function<Page, Iterable<T>> turnPage) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");

        this.turnPage = turnPage;
    }

    @Override
    public Iterator<T> iterator() {
        Iterable<T> pageContents = turnPage.apply(new Page(1, 10));
        return new BookIterator(pageContents.iterator());
    }

    private class BookIterator implements Iterator<T> {

        private final Iterator<T> pageContents;
        private Page currentPage;

        public BookIterator(Iterator<T> pageContents) { this.pageContents = pageContents; }

        @Override
        public boolean hasNext() {
            return pageContents.hasNext();
        }

        @Override
        public T next() {
            return pageContents.next();
        }
    }
}

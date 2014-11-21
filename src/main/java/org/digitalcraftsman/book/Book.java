package org.digitalcraftsman.book;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;

public class Book<T> implements Iterable<T> {

    private static final double PRELOAD_THRESSHOLD = 0.5;
    private static final long DEFAULT_START_PAGE = 1;
    private static final long DEFAULT_PAGE_SIZE = 10;

    private final Function<Page, Pageable<T>> turnPage;
    private final ExecutorService executorService;
    private final long startPage;
    private final long pageSize;

    public Book(Function<Page, Pageable<T>> turnPage) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");
        this.executorService = Executors.newFixedThreadPool(4);
        this.turnPage = turnPage;
        this.startPage = DEFAULT_START_PAGE;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public Book(Function<Page, Pageable<T>> turnPage, long startPage, long pageSize) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");
        this.executorService = Executors.newFixedThreadPool(4);
        this.turnPage = turnPage;
        this.startPage = startPage;
        this.pageSize = pageSize;
    }

    @Override
    public Iterator<T> iterator() {
        Pageable<T> firstPageContents = turnPage.apply(new Page(startPage, pageSize));
        return new BookIterator(firstPageContents);
    }

    private class BookIterator implements Iterator<T> {

        private Iterator<T> currentPageContents;
        private Future<Pageable<T>> nextPageContents;
        private long currentLine;
        private Page currentPage;
        private Page nextPage;

        public BookIterator(Pageable<T> page) {
            this.currentPageContents = page.getPageContents().iterator();
            this.currentPage = new Page(1, page.getPageContents().size());
            this.currentLine = 1;
        }

        @Override
        public boolean hasNext() {
            return currentPageContents.hasNext();
        }

        @Override
        public T next() {
            T line = currentPageContents.next();
            if(moreThanHalfPageHasBeenRead()) {
                this.nextPageContents = executorService.submit(() -> getNextPage());
            }
            if(!currentPageContents.hasNext()) {
                try {
                    this.currentPageContents =  nextPageContents.get().getPageContents().iterator();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.interrupted();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return line;
        }

        private boolean moreThanHalfPageHasBeenRead() {
            double percentage = (double)currentLine / (double)currentPage.getSize();
            return percentage >= PRELOAD_THRESSHOLD || !currentPageContents.hasNext();
        }

        private Pageable<T> getNextPage() {
            this.nextPage = new Page(currentPage.getNumber() + 1, currentPage.getSize());
            Pageable<T> contents =  turnPage.apply(nextPage);
            return contents != null ? contents: null;
        }
    }
}

package org.digitalcraftsman.book;

import java.util.concurrent.*;
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

        private long currentLine;
        private Pageable currentPage;
        private Future<Pageable<T>> nextPage;

        public BookIterator(Pageable<T> page) {
            this.currentPageContents = page.getPageContents().iterator();
            this.currentPage = page;
            this.currentLine = 1;
        }

        @Override
        public boolean hasNext() {
            return currentPageContents.hasNext();
        }

        @Override
        public T next() {
            T line = currentPageContents.next();
            if(moreThanHalfPageHasBeenRead() && nextPageHasNotBeenRequested()) {
                requestNextPage();
            }
            if(!currentPageContents.hasNext()) {
                try {
                    this.currentPage = nextPage.get();
                    this.currentPageContents =  currentPage.getPageContents().iterator();
                    this.nextPage = null;
                    this.currentLine = 1;
                    return line;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.interrupted();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            this.currentLine += 1;
            return line;
        }

        private void requestNextPage() {
            this.nextPage = executorService.submit(this::getNextPage);
        }

        private boolean nextPageHasNotBeenRequested() {
            return nextPage == null;
        }

        private boolean moreThanHalfPageHasBeenRead() {
            double percentage = (double)currentLine / (double)currentPage.getPageContents().size();
            return percentage >= PRELOAD_THRESSHOLD;
        }

        private Pageable<T> getNextPage() {
            Pageable<T> contents =  turnPage.apply(new Page(currentPage.getPageNumber() + 1, currentPage.getPageSize()));
            return contents != null ? contents: null;
        }
    }
}

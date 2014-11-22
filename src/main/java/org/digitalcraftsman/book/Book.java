package org.digitalcraftsman.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.Iterator;
import java.util.function.Function;

public class Book<T> implements Iterable<T> {

    private static final Logger log = LoggerFactory.getLogger(Book.class);

    private static final double PRELOAD_THRESHOLD = 0.5;
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
        Future<Pageable<T>> firstPage = requestFirstPage();
        return new BookIterator(firstPage);
    }

    private Future<Pageable<T>> requestFirstPage() {
        return executorService.submit(() ->
                    turnPage.apply(new Page(startPage, pageSize)));
    }

    private class BookIterator implements Iterator<T> {

        private Iterator<T> currentPageContents;

        private long currentLine;
        private Pageable<T> currentPage;
        private Future<Pageable<T>> nextPage;

        public BookIterator(Future<Pageable<T>> page) {
            this.nextPage = page;
            this.currentLine = 1;
        }

        @Override
        public boolean hasNext() {
            try {
                startReading();
                return currentPageContents.hasNext();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public T next() {
            try {
                startReading();
                T line = currentPageContents.next();
                if (moreThanHalfPageHasBeenRead() && nextPageHasNotBeenRequested()) {
                    requestNextPage();
                }
                if (doneReadingCurrentPage()) {
                    prepareNextPage();
                    return line;
                }
                this.currentLine += 1;
                return line;
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            throw new NoSuchElementException();
        }

        private void startReading() throws InterruptedException, ExecutionException {
            if(currentPageContents == null) {
                prepareNextPage();
            }
        }

        private void prepareNextPage() throws InterruptedException, ExecutionException {
            this.currentPage = nextPage.get();
            this.currentPageContents =  currentPage.getPageContents().iterator();
            this.nextPage = null;
            this.currentLine = 1;
        }

        private boolean doneReadingCurrentPage() {
            return !currentPageContents.hasNext();
        }

        private void requestNextPage() {
            log.debug("Submitting request for next page");
            this.nextPage = executorService.submit(this::getNextPage);
        }

        private boolean nextPageHasNotBeenRequested() {
            return nextPage == null;
        }

        private boolean moreThanHalfPageHasBeenRead() {
            double percentage = (double)currentLine / (double)currentPage.getPageContents().size();
            return percentage >= PRELOAD_THRESHOLD;
        }

        private Pageable<T> getNextPage() {
            Pageable<T> contents =  turnPage.apply(new Page(currentPage.getPageNumber() + 1, currentPage.getPageSize()));
            return contents != null ? contents: null;
        }
    }
}

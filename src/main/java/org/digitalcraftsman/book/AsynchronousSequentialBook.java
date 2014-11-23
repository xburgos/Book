package org.digitalcraftsman.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * @author Xabier Burgos
 * @since v0.1
 * <p>Serial asynchronous implementation of @see Book. This particular
 * implementation loads every page of the book asynchronously (i.e. in a
 * separate thread). Pre-loading is only performed for the page following
 * the one currently being iterated over, and it only happens after half of the
 * current page has been iterated over, this means that at any given point
 * there are a maximum of 2 pages loaded in memory. Page iteration is
 * guaranteed to be performed sequentially, (i.e: 1, 2, 3,...,)</p>
 *
 * <p>Use this implementation when holding all the pages of the book in memory
 * is either impossible or undesirable.</p>
 *
 * @param <T> The type of content of the pages of the book.
 */
public class AsynchronousSequentialBook<T> implements Book<T> {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousSequentialBook.class);

    private static final double PRELOAD_THRESHOLD = 0.5;
    private static final int DEFAULT_START_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_TOTAL_PAGES = -1;

    private final Function<Page, Pageable<T>> turnPage;
    private final ExecutorService executorService;
    private final int startPage;
    private final int pageSize;
    private final int totalPages;

    public AsynchronousSequentialBook(Function<Page, Pageable<T>> turnPage) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");
        this.executorService = Executors.newSingleThreadExecutor();
        this.turnPage = turnPage;
        this.startPage = DEFAULT_START_PAGE;
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.totalPages = DEFAULT_TOTAL_PAGES;
    }

    public AsynchronousSequentialBook(int startPage, int pageSize, Function<Page, Pageable<T>> turnPage) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");
        this.executorService = Executors.newSingleThreadExecutor();
        this.turnPage = turnPage;
        this.startPage = startPage;
        this.pageSize = pageSize;
        this.totalPages = DEFAULT_TOTAL_PAGES;
    }

    public AsynchronousSequentialBook(int startPage, int pageSize, int totalPages, Function<Page, Pageable<T>> turnPage) {
        if(turnPage == null) throw new IllegalArgumentException("turnPage must not be null");
        this.executorService = Executors.newSingleThreadExecutor();
        this.turnPage = turnPage;
        this.startPage = startPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
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

        private int currentLine;
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
                String message = String.format("Interrupted while fetching page, current page is [%s], current line is [%s]", currentPage, currentLine);
                log.error(message, e);
                Thread.currentThread().interrupt();

            } catch (ExecutionException e) {
                String message = String.format("Error while fetching page, current page is [%s], current line is [%s]", currentPage, currentLine);
                log.error(message, e.getCause());
            }
            return false;
        }

        @Override
        public T next() {
            T line = null;
            try {
                startReading();
                line = getLine();
                if (preloadThresholdHasBeenPassed() && nextPageHasNotBeenRequested()) {
                    requestNextPage();
                }
                if (doneReadingCurrentPage()) {
                    prepareNextPage();
                    return line;
                }

            } catch (InterruptedException e) {
                String message = String.format("Interrupted while fetching page, current page is [%s], current line is [%s]", currentPage, currentLine);
                log.error(message, e);
                Thread.currentThread().interrupt();

            } catch (ExecutionException e) {
                String message = String.format("Error while fetching page, current page is [%s], current line is [%s]", currentPage, currentLine);
                log.error(message, e.getCause());
            }
            return line == null ? getLine() : line;
        }

        private T getLine() {
            this.currentLine += 1;
            return currentPageContents.next();
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
            this.nextPage = executorService.submit(this::getNextPage);
        }

        private boolean nextPageHasNotBeenRequested() {
            return nextPage == null;
        }

        private boolean preloadThresholdHasBeenPassed() {
            double percentage = (double)currentLine / (double)currentPage.getPageContents().size();
            return percentage >= PRELOAD_THRESHOLD;
        }

        private Pageable<T> getNextPage() {
            int nextPageNumber = currentPage.getPageNumber() + 1;
            int nextPageSize = currentPage.getPageSize();

            if(totalPages > 0 && currentPage.getPageNumber() == totalPages)
                return new EmptyPageable<>(nextPageNumber, nextPageSize);

            Pageable<T> contents =  turnPage.apply(new Page(nextPageNumber, nextPageSize));

            return contents != null ? contents: new EmptyPageable<>(nextPageNumber, nextPageSize);
        }
    }
}

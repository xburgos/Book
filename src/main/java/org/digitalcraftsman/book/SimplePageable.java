package org.digitalcraftsman.book;

import java.util.Collection;

/**
 * @author Xabier Burgos.
 * @since v0.1.1
 * <p>This the most basic implementation of {@link org.digitalcraftsman.book.Pageable}
 * more complex implementations can either derive from this one, or implement
 * the interface from scratch.</p>
 *
 */
public class SimplePageable<T> implements Pageable<T> {

    private final long pageNumber;
    private final long pageSize;
    private final Collection<T> pageContents;

    public SimplePageable(long pageNumber, long pageSize, Collection<T> pageContents) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.pageContents = pageContents;
    }

    @Override
    public long getPageNumber() {
        return pageNumber;
    }

    @Override
    public long getPageSize() {
        return pageSize;
    }

    @Override
    public Collection<T> getPageContents() {
        return pageContents;
    }
}

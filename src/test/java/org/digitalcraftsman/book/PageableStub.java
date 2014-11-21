package org.digitalcraftsman.book;

import java.util.Collection;

/**
 * Created by xabier on 21/11/2014.
 */
public class PageableStub<T> implements Pageable<T>{

    private final long pageNumber;
    private final long pageSize;
    private final Collection<T> pageContents;

    public PageableStub(long pageNumber, long pageSize, Collection<T> pageContents) {
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

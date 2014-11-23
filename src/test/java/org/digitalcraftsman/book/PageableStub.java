package org.digitalcraftsman.book;

import java.util.Collection;

/**
 * Created by xabier on 21/11/2014.
 */
public class PageableStub<T> implements Pageable<T>{

    private final int pageNumber;
    private final int pageSize;
    private final Collection<T> pageContents;

    public PageableStub(int pageNumber, int pageSize, Collection<T> pageContents) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.pageContents = pageContents;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public Collection<T> getPageContents() {
        return pageContents;
    }
}

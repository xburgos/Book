package org.digitalcraftsman.book;

import java.util.Collection;

/**
 * Created by xabier on 21/11/2014.
 */
public interface Pageable<T> {

    long getPageNumber();

    long getPageSize();

    Collection<T> getPageContents();
}

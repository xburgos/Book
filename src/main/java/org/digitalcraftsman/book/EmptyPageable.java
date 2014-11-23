package org.digitalcraftsman.book;

import java.util.Collections;

/**
 * Created by xabier on 23/11/2014.
 */
public class EmptyPageable<T> extends SimplePageable<T> {

    public EmptyPageable(int pageNumber, int pageSize) {
        super(pageNumber, pageSize, Collections.emptyList());
    }
}

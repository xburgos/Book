package org.digitalcraftsman.book;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BookUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void GivenANullPageTurningFunction_WhenANewBookIsCreated_ThenExceptionIsThrown() {

        Book<Object> book = new Book<>(null);
    }

    @Test
    public void GivenAnEmptyPage_WhenIterating_ThenDoNotIterate() {
        Book<Object> book = new Book<>(page -> Collections.emptyList());

        assertThat(book.iterator().hasNext(), is(false));
    }

    @Test
    public void GivenAPageWithASingleElement_WhenIterating_ThenWeIterateOnlyOnce() {
        Book<String> book = new Book<>(page -> Arrays.asList("hello"));

        Iterator<String> iterator = book.iterator();

        assertThat(iterator, is(notNullValue()));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(equalTo("hello")));
        assertThat(iterator.hasNext(), is(false));
    }
}

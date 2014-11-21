package org.digitalcraftsman.book;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.lessThan;
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

    @Test
    public void GivenAPageWithAMultipleElements_WhenIterating_ThenWeIterateAsManyTimesAsElementsInPage() {
        Book<String> book = new Book<>(page -> Arrays.asList("hello", "my", "name", "is", "xabier"));

        Iterator<String> iterator = book.iterator();

        assertThat(iterator, is(notNullValue()));

        int actualElements = 0;
        while(iterator.hasNext()) {
            assertThat(iterator.next(), is(notNullValue()));
            actualElements++;
        }
        assertThat(actualElements, is(5));
    }

    @Test
    public void GivenMultiplePagesWithAMultipleElements_WhenIterating_ThenWeIterateAsManyTimesAsElementsInAllPages() {
        Book<String> book = new Book<>(page -> {
            if (page.getNumber() == 1) {
                return Arrays.asList("hello", "my", "name", "is", "xabier", "burgos", "and", "this", "is", "a" );
            }
            if (page.getNumber() == 2) {
                return Arrays.asList("test", "page");
            }
            return Collections.emptyList();
        });

        Iterator<String> iterator = book.iterator();

        assertThat(iterator, is(notNullValue()));

        List<String> actualElements = new ArrayList<>();
        while(iterator.hasNext()) {
            actualElements.add(iterator.next());
        }
        assertThat(actualElements, containsInAnyOrder("hello", "my", "name", "is", "xabier", "burgos", "and", "this", "is", "a", "test", "page"));
    }

    @Test
    public void GivenFetchingAPageTakes500ms_WhenIterating2Pages_ThenWeIterateInLessThan1000ms() {
        Book<String> book = new Book<>(page -> {
            if (page.getNumber() == 1) {
                try {
                    Thread.sleep(500);
                } catch(Throwable e) {};
                return Arrays.asList("hello", "my", "name", "is", "xabier", "burgos", "and", "this", "is", "a" );
            }
            if (page.getNumber() == 2) {
                try {
                    Thread.sleep(500);
                } catch(Throwable e) {};
                return Arrays.asList("test", "page");
            }
            return Collections.emptyList();
        });

        List<String> actualElements = new ArrayList<>();
        long before = System.currentTimeMillis();
        for(String line : book) {
            actualElements.add(line);
        }
        assertThat(System.currentTimeMillis() - before, is(lessThan(1000l)));
        assertThat(actualElements, containsInAnyOrder("hello", "my", "name", "is", "xabier", "burgos", "and", "this", "is", "a", "test", "page"));
    }
}

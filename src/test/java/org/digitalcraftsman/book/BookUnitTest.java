package org.digitalcraftsman.book;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class BookUnitTest {

    private static final Logger log = LoggerFactory.getLogger(BookUnitTest.class);

    @Test(expected = IllegalArgumentException.class)
    public void GivenANullPageTurningFunction_WhenANewBookIsCreated_ThenExceptionIsThrown() {

        new Book<>(null);
    }

    @Test
    public void GivenAnEmptyPage_WhenIterating_ThenDoNotIterate() {
        Book<Object> book = new Book<>(page -> new PageableStub<>(1, 10, Collections.emptyList()));

        assertThat(book.iterator().hasNext(), is(false));
    }

    @Test
    public void GivenAPageWithASingleElement_WhenIterating_ThenWeIterateOnlyOnce() {
        Book<String> book = new Book<>(page -> {
            if(page.getNumber() == 1)
                return new PageableStub<>(1, 10, Arrays.asList("hello"));
            return new PageableStub<>(2, 10, Collections.emptyList());
        });

        Iterator<String> iterator = book.iterator();

        assertThat(iterator, is(notNullValue()));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(equalTo("hello")));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void GivenAPageWithAMultipleElements_WhenIterating_ThenWeIterateAsManyTimesAsElementsInPage() {
        Book<String> book = new Book<>(page -> {
            if(page.getNumber() == 1)
                return new PageableStub<>(1, 10, Arrays.asList("hello", "my", "name", "is", "xabier"));
            return new PageableStub<>(2, 10, Collections.emptyList());
        });

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
                return new PageableStub<>(1, 10, Arrays.asList("hello", "my", "name", "is", "xabier", "burgos", "and", "this", "is", "a" ));
            }
            if (page.getNumber() == 2) {
                return new PageableStub<>(2, 10, Arrays.asList("test", "page"));
            }
            return new PageableStub<>(3, 10, Collections.emptyList());
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
    public void Given100PagesOf10000ElementsEach_AfterIterating_ThenWeHave1000000Elements() {
        Book<Long> book = new Book<>(page -> {
            log.debug("about to get page {}", page.getNumber());

            long start = page.getNumber() * page.getSize();
            long end = ((page.getNumber() + 1) * page.getSize());

            if(page.getNumber() == 100)
                return new PageableStub<>(page.getNumber(), page.getSize(), Collections.emptyList());

            return new PageableStub<>(page.getNumber(), page.getSize(), LongStream.range(start, end).boxed().collect(Collectors.toList()));
        }, 0, 10000);

        List<Long> actualElements = new ArrayList<>();
        long before = System.currentTimeMillis();
        for(Long line : book) {
            actualElements.add(line);
        }

        log.debug("Total time taken: {}",System.currentTimeMillis() - before);
        assertThat(actualElements, contains(LongStream.range(0, 1000000).boxed().collect(Collectors.toList()).toArray()));
    }

}

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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AsynchronousSequentialBookTest {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousSequentialBookTest.class);

    @Test(expected = IllegalArgumentException.class)
    public void GivenANullPageTurningFunction_WhenANewBookIsCreated_ThenExceptionIsThrown() {
        new AsynchronousSequentialBook<>(null);
    }

    @Test
    public void GivenAnEmptyPage_WhenIterating_ThenDoNotIterate() {
        Book<Object> book = new AsynchronousSequentialBook<>(page -> new PageableStub<>(1, 10, Collections.emptyList()));

        assertThat(book.iterator().hasNext(), is(false));
    }

    @Test
    public void GivenMultiplePages_WhenIterating_ThenWeStopAtTheFirstEmptyPage() {
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            if(page.getNumber() == 0)
                return new PageableStub<>(0, 10, Arrays.asList("hello"));
            return new PageableStub<>(1, 10, Collections.emptyList());
        });

       for(String line : book) {
           assertThat(line, is(equalTo("hello")));
       }
    }

    @Test(expected = NoSuchElementException.class)
    public void GivenMultiplePages_WhenIteratingPastLastPage_ThenExceptionIsThrown() {
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            if(page.getNumber() == 0)
                return new PageableStub<>(0, 10, Arrays.asList("hello"));
            return new PageableStub<>(1, 10, Collections.emptyList());
        });
        Iterator<String> iterator = book.iterator();

       for(int i = 0; i < 2 ; i++) {
           String element = iterator.next();
           assertThat(element, is(equalTo("hello")));
       }
    }

    @Test
    public void GivenRetrievingTheFirstPageThrowsRuntimeException_WhenIterating_ThenWeBehaveAsIfTheBookWasEmpty() {
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            throw new IllegalArgumentException();
        });
        Iterator<String> iterator = book.iterator();

        assertThat(iterator.hasNext(), is(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void GivenRetrievingTheSecondPageThrowsRuntimeException_WhenIterating_ThenWeBehaveAsIfTheBookWasEmpty() {
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            if(page.getNumber() == 0)
                return new PageableStub<>(0, 10, Arrays.asList("hello"));
            throw new IllegalArgumentException();
        });
        Iterator<String> iterator = book.iterator();

        for(int i = 0; i < 2 ; i++) {
            String element = iterator.next();
            assertThat(element, is(equalTo("hello")));
        }
    }

    @Test
    public void GivenAPageWithASingleElement_WhenIterating_ThenWeIterateOnlyOnce() {
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            if(page.getNumber() == 0)
                return new PageableStub<>(0, 10, Arrays.asList("hello"));
            return new PageableStub<>(1, 10, Collections.emptyList());
        });

        Iterator<String> iterator = book.iterator();

        assertThat(iterator, is(notNullValue()));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(equalTo("hello")));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void GivenAPageWithAMultipleElements_WhenIterating_ThenWeIterateAsManyTimesAsElementsInPage() {
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            if(page.getNumber() == 0)
                return new PageableStub<>(0, 10, Arrays.asList("hello", "my", "name", "is", "xabier"));
            return new PageableStub<>(1, 10, Collections.emptyList());
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
        Book<String> book = new AsynchronousSequentialBook<>(page -> {
            if (page.getNumber() == 0) {
                return new PageableStub<>(0, 10, Arrays.asList("hello", "my", "name", "is", "xabier", "burgos", "and", "this", "is", "a" ));
            }
            if (page.getNumber() == 1) {
                return new PageableStub<>(1, 10, Arrays.asList("test", "page"));
            }
            return new PageableStub<>(2, 10, Collections.emptyList());
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
        Book<Long> book = new AsynchronousSequentialBook<>(0, 10000, page -> {
            log.debug("about to get page {}", page.getNumber());

            long start = page.getNumber() * page.getSize();
            long end = ((page.getNumber() + 1) * page.getSize());

            if(page.getNumber() == 100)
                return new PageableStub<>(page.getNumber(), page.getSize(), Collections.emptyList());

            return new PageableStub<>(page.getNumber(), page.getSize(), LongStream.range(start, end).boxed().collect(Collectors.toList()));
        });

        List<Long> actualElements = new ArrayList<>();
        long before = System.currentTimeMillis();
        for(Long line : book) {
            actualElements.add(line);
        }

        log.debug("Total time taken: {}",System.currentTimeMillis() - before);
        assertThat(actualElements, contains(LongStream.range(0, 1000000).boxed().collect(Collectors.toList()).toArray()));
    }

    @Test
    public void Given100PagesOf10000ElementsEachAndOnePageFailingToBeFetched_WhenIterating_ThenWeGoThroughAllElementsUptoThePageThatFailed() {
        Book<Long> book = new AsynchronousSequentialBook<>(0, 10000, page -> {
            log.debug("about to get page {}", page.getNumber());

            long start = page.getNumber() * page.getSize();
            long end = ((page.getNumber() + 1) * page.getSize());

            if(page.getNumber() == 10) {
                throw new IllegalArgumentException();
            }

            if(page.getNumber() == 100)
                return new PageableStub<>(page.getNumber(), page.getSize(), Collections.emptyList());

            return new PageableStub<>(page.getNumber(), page.getSize(), LongStream.range(start, end).boxed().collect(Collectors.toList()));
        });

        List<Long> actualElements = new ArrayList<>();
        long before = System.currentTimeMillis();
        for(Long line : book) {
            actualElements.add(line);
        }

        log.debug("Total time taken: {}",System.currentTimeMillis() - before);
        assertThat(actualElements, contains(LongStream.range(0, 100000).boxed().collect(Collectors.toList()).toArray()));
    }

    @Test
    public void Given100PagesOf10000ElementsEachAndTheTotalNumberOfPages_WhenIterating_ThenWeGoThroughAllElementsUptoTheTotalPagesSpecified() {
        Book<Long> book = new AsynchronousSequentialBook<>(0, 10000, 10, page -> {
            log.debug("about to get page {}", page.getNumber());

            long start = page.getNumber() * page.getSize();
            long end = ((page.getNumber() + 1) * page.getSize());

            if(page.getNumber() == 100)
                return new PageableStub<>(page.getNumber(), page.getSize(), Collections.emptyList());

            return new PageableStub<>(page.getNumber(), page.getSize(), LongStream.range(start, end).boxed().collect(Collectors.toList()));
        });

        List<Long> actualElements = new ArrayList<>();
        long before = System.currentTimeMillis();
        for(Long line : book) {
            actualElements.add(line);
        }

        log.debug("Total time taken: {}",System.currentTimeMillis() - before);
        assertThat(actualElements, contains(LongStream.range(0, 110000).boxed().collect(Collectors.toList()).toArray()));
    }
}

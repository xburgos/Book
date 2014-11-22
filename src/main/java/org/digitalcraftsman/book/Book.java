package org.digitalcraftsman.book;


/**
 * @author Xabier Burgos
 * @since v0.1
 * <p>Book implementations try to model the idea of a physical book
 * in a programming context. In real life when you're reading a book, you
 * don't open all the pages at once and then start reading, you open the book
 * at a chosen page, and then proceed to reading the contents.</p>
 *
 * <p>Based upon the premise above, book implementations must also conform
 * to the {@link java.lang.Iterable} contract, so as to provide a seamless
 * experience; users of any book implementations must not be made aware
 * of the underlying iteration mechanism, nor forced to read the contents of
 * the book in any specific order, or following a particular sequence of actions
 * .</p>
 * @param <T> The type of content of the pages of the book.
 */
public interface Book<T> extends Iterable<T> {}

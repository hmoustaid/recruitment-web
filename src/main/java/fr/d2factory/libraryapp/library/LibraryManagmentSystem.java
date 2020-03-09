package fr.d2factory.libraryapp.library;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.BookRepository;
import fr.d2factory.libraryapp.book.ISBN;
import fr.d2factory.libraryapp.member.Member;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LibraryManagmentSystem implements Library {

    private BookRepository bookRepository;

    public LibraryManagmentSystem(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book borrowBook(long isbnCode, Member member, LocalDate borrowedAt){
        Book book = null;
        if(isLate(member)){
            throw new HasLateBooksException();
        }
        book = bookRepository.findBook(isbnCode);
        if(book != null){
            bookRepository.saveBookBorrow(book, borrowedAt);
            bookRepository.saveBorrower(book, member);
        }
        return book;
    }

    @Override
    public void returnBook(Book book, Member member){
        int numberOfDays = (int)bookRepository.findBorrowedBookDate(book).until(LocalDate.now(), ChronoUnit.DAYS);
        bookRepository.returnBook(book);
        member.payBook(numberOfDays);
    }


    private boolean isLate(Member member){
        boolean result = false;
        List<Book> booksBorrowedByTheMember  = bookRepository.booksBorrowedByMember().get(member);
        if(booksBorrowedByTheMember != null){
            int memberMaxPeriod = member.getConfig().getMaxPeriod();
            result = booksBorrowedByTheMember.stream()
                    .anyMatch(b->bookNotReturned(b, memberMaxPeriod));
        }
        return result;
    }

    private boolean bookNotReturned(Book book, int maxPeriod){
        boolean result = false;
        int daysBorrowed = daysBorrowed(book);
        result =  daysBorrowed > maxPeriod;
        return result;
    }


    private int daysBorrowed(Book book){
        int daysBorrowed = 0;
        LocalDate now = LocalDate.now();
        LocalDate borrowedAt = bookRepository.findBorrowedBookDate(book);
        daysBorrowed = (int)borrowedAt.until(now, ChronoUnit.DAYS);
        return daysBorrowed;
    }


}

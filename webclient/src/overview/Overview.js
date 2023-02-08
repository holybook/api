import {TopBar} from './TopBar';
import {useLoaderData, useSearchParams} from 'react-router-dom';
import './Overview.scss';
import {AuthorBookItems} from './AuthorBookItems';
import React, {useState} from "react";

export function Overview() {
  const supportedLanguages = useLoaderData();
  const [params,] = useSearchParams();
  const language = params.get('lang') ?? 'en';
  const [books, setBooks] = useState({
    language: null,
    books: {}
  });

  function fetchBooks() {
    fetch(`/api/books?lang=${language}`).then(response => {
      return response.json()
    }).then((booksJson) => {
      setBooks({
        language: language,
        books: booksJson
      })
    })
  }

  if (books.language !== language) {
    fetchBooks();
    return <div>
      Loading...
    </div>
  }

  return (
    <div className="overview">
      <TopBar supportedLanguages={supportedLanguages}
              activeLanguage={language}/>
      <div id="content-container">
        <div id="content">
          {Object.keys(books.books).map(author =>
            <AuthorBookItems author={author} books={books.books[author]}/>
          )}
        </div>
      </div>
    </div>
  );
}
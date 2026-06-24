import {TopBar} from '../common/TopBar';
import {useLoaderData, useSearchParams} from 'react-router-dom';
import './Overview.scss';
import {AuthorBookItems} from './AuthorBookItems';
import React from "react";

export function Overview() {
  const {supportedLanguages, books} = useLoaderData();
  const [params,] = useSearchParams();
  const language = params.get('lang') ?? 'en';

  const authors = Object.keys(books);
  const bookCount = authors.reduce(
    (total, author) => total + books[author].length, 0);

  return (
    <div className="overview">
      <TopBar supportedLanguages={supportedLanguages}
              activeLanguage={language}/>
      <div id="content-container">
        <div id="content">
          <header className="library-hero">
            <p className="library-hero__eyebrow">The Library</p>
            <h1 className="library-hero__title">Sacred writings, beautifully readable</h1>
            <p className="library-hero__subtitle">
              {bookCount} {bookCount === 1 ? 'work' : 'works'} across {authors.length}
              {authors.length === 1 ? ' author' : ' authors'}. Browse below or
              search the full text.
            </p>
          </header>
          {authors.map(author =>
            <AuthorBookItems author={author} books={books[author]}
                             key={author}/>
          )}
        </div>
      </div>
    </div>
  );
}

import {TopBar} from '../common/TopBar';
import {useLoaderData, useSearchParams} from 'react-router-dom';
import './Overview.scss';
import {AuthorBookItems} from './AuthorBookItems';
import React, {useMemo, useState} from "react";

export function Overview() {
  const {supportedLanguages, books} = useLoaderData();
  const [params,] = useSearchParams();
  const language = params.get('lang') ?? 'en';

  const [query, setQuery] = useState('');
  const q = query.trim().toLowerCase();

  // Filter client-side on author name and book title. When the author name matches we keep all of
  // their works; otherwise we keep only the works whose title matches.
  const filtered = useMemo(() => {
    if (!q) {
      return books;
    }
    const result = {};
    for (const author of Object.keys(books)) {
      const authorMatches = author.toLowerCase().includes(q);
      const matchingBooks = authorMatches
        ? books[author]
        : books[author].filter(b => b.title.toLowerCase().includes(q));
      if (matchingBooks.length > 0) {
        result[author] = matchingBooks;
      }
    }
    return result;
  }, [books, q]);

  const authors = Object.keys(filtered);
  const bookCount = authors.reduce(
    (total, author) => total + filtered[author].length, 0);

  // Expand authors by default while searching or when the catalog is small.
  const expandByDefault = q.length > 0 || authors.length <= 8;

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

          <div className="library-filter">
            <i className="fa-solid fa-magnifying-glass library-filter__icon"
               aria-hidden="true"/>
            <input
              className="library-filter__input"
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Filter by author or title…"
              aria-label="Filter by author or title"/>
          </div>

          {authors.length === 0 &&
            <p className="state-message">No works match “{query}”.</p>}

          {authors.map(author =>
            <AuthorBookItems author={author} books={filtered[author]}
                             defaultOpen={expandByDefault}
                             key={`${author}|${expandByDefault}`}/>
          )}
        </div>
      </div>
    </div>
  );
}

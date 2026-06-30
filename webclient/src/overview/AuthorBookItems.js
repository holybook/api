import React, {useState} from 'react';
import {BookItem} from './BookItem';

export function AuthorBookItems({author, books, defaultOpen = true}) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className={`author-books ${open ? 'is-open' : 'is-closed'}`}>
      <button className="author-books__header" onClick={() => setOpen(!open)}
              aria-expanded={open}>
        <i className={`author-books__caret fa-solid ${open ? 'fa-chevron-down' : 'fa-chevron-right'}`}
           aria-hidden="true"/>
        <span className="author-books__name">{author}</span>
        <span className="author-books__count">{books.length}</span>
      </button>
      {open &&
        <div className="author-books__list">
          {books.map(item =>
            <BookItem title={item.title} language={item.language}
                      id={item.bookId} key={item.bookId}/>
          )}
        </div>}
    </div>
  )
}

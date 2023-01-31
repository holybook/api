import {TopBar} from './TopBar';
import {useLoaderData, useSearchParams} from 'react-router-dom';
import {BookItem} from './BookItem';
import './Overview.scss';
import {getSupportedLanguages} from "../common/Utils";
import {AuthorBookItems} from './AuthorBookItems';

export function Overview() {
  const books = useLoaderData();
  const [params,] = useSearchParams();
  const language = params.get('lang') ?? 'en';

  const bookItems = getBookItemsByAuthor(books, language);

  return (
      <div className="overview">
        <TopBar supportedLanguages={getSupportedLanguages(books)}
                activeLanguage={language}/>
        <div id="content-container">
          <div id="content">
            {Object.keys(bookItems).map(author =>
                <AuthorBookItems author={author} books={bookItems[author]}/>
            )}
          </div>
        </div>
      </div>
  );
}

function getBookItemsByAuthor(books, language) {
  return books
      .map(book => {
        return getBookItemForLanguage(book, language);
      })
      .filter((item) => item !== null)
      .reduce((booksByAuthor, item) => {
        const authorBooks = booksByAuthor[item.author]
        if (authorBooks === undefined) {
          booksByAuthor[item.author] = [item]
        } else {
          authorBooks.push(item)
        }
        return booksByAuthor
      }, {})
  // .sort((a, b) => a.title.localeCompare(b.title))
}

function getBookItemForLanguage(book, language) {
  const title = findTitle(book.translations, language);
  if (title === null) {
    return null;
  }
  return {
    id: book.id,
    language: language,
    author: book.author,
    title: title
  };
}

function findTitle(translations, language) {
  const translation = translations.find(t => t.language === language);
  if (translation === undefined) {
    return null;
  }
  return translation.title;
}
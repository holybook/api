import '@fortawesome/fontawesome-free/css/all.min.css';
import './App.scss';
import {createBrowserRouter, RouterProvider, useParams,} from 'react-router-dom';
import {Reader} from './reader/Reader.js';
import {Overview} from './overview/Overview';
import {SearchResults, SearchResultsPage} from "./search/SearchResults";
import {Translate} from './translate/Translate';

const router = createBrowserRouter([
  {
    path: '/',
    loader: async ({request}) => {
      const currentLanguage = new URL(request.url).searchParams.get('lang') ?? 'en';
      const supportedLanguages = await fetch(`/api/languages`);
      const books = await fetch(`/api/books?lang=${currentLanguage}`);
      return {
        supportedLanguages: await supportedLanguages.json(),
        books: await books.json()
      };
    },
    element: <Overview />,
  },
  {
    path: '/books/:id',
    loader: async ({params, request}) => {
      const currentLanguage = new URL(request.url).searchParams.get("lang");
      const book = await fetch(`/api/books/${params.id}`);
      const paragraphs = await fetch(`/api/books/${params.id}/paragraphs?lang=${currentLanguage}`)
      return {
        book: await book.json(),
        paragraphs: await paragraphs.json()
      };
    },
    element: <Reader />
  },
  {
    path: '/search',
    loader: async ({params}) => {
      return fetch(`/api/languages`);
    },
    element: <SearchResultsPage />
  },
  {
    path: '/translate',
    loader: async ({params}) => {
      return fetch(`/api/languages`);
    },
    element: <Translate />
  }
]);

function App() {
  return (
      <RouterProvider router={router} />
  );
}

export default App;

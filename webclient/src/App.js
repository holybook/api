import './App.css';
import {createBrowserRouter, RouterProvider, useParams,} from 'react-router-dom';
import {Reader} from './reader/Reader.js';
import {Overview} from './overview/Overview';
import {SearchResults, SearchResultsPage} from "./search/SearchResults";
import {Translate} from './translate/Translate';

const router = createBrowserRouter([
  {
    path: '/',
    loader: async ({params}) => {
      return fetch(`/api/books`);
    },
    element: <Overview />,
  },
  {
    path: '/books/:id',
    loader: async ({params}) => {
      return fetch(`/api/books/${params.id}`);
    },
    element: <Reader />
  },
  {
    path: '/search',
    loader: async ({params}) => {
      return fetch(`/api/books`);
    },
    element: <SearchResultsPage />
  },
  {
    path: '/translate',
    element: <Translate />
  }
]);

function App() {
  return (
      <RouterProvider router={router} />
  );
}

export default App;

import {useNavigate, useSearchParams} from 'react-router-dom';
import {useState} from 'react';
import './SearchBar.scss';

export function SearchBar() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const queryFromParam = params.get('q');
  const language = params.get('lang') ?? 'en';

  const [query, setQuery] = useState(queryFromParam ?? '');

  function search(event) {
    event?.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) {
      return;
    }
    navigate(`/search?q=${encodeURIComponent(trimmed)}&lang=${language}`);
  }

  return (
    <form className="searchbar" role="search" onSubmit={search}>
      <i
        className="fa-solid fa-magnifying-glass searchbar__icon"
        aria-hidden="true" />
      <input
        className="searchbar__input"
        type="search"
        placeholder="Search the writings…"
        aria-label="Search the writings"
        value={query}
        onChange={(event) => setQuery(event.target.value)} />
    </form>
  );
}

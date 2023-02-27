import {TextField} from '@mui/material';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {Form} from 'react-bulma-components';
import {useState} from 'react';

export function SearchBar() {
  const navigate = useNavigate();
  const [params,] = useSearchParams();
  const queryFromParam = params.get('q');
  const language = params.get('lang') ?? 'en';

  const [query, setQuery] = useState(queryFromParam);

  function keyPress(e) {
    if (e.keyCode === 13) {
      search();
    }
  }

  function search() {
    navigate(`/search?q=${encodeURIComponent(query)}&lang=${language}`)
  }

  return (
      <Form.Input
          placeholder="Search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={keyPress}
      />
  );
}
import {TextField} from '@mui/material';
import {useNavigate, useSearchParams} from 'react-router-dom';

export function SearchBar() {
  const navigate = useNavigate();
  const [params,] = useSearchParams();
  const query = params.get('q');
  const language = params.get('lang') ?? 'en';

  function keyPress(e) {
    if (e.keyCode === 13) {
      search(e.target.value);
    }
  }

  function search(query) {
    navigate(`/search?q=${encodeURIComponent(query)}&lang=${language}`)
  }

  return (
      <TextField variant="outlined"
                 size="small"
                 placeholder="Search"
                 defaultValue={query}
                 onKeyDown={keyPress}
                 fullWidth/>
  );
}
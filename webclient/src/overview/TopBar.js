import {LanguageSelect} from '../common/LanguageSelect';
import {useNavigate} from 'react-router-dom';
import {SearchBar} from '../search/SearchBar';

export function TopBar({supportedLanguages, activeLanguage}) {

  const navigate = useNavigate();

  function setLanguage(language) {
    navigate(`/?lang=${language}`);
  }

  return (<div className="topbar">
    <LanguageSelect
        supportedLanguages={supportedLanguages}
        activeLanguage={activeLanguage}
        onLanguageChanged={setLanguage}
    />
    <SearchBar />
  </div>);
}
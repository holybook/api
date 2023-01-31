import {LanguageSelect} from '../common/LanguageSelect';
import {getScrollPosition} from './ScrollPosition';
import {useNavigate} from 'react-router-dom';
import HomeIcon from '@mui/icons-material/Home';

export function TopBar({book, activeLanguage}) {

  const navigate = useNavigate();
  const supportedLanguages = book.translations.map(
      (translation) => translation.language)

  function setLanguage(language) {
    console.log(`/books/${book.id}?lang=${language}`)
    navigate(`/books/${book.id}?lang=${language}&pos=${getScrollPosition()}`)
  }

  function goHome() {
    navigate(`/?lang=${activeLanguage}`)
  }

  return (<div className="topbar">
    <div id="home-button" onClick={goHome}>
      <HomeIcon />
    </div>
    <LanguageSelect
        supportedLanguages={supportedLanguages}
        activeLanguage={activeLanguage}
        onLanguageChanged={setLanguage}
    />
  </div>);
}
import {Link, NavLink} from 'react-router-dom';
import {SearchBar} from '../search/SearchBar';
import {LanguageSelect} from './LanguageSelect';
import './TopBar.scss';

export function TopBar({supportedLanguages, activeLanguage}) {
  return (
    <header className="app-header">
      <div className="app-header__inner">
        <Link to="/" className="brand" aria-label="Holybook — home">
          <span className="brand__mark" aria-hidden="true">
            <i className="fa-solid fa-book-open" />
          </span>
          <span className="brand__name">Holybook</span>
        </Link>

        <div className="app-header__search">
          <SearchBar />
        </div>

        <nav className="app-nav" aria-label="Primary">
          <NavLink to="/" end className="app-nav__link" title="Library">
            <i className="fa-solid fa-book" aria-hidden="true" />
            <span>Library</span>
          </NavLink>
          <NavLink to="/translate" className="app-nav__link" title="Translate">
            <i className="fa-solid fa-language" aria-hidden="true" />
            <span>Translate</span>
          </NavLink>
          <NavLink
            to="/aitranslate"
            className="app-nav__link"
            title="AI Translate">
            <i className="fa-solid fa-wand-magic-sparkles" aria-hidden="true" />
            <span>AI Translate</span>
          </NavLink>
        </nav>

        <div className="app-header__lang">
          <LanguageSelect
            activeLanguage={activeLanguage}
            supportedLanguages={supportedLanguages} />
        </div>
      </div>
    </header>
  );
}

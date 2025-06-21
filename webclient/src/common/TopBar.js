import {useNavigate, useSearchParams} from 'react-router-dom';
import {Dropdown, Icon, Navbar, Form} from 'react-bulma-components';
import {SearchBar} from '../search/SearchBar';
import {LanguageSelect} from "./LanguageSelect";
import {useState} from "react";
import './TobBar.scss';

export function TopBar({supportedLanguages, activeLanguage}) {

  const [isActive, setActive] = useState(false);

  function toggleActive() {
    setActive(!isActive);
  }

  return (<Navbar color="primary" active={isActive} fixed="top">
    <Navbar.Brand>
      <Navbar.Item renderAs="div" className="language-select-container">
        <LanguageSelect
          activeLanguage={activeLanguage}
          supportedLanguages={supportedLanguages} />
      </Navbar.Item>
      <Navbar.Item renderAs="div" className="searchbar-container">
        <SearchBar/>
      </Navbar.Item>
      <Navbar.Burger
        onClick={toggleActive} />
    </Navbar.Brand>
    <Navbar.Menu>
      <Navbar.Container>
      </Navbar.Container>
      <Navbar.Container align="right">
        <Navbar.Item href="/">
          <Icon><i aria-hidden="true" className="fa-solid fa-list"/></Icon>
          &nbsp;&nbsp;Document list
        </Navbar.Item>
        <Navbar.Item href="/translate">
          <Icon><i aria-hidden="true" className="fa-solid fa-language"/></Icon>
          &nbsp;&nbsp;Translate
        </Navbar.Item>
        <Navbar.Item href="/aitranslate">
          <Icon><i aria-hidden="true" className="fa-solid fa-robot"/></Icon>
          &nbsp;&nbsp;AI Translate
        </Navbar.Item>
      </Navbar.Container>
    </Navbar.Menu>
  </Navbar>);
}